package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalAgencyInfoDto {
    
    @JsonProperty("externalagency:agencyId")
    private String agencyId;
    
    @JsonProperty("externalagency:nameEn")
    private String nameEn;
    
    @JsonProperty("externalagency:nameAr")
    private String nameAr;
    
    @JsonProperty("externalagency:country")
    private String country;
    
    @JsonProperty("externalagency:typee")
    private String type;

    public ExternalAgencyInfoDto() {
    }

    public ExternalAgencyInfoDto(String agencyId, String nameEn, String nameAr, String country, String type) {
        this.agencyId = agencyId;
        this.nameEn = nameEn;
        this.nameAr = nameAr;
        this.country = country;
        this.type = type;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameAr() {
        return nameAr;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ExternalAgencyInfoDto{" +
                "agencyId='" + agencyId + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", nameAr='" + nameAr + '\'' +
                ", country='" + country + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}