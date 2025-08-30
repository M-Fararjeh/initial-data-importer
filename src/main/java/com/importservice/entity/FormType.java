package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "form_types")
public class FormType extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid")
    private Integer guid;
    
    @JsonProperty("EnglishName")
    @Column(name = "english_name", columnDefinition = "TEXT")
    private String englishName;
    
    @JsonProperty("LocalName")
    @Column(name = "local_name", columnDefinition = "TEXT")
    private String localName;
    
    @JsonProperty("Notes")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @JsonProperty("IsVisible")
    @Column(name = "is_visible")
    private Integer isVisible;
    
    // Constructors
    public FormType() {}
    
    // Getters and Setters
    public Integer getGuid() {
        return guid;
    }
    
    public void setGuid(Integer guid) {
        this.guid = guid;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }
    
    public String getLocalName() {
        return localName;
    }
    
    public void setLocalName(String localName) {
        this.localName = localName;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Integer getIsVisible() {
        return isVisible;
    }
    
    public void setIsVisible(Integer isVisible) {
        this.isVisible = isVisible;
    }
}