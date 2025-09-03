package com.importservice.service;

import com.importservice.dto.KeycloakTokenRequest;
import com.importservice.dto.KeycloakTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class KeycloakTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(KeycloakTokenService.class);
    
    @Value("${keycloak.auth.server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.auth.realm}")
    private String realm;
    
    @Value("${keycloak.auth.client-id}")
    private String clientId;
    
    @Value("${keycloak.auth.username}")
    private String username;
    
    @Value("${keycloak.auth.password}")
    private String password;
    
    @Value("${keycloak.auth.enabled:true}")
    private boolean keycloakEnabled;
    
    private final RestTemplate restTemplate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private String currentToken;
    private LocalDateTime tokenExpiryTime;
    private String refreshToken;
    
    public KeycloakTokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @PostConstruct
    public void initializeToken() {
        if (!keycloakEnabled) {
            logger.info("Keycloak token generation is disabled");
            return;
        }
        
        logger.info("Initializing Keycloak token service");
        
        try {
            // Generate initial token
            generateToken();
            
            // Schedule token refresh (refresh 5 minutes before expiry)
            if (tokenExpiryTime != null) {
                long refreshDelayMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), tokenExpiryTime) - 5;
                if (refreshDelayMinutes > 0) {
                    scheduleTokenRefresh(refreshDelayMinutes);
                } else {
                    // Token expires soon, refresh immediately
                    scheduleTokenRefresh(1);
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to initialize Keycloak token", e);
        }
    }
    
    /**
     * Generates a new JWT token from Keycloak
     */
    public void generateToken() {
        if (!keycloakEnabled) {
            logger.debug("Keycloak is disabled, skipping token generation");
            return;
        }
        
        try {
            logger.info("Generating new Keycloak token for user: {}", username);
            
            String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            // Create form data for token request
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("client_id", clientId);
            formData.add("username", username);
            formData.add("password", password);
            
            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json");
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            
            logger.debug("Requesting token from: {}", tokenUrl);
            logger.debug("Request data: grant_type=password, client_id={}, username={}", clientId, username);
            
            ResponseEntity<KeycloakTokenResponse> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                KeycloakTokenResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                KeycloakTokenResponse tokenResponse = response.getBody();
                
                this.currentToken = tokenResponse.getAccessToken();
                this.refreshToken = tokenResponse.getRefreshToken();
                
                // Calculate expiry time (subtract 5 minutes for safety)
                if (tokenResponse.getExpiresIn() != null) {
                    this.tokenExpiryTime = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn() - 300);
                }
                
                logger.info("Successfully generated Keycloak token");
                logger.info("Token expires in: {} seconds", tokenResponse.getExpiresIn());
                logger.info("Token will be refreshed at: {}", tokenExpiryTime);
                
                // Schedule next refresh
                if (tokenResponse.getExpiresIn() != null) {
                    long refreshDelayMinutes = (tokenResponse.getExpiresIn() - 300) / 60; // Refresh 5 minutes before expiry
                    if (refreshDelayMinutes > 0) {
                        scheduleTokenRefresh(refreshDelayMinutes);
                    }
                }
                
            } else {
                logger.error("Failed to generate token - Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to generate Keycloak token: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error generating Keycloak token", e);
            throw new RuntimeException("Failed to generate Keycloak token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Refreshes the current token using refresh token
     */
    public void refreshToken() {
        if (!keycloakEnabled || refreshToken == null) {
            logger.debug("Cannot refresh token - Keycloak disabled or no refresh token available");
            generateToken(); // Fall back to generating new token
            return;
        }
        
        try {
            logger.info("Refreshing Keycloak token");
            
            String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            // Create form data for refresh request
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "refresh_token");
            formData.add("client_id", clientId);
            formData.add("refresh_token", refreshToken);
            
            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json");
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            
            ResponseEntity<KeycloakTokenResponse> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                KeycloakTokenResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                KeycloakTokenResponse tokenResponse = response.getBody();
                
                this.currentToken = tokenResponse.getAccessToken();
                this.refreshToken = tokenResponse.getRefreshToken();
                
                // Calculate expiry time
                if (tokenResponse.getExpiresIn() != null) {
                    this.tokenExpiryTime = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn() - 300);
                }
                
                logger.info("Successfully refreshed Keycloak token");
                logger.info("New token expires in: {} seconds", tokenResponse.getExpiresIn());
                
                // Schedule next refresh
                if (tokenResponse.getExpiresIn() != null) {
                    long refreshDelayMinutes = (tokenResponse.getExpiresIn() - 300) / 60;
                    if (refreshDelayMinutes > 0) {
                        scheduleTokenRefresh(refreshDelayMinutes);
                    }
                }
                
            } else {
                logger.error("Failed to refresh token - Status: {}, falling back to new token generation", response.getStatusCode());
                generateToken(); // Fall back to generating new token
            }
            
        } catch (Exception e) {
            logger.error("Error refreshing Keycloak token, falling back to new token generation", e);
            generateToken(); // Fall back to generating new token
        }
    }
    
    /**
     * Schedules the next token refresh
     */
    private void scheduleTokenRefresh(long delayMinutes) {
        logger.info("Scheduling token refresh in {} minutes", delayMinutes);
        
        scheduler.schedule(() -> {
            try {
                refreshToken();
            } catch (Exception e) {
                logger.error("Scheduled token refresh failed", e);
                // Try to generate a new token as fallback
                try {
                    generateToken();
                } catch (Exception fallbackError) {
                    logger.error("Fallback token generation also failed", fallbackError);
                }
            }
        }, delayMinutes, TimeUnit.MINUTES);
    }
    
    /**
     * Gets the current valid token
     */
    public String getCurrentToken() {
        if (!keycloakEnabled) {
            logger.debug("Keycloak is disabled, returning null token");
            return null;
        }
        
        // Check if token is about to expire (within 2 minutes)
        if (tokenExpiryTime != null && LocalDateTime.now().isAfter(tokenExpiryTime.minusMinutes(2))) {
            logger.info("Token is about to expire, refreshing...");
            try {
                refreshToken();
            } catch (Exception e) {
                logger.error("Failed to refresh expiring token", e);
            }
        }
        
        return currentToken;
    }
    
    /**
     * Checks if the current token is valid
     */
    public boolean isTokenValid() {
        if (!keycloakEnabled) {
            return false;
        }
        
        return currentToken != null && 
               tokenExpiryTime != null && 
               LocalDateTime.now().isBefore(tokenExpiryTime);
    }
    
    /**
     * Gets token expiry information
     */
    public LocalDateTime getTokenExpiryTime() {
        return tokenExpiryTime;
    }
    
    /**
     * Forces token regeneration (for manual refresh)
     */
    public void forceTokenRefresh() {
        logger.info("Forcing token refresh");
        generateToken();
    }
    
    /**
     * Gets token status information
     */
    public String getTokenStatus() {
        if (!keycloakEnabled) {
            return "DISABLED";
        }
        
        if (currentToken == null) {
            return "NOT_GENERATED";
        }
        
        if (tokenExpiryTime == null) {
            return "UNKNOWN_EXPIRY";
        }
        
        if (LocalDateTime.now().isAfter(tokenExpiryTime)) {
            return "EXPIRED";
        }
        
        long minutesUntilExpiry = ChronoUnit.MINUTES.between(LocalDateTime.now(), tokenExpiryTime);
        if (minutesUntilExpiry < 5) {
            return "EXPIRING_SOON";
        }
        
        return "VALID";
    }
    
    /**
     * Cleanup method
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}