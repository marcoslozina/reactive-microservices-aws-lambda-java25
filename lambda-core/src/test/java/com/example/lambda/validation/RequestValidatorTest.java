package com.example.lambda.validation;

import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

/**
 * Unit tests for RequestValidator.
 */
class RequestValidatorTest {

    private record Payload(@NotBlank String name) {}

    private RequestValidator requestValidator;

    @BeforeEach
    void setUp() {
        requestValidator = new RequestValidator(Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Test
    void shouldPassThroughValidObject() {
        Payload payload = new Payload("Marcos");

        StepVerifier.create(requestValidator.validate(payload))
            .expectNext(payload)
            .verifyComplete();
    }

    @Test
    void shouldFailWithValidationExceptionWhenConstraintIsViolated() {
        StepVerifier.create(requestValidator.validate(new Payload("")))
            .expectErrorMatches(error ->
                error instanceof RequestValidator.ValidationException
                    && error.getMessage().contains("name"))
            .verify();
    }

    @Test
    void shouldFailWhenValueIsNull() {
        StepVerifier.create(requestValidator.validate(new Payload(null)))
            .expectError(RequestValidator.ValidationException.class)
            .verify();
    }
}
