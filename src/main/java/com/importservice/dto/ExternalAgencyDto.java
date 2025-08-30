package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalAgencyDto {
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("label_en")
    private String labelEn;
    
    @JsonProperty("label_ar")
    private String labelAr;

    public ExternalAgencyDto() {
    }

    public ExternalAgencyDto(Integer id, String category, String labelEn, String labelAr) {
        this.id = id;
        this.category = category;
        this.labelEn = labelEn;
        this.labelAr = labelAr;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLabelEn() {
        return labelEn;
    }

    public void setLabelEn(String labelEn) {
        this.labelEn = labelEn;
    }

    public String getLabelAr() {
        return labelAr;
    }

    public void setLabelAr(String labelAr) {
        this.labelAr = labelAr;
    }

    @Override
    public String toString() {
        return "ExternalAgencyDto{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", labelEn='" + labelEn + '\'' +
                ", labelAr='" + labelAr + '\'' +
                '}';
    }
}