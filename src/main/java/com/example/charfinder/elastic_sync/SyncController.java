package com.example.charfinder.elastic_sync;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final MongoToElasticSyncService syncService;

    public SyncController(MongoToElasticSyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/database/{dbName}")
    public Map<String, Object> syncDatabase(@PathVariable String dbName) throws IOException {
        return syncService.syncDatabase(dbName);
    }
}