package com.example.search.model;

/**
 * One search hit shown on the page.
 */
public record SearchResult(String title, String url, String snippet) {
}
