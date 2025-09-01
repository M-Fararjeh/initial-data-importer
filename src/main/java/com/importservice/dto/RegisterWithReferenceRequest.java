package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class RegisterWithReferenceRequest {
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    @JsonProperty("context")
    private Map<String, Object> context;
    
    public RegisterWithReferenceRequest() {
        this.params = new HashMap<>();
        this.context = new HashMap<>();
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
    
    // Helper methods for params
    public void setOperationName(String operationName) {
        this.params.put("operationName", operationName);
    }
    
    public void setAsUser(String asUser) {
        this.params.put("asUser", asUser);
    }
    
    public void setDocID(String docID) {
        this.params.put("docID", docID);
    }
    
    // Helper method for context
    public void setIncCorrespondence(Map<String, Object> incCorrespondence) {
        this.context.put("incCorrespondence", incCorrespondence);
    }
    
    @Override
    public String toString() {
        return "RegisterWithReferenceRequest{" +
                "params=" + params +
                ", context=" + context +
                '}';
    }
}