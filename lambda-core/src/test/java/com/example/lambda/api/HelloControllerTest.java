package com.example.lambda.api;

import com.example.lambda.handlers.HelloHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for HelloController.
 */
class HelloControllerTest {

    private HelloController helloController;

    @BeforeEach
    void setUp() {
        HelloHandler helloHandler = new HelloHandler(new SimpleMeterRegistry());
        helloController = new HelloController(helloHandler);
    }

    @Test
    void shouldGreetByNameOnGet() {
        StepVerifier.create(helloController.helloGet("Marcos"))
            .assertNext(response -> {
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).containsEntry("greeting", "Hello, Marcos!");
                assertThat(response.getBody()).containsEntry("name", "Marcos");
            })
            .verifyComplete();
    }

    @Test
    void shouldFallBackToDefaultGreetingWhenNameIsMissingOnGet() {
        StepVerifier.create(helloController.helloGet(null))
            .assertNext(response -> {
                assertThat(response.getBody()).containsEntry("greeting", "Hello, World!");
                assertThat(response.getBody()).doesNotContainKey("name");
            })
            .verifyComplete();
    }

    @Test
    void shouldPreferQueryParamOverBodyNameOnPost() {
        StepVerifier.create(helloController.helloPost("Marcos", Map.of("name", "Ignored")))
            .assertNext(response -> assertThat(response.getBody()).containsEntry("greeting", "Hello, Marcos!"))
            .verifyComplete();
    }

    @Test
    void shouldFallBackToBodyNameWhenQueryParamIsMissingOnPost() {
        StepVerifier.create(helloController.helloPost(null, Map.of("name", "FromBody")))
            .assertNext(response -> assertThat(response.getBody()).containsEntry("greeting", "Hello, FromBody!"))
            .verifyComplete();
    }

    @Test
    void shouldIncludeBodyInResponseWhenPresent() {
        Map<String, Object> body = Map.of("name", "Marcos", "extra", "data");

        StepVerifier.create(helloController.helloPost(null, body))
            .assertNext(response -> assertThat(response.getBody()).containsEntry("body", body))
            .verifyComplete();
    }

    @Test
    void shouldUseDefaultGreetingWhenNameAndBodyAreMissingOnPost() {
        StepVerifier.create(helloController.helloPost(null, null))
            .assertNext(response -> {
                assertThat(response.getBody()).containsEntry("greeting", "Hello, World!");
                assertThat(response.getBody()).doesNotContainKey("body");
            })
            .verifyComplete();
    }

    @Test
    void shouldFallBackToDefaultGreetingWhenHandlerFails() {
        HelloHandler failingHandler = mock(HelloHandler.class);
        when(failingHandler.processGreeting("Marcos")).thenReturn(Mono.error(new RuntimeException("downstream failure")));
        HelloController controller = new HelloController(failingHandler);

        StepVerifier.create(controller.helloGet("Marcos"))
            .assertNext(response -> {
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).containsEntry("greeting", "Hello, World!");
            })
            .verifyComplete();
    }
}
