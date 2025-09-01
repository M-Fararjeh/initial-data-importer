package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class AssignmentCreateRequest {
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    @JsonProperty("context")
    private Map<String, Object> context;
    
    public AssignmentCreateRequest() {
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
    
    public void setDocDate(String docDate) {
        this.params.put("docDate", docDate);
    }
    
    public void setGuid(String guid) {
        this.params.put("guid", guid);
    }
    
    // Helper method for context
    public void setAssignment(Map<String, Object> assignment) {
        this.context.put("assignment", assignment);
    }
    
    @Override
    public String toString() {
        return "AssignmentCreateRequest{" +
                "params=" + params +
                ", context=" + context +
                '}';
    }
}