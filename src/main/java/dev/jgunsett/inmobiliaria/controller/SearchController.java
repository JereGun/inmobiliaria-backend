package dev.jgunsett.inmobiliaria.controller;

import dev.jgunsett.inmobiliaria.application.dto.search.GlobalSearchResponse;
import dev.jgunsett.inmobiliaria.application.service.GlobalSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping
    public ResponseEntity<GlobalSearchResponse> search(@RequestParam String q) {
        return ResponseEntity.ok(globalSearchService.search(q));
    }
}
