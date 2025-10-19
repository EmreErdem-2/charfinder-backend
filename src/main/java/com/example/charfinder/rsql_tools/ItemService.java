package com.example.charfinder.rsql_tools;

import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.DEFAULT_CONTEXT;

@Service
public class ItemService {
    private final MongoTemplate mongoTemplate;
    private final RsqlMongoService rsqlMongoService;
    private static final String COLLECTION = "classes";

    public ItemService(MongoTemplate mongoTemplate,  RsqlMongoService rsqlMongoService) {
        this.mongoTemplate = mongoTemplate;
        this.rsqlMongoService = rsqlMongoService;
    }

    public PagedResultDto searchWithFacet(
            String rsql,
            int page,
            int pageSize,
            List<String> sortParams,
            List<String> projectionFields) {
        PagedResultDto pagedResult = new PagedResultDto();
        try {
            // 1) Compute skip
            int skip = (page - 1) * pageSize;
            // 2) Build base Criteria from RSQL
            Criteria baseCriteria = rsql != null
                    ? rsqlMongoService.buildCriteria(rsql)
                    : new Criteria();

            // 3) Parse Sort orders
            Sort sort = parseSort(sortParams);

            // 4) Build pipeline stages
            List<AggregationOperation> stages = new ArrayList<>();

            // 4a) $match
            stages.add(Aggregation.match(baseCriteria));

            // 4b) $sort (if provided)
            if (sort.isSorted()) {
                stages.add(Aggregation.sort(sort));
            }

            // 4c) $facet with metadata and data sub-pipelines
            FacetOperation facet = Aggregation.facet(
                            Aggregation.count().as("totalCount")
                    ).as("metadata")
                    .and(buildDataPipeline(projectionFields, skip, pageSize))
                    .as("data");

            stages.add(facet);

            // 5) Run aggregation
            Aggregation agg = Aggregation.newAggregation(stages)
                    .withOptions(AggregationOptions.builder().build());
            AggregationResults<Document> results =
                    mongoTemplate.aggregate(agg, COLLECTION, Document.class);

            Document root = results.getUniqueMappedResult();

            // 6) Log mongo command query
            List<Document> pipeline = agg.toPipeline(DEFAULT_CONTEXT);
            String mongoQuery = pipeline.stream()
                                        .map(Document::toJson)
                                        .collect(Collectors.joining(", ", "[", "]"));
            pagedResult.setMongoQuery(mongoQuery);

            // 7) Extract metadata
            List<Document> metaList = root.getList("metadata", Document.class);
            long totalCount = metaList.stream()
                    .findFirst()
                    .map(doc -> doc.get("totalCount", Number.class).longValue())
                    .orElse(0L);
            // 8) Extract page data
            List<Document> data = root.getList("data", Document.class);

            // 9) Wrap into PagedResult
            pagedResult.setMetadata(new PagedResultDto.Metadata(totalCount, page, pageSize));
            pagedResult.setData(data);
        } catch (Exception e) {
            pagedResult.setError(e.getMessage());
        }

        return pagedResult;
    }

    // Helper to parse sort parameters like ["name,asc","age,desc"]
    private Sort parseSort(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = sortParams.stream()
                .map(spec -> {
                    String[] parts = spec.split(",");
                    return new Sort.Order(
                            Sort.Direction.fromString(parts[1].trim()),
                            parts[0].trim()
                    );
                })
                .toList();
        return Sort.by(orders);
    }

    // Build the `data` sub-pipeline: projection → skip → limit
    private AggregationOperation[] buildDataPipeline(
            List<String> projectionFields, int skip, int limit) {

        List<AggregationOperation> ops = new ArrayList<>();

        // Projection stage (if any fields specified)
        if (projectionFields != null && !projectionFields.isEmpty()) {
            ops.add(Aggregation.project(projectionFields.toArray(new String[0])));
        }

        // Pagination stages
        ops.add(Aggregation.skip((long) skip));
        ops.add(Aggregation.limit(limit));

        return ops.toArray(new AggregationOperation[0]);
    }
}
