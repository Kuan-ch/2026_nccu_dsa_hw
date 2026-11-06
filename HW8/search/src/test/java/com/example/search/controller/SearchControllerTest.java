package com.example.search.controller;

import com.example.search.model.SearchResult;
import com.example.search.service.SearchService;
import com.example.search.service.SearchUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private SearchService searchService;

	@Test
	void returnsResultsAsJson() throws Exception {
		when(searchService.search("nccu")).thenReturn(
				List.of(new SearchResult("NCCU", "https://www.nccu.edu.tw", "snippet")));

		mvc.perform(get("/api/search").param("q", "  nccu  "))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("NCCU"))
				.andExpect(jsonPath("$[0].url").value("https://www.nccu.edu.tw"));
	}

	@Test
	void blankQueryReturnsEmptyWithoutCallingProvider() throws Exception {
		mvc.perform(get("/api/search").param("q", "   "))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
		verifyNoInteractions(searchService);
	}

	@Test
	void providerFailureReturns503WithMessage() throws Exception {
		when(searchService.search(anyString())).thenThrow(new SearchUnavailableException("尚未設定 Tavily API 金鑰。"));

		mvc.perform(get("/api/search").param("q", "x"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.message").value("尚未設定 Tavily API 金鑰。"));
	}
}
