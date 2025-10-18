package com.example.charfinder.rules;

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
public class RulesResponseDto {
    private String filterQuery;
    private String fieldsQuery;
    private String mongoQuery;
    private String error;

    // Diagnostics
    private boolean connected;
    private boolean databaseFound;
    private boolean collectionFound;
    private boolean hasDocuments;

    // Result
    private List<Document> results;

}

