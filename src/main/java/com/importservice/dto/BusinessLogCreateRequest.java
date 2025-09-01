package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class BusinessLogCreateRequest {
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    public BusinessLogCreateRequest() {
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
    
    public void setEventCategory(String eventCategory) {
        this.params.put("eventCategory", eventCategory);
    }
    
    public void setEventName(String eventName) {
        this.params.put("eventName", eventName);
    }
    
    public void setEventDate(String eventDate) {
        this.params.put("eventDate", eventDate);
    }
    
    public void setEventTypes(String eventTypes) {
        this.params.put("eventTypes", eventTypes);
    }
    
    public void setEventComment(String eventComment) {
        this.params.put("eventComment", eventComment);
    }
    
    public void setDocumentTypes(String documentTypes) {
        this.params.put("documentTypes", documentTypes);
    }
    
    public void setExtendedInfo(Object extendedInfo) {
        this.params.put("extendedInfo", extendedInfo);
    }
    
    public void setCurrentLifeCycle(String currentLifeCycle) {
        this.params.put("currentLifeCycle", currentLifeCycle);
    }
    
    public void setPerson(String person) {
        this.params.put("person", person);
    }
    
    @Override
    public String toString() {
        return "BusinessLogCreateRequest{" +
                "params=" + params +
                '}';
    }
}