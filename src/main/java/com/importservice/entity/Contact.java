package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "contacts")
public class Contact extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 2147483647)
    private String guid;
    
    @JsonProperty("EnglishName")
    @Column(name = "english_name", length = 2147483647)
    private String englishName;
    
    @JsonProperty("LocalName")
    @Column(name = "local_name", length = 2147483647)
    private String localName;
    
    @JsonProperty("ParentGUId")
    @Column(name = "parent_guid", length = 2147483647)
    private String parentGuid;
    
    @JsonProperty("EnglishTitle")
    @Column(name = "english_title", length = 2147483647)
    private String englishTitle;
    
    @JsonProperty("LocalTitle")
    @Column(name = "local_title", length = 2147483647)
    private String localTitle;
    
    @JsonProperty("EMail")
    @Column(name = "email", length = 2147483647)
    private String email;
    
    @JsonProperty("Phone")
    @Column(name = "phone", length = 2147483647)
    private String phone;
    
    @JsonProperty("FAX")
    @Column(name = "fax", length = 2147483647)
    private String fax;
    
    @JsonProperty("GlobalId")
    @Column(name = "global_id", length = 2147483647)
    private String globalId;
    
    @JsonProperty("EnglishPrefix")
    @Column(name = "english_prefix", length = 2147483647)
    private String englishPrefix;
    
    @JsonProperty("LocalPrefix")
    @Column(name = "local_prefix", length = 2147483647)
    private String localPrefix;
    
    @JsonProperty("EnglishSuffix")
    @Column(name = "english_suffix", length = 2147483647)
    private String englishSuffix;
    
    @JsonProperty("LocalSuffix")
    @Column(name = "local_suffix", length = 2147483647)
    private String localSuffix;
    
    @JsonProperty("IsBlocked")
    @Column(name = "is_blocked")
    private Integer isBlocked;
    
    // Constructors
    public Contact() {}
    
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
    
    public String getParentGuid() {
        return parentGuid;
    }
    
    public void setParentGuid(String parentGuid) {
        this.parentGuid = parentGuid;
    }
    
    public String getEnglishTitle() {
        return englishTitle;
    }
    
    public void setEnglishTitle(String englishTitle) {
        this.englishTitle = englishTitle;
    }
    
    public String getLocalTitle() {
        return localTitle;
    }
    
    public void setLocalTitle(String localTitle) {
        this.localTitle = localTitle;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getFax() {
        return fax;
    }
    
    public void setFax(String fax) {
        this.fax = fax;
    }
    
    public String getGlobalId() {
        return globalId;
    }
    
    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }
    
    public String getEnglishPrefix() {
        return englishPrefix;
    }
    
    public void setEnglishPrefix(String englishPrefix) {
        this.englishPrefix = englishPrefix;
    }
    
    public String getLocalPrefix() {
        return localPrefix;
    }
    
    public void setLocalPrefix(String localPrefix) {
        this.localPrefix = localPrefix;
    }
    
    public String getEnglishSuffix() {
        return englishSuffix;
    }
    
    public void setEnglishSuffix(String englishSuffix) {
        this.englishSuffix = englishSuffix;
    }
    
    public String getLocalSuffix() {
        return localSuffix;
    }
    
    public void setLocalSuffix(String localSuffix) {
        this.localSuffix = localSuffix;
    }
    
    public Integer getIsBlocked() {
        return isBlocked;
    }
    
    public void setIsBlocked(Integer isBlocked) {
        this.isBlocked = isBlocked;
    }
}