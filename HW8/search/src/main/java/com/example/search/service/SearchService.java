package com.example.search.service;

import com.example.search.model.SearchResult;

import java.util.List;

/**
 * Abstraction over a web search provider, so the controller does not depend
 * on Tavily directly (Dependency Inversion) and another provider can be
 * swapped in without touching callers (Open/Closed).
 */
public interface SearchService {

	/**
	 * @return the hits for {@code query}; an empty list when nothing matched
	 * @throws SearchUnavailableException when the provider cannot be used or fails
	 */
	List<SearchResult> search(String query);
}
