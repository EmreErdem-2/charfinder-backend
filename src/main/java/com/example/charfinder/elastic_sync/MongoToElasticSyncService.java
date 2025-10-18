package com.example.charfinder.elastic_sync;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;

import java.io.IOException;
import java.util.*;



@Service
public class MongoToElasticSyncService {

    private final MongoClient mongoClient;
    private final ElasticsearchClient esClient;

    public MongoToElasticSyncService(MongoClient mongoClient, ElasticsearchClient esClient) {
        this.mongoClient = mongoClient;
        this.esClient = esClient;
    }

    // Build a template bound to the given database name
    private MongoTemplate templateForDb(String dbName) {
        return new MongoTemplate(mongoClient, dbName);
    }

    public Map<String, Object> syncDatabase(String dbName) throws IOException {
        MongoDatabase db = mongoClient.getDatabase(dbName);
        // Use a LinkedHashSet to keep stable iteration order
        Set<String> collections = new LinkedHashSet<>();
        db.listCollectionNames().into(collections);

        Map<String, Integer> collectionCounts = new LinkedHashMap<>();
        int totalDocs = 0;

        for (String collectionName : collections) {
            try {
                int count = syncCollection(dbName, collectionName, collectionName);
                collectionCounts.put(collectionName, count);
                totalDocs += count;
            } catch (Exception e) {
                collectionCounts.put(collectionName, -1); // -1 means failed
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("databaseName", dbName);
        summary.put("collections", new ArrayList<>(collections)); // return as array, not toString()
        summary.put("collectionsSynced", collectionCounts.size());
        summary.put("totalDocs", totalDocs);
        summary.put("details", collectionCounts);
        return summary;
    }

    // Sync one collection to the given ES index
    private int syncCollection(String dbName, String collectionName, String indexName) throws IOException {
        MongoTemplate template = templateForDb(dbName);

        // Pull all docs; for large collections, switch to streaming/pagination
        List<Document> docs = template.find(new Query(), Document.class, collectionName);
        if (docs.isEmpty()) return 0;

        List<BulkOperation> ops = new ArrayList<>(docs.size());
        for (Document doc : docs) {
            // Extract id
            String id = doc.get("_id") instanceof ObjectId
                    ? ((ObjectId) doc.get("_id")).toHexString()
                    : String.valueOf(doc.get("_id"));

            // Remove _id from the document body
            doc.remove("_id");

            // Convert to plain map
            Map<String, Object> payload = toPlainMap(doc);

            ops.add(BulkOperation.of(b -> b.index(i -> i
                    .index(indexName)
                    .id(id)              // ✅ use as ES _id
                    .document(payload)   // ✅ no _id field inside
            )));
        }


        BulkRequest request = new BulkRequest.Builder().operations(ops).build();
        var response = esClient.bulk(request);

        if (response.errors()) {
            // Optional: collect item failures for diagnostics
            long failures = response.items().stream().filter(it -> it.error() != null).count();
            throw new IOException("Bulk indexing had errors. Failed items: " + failures);
        }

        System.out.printf("✅ Synced %d docs from '%s.%s' into ES index '%s'%n",
                docs.size(), dbName, collectionName, indexName);

        return docs.size();
    }

    // Convert BSON Document to a plain Map; unwrap nested Documents and ObjectIds
    private Map<String, Object> toPlainMap(Document doc) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : doc.entrySet()) {
            out.put(e.getKey(), normalizeValue(e.getValue()));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Object normalizeValue(Object v) {
        if (v == null) return null;
        if (v instanceof ObjectId) return ((ObjectId) v).toHexString();
        if (v instanceof Document) return toPlainMap((Document) v);
        if (v instanceof List<?>) {
            List<Object> list = (List<Object>) v;
            List<Object> normalized = new ArrayList<>(list.size());
            for (Object item : list) normalized.add(normalizeValue(item));
            return normalized;
        }
        // Basic types (String, Number, Boolean, Date) pass through
        return v;
    }


    // Example: run nightly for the whole DB
    @Scheduled(cron = "0 0 2 * * *")
    public void nightlyDatabaseSync() throws IOException {
        syncDatabase("rules");
    }
}