package com.example.charfinder.rsql_tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.bson.Document;

import java.util.List;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class PagedResultDto{
    private Metadata metadata;
    private String mongoQuery;
    private String error;
    private List<Document> data;

    // constructors, getters/setters
    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private long totalCount;
        private int page;
        private int pageSize;

        // constructors, getters/setters
    }
}