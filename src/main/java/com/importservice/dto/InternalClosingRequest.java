package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class InternalClosingRequest {
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    @JsonProperty("context")
    private Map<String, Object> context;
    
    public InternalClosingRequest() {
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

    public void setDocCreator(String docCreator) {
        this.params.put("docCreator", docCreator);
    }

    public void setUpdateProp(Map<String, Object> updateProp) {
        this.params.put("updateProp", updateProp);
    }

    public void setTenantID(String tenantID) {
        this.context.put("tenantId", tenantID);
    }

    @Override
    public String toString() {
        return "InternalClosingRequest{" +
                "params=" + params +
                ", context=" + context +
                '}';
    }
}