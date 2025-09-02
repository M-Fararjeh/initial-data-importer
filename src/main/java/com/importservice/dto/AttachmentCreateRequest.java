package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class AttachmentCreateRequest {
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    @JsonProperty("context")
    private Map<String, Object> context;
    
    public AttachmentCreateRequest() {
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
    
    public void setDocDate(String docDate) {
        this.params.put("docDate", docDate);
    }
    
    public void setAsUser(String asUser) {
        this.params.put("asUser", asUser);
    }
    
    public void setDocID(String docID) {
        this.params.put("docID", docID);
    }
    
    public void setGuid(String guid) {
        this.params.put("guid", guid);
    }

    public void setDocCreator(String docCreator) {
        this.params.put("docCreator", docCreator);
    }

    // Helper method for context
    public void setAttachment(Map<String, Object> attachment) {
        this.context.put("attachment", attachment);
    }
    
    @Override
    public String toString() {
        return "AttachmentCreateRequest{" +
                "params=" + params +
                ", context=" + context +
                '}';
    }
}