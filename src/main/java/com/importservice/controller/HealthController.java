package com.importservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import com.importservice.util.CorrespondenceSubjectGenerator;
import com.importservice.service.KeycloakTokenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Controller", description = "Health check operations")
public class HealthController {

    @Autowired
    private CorrespondenceSubjectGenerator subjectGenerator;

    @Autowired
    private KeycloakTokenService keycloakTokenService;

    @GetMapping
    @Operation(summary = "Health Check", description = "Returns the health status of the service")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "data-import-service");
        response.put("version", "1.0.0");
        response.put("randomSubjectEnabled", String.valueOf(subjectGenerator.isRandomSubjectEnabled()));
        response.put("subjectPrefix", subjectGenerator.getSubjectPrefix());
        response.put("keycloakTokenStatus", keycloakTokenService.getTokenStatus());
        response.put("keycloakTokenValid", String.valueOf(keycloakTokenService.isTokenValid()));
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/keycloak-token")
    @Operation(summary = "Get Keycloak Token Status", description = "Returns the current Keycloak token status and information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token status retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getKeycloakTokenStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", keycloakTokenService.getTokenStatus());
        response.put("isValid", keycloakTokenService.isTokenValid());
        response.put("expiryTime", keycloakTokenService.getTokenExpiryTime());
        response.put("hasToken", keycloakTokenService.getCurrentToken() != null);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/keycloak-token/refresh")
    @Operation(summary = "Force Refresh Keycloak Token", description = "Forces a refresh of the Keycloak token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "500", description = "Token refresh failed")
    })
    public ResponseEntity<Map<String, String>> refreshKeycloakToken() {
        Map<String, String> response = new HashMap<>();
        
        try {
            keycloakTokenService.forceTokenRefresh();
            response.put("status", "SUCCESS");
            response.put("message", "Token refreshed successfully");
            response.put("tokenStatus", keycloakTokenService.getTokenStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to refresh token: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/subject-sample")
    @Operation(summary = "Generate Sample Subject", description = "Generates a sample random subject for testing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sample subject generated successfully")
    })
    public ResponseEntity<Map<String, String>> generateSampleSubject() {
        Map<String, String> response = new HashMap<>();
        
        if (subjectGenerator.isRandomSubjectEnabled()) {
            response.put("randomSubject", subjectGenerator.generateRandomSubject());
            response.put("categorySubject", subjectGenerator.generateSubjectWithCategory("General"));
            response.put("typeSubject", subjectGenerator.generateSubjectByType("Incoming", "H"));
        } else {
            response.put("message", "Random subject generation is disabled");
        }
        
        response.put("enabled", String.valueOf(subjectGenerator.isRandomSubjectEnabled()));
        return ResponseEntity.ok(response);
    }
}