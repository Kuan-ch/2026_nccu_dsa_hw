package com.example.search.service;

/**
 * Thrown when the search provider is not configured or the call fails.
 * The message is safe to show to end users.
 */
public class SearchUnavailableException extends RuntimeException {

	public SearchUnavailableException(String message) {
		super(message);
	}

	public SearchUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
