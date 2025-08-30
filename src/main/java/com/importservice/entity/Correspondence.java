package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "correspondences")
public class Correspondence extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 36)
    private String guid;
    
    @JsonProperty("SystemNo")
    @Column(name = "system_no")
    private Integer systemNo;
    
    @JsonProperty("SerialNumber")
    @Column(name = "serial_number", length = 2147483647)
    private String serialNumber;
    
    @JsonProperty("DbCreationDate")
    @Column(name = "db_creation_date")
    private LocalDateTime dbCreationDate;
    
    @JsonProperty("CreationDate")
    @Column(name = "correspondence_creation_date")
    private LocalDateTime correspondenceCreationDate;
    
    @JsonProperty("LastModifiedDate")
    @Column(name = "correspondence_last_modified_date")
    private LocalDateTime correspondenceLastModifiedDate;
    
    @JsonProperty("IncomingDate")
    @Column(name = "incoming_date")
    private LocalDateTime incomingDate;
    
    @JsonProperty("DueDate")
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @JsonProperty("CorrespondenceTypeId")
    @Column(name = "correspondence_type_id")
    private Integer correspondenceTypeId;
    
    @JsonProperty("CorrespondenceFormTypeGUId")
    @Column(name = "correspondence_form_type_guid", length = 2147483647)
    private String correspondenceFormTypeGuid;
    
    @JsonProperty("ClassificationGUId")
    @Column(name = "classification_guid", length = 2147483647)
    private String classificationGuid;
    
    @JsonProperty("Subject")
    @Column(name = "subject", columnDefinition = "TEXT")
    private String subject;
    
    @JsonProperty("ReferenceNo")
    @Column(name = "reference_no", length = 2147483647)
    private String referenceNo;
    
    @JsonProperty("ExternalReferanceNumber")
    @Column(name = "external_reference_number", length = 2147483647)
    private String externalReferenceNumber;
    
    @JsonProperty("Notes")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @JsonProperty("ManualAttacmentsCount")
    @Column(name = "manual_attachments_count", length = 2147483647)
    private String manualAttachmentsCount;
    
    @JsonProperty("ImportanceId")
    @Column(name = "importance_id")
    private Integer importanceId;
    
    @JsonProperty("PriorityId")
    @Column(name = "priority_id")
    private Integer priorityId;
    
    @JsonProperty("SecrecyId")
    @Column(name = "secrecy_id")
    private Integer secrecyId;
    
    @JsonProperty("NeedReplyStatus")
    @Column(name = "need_reply_status", length = 2147483647)
    private String needReplyStatus;
    
    @JsonProperty("ComingFromGUId")
    @Column(name = "coming_from_guid", length = 2147483647)
    private String comingFromGuid;
    
    @JsonProperty("ComingFromType")
    @Column(name = "coming_from_type", length = 2147483647)
    private String comingFromType;
    
    @JsonProperty("LastDecisionGUId")
    @Column(name = "last_decision_guid", length = 2147483647)
    private String lastDecisionGuid;
    
    @JsonProperty("IsDraft")
    @Column(name = "is_draft")
    private Boolean isDraft;
    
    @JsonProperty("IsDeleted")
    @Column(name = "is_deleted")
    private Boolean isDeleted;
    
    @JsonProperty("IsBlocked")
    @Column(name = "is_blocked")
    private Boolean isBlocked;
    
    @JsonProperty("IsCanceled")
    @Column(name = "is_canceled")
    private Integer isCanceled;
    
    @JsonProperty("IsArchive")
    @Column(name = "is_archive")
    private Boolean isArchive;
    
    @JsonProperty("IsFinal")
    @Column(name = "is_final")
    private Boolean isFinal;
    
    @JsonProperty("IsMigrated")
    @Column(name = "is_migrated")
    private Boolean isMigrated;
    
    @JsonProperty("ArchivedDocumentId")
    @Column(name = "archived_document_id", length = 2147483647)
    private String archivedDocumentId;
    
    @JsonProperty("CreationUserName")
    @Column(name = "creation_user_name", length = 2147483647)
    private String creationUserName;
    
    @JsonProperty("CreationUserGUId")
    @Column(name = "creation_user_guid", length = 2147483647)
    private String creationUserGuid;
    
    @JsonProperty("CreationPositionGUId")
    @Column(name = "creation_position_guid", length = 2147483647)
    private String creationPositionGuid;
    
    @JsonProperty("CreationDepartmentGUId")
    @Column(name = "creation_department_guid", length = 2147483647)
    private String creationDepartmentGuid;
    
    @JsonProperty("FromPositionGUId")
    @Column(name = "from_position_guid", length = 2147483647)
    private String fromPositionGuid;
    
    @JsonProperty("FromDepartmentGUId")
    @Column(name = "from_department_guid", length = 2147483647)
    private String fromDepartmentGuid;
    
    @JsonProperty("ToPositionGUId")
    @Column(name = "to_position_guid", length = 2147483647)
    private String toPositionGuid;
    
    @Column(name = "import_status", length = 2147483647)
    private String importStatus = "PENDING";
    
    // Constructors
    public Correspondence() {}
    
    // Getters and Setters
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }
    
    public Integer getSystemNo() {
        return systemNo;
    }
    
    public void setSystemNo(Integer systemNo) {
        this.systemNo = systemNo;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public LocalDateTime getDbCreationDate() {
        return dbCreationDate;
    }
    
    public void setDbCreationDate(LocalDateTime dbCreationDate) {
        this.dbCreationDate = dbCreationDate;
    }
    
    public LocalDateTime getCorrespondenceCreationDate() {
        return correspondenceCreationDate;
    }
    
    public void setCorrespondenceCreationDate(LocalDateTime correspondenceCreationDate) {
        this.correspondenceCreationDate = correspondenceCreationDate;
    }
    
    public LocalDateTime getCorrespondenceLastModifiedDate() {
        return correspondenceLastModifiedDate;
    }
    
    public void setCorrespondenceLastModifiedDate(LocalDateTime correspondenceLastModifiedDate) {
        this.correspondenceLastModifiedDate = correspondenceLastModifiedDate;
    }
    
    public LocalDateTime getIncomingDate() {
        return incomingDate;
    }
    
    public void setIncomingDate(LocalDateTime incomingDate) {
        this.incomingDate = incomingDate;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public Integer getCorrespondenceTypeId() {
        return correspondenceTypeId;
    }
    
    public void setCorrespondenceTypeId(Integer correspondenceTypeId) {
        this.correspondenceTypeId = correspondenceTypeId;
    }
    
    public String getCorrespondenceFormTypeGuid() {
        return correspondenceFormTypeGuid;
    }
    
    public void setCorrespondenceFormTypeGuid(String correspondenceFormTypeGuid) {
        this.correspondenceFormTypeGuid = correspondenceFormTypeGuid;
    }
    
    public String getClassificationGuid() {
        return classificationGuid;
    }
    
    public void setClassificationGuid(String classificationGuid) {
        this.classificationGuid = classificationGuid;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getReferenceNo() {
        return referenceNo;
    }
    
    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }
    
    public String getExternalReferenceNumber() {
        return externalReferenceNumber;
    }
    
    public void setExternalReferenceNumber(String externalReferenceNumber) {
        this.externalReferenceNumber = externalReferenceNumber;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getManualAttachmentsCount() {
        return manualAttachmentsCount;
    }
    
    public void setManualAttachmentsCount(String manualAttachmentsCount) {
        this.manualAttachmentsCount = manualAttachmentsCount;
    }
    
    public Integer getImportanceId() {
        return importanceId;
    }
    
    public void setImportanceId(Integer importanceId) {
        this.importanceId = importanceId;
    }
    
    public Integer getPriorityId() {
        return priorityId;
    }
    
    public void setPriorityId(Integer priorityId) {
        this.priorityId = priorityId;
    }
    
    public Integer getSecrecyId() {
        return secrecyId;
    }
    
    public void setSecrecyId(Integer secrecyId) {
        this.secrecyId = secrecyId;
    }
    
    public String getNeedReplyStatus() {
        return needReplyStatus;
    }
    
    public void setNeedReplyStatus(String needReplyStatus) {
        this.needReplyStatus = needReplyStatus;
    }
    
    public String getComingFromGuid() {
        return comingFromGuid;
    }
    
    public void setComingFromGuid(String comingFromGuid) {
        this.comingFromGuid = comingFromGuid;
    }
    
    public String getComingFromType() {
        return comingFromType;
    }
    
    public void setComingFromType(String comingFromType) {
        this.comingFromType = comingFromType;
    }
    
    public String getLastDecisionGuid() {
        return lastDecisionGuid;
    }
    
    public void setLastDecisionGuid(String lastDecisionGuid) {
        this.lastDecisionGuid = lastDecisionGuid;
    }
    
    public Boolean getIsDraft() {
        return isDraft;
    }
    
    public void setIsDraft(Boolean isDraft) {
        this.isDraft = isDraft;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public Boolean getIsBlocked() {
        return isBlocked;
    }
    
    public void setIsBlocked(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
    
    public Integer getIsCanceled() {
        return isCanceled;
    }
    
    public void setIsCanceled(Integer isCanceled) {
        this.isCanceled = isCanceled;
    }
    
    public Boolean getIsArchive() {
        return isArchive;
    }
    
    public void setIsArchive(Boolean isArchive) {
        this.isArchive = isArchive;
    }
    
    public Boolean getIsFinal() {
        return isFinal;
    }
    
    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }
    
    public Boolean getIsMigrated() {
        return isMigrated;
    }
    
    public void setIsMigrated(Boolean isMigrated) {
        this.isMigrated = isMigrated;
    }
    
    public String getArchivedDocumentId() {
        return archivedDocumentId;
    }
    
    public void setArchivedDocumentId(String archivedDocumentId) {
        this.archivedDocumentId = archivedDocumentId;
    }
    
    public String getCreationUserName() {
        return creationUserName;
    }
    
    public void setCreationUserName(String creationUserName) {
        this.creationUserName = creationUserName;
    }
    
    public String getCreationUserGuid() {
        return creationUserGuid;
    }
    
    public void setCreationUserGuid(String creationUserGuid) {
        this.creationUserGuid = creationUserGuid;
    }
    
    public String getCreationPositionGuid() {
        return creationPositionGuid;
    }
    
    public void setCreationPositionGuid(String creationPositionGuid) {
        this.creationPositionGuid = creationPositionGuid;
    }
    
    public String getCreationDepartmentGuid() {
        return creationDepartmentGuid;
    }
    
    public void setCreationDepartmentGuid(String creationDepartmentGuid) {
        this.creationDepartmentGuid = creationDepartmentGuid;
    }
    
    public String getFromPositionGuid() {
        return fromPositionGuid;
    }
    
    public void setFromPositionGuid(String fromPositionGuid) {
        this.fromPositionGuid = fromPositionGuid;
    }
    
    public String getFromDepartmentGuid() {
        return fromDepartmentGuid;
    }
    
    public void setFromDepartmentGuid(String fromDepartmentGuid) {
        this.fromDepartmentGuid = fromDepartmentGuid;
    }
    
    public String getToPositionGuid() {
        return toPositionGuid;
    }
    
    public void setToPositionGuid(String toPositionGuid) {
        this.toPositionGuid = toPositionGuid;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}