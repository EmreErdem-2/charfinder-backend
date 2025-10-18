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
public class QueryResponseDto {
    @Builder.Default
    private String filterQuery = "";
    @Builder.Default
    private String fieldsQuery = "";
    @Builder.Default
    private String mongoQuery = "";
    @Builder.Default
    private String error = "";

    // Diagnostics
    private boolean connected;
    private boolean databaseFound;
    private boolean collectionFound;
    private boolean hasDocuments;

    // Result
    private List<Document> results;
}

