package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "decisions")
public class Decision extends BaseEntity {
    
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
    
    @JsonProperty("DisplayName")
    @Column(name = "display_name", columnDefinition = "TEXT")
    private String displayName;
    
    // Constructors
    public Decision() {}
    
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
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}