package com.importservice.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incoming_correspondence_migrations")
public class IncomingCorrespondenceMigration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "correspondence_guid", length = 255, nullable = false)
    private String correspondenceGuid;
    
    @Column(name = "current_phase", length = 50, nullable = false)
    private String currentPhase = "PREPARE_DATA";
    
    @Column(name = "next_phase", length = 50)
    private String nextPhase = "CREATION";
    
    @Column(name = "phase_status", length = 20, nullable = false)
    private String phaseStatus = "PENDING";
    
    @Column(name = "is_need_to_close")
    private Boolean isNeedToClose = false;
    
    @Column(name = "prepare_data_status", length = 20)
    private String prepareDataStatus = "PENDING";
    
    @Column(name = "prepare_data_error", columnDefinition = "TEXT")
    private String prepareDataError;
    
    @Column(name = "creation_status", length = 20)
    private String creationStatus = "PENDING";
    
    @Column(name = "creation_error", columnDefinition = "TEXT")
    private String creationError;
    
    @Column(name = "creation_step", length = 50)
    private String creationStep = "GET_DETAILS";
    
    @Column(name = "assignment_status", length = 20)
    private String assignmentStatus = "PENDING";
    
    @Column(name = "assignment_error", columnDefinition = "TEXT")
    private String assignmentError;
    
    @Column(name = "business_log_status", length = 20)
    private String businessLogStatus = "PENDING";
    
    @Column(name = "business_log_error", columnDefinition = "TEXT")
    private String businessLogError;
    
    @Column(name = "comment_status", length = 20)
    private String commentStatus = "PENDING";
    
    @Column(name = "comment_error", columnDefinition = "TEXT")
    private String commentError;
    
    @Column(name = "closing_status", length = 20)
    private String closingStatus = "PENDING";
    
    @Column(name = "closing_error", columnDefinition = "TEXT")
    private String closingError;
    
    @Column(name = "overall_status", length = 20)
    private String overallStatus = "IN_PROGRESS";
    
    @Column(name = "created_document_id", length = 255)
    private String createdDocumentId;
    
    @Column(name = "batch_id", length = 255)
    private String batchId;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
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
    public IncomingCorrespondenceMigration() {}
    
    public IncomingCorrespondenceMigration(String correspondenceGuid, Boolean isNeedToClose) {
        this.correspondenceGuid = correspondenceGuid;
        this.isNeedToClose = isNeedToClose;
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
    
    public String getCurrentPhase() {
        return currentPhase;
    }
    
    public void setCurrentPhase(String currentPhase) {
        this.currentPhase = currentPhase;
    }
    
    public String getNextPhase() {
        return nextPhase;
    }
    
    public void setNextPhase(String nextPhase) {
        this.nextPhase = nextPhase;
    }
    
    public String getPhaseStatus() {
        return phaseStatus;
    }
    
    public void setPhaseStatus(String phaseStatus) {
        this.phaseStatus = phaseStatus;
    }
    
    public Boolean getIsNeedToClose() {
        return isNeedToClose;
    }
    
    public void setIsNeedToClose(Boolean isNeedToClose) {
        this.isNeedToClose = isNeedToClose;
    }
    
    public String getPrepareDataStatus() {
        return prepareDataStatus;
    }
    
    public void setPrepareDataStatus(String prepareDataStatus) {
        this.prepareDataStatus = prepareDataStatus;
    }
    
    public String getPrepareDataError() {
        return prepareDataError;
    }
    
    public void setPrepareDataError(String prepareDataError) {
        this.prepareDataError = prepareDataError;
    }
    
    public String getCreationStatus() {
        return creationStatus;
    }
    
    public void setCreationStatus(String creationStatus) {
        this.creationStatus = creationStatus;
    }
    
    public String getCreationError() {
        return creationError;
    }
    
    public void setCreationError(String creationError) {
        this.creationError = creationError;
    }
    
    public String getCreationStep() {
        return creationStep;
    }
    
    public void setCreationStep(String creationStep) {
        this.creationStep = creationStep;
    }
    
    public String getAssignmentStatus() {
        return assignmentStatus;
    }
    
    public void setAssignmentStatus(String assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }
    
    public String getAssignmentError() {
        return assignmentError;
    }
    
    public void setAssignmentError(String assignmentError) {
        this.assignmentError = assignmentError;
    }
    
    public String getBusinessLogStatus() {
        return businessLogStatus;
    }
    
    public void setBusinessLogStatus(String businessLogStatus) {
        this.businessLogStatus = businessLogStatus;
    }
    
    public String getBusinessLogError() {
        return businessLogError;
    }
    
    public void setBusinessLogError(String businessLogError) {
        this.businessLogError = businessLogError;
    }
    
    public String getCommentStatus() {
        return commentStatus;
    }
    
    public void setCommentStatus(String commentStatus) {
        this.commentStatus = commentStatus;
    }
    
    public String getCommentError() {
        return commentError;
    }
    
    public void setCommentError(String commentError) {
        this.commentError = commentError;
    }
    
    public String getClosingStatus() {
        return closingStatus;
    }
    
    public void setClosingStatus(String closingStatus) {
        this.closingStatus = closingStatus;
    }
    
    public String getClosingError() {
        return closingError;
    }
    
    public void setClosingError(String closingError) {
        this.closingError = closingError;
    }
    
    public String getOverallStatus() {
        return overallStatus;
    }
    
    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }
    
    public String getCreatedDocumentId() {
        return createdDocumentId;
    }
    
    public void setCreatedDocumentId(String createdDocumentId) {
        this.createdDocumentId = createdDocumentId;
    }
    
    public String getBatchId() {
        return batchId;
    }
    
    public void setBatchId(String batchId) {
        this.batchId = batchId;
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
    
    public void markPhaseCompleted(String phase) {
        switch (phase) {
            case "PREPARE_DATA":
                this.prepareDataStatus = "COMPLETED";
                this.currentPhase = "CREATION";
                this.nextPhase = "ASSIGNMENT";
                break;
            case "CREATION":
                this.creationStatus = "COMPLETED";
                this.currentPhase = "ASSIGNMENT";
                this.nextPhase = "BUSINESS_LOG";
                break;
            case "ASSIGNMENT":
                this.assignmentStatus = "COMPLETED";
                this.currentPhase = "BUSINESS_LOG";
                this.nextPhase = "COMMENT";
                break;
            case "BUSINESS_LOG":
                this.businessLogStatus = "COMPLETED";
                this.currentPhase = "COMMENT";
                this.nextPhase = "CLOSING";
                break;
            case "COMMENT":
                this.commentStatus = "COMPLETED";
                this.currentPhase = "CLOSING";
                this.nextPhase = null;
                break;
            case "CLOSING":
                this.closingStatus = "COMPLETED";
                this.currentPhase = "COMPLETED";
                this.nextPhase = null;
                this.overallStatus = "COMPLETED";
                this.completedAt = LocalDateTime.now();
                break;
        }
        this.phaseStatus = "COMPLETED";
    }
    
    public void markPhaseError(String phase, String error) {
        this.lastErrorAt = LocalDateTime.now();
        this.phaseStatus = "ERROR";
        
        switch (phase) {
            case "PREPARE_DATA":
                this.prepareDataStatus = "ERROR";
                this.prepareDataError = error;
                break;
            case "CREATION":
                this.creationStatus = "ERROR";
                this.creationError = error;
                break;
            case "ASSIGNMENT":
                this.assignmentStatus = "ERROR";
                this.assignmentError = error;
                break;
            case "BUSINESS_LOG":
                this.businessLogStatus = "ERROR";
                this.businessLogError = error;
                break;
            case "COMMENT":
                this.commentStatus = "ERROR";
                this.commentError = error;
                break;
            case "CLOSING":
                this.closingStatus = "ERROR";
                this.closingError = error;
                break;
        }
        
        if (!canRetry()) {
            this.overallStatus = "FAILED";
        }
    }
}