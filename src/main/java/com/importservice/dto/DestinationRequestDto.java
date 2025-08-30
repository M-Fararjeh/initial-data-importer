package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class DestinationRequestDto {
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    @JsonProperty("context")
    private Map<String, Object> context;

    public DestinationRequestDto() {
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
    
    public void setExternalAgencyInfo(ExternalAgencyInfoDto externalAgencyInfo) {
        this.context.put("ExternalAgencyInfo", externalAgencyInfo);
    }

    @Override
    public String toString() {
        return "DestinationRequestDto{" +
                "params=" + params +
                ", context=" + context +
                '}';
    }
}