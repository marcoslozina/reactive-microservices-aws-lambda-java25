package com.example.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.lambda.handlers.HelloHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Configuration for AWS Lambda functions using Spring Cloud Function.
 * All functions are implemented reactively using Project Reactor.
 */
@Configuration(proxyBeanMethods = false)
public class FunctionConfig {

    private static final Logger logger = LoggerFactory.getLogger(FunctionConfig.class);
    private final HelloHandler helloHandler;

    public FunctionConfig(HelloHandler helloHandler) {
        this.helloHandler = helloHandler;
    }

    /**
     * Hello function that handles API Gateway requests reactively.
     * Can be activated by setting SPRING_CLOUD_FUNCTION_DEFINITION=hello
     * 
     * NOTE: Spring Cloud Function adapter for AWS Lambda requires synchronous Function signature,
     * but we process reactively internally using Reactor and block only at the boundary
     * (which is acceptable for Lambda as each invocation is independent).
     * 
     * For truly reactive endpoints, use WebFlux controllers with spring-cloud-starter-function-web.
     */
    @Bean
    public Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> hello() {
        return request -> {
            try {
                // Extract request data
                String path = request.getPath();
                Map<String, String> pathParameters = request.getPathParameters();
                Map<String, String> queryStringParameters = request.getQueryStringParameters();
                String body = request.getBody();
                
                // Build echo message
                StringBuilder echo = new StringBuilder();
                echo.append("path=").append(path != null ? path : "");
                
                if (pathParameters != null && !pathParameters.isEmpty()) {
                    echo.append(", pathParams=").append(pathParameters);
                }
                
                if (queryStringParameters != null && !queryStringParameters.isEmpty()) {
                    echo.append(", queryParams=").append(queryStringParameters);
                }
                
                if (body != null && !body.isEmpty()) {
                    echo.append(", body=").append(body);
                }
                
                // Process greeting reactively (if name is provided)
                // Note: We block here because Lambda Function must return synchronously
                // This is the reactive boundary - inside we use Mono/Flux
                String name = queryStringParameters != null ? queryStringParameters.get("name") : null;
                String greeting;
                
                if (name != null && !name.trim().isEmpty()) {
                    // Process greeting reactively
                    greeting = helloHandler.processGreeting(name)
                        .timeout(java.time.Duration.ofSeconds(5))
                        .onErrorResume(error -> {
                            logger.warn("Error processing greeting, using default", error);
                            return Mono.just("Hello, World!");
                        })
                        .block(); // Necessary boundary: Lambda requires synchronous return
                } else {
                    greeting = "Hello, World!";
                }
                
                String jsonResponse = String.format(
                    "{\"message\":\"ok\",\"echo\":\"%s\",\"greeting\":\"%s\",\"timestamp\":\"%s\"}",
                    echo.toString().replace("\"", "\\\""),
                    greeting.replace("\"", "\\\""),
                    java.time.Instant.now().toString()
                );
                
                APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
                response.setStatusCode(HttpStatus.OK.value());
                response.setBody(jsonResponse);
                
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                headers.put("X-Request-Id", request.getRequestContext() != null 
                    ? request.getRequestContext().getRequestId() 
                    : "unknown");
                response.setHeaders(headers);
                
                return response;
                
            } catch (Exception e) {
                String requestId = request.getRequestContext() != null 
                    ? request.getRequestContext().getRequestId() 
                    : "unknown";
                return com.example.lambda.exception.GlobalExceptionHandler.handleException(e, requestId);
            }
        };
    }


    /**
     * Simple supplier function for health checks.
     * Can be activated by setting SPRING_CLOUD_FUNCTION_DEFINITION=pong
     */
    @Bean
    public Supplier<String> pong() {
        return () -> "pong";
    }
}



