package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignUserRequestDto {
    
    @JsonProperty("params")
    private Map<String, Object> params;
    
    @JsonProperty("context")
    private Map<String, Object> context;
    
    public AssignUserRequestDto() {
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
    
    // Helper methods to set context values
    public void setUsername(String username) {
        this.context.put("username", username);
    }
    
    public void setBasicRole(String basicRole) {
        this.context.put("basicRole", basicRole);
    }
    
    public void setDepartment(String department) {
        this.context.put("department", department);
    }
    
    public void setRoles(List<String> roles) {
        this.context.put("roles", roles);
    }
    
    public void setListOfRoles(List<String> listOfRoles) {
        this.context.put("listOfRoles", listOfRoles);
    }
    
    public void setTenantID(String tenantID) {
        this.context.put("tenantID", tenantID);
    }
    
    @Override
    public String toString() {
        return "AssignUserRequestDto{" +
                "params=" + params +
                ", context=" + context +
                '}';
    }
}