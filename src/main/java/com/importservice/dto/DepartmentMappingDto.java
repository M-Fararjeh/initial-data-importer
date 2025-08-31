package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DepartmentMappingDto {
    
    @JsonProperty("DepartmentCode")
    private String departmentCode;
    
    @JsonProperty("OldDepartmentGuid")
    private String oldDepartmentGuid;
    
    public DepartmentMappingDto() {}
    
    public DepartmentMappingDto(String departmentCode, String oldDepartmentGuid) {
        this.departmentCode = departmentCode;
        this.oldDepartmentGuid = oldDepartmentGuid;
    }
    
    public String getDepartmentCode() {
        return departmentCode;
    }
    
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
    
    public String getOldDepartmentGuid() {
        return oldDepartmentGuid;
    }
    
    public void setOldDepartmentGuid(String oldDepartmentGuid) {
        this.oldDepartmentGuid = oldDepartmentGuid;
    }
    
    @Override
    public String toString() {
        return "DepartmentMappingDto{" +
                "departmentCode='" + departmentCode + '\'' +
                ", oldDepartmentGuid='" + oldDepartmentGuid + '\'' +
                '}';
    }
}