package com.importservice.entity;

import javax.persistence.*;

@Entity
@Table(name = "decisions")
public class Decision extends BaseEntity {
    
    @Id
    @Column(name = "guid", length = 36)
    private String guid;
    
    @Column(name = "english_name", length = 500)
    private String englishName;
    
    @Column(name = "local_name", length = 500)
    private String localName;
    
    @Column(name = "display_name", length = 500)
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