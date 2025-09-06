package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "departments")
public class Department extends BaseEntity {
    
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
    
    @JsonProperty("PrintCode")
    @Column(name = "print_code", length = 255)
    private String printCode;
    
    @JsonProperty("GlobalId")
    @Column(name = "global_id", length = 255)
    private String globalId;
    
    @JsonProperty("ParentGUId")
    @Column(name = "parent_guid", length = 255)
    private String parentGuid;
    
    @JsonProperty("MainParentGUId")
    @Column(name = "main_parent_guid", length = 255)
    private String mainParentGuid;
    
    @JsonProperty("IsHidden")
    @Column(name = "is_hidden")
    private Boolean isHidden;
    
    // Constructors
    public Department() {}
    
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
    
    public String getPrintCode() {
        return printCode;
    }
    
    public void setPrintCode(String printCode) {
        this.printCode = printCode;
    }
    
    public String getGlobalId() {
        return globalId;
    }
    
    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }
    
    public String getParentGuid() {
        return parentGuid;
    }
    
    public void setParentGuid(String parentGuid) {
        this.parentGuid = parentGuid;
    }
    
    public String getMainParentGuid() {
        return mainParentGuid;
    }
    
    public void setMainParentGuid(String mainParentGuid) {
        this.mainParentGuid = mainParentGuid;
    }
    
    public Boolean getIsHidden() {
        return isHidden;
    }
    
    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
}