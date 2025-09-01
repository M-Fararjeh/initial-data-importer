package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
                '}';
    }
}