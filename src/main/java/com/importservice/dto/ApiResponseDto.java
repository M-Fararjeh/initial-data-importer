package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ApiResponseDto<T> {
    
    @JsonProperty("Data")
    private List<T> data;
    
    @JsonProperty("Success")
    private Boolean success;
    
    @JsonProperty("Message")
    private String message;
    
    public ApiResponseDto() {}
    
    public List<T> getData() {
        return data;
    }
    
    public void setData(List<T> data) {
        this.data = data;
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
}