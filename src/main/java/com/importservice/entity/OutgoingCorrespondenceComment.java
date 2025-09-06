package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outgoing_correspondence_comments")
public class OutgoingCorrespondenceComment extends BaseEntity {
    
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
    
    @JsonProperty("CreationUserName")
    @Column(name = "creation_user_name", length = 500)
    private String creationUserName;
    
    @JsonProperty("RoleGUId")
    @Column(name = "role_guid", length = 255)
    private String roleGuid;
    
    @JsonProperty("PositionGUId")
    @Column(name = "position_guid", length = 255)
    private String positionGuid;
    
    @JsonProperty("DepartmentGUId")
    @Column(name = "department_guid", length = 255)
    private String departmentGuid;
    
    @JsonProperty("IsPrivate")
    @Column(name = "is_private")
    private Boolean isPrivate;
    
    @JsonProperty("IsDeleted")
    @Column(name = "is_deleted")
    private Boolean isDeleted;
    
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
    
    // Constructors
    public OutgoingCorrespondenceComment() {}
    
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
    
    public String getCreationUserName() {
        return creationUserName;
    }
    
    public void setCreationUserName(String creationUserName) {
        this.creationUserName = creationUserName;
    }
    
    public String getRoleGuid() {
        return roleGuid;
    }
    
    public void setRoleGuid(String roleGuid) {
        this.roleGuid = roleGuid;
    }
    
    public String getPositionGuid() {
        return positionGuid;
    }
    
    public void setPositionGuid(String positionGuid) {
        this.positionGuid = positionGuid;
    }
    
    public String getDepartmentGuid() {
        return departmentGuid;
    }
    
    public void setDepartmentGuid(String departmentGuid) {
        this.departmentGuid = departmentGuid;
    }
    
    public Boolean getIsPrivate() {
        return isPrivate;
    }
    
    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
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
}