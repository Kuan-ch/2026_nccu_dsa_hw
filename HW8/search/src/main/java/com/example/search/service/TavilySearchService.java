package com.example.search.service;

import com.example.search.config.TavilyProperties;
import com.example.search.model.SearchResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Calls the Tavily Search API and maps its JSON into {@link SearchResult}s.
 *
 * @see <a href="https://docs.tavily.com/documentation/api-reference/endpoint/search">Tavily search API</a>
 */
@Service
public class TavilySearchService implements SearchService {

	private static final String BASE_URL = "https://api.tavily.com";

	private final TavilyProperties properties;
	private final RestClient restClient;

	public TavilySearchService(TavilyProperties properties, RestClient.Builder restClientBuilder) {
		this.properties = properties;
		this.restClient = restClientBuilder.baseUrl(BASE_URL).build();
	}

	@Override
	public List<SearchResult> search(String query) {
		ensureConfigured();

		TavilyResponse response;
		try {
			response = restClient.post()
					.uri("/search")
					.contentType(MediaType.APPLICATION_JSON)
					.headers(h -> h.setBearerAuth(properties.apiKey()))
					.body(Map.of("query", query, "max_results", properties.maxResults()))
					.retrieve()
					.body(TavilyResponse.class);
		} catch (RestClientException e) {
			throw new SearchUnavailableException("搜尋服務暫時無法使用，請稍後再試。", e);
		}

		if (response == null || response.results() == null) {
			return List.of();
		}
		return response.results().stream()
				.filter(Objects::nonNull)
				.filter(r -> isPresent(r.title()) && isPresent(r.url()))
				.map(r -> new SearchResult(r.title(), r.url(), r.content()))
				.toList();
	}

	private void ensureConfigured() {
		if (!properties.enabled()) {
			throw new SearchUnavailableException("搜尋功能目前未啟用。");
		}
		if (!properties.hasApiKey()) {
			throw new SearchUnavailableException("尚未設定 Tavily API 金鑰。");
		}
	}

	private static boolean isPresent(String s) {
		return s != null && !s.isBlank();
	}

	/** Subset of Tavily's response body that this app uses. */
	record TavilyResponse(List<TavilyResult> results) {
	}

	record TavilyResult(String title, String url, String content) {
	}
}
