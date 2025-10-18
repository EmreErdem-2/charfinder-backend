package com.example.charfinder.rules;

import com.example.charfinder.rsql_tools.QueryResponseDto;
import com.example.charfinder.rsql_tools.RsqlMongoService;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RulesController {

    private final RsqlMongoService rsqlMongoService;

    public RulesController(RsqlMongoService rsqlMongoService) {
        this.rsqlMongoService = rsqlMongoService;
    }

    @GetMapping("/test")
    public String test() {
        return "This is a connection test for controller";
    }

    // Example: GET /api/classes?filter=status==active;age=ge=18
    @GetMapping("/{collection}")
    public RulesResponseDto queryCollection(@PathVariable String collection,
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "fields", required = false) String fields){

        if (filter == null || filter.isBlank()) {
            // no filter → return all docs
            var response = rsqlMongoService.query(collection, filter, fields);
            return new RulesResponseDto(
                    response.getFilterQuery(),
                    response.getFieldsQuery(),
                    response.getMongoQuery(),
                    response.getError(),
                    response.isConnected(),
                    response.isDatabaseFound(),
                    response.isCollectionFound(),
                    response.isHasDocuments(),
                    response.getResults());
        }
        var response = rsqlMongoService.query(collection, filter, fields);
        return new RulesResponseDto(
                response.getFilterQuery(),
                response.getFieldsQuery(),
                response.getMongoQuery(),
                response.getError(),
                response.isConnected(),
                response.isDatabaseFound(),
                response.isCollectionFound(),
                response.isHasDocuments(),
                response.getResults());
    }
}