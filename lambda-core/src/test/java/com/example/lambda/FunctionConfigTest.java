package com.example.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.lambda.handlers.HelloHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FunctionConfig, the Spring Cloud Function bean definitions.
 */
class FunctionConfigTest {

    private FunctionConfig functionConfig;

    @BeforeEach
    void setUp() {
        functionConfig = new FunctionConfig(new HelloHandler(new SimpleMeterRegistry()));
    }

    private static APIGatewayProxyRequestEvent requestWithRequestId(String requestId) {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        if (requestId != null) {
            APIGatewayProxyRequestEvent.ProxyRequestContext context = new APIGatewayProxyRequestEvent.ProxyRequestContext();
            context.setRequestId(requestId);
            request.setRequestContext(context);
        }
        return request;
    }

    @Test
    void shouldGreetByNameFromQueryParams() {
        Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> hello = functionConfig.hello();
        APIGatewayProxyRequestEvent request = requestWithRequestId("req-1");
        request.setPath("/hello");
        request.setQueryStringParameters(Map.of("name", "Marcos"));

        APIGatewayProxyResponseEvent response = hello.apply(request);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"greeting\":\"Hello, Marcos!\"").contains("path=/hello");
        assertThat(response.getHeaders()).containsEntry("X-Request-Id", "req-1");
    }

    @Test
    void shouldUseDefaultGreetingWhenNameIsBlank() {
        Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> hello = functionConfig.hello();
        APIGatewayProxyRequestEvent request = requestWithRequestId("req-2");
        request.setQueryStringParameters(Map.of("name", "   "));

        APIGatewayProxyResponseEvent response = hello.apply(request);

        assertThat(response.getBody()).contains("\"greeting\":\"Hello, World!\"");
    }

    @Test
    void shouldEchoPathParametersAndBody() {
        Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> hello = functionConfig.hello();
        APIGatewayProxyRequestEvent request = requestWithRequestId("req-3");
        request.setPath("/hello/{id}");
        request.setPathParameters(Map.of("id", "42"));
        request.setBody("{\"raw\":true}");

        APIGatewayProxyResponseEvent response = hello.apply(request);

        assertThat(response.getBody())
            .contains("pathParams={id=42}")
            .contains("body={\\\"raw\\\":true}");
    }

    @Test
    void shouldFallBackToUnknownRequestIdWhenContextIsMissing() {
        Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> hello = functionConfig.hello();
        APIGatewayProxyRequestEvent request = requestWithRequestId(null);

        APIGatewayProxyResponseEvent response = hello.apply(request);

        assertThat(response.getHeaders()).containsEntry("X-Request-Id", "unknown");
    }

    @Test
    void shouldFallBackToDefaultGreetingWhenHandlerErrorsReactively() {
        HelloHandler failingHandler = mock(HelloHandler.class);
        when(failingHandler.processGreeting("Marcos")).thenReturn(Mono.error(new RuntimeException("downstream failure")));
        FunctionConfig config = new FunctionConfig(failingHandler);
        APIGatewayProxyRequestEvent request = requestWithRequestId("req-4");
        request.setQueryStringParameters(Map.of("name", "Marcos"));

        APIGatewayProxyResponseEvent response = config.hello().apply(request);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"greeting\":\"Hello, World!\"");
    }

    @Test
    void shouldDelegateToGlobalExceptionHandlerWhenHandlerThrowsSynchronously() {
        HelloHandler failingHandler = mock(HelloHandler.class);
        when(failingHandler.processGreeting("Marcos")).thenThrow(new IllegalArgumentException("bad name"));
        FunctionConfig config = new FunctionConfig(failingHandler);
        APIGatewayProxyRequestEvent request = requestWithRequestId("req-5");
        request.setQueryStringParameters(Map.of("name", "Marcos"));

        APIGatewayProxyResponseEvent response = config.hello().apply(request);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getHeaders()).containsEntry("X-Request-Id", "req-5");
    }

    @Test
    void pongShouldRespondWithPong() {
        Supplier<String> pong = functionConfig.pong();

        assertThat(pong.get()).isEqualTo("pong");
    }
}
