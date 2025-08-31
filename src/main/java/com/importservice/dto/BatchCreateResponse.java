package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BatchCreateResponse {
    
    @JsonProperty("batchId")
    private String batchId;
    
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("message")
    private String message;
    
    public BatchCreateResponse() {}
    
    public BatchCreateResponse(String batchId, Boolean success, String message) {
        this.batchId = batchId;
        this.success = success;
        this.message = message;
    }
    
    public String getBatchId() {
        return batchId;
    }
    
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "BatchCreateResponse{" +
                "batchId='" + batchId + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}