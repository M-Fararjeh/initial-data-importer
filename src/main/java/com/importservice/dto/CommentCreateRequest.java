package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class CommentCreateRequest {
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    public CommentCreateRequest() {
        this.params = new HashMap<>();
    }
    
    public Map<String, Object> getParams() {
        return params;
    }
    
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    // Helper methods for params
    public void setOperationName(String operationName) {
        this.params.put("operationName", operationName);
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
    
    public void setAuthor(String author) {
        this.params.put("author", author);
    }
    
    public void setDate(String date) {
        this.params.put("date", date);
    }
    
    public void setText(String text) {
        this.params.put("text", text);
    }
    
    @Override
    public String toString() {
        return "CommentCreateRequest{" +
                "params=" + params +
                '}';
    }
}