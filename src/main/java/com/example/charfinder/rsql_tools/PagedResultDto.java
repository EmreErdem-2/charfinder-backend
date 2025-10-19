package com.example.charfinder.rsql_tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class PagedResultDto<T> {
    private Metadata metadata;
    private List<T> data;

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