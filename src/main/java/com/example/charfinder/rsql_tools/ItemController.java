package com.example.charfinder.rsql_tools;

import org.bson.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping("/test")
    public String test() {
        return "Welcome to pagination controller!";
    }

    @GetMapping("/search")
    public PagedResultDto search(
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            @RequestParam(required = false) List<String> sort,
            @RequestParam(required = false) List<String> fields) {

        return service.searchWithFacet(filter, page, pageSize, sort, fields);
    }
}