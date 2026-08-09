package com.resumematcher.matching_service.exception;

public class LlmResponseException extends RuntimeException {
    public LlmResponseException(String message) {
        super(message);
    }
}
