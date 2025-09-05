package com.importservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    
    @Value("${destination.api.logging.enabled:false}")
    private boolean loggingEnabled;
    
    @Value("${destination.api.logging.include-headers:false}")
    private boolean includeHeaders;
    
    @Value("${destination.api.logging.include-response:false}")
    private boolean includeResponse;
    
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, 
            byte[] body, 
            ClientHttpRequestExecution execution) throws IOException {
        
        if (loggingEnabled) {
            logRequest(request, body);
        }
        
        ClientHttpResponse response = execution.execute(request, body);
        
        if (loggingEnabled && includeResponse) {
            response = logResponse(response);
        }
        
        return response;
    }
    
    private void logRequest(HttpRequest request, byte[] body) {
        try {
            if (request.getURI().getRawPath().toLowerCase().contains("upload")) {
                return;
            }
            System.out.println("=== DESTINATION API REQUEST ===");
            System.out.println("URL: " + request.getURI());
            System.out.println("Method: " + request.getMethod());
            
            if (includeHeaders && request.getHeaders() != null) {
                System.out.println("Headers:");
                request.getHeaders().forEach((name, values) -> {
                    // Mask sensitive headers
                    if ("Authorization".equalsIgnoreCase(name)) {
                        System.out.println("  " + name + ": Bearer [MASKED]");
                    } else {
                        System.out.println("  " + name + ": " + String.join(", ", values));
                    }
                });
            }
            
            if (body != null && body.length > 0) {
                String requestBody = new String(body, StandardCharsets.UTF_8);
                System.out.println("Request Body:");
                System.out.println(requestBody);
            }
            
            System.out.println("=== END REQUEST ===");
            
        } catch (Exception e) {
            logger.error("Error logging request", e);
        }
    }
    
    private ClientHttpResponse logResponse(ClientHttpResponse response) throws IOException {
        try {
            System.out.println("=== DESTINATION API RESPONSE ===");
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Status Text: " + response.getStatusText());
            
            if (includeHeaders && response.getHeaders() != null) {
                System.out.println("Response Headers:");
                response.getHeaders().forEach((name, values) -> {
                    System.out.println("  " + name + ": " + String.join(", ", values));
                });
            }
            
            // Read response body
            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line).append("\n");
                }
            }
            
            System.out.println("Response Body:");
            System.out.println(responseBody.toString());
            System.out.println("=== END RESPONSE ===");
            
            // Create a new response with the body we just read
            return new BufferedClientHttpResponse(response, responseBody.toString().getBytes(StandardCharsets.UTF_8));
            
        } catch (Exception e) {
            logger.error("Error logging response", e);
            return response;
        }
    }
}