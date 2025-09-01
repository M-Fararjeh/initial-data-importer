package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AgencyMappingDto {
    
    @JsonProperty("AgencyGUID")
    private String agencyGuid;
    
    @JsonProperty("AgencyCode")
    private Integer agencyCode;
    
    public AgencyMappingDto() {}
    
    public AgencyMappingDto(String agencyGuid, Integer agencyCode) {
        this.agencyGuid = agencyGuid;
        this.agencyCode = agencyCode;
    }
    
    public String getAgencyGuid() {
        return agencyGuid;
    }
    
    public void setAgencyGuid(String agencyGuid) {
        this.agencyGuid = agencyGuid;
    }
    
    public Integer getAgencyCode() {
        return agencyCode;
    }
    
    public void setAgencyCode(Integer agencyCode) {
        this.agencyCode = agencyCode;
    }
    
    @Override
    public String toString() {
        return "AgencyMappingDto{" +
                "agencyGuid='" + agencyGuid + '\'' +
                ", agencyCode=" + agencyCode +
                '}';
    }
}