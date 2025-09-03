package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "correspondence_comments", indexes = {
    @javax.persistence.Index(name = "idx_corr_comment_doc_guid", columnList = "doc_guid"),
    @javax.persistence.Index(name = "idx_corr_comment_migrate_status", columnList = "migrate_status"),
    @javax.persistence.Index(name = "idx_corr_comment_type", columnList = "comment_type"),
    @javax.persistence.Index(name = "idx_corr_comment_creation_date", columnList = "comment_creation_date"),
    @javax.persistence.Index(name = "idx_corr_comment_retry_count", columnList = "retry_count"),
    @javax.persistence.Index(name = "idx_corr_comment_processing", columnList = "migrate_status, retry_count"),
    @javax.persistence.Index(name = "idx_corr_comment_creation_user", columnList = "creation_user_guid")
})
public class CorrespondenceComment extends BaseEntity {
    
    @Id
    @JsonProperty("CommentGUId")
    @Column(name = "comment_guid", length = 255)
    private String commentGuid;
    
    @JsonProperty("DocGUId")
    @Column(name = "doc_guid", length = 255)
    private String docGuid;
    
    @JsonProperty("CreationDate")
    @Column(name = "comment_creation_date")
    private LocalDateTime commentCreationDate;
    
    @JsonProperty("Comment")
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
    
    @JsonProperty("CommentType")
    @Column(name = "comment_type", length = 255)
    private String commentType;
    
    @JsonProperty("CreationUserGUId")
    @Column(name = "creation_user_guid", length = 255)
    private String creationUserGuid;
    
    @JsonProperty("RoleGUId")
    @Column(name = "role_guid", length = 255)
    private String roleGuid;
    
    @JsonProperty("AttachmentCaption")
    @Column(name = "attachment_caption", length = 500)
    private String attachmentCaption;
    
    @JsonProperty("AttachmentFileData")
    @Column(name = "attachment_file_data", columnDefinition = "LONGTEXT")
    private String attachmentFileData;
    
    @JsonProperty("AttachmentFileDataErrorMessage")
    @Column(name = "attachment_file_data_error_message", columnDefinition = "TEXT")
    private String attachmentFileDataErrorMessage;
    
    @Column(name = "import_status", length = 255)
    private String importStatus = "PENDING";
    
    @Column(name = "migrate_status", length = 255)
    private String migrateStatus = "PENDING";
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    // Constructors
    public CorrespondenceComment() {}
    
    // Getters and Setters
    public String getCommentGuid() {
        return commentGuid;
    }
    
    public void setCommentGuid(String commentGuid) {
        this.commentGuid = commentGuid;
    }
    
    public String getDocGuid() {
        return docGuid;
    }
    
    public void setDocGuid(String docGuid) {
        this.docGuid = docGuid;
    }
    
    public LocalDateTime getCommentCreationDate() {
        return commentCreationDate;
    }
    
    public void setCommentCreationDate(LocalDateTime commentCreationDate) {
        this.commentCreationDate = commentCreationDate;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getCommentType() {
        return commentType;
    }
    
    public void setCommentType(String commentType) {
        this.commentType = commentType;
    }
    
    public String getCreationUserGuid() {
        return creationUserGuid;
    }
    
    public void setCreationUserGuid(String creationUserGuid) {
        this.creationUserGuid = creationUserGuid;
    }
    
    public String getRoleGuid() {
        return roleGuid;
    }
    
    public void setRoleGuid(String roleGuid) {
        this.roleGuid = roleGuid;
    }
    
    public String getAttachmentCaption() {
        return attachmentCaption;
    }
    
    public void setAttachmentCaption(String attachmentCaption) {
        this.attachmentCaption = attachmentCaption;
    }
    
    public String getAttachmentFileData() {
        return attachmentFileData;
    }
    
    public void setAttachmentFileData(String attachmentFileData) {
        this.attachmentFileData = attachmentFileData;
    }
    
    public String getAttachmentFileDataErrorMessage() {
        return attachmentFileDataErrorMessage;
    }
    
    public void setAttachmentFileDataErrorMessage(String attachmentFileDataErrorMessage) {
        this.attachmentFileDataErrorMessage = attachmentFileDataErrorMessage;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
    
    public String getMigrateStatus() {
        return migrateStatus;
    }
    
    public void setMigrateStatus(String migrateStatus) {
        this.migrateStatus = migrateStatus;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
}