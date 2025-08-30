package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "positions")
public class Position extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 36)
    private String guid;
    
    @JsonProperty("DepartmentGUId")
    @Column(name = "department_guid", length = 36)
    private String departmentGuid;
    
    @JsonProperty("EnglishPositionName")
    @Column(name = "english_position_name", length = 500)
    private String englishPositionName;
    
    @JsonProperty("LocalPositionName")
    @Column(name = "local_position_name", length = 500)
    private String localPositionName;
    
    @JsonProperty("EnglishPositionTitle")
    @Column(name = "english_position_title", length = 500)
    private String englishPositionTitle;
    
    @JsonProperty("LocalPositionTitle")
    @Column(name = "local_position_title", length = 500)
    private String localPositionTitle;
    
    @JsonProperty("IsManager")
    @Column(name = "is_manager")
    private Boolean isManager;
    
    @JsonProperty("IsHidden")
    @Column(name = "is_hidden")
    private Boolean isHidden;
    
    @JsonProperty("EnglishPrefix")
    @Column(name = "english_prefix", length = 100)
    private String englishPrefix;
    
    @JsonProperty("EnglishSuffix")
    @Column(name = "english_suffix", length = 100)
    private String englishSuffix;
    
    @JsonProperty("LocalPrefix")
    @Column(name = "local_prefix", length = 100)
    private String localPrefix;
    
    @JsonProperty("LocalSuffix")
    @Column(name = "local_suffix", length = 100)
    private String localSuffix;
    
    // Constructors
    public Position() {}
    
    // Getters and Setters
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }
    
    public String getDepartmentGuid() {
        return departmentGuid;
    }
    
    public void setDepartmentGuid(String departmentGuid) {
        this.departmentGuid = departmentGuid;
    }
    
    public String getEnglishPositionName() {
        return englishPositionName;
    }
    
    public void setEnglishPositionName(String englishPositionName) {
        this.englishPositionName = englishPositionName;
    }
    
    public String getLocalPositionName() {
        return localPositionName;
    }
    
    public void setLocalPositionName(String localPositionName) {
        this.localPositionName = localPositionName;
    }
    
    public String getEnglishPositionTitle() {
        return englishPositionTitle;
    }
    
    public void setEnglishPositionTitle(String englishPositionTitle) {
        this.englishPositionTitle = englishPositionTitle;
    }
    
    public String getLocalPositionTitle() {
        return localPositionTitle;
    }
    
    public void setLocalPositionTitle(String localPositionTitle) {
        this.localPositionTitle = localPositionTitle;
    }
    
    public Boolean getIsManager() {
        return isManager;
    }
    
    public void setIsManager(Boolean isManager) {
        this.isManager = isManager;
    }
    
    public Boolean getIsHidden() {
        return isHidden;
    }
    
    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
    
    public String getEnglishPrefix() {
        return englishPrefix;
    }
    
    public void setEnglishPrefix(String englishPrefix) {
        this.englishPrefix = englishPrefix;
    }
    
    public String getEnglishSuffix() {
        return englishSuffix;
    }
    
    public void setEnglishSuffix(String englishSuffix) {
        this.englishSuffix = englishSuffix;
    }
    
    public String getLocalPrefix() {
        return localPrefix;
    }
    
    public void setLocalPrefix(String localPrefix) {
        this.localPrefix = localPrefix;
    }
    
    public String getLocalSuffix() {
        return localSuffix;
    }
    
    public void setLocalSuffix(String localSuffix) {
        this.localSuffix = localSuffix;
    }
}