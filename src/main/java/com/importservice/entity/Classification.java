package com.importservice.entity;

import javax.persistence.*;

@Entity
@Table(name = "classifications")
public class Classification extends BaseEntity {
    
    @Id
    @Column(name = "guid", length = 36)
    private String guid;
    
    @Column(name = "english_name", length = 500)
    private String englishName;
    
    @Column(name = "local_name", length = 500)
    private String localName;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "is_hidden")
    private Boolean isHidden;
    
    // Constructors
    public Classification() {}
    
    public Classification(String guid, String englishName, String localName, String notes, Boolean isHidden) {
        this.guid = guid;
        this.englishName = englishName;
        this.localName = localName;
        this.notes = notes;
        this.isHidden = isHidden;
    }
    
    // Getters and Setters
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
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
    
    public Boolean getIsHidden() {
        return isHidden;
    }
    
    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
}