package com.example.charfinder.rsql_tools;

import org.bson.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules/search")
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping("/test")
    public String test() {
        return "Welcome to pagination controller!";
    }

    @GetMapping("/{collection}")
    public PagedResultDto search(@PathVariable String collection,
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "50") int pageSize,
            @RequestParam(name = "sort", required = false) List<String> sort,
            @RequestParam(name = "fields", required = false) List<String> fields) {

        return service.searchWithFacet(collection, filter, page, pageSize, sort, fields);
    }
}