package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "roles", indexes = {
    @javax.persistence.Index(name = "idx_roles_main_parent_guid", columnList = "main_parent_guid")
})
public class Role extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 255)
    private String guid;
    
    @JsonProperty("EnglishName")
    @Column(name = "english_name", columnDefinition = "TEXT")
    private String englishName;
    
    @JsonProperty("LocalName")
    @Column(name = "local_name", columnDefinition = "TEXT")
    private String localName;
    
    @JsonProperty("Notes")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @JsonProperty("MainParentGUId")
    @Column(name = "main_parent_guid", length = 255)
    private String mainParentGuid;
    
    // Constructors
    public Role() {}
    
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
    
    public String getMainParentGuid() {
        return mainParentGuid;
    }
    
    public void setMainParentGuid(String mainParentGuid) {
        this.mainParentGuid = mainParentGuid;
    }
}