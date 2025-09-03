package com.importservice.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "correspondence_import_status", indexes = {
    @javax.persistence.Index(name = "idx_corr_import_guid", columnList = "correspondence_guid"),
    @javax.persistence.Index(name = "idx_corr_import_overall_status", columnList = "overall_status"),
    @javax.persistence.Index(name = "idx_corr_import_retry", columnList = "retry_count, max_retries"),
    @javax.persistence.Index(name = "idx_corr_import_last_error", columnList = "last_error_at"),
    @javax.persistence.Index(name = "idx_corr_import_retryable", columnList = "overall_status, retry_count, max_retries")
})
public class CorrespondenceImportStatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "correspondence_guid", length = 255, nullable = false, unique = true)
    private String correspondenceGuid;
    
    @Column(name = "overall_status", length = 20, nullable = false)
    private String overallStatus = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED, FAILED
    
    // Individual entity import statuses
    @Column(name = "attachments_status", length = 20)
    private String attachmentsStatus = "PENDING";
    
    @Column(name = "comments_status", length = 20)
    private String commentsStatus = "PENDING";
    
    @Column(name = "copy_tos_status", length = 20)
    private String copyTosStatus = "PENDING";
    
    @Column(name = "current_departments_status", length = 20)
    private String currentDepartmentsStatus = "PENDING";
    
    @Column(name = "current_positions_status", length = 20)
    private String currentPositionsStatus = "PENDING";
    
    @Column(name = "current_users_status", length = 20)
    private String currentUsersStatus = "PENDING";
    
    @Column(name = "custom_fields_status", length = 20)
    private String customFieldsStatus = "PENDING";
    
    @Column(name = "links_status", length = 20)
    private String linksStatus = "PENDING";
    
    @Column(name = "send_tos_status", length = 20)
    private String sendTosStatus = "PENDING";
    
    @Column(name = "transactions_status", length = 20)
    private String transactionsStatus = "PENDING";
    
    // Error tracking
    @Column(name = "attachments_error", columnDefinition = "TEXT")
    private String attachmentsError;
    
    @Column(name = "comments_error", columnDefinition = "TEXT")
    private String commentsError;
    
    @Column(name = "copy_tos_error", columnDefinition = "TEXT")
    private String copyTosError;
    
    @Column(name = "current_departments_error", columnDefinition = "TEXT")
    private String currentDepartmentsError;
    
    @Column(name = "current_positions_error", columnDefinition = "TEXT")
    private String currentPositionsError;
    
    @Column(name = "current_users_error", columnDefinition = "TEXT")
    private String currentUsersError;
    
    @Column(name = "custom_fields_error", columnDefinition = "TEXT")
    private String customFieldsError;
    
    @Column(name = "links_error", columnDefinition = "TEXT")
    private String linksError;
    
    @Column(name = "send_tos_error", columnDefinition = "TEXT")
    private String sendTosError;
    
    @Column(name = "transactions_error", columnDefinition = "TEXT")
    private String transactionsError;
    
    // Record counts
    @Column(name = "attachments_count")
    private Integer attachmentsCount = 0;
    
    @Column(name = "comments_count")
    private Integer commentsCount = 0;
    
    @Column(name = "copy_tos_count")
    private Integer copyTosCount = 0;
    
    @Column(name = "current_departments_count")
    private Integer currentDepartmentsCount = 0;
    
    @Column(name = "current_positions_count")
    private Integer currentPositionsCount = 0;
    
    @Column(name = "current_users_count")
    private Integer currentUsersCount = 0;
    
    @Column(name = "custom_fields_count")
    private Integer customFieldsCount = 0;
    
    @Column(name = "links_count")
    private Integer linksCount = 0;
    
    @Column(name = "send_tos_count")
    private Integer sendTosCount = 0;
    
    @Column(name = "transactions_count")
    private Integer transactionsCount = 0;
    
    @Column(name = "total_entities_count")
    private Integer totalEntitiesCount = 0;
    
    @Column(name = "successful_entities_count")
    private Integer successfulEntitiesCount = 0;
    
    @Column(name = "failed_entities_count")
    private Integer failedEntitiesCount = 0;
    
    // Retry tracking
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
    // Timestamps
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "last_error_at")
    private LocalDateTime lastErrorAt;
    
    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;
    
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        creationDate = now;
        lastModifiedDate = now;
        if (startedAt == null) {
            startedAt = now;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
    
    // Constructors
    public CorrespondenceImportStatus() {}
    
    public CorrespondenceImportStatus(String correspondenceGuid) {
        this.correspondenceGuid = correspondenceGuid;
        this.overallStatus = "PENDING";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCorrespondenceGuid() {
        return correspondenceGuid;
    }
    
    public void setCorrespondenceGuid(String correspondenceGuid) {
        this.correspondenceGuid = correspondenceGuid;
    }
    
    public String getOverallStatus() {
        return overallStatus;
    }
    
    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }
    
    public String getAttachmentsStatus() {
        return attachmentsStatus;
    }
    
    public void setAttachmentsStatus(String attachmentsStatus) {
        this.attachmentsStatus = attachmentsStatus;
    }
    
    public String getCommentsStatus() {
        return commentsStatus;
    }
    
    public void setCommentsStatus(String commentsStatus) {
        this.commentsStatus = commentsStatus;
    }
    
    public String getCopyTosStatus() {
        return copyTosStatus;
    }
    
    public void setCopyTosStatus(String copyTosStatus) {
        this.copyTosStatus = copyTosStatus;
    }
    
    public String getCurrentDepartmentsStatus() {
        return currentDepartmentsStatus;
    }
    
    public void setCurrentDepartmentsStatus(String currentDepartmentsStatus) {
        this.currentDepartmentsStatus = currentDepartmentsStatus;
    }
    
    public String getCurrentPositionsStatus() {
        return currentPositionsStatus;
    }
    
    public void setCurrentPositionsStatus(String currentPositionsStatus) {
        this.currentPositionsStatus = currentPositionsStatus;
    }
    
    public String getCurrentUsersStatus() {
        return currentUsersStatus;
    }
    
    public void setCurrentUsersStatus(String currentUsersStatus) {
        this.currentUsersStatus = currentUsersStatus;
    }
    
    public String getCustomFieldsStatus() {
        return customFieldsStatus;
    }
    
    public void setCustomFieldsStatus(String customFieldsStatus) {
        this.customFieldsStatus = customFieldsStatus;
    }
    
    public String getLinksStatus() {
        return linksStatus;
    }
    
    public void setLinksStatus(String linksStatus) {
        this.linksStatus = linksStatus;
    }
    
    public String getSendTosStatus() {
        return sendTosStatus;
    }
    
    public void setSendTosStatus(String sendTosStatus) {
        this.sendTosStatus = sendTosStatus;
    }
    
    public String getTransactionsStatus() {
        return transactionsStatus;
    }
    
    public void setTransactionsStatus(String transactionsStatus) {
        this.transactionsStatus = transactionsStatus;
    }
    
    // Error getters and setters
    public String getAttachmentsError() {
        return attachmentsError;
    }
    
    public void setAttachmentsError(String attachmentsError) {
        this.attachmentsError = attachmentsError;
    }
    
    public String getCommentsError() {
        return commentsError;
    }
    
    public void setCommentsError(String commentsError) {
        this.commentsError = commentsError;
    }
    
    public String getCopyTosError() {
        return copyTosError;
    }
    
    public void setCopyTosError(String copyTosError) {
        this.copyTosError = copyTosError;
    }
    
    public String getCurrentDepartmentsError() {
        return currentDepartmentsError;
    }
    
    public void setCurrentDepartmentsError(String currentDepartmentsError) {
        this.currentDepartmentsError = currentDepartmentsError;
    }
    
    public String getCurrentPositionsError() {
        return currentPositionsError;
    }
    
    public void setCurrentPositionsError(String currentPositionsError) {
        this.currentPositionsError = currentPositionsError;
    }
    
    public String getCurrentUsersError() {
        return currentUsersError;
    }
    
    public void setCurrentUsersError(String currentUsersError) {
        this.currentUsersError = currentUsersError;
    }
    
    public String getCustomFieldsError() {
        return customFieldsError;
    }
    
    public void setCustomFieldsError(String customFieldsError) {
        this.customFieldsError = customFieldsError;
    }
    
    public String getLinksError() {
        return linksError;
    }
    
    public void setLinksError(String linksError) {
        this.linksError = linksError;
    }
    
    public String getSendTosError() {
        return sendTosError;
    }
    
    public void setSendTosError(String sendTosError) {
        this.sendTosError = sendTosError;
    }
    
    public String getTransactionsError() {
        return transactionsError;
    }
    
    public void setTransactionsError(String transactionsError) {
        this.transactionsError = transactionsError;
    }
    
    // Count getters and setters
    public Integer getAttachmentsCount() {
        return attachmentsCount;
    }
    
    public void setAttachmentsCount(Integer attachmentsCount) {
        this.attachmentsCount = attachmentsCount;
    }
    
    public Integer getCommentsCount() {
        return commentsCount;
    }
    
    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }
    
    public Integer getCopyTosCount() {
        return copyTosCount;
    }
    
    public void setCopyTosCount(Integer copyTosCount) {
        this.copyTosCount = copyTosCount;
    }
    
    public Integer getCurrentDepartmentsCount() {
        return currentDepartmentsCount;
    }
    
    public void setCurrentDepartmentsCount(Integer currentDepartmentsCount) {
        this.currentDepartmentsCount = currentDepartmentsCount;
    }
    
    public Integer getCurrentPositionsCount() {
        return currentPositionsCount;
    }
    
    public void setCurrentPositionsCount(Integer currentPositionsCount) {
        this.currentPositionsCount = currentPositionsCount;
    }
    
    public Integer getCurrentUsersCount() {
        return currentUsersCount;
    }
    
    public void setCurrentUsersCount(Integer currentUsersCount) {
        this.currentUsersCount = currentUsersCount;
    }
    
    public Integer getCustomFieldsCount() {
        return customFieldsCount;
    }
    
    public void setCustomFieldsCount(Integer customFieldsCount) {
        this.customFieldsCount = customFieldsCount;
    }
    
    public Integer getLinksCount() {
        return linksCount;
    }
    
    public void setLinksCount(Integer linksCount) {
        this.linksCount = linksCount;
    }
    
    public Integer getSendTosCount() {
        return sendTosCount;
    }
    
    public void setSendTosCount(Integer sendTosCount) {
        this.sendTosCount = sendTosCount;
    }
    
    public Integer getTransactionsCount() {
        return transactionsCount;
    }
    
    public void setTransactionsCount(Integer transactionsCount) {
        this.transactionsCount = transactionsCount;
    }
    
    public Integer getTotalEntitiesCount() {
        return totalEntitiesCount;
    }
    
    public void setTotalEntitiesCount(Integer totalEntitiesCount) {
        this.totalEntitiesCount = totalEntitiesCount;
    }
    
    public Integer getSuccessfulEntitiesCount() {
        return successfulEntitiesCount;
    }
    
    public void setSuccessfulEntitiesCount(Integer successfulEntitiesCount) {
        this.successfulEntitiesCount = successfulEntitiesCount;
    }
    
    public Integer getFailedEntitiesCount() {
        return failedEntitiesCount;
    }
    
    public void setFailedEntitiesCount(Integer failedEntitiesCount) {
        this.failedEntitiesCount = failedEntitiesCount;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
    
    public Integer getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public LocalDateTime getLastErrorAt() {
        return lastErrorAt;
    }
    
    public void setLastErrorAt(LocalDateTime lastErrorAt) {
        this.lastErrorAt = lastErrorAt;
    }
    
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
    
    // Helper methods
    public boolean canRetry() {
        return retryCount < maxRetries;
    }
    
    public void incrementRetryCount() {
        this.retryCount++;
        this.lastErrorAt = LocalDateTime.now();
    }
    
    public void markEntitySuccess(String entityType, Integer count) {
        switch (entityType.toLowerCase()) {
            case "attachments":
                this.attachmentsStatus = "SUCCESS";
                this.attachmentsCount = count;
                this.attachmentsError = null;
                break;
            case "comments":
                this.commentsStatus = "SUCCESS";
                this.commentsCount = count;
                this.commentsError = null;
                break;
            case "copytos":
                this.copyTosStatus = "SUCCESS";
                this.copyTosCount = count;
                this.copyTosError = null;
                break;
            case "currentdepartments":
                this.currentDepartmentsStatus = "SUCCESS";
                this.currentDepartmentsCount = count;
                this.currentDepartmentsError = null;
                break;
            case "currentpositions":
                this.currentPositionsStatus = "SUCCESS";
                this.currentPositionsCount = count;
                this.currentPositionsError = null;
                break;
            case "currentusers":
                this.currentUsersStatus = "SUCCESS";
                this.currentUsersCount = count;
                this.currentUsersError = null;
                break;
            case "customfields":
                this.customFieldsStatus = "SUCCESS";
                this.customFieldsCount = count;
                this.customFieldsError = null;
                break;
            case "links":
                this.linksStatus = "SUCCESS";
                this.linksCount = count;
                this.linksError = null;
                break;
            case "sendtos":
                this.sendTosStatus = "SUCCESS";
                this.sendTosCount = count;
                this.sendTosError = null;
                break;
            case "transactions":
                this.transactionsStatus = "SUCCESS";
                this.transactionsCount = count;
                this.transactionsError = null;
                break;
        }
        updateOverallStatus();
    }
    
    public void markEntityFailed(String entityType, String error) {
        switch (entityType.toLowerCase()) {
            case "attachments":
                this.attachmentsStatus = "FAILED";
                this.attachmentsError = error;
                break;
            case "comments":
                this.commentsStatus = "FAILED";
                this.commentsError = error;
                break;
            case "copytos":
                this.copyTosStatus = "FAILED";
                this.copyTosError = error;
                break;
            case "currentdepartments":
                this.currentDepartmentsStatus = "FAILED";
                this.currentDepartmentsError = error;
                break;
            case "currentpositions":
                this.currentPositionsStatus = "FAILED";
                this.currentPositionsError = error;
                break;
            case "currentusers":
                this.currentUsersStatus = "FAILED";
                this.currentUsersError = error;
                break;
            case "customfields":
                this.customFieldsStatus = "FAILED";
                this.customFieldsError = error;
                break;
            case "links":
                this.linksStatus = "FAILED";
                this.linksError = error;
                break;
            case "sendtos":
                this.sendTosStatus = "FAILED";
                this.sendTosError = error;
                break;
            case "transactions":
                this.transactionsStatus = "FAILED";
                this.transactionsError = error;
                break;
        }
        this.lastErrorAt = LocalDateTime.now();
        updateOverallStatus();
    }
    
    public void markEntityInProgress(String entityType) {
        switch (entityType.toLowerCase()) {
            case "attachments":
                this.attachmentsStatus = "IN_PROGRESS";
                break;
            case "comments":
                this.commentsStatus = "IN_PROGRESS";
                break;
            case "copytos":
                this.copyTosStatus = "IN_PROGRESS";
                break;
            case "currentdepartments":
                this.currentDepartmentsStatus = "IN_PROGRESS";
                break;
            case "currentpositions":
                this.currentPositionsStatus = "IN_PROGRESS";
                break;
            case "currentusers":
                this.currentUsersStatus = "IN_PROGRESS";
                break;
            case "customfields":
                this.customFieldsStatus = "IN_PROGRESS";
                break;
            case "links":
                this.linksStatus = "IN_PROGRESS";
                break;
            case "sendtos":
                this.sendTosStatus = "IN_PROGRESS";
                break;
            case "transactions":
                this.transactionsStatus = "IN_PROGRESS";
                break;
        }
        this.overallStatus = "IN_PROGRESS";
    }
    
    private void updateOverallStatus() {
        String[] statuses = {
            attachmentsStatus, commentsStatus, copyTosStatus, currentDepartmentsStatus,
            currentPositionsStatus, currentUsersStatus, customFieldsStatus, 
            linksStatus, sendTosStatus, transactionsStatus
        };
        
        boolean allCompleted = true;
        boolean anyFailed = false;
        boolean anyInProgress = false;
        
        for (String status : statuses) {
            if ("FAILED".equals(status)) {
                anyFailed = true;
            } else if ("IN_PROGRESS".equals(status)) {
                anyInProgress = true;
                allCompleted = false;
            } else if (!"SUCCESS".equals(status)) {
                allCompleted = false;
            }
        }
        
        if (anyInProgress) {
            this.overallStatus = "IN_PROGRESS";
        } else if (allCompleted) {
            this.overallStatus = "COMPLETED";
            this.completedAt = LocalDateTime.now();
        } else if (anyFailed) {
            this.overallStatus = "FAILED";
        } else {
            this.overallStatus = "PENDING";
        }
        
        // Update counts
        this.totalEntitiesCount = 10; // Total number of entity types
        this.successfulEntitiesCount = 0;
        this.failedEntitiesCount = 0;
        
        for (String status : statuses) {
            if ("SUCCESS".equals(status)) {
                this.successfulEntitiesCount++;
            } else if ("FAILED".equals(status)) {
                this.failedEntitiesCount++;
            }
        }
    }
    
    public boolean hasAnyFailures() {
        String[] statuses = {
            attachmentsStatus, commentsStatus, copyTosStatus, currentDepartmentsStatus,
            currentPositionsStatus, currentUsersStatus, customFieldsStatus, 
            linksStatus, sendTosStatus, transactionsStatus
        };
        
        for (String status : statuses) {
            if ("FAILED".equals(status)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(overallStatus);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(overallStatus);
    }
    
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(overallStatus);
    }
    
    public double getCompletionPercentage() {
        if (totalEntitiesCount == 0) return 0.0;
        return (double) successfulEntitiesCount / totalEntitiesCount * 100.0;
    }
}