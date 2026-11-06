package com.example.search.service;

import com.example.search.config.TavilyProperties;
import com.example.search.model.SearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TavilySearchServiceTest {

	private static final TavilyProperties CONFIGURED = new TavilyProperties(true, "tvly-test", 5);

	private final RestClient.Builder builder = RestClient.builder();
	private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

	@Test
	void mapsResultsAndSkipsEntriesWithoutTitleOrUrl() {
		server.expect(requestTo("https://api.tavily.com/search"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("Authorization", "Bearer tvly-test"))
				.andExpect(jsonPath("$.query").value("nccu"))
				.andExpect(jsonPath("$.max_results").value(5))
				.andRespond(withSuccess("""
						{"query":"nccu","results":[
						  {"title":"NCCU","url":"https://www.nccu.edu.tw","content":"National Chengchi University","score":0.9},
						  {"title":"","url":"https://x.example"},
						  {"title":"No url"}
						]}""", MediaType.APPLICATION_JSON));

		List<SearchResult> results = new TavilySearchService(CONFIGURED, builder).search("nccu");

		assertThat(results).containsExactly(
				new SearchResult("NCCU", "https://www.nccu.edu.tw", "National Chengchi University"));
		server.verify();
	}

	@Test
	void returnsEmptyListWhenNoResults() {
		server.expect(requestTo("https://api.tavily.com/search"))
				.andRespond(withSuccess("{\"results\":[]}", MediaType.APPLICATION_JSON));

		assertThat(new TavilySearchService(CONFIGURED, builder).search("zzz")).isEmpty();
	}

	@Test
	void wrapsHttpErrors() {
		server.expect(requestTo("https://api.tavily.com/search"))
				.andRespond(withStatus(HttpStatus.UNAUTHORIZED));

		assertThatThrownBy(() -> new TavilySearchService(CONFIGURED, builder).search("x"))
				.isInstanceOf(SearchUnavailableException.class);
	}

	@Test
	void failsFastWithoutApiKey() {
		TavilyProperties noKey = new TavilyProperties(true, "", null);

		assertThatThrownBy(() -> new TavilySearchService(noKey, builder).search("x"))
				.isInstanceOf(SearchUnavailableException.class)
				.hasMessageContaining("金鑰");
	}
}
