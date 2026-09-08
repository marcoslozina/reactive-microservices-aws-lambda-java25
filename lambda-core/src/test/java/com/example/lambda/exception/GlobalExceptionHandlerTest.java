package com.example.lambda.exception;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.lambda.validation.RequestValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {

    @Test
    void shouldReturnBadRequestForIllegalArgumentException() {
        APIGatewayProxyResponseEvent response =
            GlobalExceptionHandler.handleException(new IllegalArgumentException("bad input"), "req-1");

        assertThat(response.getStatusCode()).isEqualTo(400);
    }

    @Test
    void shouldReturnBadRequestForValidationException() {
        APIGatewayProxyResponseEvent response = GlobalExceptionHandler.handleException(
            new RequestValidator.ValidationException("name: must not be blank"), "req-2");

        assertThat(response.getStatusCode()).isEqualTo(400);
    }

    @Test
    void shouldReturnForbiddenForSecurityException() {
        APIGatewayProxyResponseEvent response =
            GlobalExceptionHandler.handleException(new SecurityException("denied"), "req-3");

        assertThat(response.getStatusCode()).isEqualTo(403);
    }

    @Test
    void shouldReturnInternalServerErrorForRuntimeException() {
        APIGatewayProxyResponseEvent response =
            GlobalExceptionHandler.handleException(new RuntimeException("boom"), "req-4");

        assertThat(response.getStatusCode()).isEqualTo(500);
    }

    @Test
    void shouldReturnInternalServerErrorForCheckedException() {
        APIGatewayProxyResponseEvent response =
            GlobalExceptionHandler.handleException(new Exception("checked failure"), "req-5");

        assertThat(response.getStatusCode()).isEqualTo(500);
    }

    @Test
    void shouldSanitizeMessageInsteadOfLeakingInternalDetails() {
        APIGatewayProxyResponseEvent response =
            GlobalExceptionHandler.handleException(new RuntimeException("stack trace with secrets"), "req-6");

        assertThat(response.getBody())
            .contains("\"error\":\"Internal server error\"")
            .doesNotContain("secrets");
    }

    @Test
    void shouldUseGenericMessageWhenThrowableMessageIsNull() {
        APIGatewayProxyResponseEvent response =
            GlobalExceptionHandler.handleException(new RuntimeException(), "req-7");

        assertThat(response.getBody()).contains("\"error\":\"An unexpected error occurred\"");
    }

    @Test
    void shouldSetTracingHeaders() {
        APIGatewayProxyResponseEvent response =
            GlobalExceptionHandler.handleException(new RuntimeException("boom"), "req-8");

        assertThat(response.getHeaders())
            .containsEntry("Content-Type", "application/json")
            .containsEntry("X-Request-Id", "req-8");
    }

    @Test
    void shouldNotIncludeStackTraceByDefault() {
        APIGatewayProxyResponseEvent response =
            GlobalExceptionHandler.handleException(new RuntimeException("boom"), "req-9");

        assertThat(response.getBody()).doesNotContain("stackTrace");
    }
}
