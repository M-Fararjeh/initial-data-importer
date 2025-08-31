package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class CreateUserRequestDto {
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    @JsonProperty("context")
    private Map<String, Object> context;
    
    @JsonProperty("input")
    private String input;
    
    public CreateUserRequestDto() {
        this.params = new HashMap<>();
        this.context = new HashMap<>();
        this.input = "";
    }
    
    public Map<String, Object> getParams() {
        return params;
    }
    
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
    
    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
    
    public String getInput() {
        return input;
    }
    
    public void setInput(String input) {
        this.input = input;
    }
    
    // Helper methods to set context values
    public void setUsername(String username) {
        this.context.put("username", username);
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.context.put("phoneNumber", phoneNumber);
    }
    
    public void setEmail(String email) {
        this.context.put("email", email);
    }
    
    public void setFirstNameAr(String firstNameAr) {
        this.context.put("firstNameAr", firstNameAr);
    }
    
    public void setLastNameAr(String lastNameAr) {
        this.context.put("lastNameAr", lastNameAr);
    }
    
    public void setFirstNameEn(String firstNameEn) {
        this.context.put("firstNameEn", firstNameEn);
    }
    
    public void setLastNameEn(String lastNameEn) {
        this.context.put("lastNameEn", lastNameEn);
    }
    
    public void setJobGradeEn(String jobGradeEn) {
        this.context.put("jobGradeEn", jobGradeEn);
    }
    
    public void setJobGradeAr(String jobGradeAr) {
        this.context.put("jobGradeAr", jobGradeAr);
    }
    
    public void setKeycloak(String keycloak) {
        this.context.put("keycloak", keycloak);
    }
    
    public void setTenantID(String tenantID) {
        this.context.put("tenantID", tenantID);
    }
    
    @Override
    public String toString() {
        return "CreateUserRequestDto{" +
                "params=" + params +
                ", context=" + context +
                ", input='" + input + '\'' +
                '}';
    }
}