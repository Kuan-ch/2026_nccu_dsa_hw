package com.example.search.controller;

import com.example.search.model.SearchResult;
import com.example.search.service.SearchService;
import com.example.search.service.SearchUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

	private final SearchService searchService;

	public SearchController(SearchService searchService) {
		this.searchService = searchService;
	}

	/** GET /api/search?q=... → [{title, url, snippet}, ...] */
	@GetMapping("/api/search")
	public List<SearchResult> search(@RequestParam(name = "q", defaultValue = "") String query) {
		String trimmed = query.trim();
		if (trimmed.isEmpty()) {
			return List.of();
		}
		return searchService.search(trimmed);
	}

	@ExceptionHandler(SearchUnavailableException.class)
	public ResponseEntity<Map<String, String>> handleUnavailable(SearchUnavailableException e) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(Map.of("message", e.getMessage()));
	}
}
