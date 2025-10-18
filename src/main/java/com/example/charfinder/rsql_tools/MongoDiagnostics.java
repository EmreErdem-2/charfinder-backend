package com.example.charfinder.rsql_tools;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Service;


@Service
public class MongoDiagnostics {

    private final MongoClient mongoClient;

    public MongoDiagnostics(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    /** Check if we can connect to the server (ping command). */
    public boolean canConnect(String dbName) {
        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            db.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Check if a database exists on the server. */
    public boolean databaseExists(String dbName) {
        try {
            for (String name : mongoClient.listDatabaseNames()) {
                if (name.equals(dbName)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /** Check if a collection exists inside a given database. */
    public boolean collectionExists(String dbName, String collectionName) {
        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            for (String name : db.listCollectionNames()) {
                if (name.equals(collectionName)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /** ✅ Check if a collection has at least one document. */
    public boolean hasDocuments(String dbName, String collectionName) {
        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(collectionName);
            return collection.find().limit(1).iterator().hasNext();
        } catch (Exception e) {
            return false;
        }
    }
}