package com.example.lambda.handlers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Demonstrative handler that uses Project Reactor and Micrometer for observability.
 */
@Component
public class HelloHandler {

    private final Counter invocationsCounter;
    private final MeterRegistry meterRegistry;

    public HelloHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.invocationsCounter = Counter.builder("lambda.invocations")
            .description("Total number of Lambda invocations")
            .tag("handler", "hello")
            .register(meterRegistry);
    }

    /**
     * Process a greeting request reactively.
     *
     * @param name the name to greet
     * @return a Mono containing the greeting message
     */
    public Mono<String> processGreeting(String name) {
        return Mono.justOrEmpty(name)
            .defaultIfEmpty("") // name can be null; Mono forbids null elements (Reactive Streams spec)
            .delayElement(Duration.ofMillis(10)) // Simulate async processing
            .map(n -> String.format("Hello, %s!", n != null && !n.isEmpty() ? n : "World"))
            .doOnNext(result -> {
                invocationsCounter.increment();
                meterRegistry.counter("lambda.processed", "status", "success").increment();
            })
            .doOnError(error -> {
                meterRegistry.counter("lambda.processed", "status", "error").increment();
            });
    }

    /**
     * Get invocation count.
     *
     * @return current invocation count
     */
    public double getInvocationCount() {
        return invocationsCounter.count();
    }
}



