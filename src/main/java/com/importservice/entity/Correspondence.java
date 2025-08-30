package com.importservice.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "correspondences")
public class Correspondence extends BaseEntity {
    
    @Id
    @Column(name = "guid", length = 36)
    private String guid;
    
    @Column(name = "system_no")
    private Integer systemNo;
    
    @Column(name = "serial_number", length = 50)
    private String serialNumber;
    
    @Column(name = "db_creation_date")
    private LocalDateTime dbCreationDate;
    
    @Column(name = "correspondence_creation_date")
    private LocalDateTime correspondenceCreationDate;
    
    @Column(name = "correspondence_last_modified_date")
    private LocalDateTime correspondenceLastModifiedDate;
    
    @Column(name = "incoming_date")
    private LocalDateTime incomingDate;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "correspondence_type_id")
    private Integer correspondenceTypeId;
    
    @Column(name = "correspondence_form_type_guid", length = 36)
    private String correspondenceFormTypeGuid;
    
    @Column(name = "classification_guid", length = 36)
    private String classificationGuid;
    
    @Column(name = "subject", columnDefinition = "TEXT")
    private String subject;
    
    @Column(name = "reference_no", length = 100)
    private String referenceNo;
    
    @Column(name = "external_reference_number", length = 100)
    private String externalReferenceNumber;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "manual_attachments_count", length = 50)
    private String manualAttachmentsCount;
    
    @Column(name = "importance_id")
    private Integer importanceId;
    
    @Column(name = "priority_id")
    private Integer priorityId;
    
    @Column(name = "secrecy_id")
    private Integer secrecyId;
    
    @Column(name = "need_reply_status", length = 50)
    private String needReplyStatus;
    
    @Column(name = "coming_from_guid", length = 36)
    private String comingFromGuid;
    
    @Column(name = "coming_from_type", length = 50)
    private String comingFromType;
    
    @Column(name = "last_decision_guid", length = 36)
    private String lastDecisionGuid;
    
    @Column(name = "is_draft")
    private Boolean isDraft;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted;
    
    @Column(name = "is_blocked")
    private Boolean isBlocked;
    
    @Column(name = "is_canceled")
    private Integer isCanceled;
    
    @Column(name = "is_archive")
    private Boolean isArchive;
    
    @Column(name = "is_final")
    private Boolean isFinal;
    
    @Column(name = "is_migrated")
    private Boolean isMigrated;
    
    @Column(name = "archived_document_id", length = 100)
    private String archivedDocumentId;
    
    @Column(name = "creation_user_name", length = 100)
    private String creationUserName;
    
    @Column(name = "creation_user_guid", length = 36)
    private String creationUserGuid;
    
    @Column(name = "creation_position_guid", length = 36)
    private String creationPositionGuid;
    
    @Column(name = "creation_department_guid", length = 36)
    private String creationDepartmentGuid;
    
    @Column(name = "from_position_guid", length = 36)
    private String fromPositionGuid;
    
    @Column(name = "from_department_guid", length = 36)
    private String fromDepartmentGuid;
    
    @Column(name = "to_position_guid", length = 36)
    private String toPositionGuid;
    
    @Column(name = "import_status", length = 20)
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