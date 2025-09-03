package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KeycloakTokenRequest {
    
    @JsonProperty("grant_type")
    private String grantType = "password";
    
    @JsonProperty("client_id")
    private String clientId;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("password")
    private String password;
    
    public KeycloakTokenRequest() {}
    
    public KeycloakTokenRequest(String clientId, String username, String password) {
        this.clientId = clientId;
        this.username = username;
        this.password = password;
    }
    
    public String getGrantType() {
        return grantType;
    }
    
    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String toString() {
        return "KeycloakTokenRequest{" +
                "grantType='" + grantType + '\'' +
                ", clientId='" + clientId + '\'' +
                ", username='" + username + '\'' +
                ", password='[MASKED]'" +
                '}';
    }
}