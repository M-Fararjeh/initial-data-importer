package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CorrespondenceCreateResponse {
    
    @JsonProperty("entity-type")
    private String entityType;
    
    @JsonProperty("repository")
    private String repository;
    
    @JsonProperty("uid")
    private String uid;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("lastModified")
    private String lastModified;
    
    @JsonProperty("parentRef")
    private String parentRef;
    
    @JsonProperty("isCheckedOut")
    private Boolean isCheckedOut;
    
    @JsonProperty("isRecord")
    private Boolean isRecord;
    
    @JsonProperty("retainUntil")
    private String retainUntil;
    
    @JsonProperty("hasLegalHold")
    private Boolean hasLegalHold;
    
    @JsonProperty("isUnderRetentionOrLegalHold")
    private Boolean isUnderRetentionOrLegalHold;
    
    @JsonProperty("isVersion")
    private Boolean isVersion;
    
    @JsonProperty("isProxy")
    private Boolean isProxy;
    
    @JsonProperty("changeToken")
    private String changeToken;
    
    @JsonProperty("isTrashed")
    private Boolean isTrashed;
    
    @JsonProperty("facets")
    private List<String> facets;
    
    @JsonProperty("schemas")
    private List<Map<String, Object>> schemas;
    
    // Constructors
    public CorrespondenceCreateResponse() {}
    
    // Getters and Setters
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public String getRepository() {
        return repository;
    }
    
    public void setRepository(String repository) {
        this.repository = repository;
    }
    
    public String getUid() {
        return uid;
    }
    
    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
    
    public String getParentRef() {
        return parentRef;
    }
    
    public void setParentRef(String parentRef) {
        this.parentRef = parentRef;
    }
    
    public Boolean getIsCheckedOut() {
        return isCheckedOut;
    }
    
    public void setIsCheckedOut(Boolean isCheckedOut) {
        this.isCheckedOut = isCheckedOut;
    }
    
    public Boolean getIsRecord() {
        return isRecord;
    }
    
    public void setIsRecord(Boolean isRecord) {
        this.isRecord = isRecord;
    }
    
    public String getRetainUntil() {
        return retainUntil;
    }
    
    public void setRetainUntil(String retainUntil) {
        this.retainUntil = retainUntil;
    }
    
    public Boolean getHasLegalHold() {
        return hasLegalHold;
    }
    
    public void setHasLegalHold(Boolean hasLegalHold) {
        this.hasLegalHold = hasLegalHold;
    }
    
    public Boolean getIsUnderRetentionOrLegalHold() {
        return isUnderRetentionOrLegalHold;
    }
    
    public void setIsUnderRetentionOrLegalHold(Boolean isUnderRetentionOrLegalHold) {
        this.isUnderRetentionOrLegalHold = isUnderRetentionOrLegalHold;
    }
    
    public Boolean getIsVersion() {
        return isVersion;
    }
    
    public void setIsVersion(Boolean isVersion) {
        this.isVersion = isVersion;
    }
    
    public Boolean getIsProxy() {
        return isProxy;
    }
    
    public void setIsProxy(Boolean isProxy) {
        this.isProxy = isProxy;
    }
    
    public String getChangeToken() {
        return changeToken;
    }
    
    public void setChangeToken(String changeToken) {
        this.changeToken = changeToken;
    }
    
    public Boolean getIsTrashed() {
        return isTrashed;
    }
    
    public void setIsTrashed(Boolean isTrashed) {
        this.isTrashed = isTrashed;
    }
    
    public List<String> getFacets() {
        return facets;
    }
    
    public void setFacets(List<String> facets) {
        this.facets = facets;
    }
    
    public List<Map<String, Object>> getSchemas() {
        return schemas;
    }
    
    public void setSchemas(List<Map<String, Object>> schemas) {
        this.schemas = schemas;
    }
    
    @Override
    public String toString() {
        return "CorrespondenceCreateResponse{" +
                "entityType='" + entityType + '\'' +
                ", repository='" + repository + '\'' +
                ", uid='" + uid + '\'' +
                ", path='" + path + '\'' +
                ", type='" + type + '\'' +
                ", state='" + state + '\'' +
                ", title='" + title + '\'' +
                ", lastModified='" + lastModified + '\'' +
                ", parentRef='" + parentRef + '\'' +
                ", isCheckedOut=" + isCheckedOut +
                ", changeToken='" + changeToken + '\'' +
                '}';
    }
}