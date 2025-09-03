package com.importservice.entity;

import javax.persistence.*;
import org.hibernate.annotations.Index;
import java.time.LocalDateTime;

@MappedSuperclass
@Table(indexes = {
    @Index(name = "idx_base_creation_date", columnList = "creation_date"),
    @Index(name = "idx_base_last_modified", columnList = "last_modified_date"),
    @Index(name = "idx_base_migrate_status", columnList = "migrate_status")
})
public abstract class BaseEntity {
    
    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;
    
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
    
    @Column(name = "migrate_status", length = 20)
    private String migrateStatus = "PENDING";
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        creationDate = now;
        lastModifiedDate = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    
    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
    
    public String getMigrateStatus() {
        return migrateStatus;
    }
    
    public void setMigrateStatus(String migrateStatus) {
        this.migrateStatus = migrateStatus;
    }
}