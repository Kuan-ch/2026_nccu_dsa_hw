package com.example.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed binding for the {@code tavily.*} settings.
 */
@ConfigurationProperties(prefix = "tavily")
public record TavilyProperties(boolean enabled, String apiKey, Integer maxResults) {

	private static final int DEFAULT_MAX_RESULTS = 10;

	public TavilyProperties {
		if (maxResults == null || maxResults <= 0) {
			maxResults = DEFAULT_MAX_RESULTS;
		}
	}

	public boolean hasApiKey() {
		return apiKey != null && !apiKey.isBlank();
	}
}
