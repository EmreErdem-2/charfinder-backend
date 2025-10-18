package com.example.charfinder.rsql_tools;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RsqlMongoService {

    private final MongoTemplate mongoTemplate;
    private final MongoDiagnostics mongoDiagnostics;

    public RsqlMongoService(MongoTemplate mongoTemplate, MongoDiagnostics mongoDiagnostics) {
        this.mongoTemplate = mongoTemplate;
        this.mongoDiagnostics = mongoDiagnostics;
    }

    public QueryResponseDto query(String collection, String rsql, String fields) {
        QueryResponseDto response = new QueryResponseDto();
        response.setFilterQuery(rsql);
        response.setFieldsQuery(fields);

        try {
            // Build query (handles both filter and findAll)
            Query query = buildQuery(rsql, fields);

            // Save the generated Mongo query string for debugging
            response.setMongoQuery(query.toString());

            // Execute query
            List<Document> results = mongoTemplate.find(query, Document.class, collection);
            response.setResults(results);

            // Diagnostics
            applyDiagnostics(response, collection);

        } catch (Exception e) {
            response.setError(e.getMessage());
        }

        return response;
    }

    /** Build a Query object from RSQL + projection fields */
    private Query buildQuery(String rsql, String fields) {
        Query query;

        if (rsql == null || rsql.isBlank()) {
            query = new Query(); // findAll
        } else {
            Node rootNode = new RSQLParser().parse(rsql);
            Criteria criteria = rootNode.accept(new MongoRsqlVisitor(), null);
            query = new Query(criteria);
        }

        // Apply projection if fields are provided
        if (fields != null && !fields.isBlank()) {
            for (String field : fields.split(",")) {
                query.fields().include(field.trim());
            }
        }

        return query;
    }

    /** Run diagnostics and enrich the response DTO */
    private void applyDiagnostics(QueryResponseDto response, String collection) {
        String dbName = mongoTemplate.getDb().getName();
        response.setConnected(mongoDiagnostics.canConnect(dbName));
        response.setDatabaseFound(mongoDiagnostics.databaseExists(dbName));
        response.setCollectionFound(mongoDiagnostics.collectionExists(dbName, collection));
        response.setHasDocuments(mongoDiagnostics.hasDocuments(dbName, collection));
    }
}
