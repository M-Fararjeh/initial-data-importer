package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "correspondence_attachments", indexes = {
    @javax.persistence.Index(name = "idx_corr_attach_doc_guid", columnList = "doc_guid"),
    @javax.persistence.Index(name = "idx_corr_attach_import_status", columnList = "import_status"),
    @javax.persistence.Index(name = "idx_corr_attach_file_type", columnList = "file_type"),
    @javax.persistence.Index(name = "idx_corr_attach_is_primary", columnList = "is_primary"),
    @javax.persistence.Index(name = "idx_corr_attach_creation_date", columnList = "file_creation_date")
})
public class CorrespondenceAttachment extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 255)
    private String guid;
    
    @JsonProperty("DocGUId")
    @Column(name = "doc_guid", length = 255)
    private String docGuid;
    
    @JsonProperty("FileUId")
    @Column(name = "file_uid", length = 255)
    private String fileUid;
    
    @JsonProperty("FileType")
    @Column(name = "file_type", length = 255)
    private String fileType;
    
    @JsonProperty("CreationDate")
    @Column(name = "file_creation_date")
    private LocalDateTime fileCreationDate;
    
    @JsonProperty("Name")
    @Column(name = "name", length = 500)
    private String name;
    
    @JsonProperty("Caption")
    @Column(name = "caption", length = 500)
    private String caption;
    
    @JsonProperty("Description")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @JsonProperty("Order")
    @Column(name = "order_value")
    private Integer orderValue;
    
    @JsonProperty("IsPrimary")
    @Column(name = "is_primary")
    private Boolean isPrimary;
    
    @JsonProperty("IsTemp")
    @Column(name = "is_temp")
    private Boolean isTemp;
    
    @JsonProperty("ArciveFileId")
    @Column(name = "archive_file_id", length = 255)
    private String archiveFileId;
    
    @JsonProperty("CreationUserName")
    @Column(name = "creation_user_name", length = 500)
    private String creationUserName;
    
    @JsonProperty("Annotation")
    @Column(name = "annotation", columnDefinition = "TEXT")
    private String annotation;
    
    @JsonProperty("PrivateAccessGUId")
    @Column(name = "private_access_guid", length = 255)
    private String privateAccessGuid;
    
    @JsonProperty("PrivateAccessType")
    @Column(name = "private_access_type", length = 255)
    private String privateAccessType;
    
    @JsonProperty("FileData")
    @Column(name = "file_data", columnDefinition = "LONGTEXT")
    private String fileData;
    
    @JsonProperty("FileDataErrorMessage")
    @Column(name = "file_data_error_message", columnDefinition = "TEXT")
    private String fileDataErrorMessage;
    
    @Column(name = "import_status", length = 255)
    private String importStatus = "PENDING";
    
    // Constructors
    public CorrespondenceAttachment() {}
    
    // Getters and Setters
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }
    
    public String getDocGuid() {
        return docGuid;
    }
    
    public void setDocGuid(String docGuid) {
        this.docGuid = docGuid;
    }
    
    public String getFileUid() {
        return fileUid;
    }
    
    public void setFileUid(String fileUid) {
        this.fileUid = fileUid;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public LocalDateTime getFileCreationDate() {
        return fileCreationDate;
    }
    
    public void setFileCreationDate(LocalDateTime fileCreationDate) {
        this.fileCreationDate = fileCreationDate;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getOrderValue() {
        return orderValue;
    }
    
    public void setOrderValue(Integer orderValue) {
        this.orderValue = orderValue;
    }
    
    public Boolean getIsPrimary() {
        return isPrimary;
    }
    
    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    
    public Boolean getIsTemp() {
        return isTemp;
    }
    
    public void setIsTemp(Boolean isTemp) {
        this.isTemp = isTemp;
    }
    
    public String getArchiveFileId() {
        return archiveFileId;
    }
    
    public void setArchiveFileId(String archiveFileId) {
        this.archiveFileId = archiveFileId;
    }
    
    public String getCreationUserName() {
        return creationUserName;
    }
    
    public void setCreationUserName(String creationUserName) {
        this.creationUserName = creationUserName;
    }
    
    public String getAnnotation() {
        return annotation;
    }
    
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
    
    public String getPrivateAccessGuid() {
        return privateAccessGuid;
    }
    
    public void setPrivateAccessGuid(String privateAccessGuid) {
        this.privateAccessGuid = privateAccessGuid;
    }
    
    public String getPrivateAccessType() {
        return privateAccessType;
    }
    
    public void setPrivateAccessType(String privateAccessType) {
        this.privateAccessType = privateAccessType;
    }
    
    public String getFileData() {
        return fileData;
    }
    
    public void setFileData(String fileData) {
        this.fileData = fileData;
    }
    
    public String getFileDataErrorMessage() {
        return fileDataErrorMessage;
    }
    
    public void setFileDataErrorMessage(String fileDataErrorMessage) {
        this.fileDataErrorMessage = fileDataErrorMessage;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}