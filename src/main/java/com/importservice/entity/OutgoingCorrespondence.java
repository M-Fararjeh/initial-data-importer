package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outgoing_correspondences")
public class OutgoingCorrespondence extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 255)
    private String guid;
    
    @JsonProperty("SystemNo")
    @Column(name = "system_no")
    private Integer systemNo;
    
    @JsonProperty("SerialNumber")
    @Column(name = "serial_number", length = 500)
    private String serialNumber;
    
    @JsonProperty("Subject")
    @Column(name = "subject", columnDefinition = "TEXT")
    private String subject;
    
    @JsonProperty("ReferenceNo")
    @Column(name = "reference_no", length = 500)
    private String referenceNo;
    
    @JsonProperty("CreationDate")
    @Column(name = "correspondence_creation_date")
    private LocalDateTime correspondenceCreationDate;
    
    @JsonProperty("LastModifiedDate")
    @Column(name = "correspondence_last_modified_date")
    private LocalDateTime correspondenceLastModifiedDate;
    
    @JsonProperty("SendDate")
    @Column(name = "send_date")
    private LocalDateTime sendDate;
    
    @JsonProperty("Status")
    @Column(name = "status", length = 255)
    private String status;
    
    @JsonProperty("IsClosed")
    @Column(name = "is_closed")
    private Boolean isClosed;
    
    @JsonProperty("ClosingDate")
    @Column(name = "closing_date")
    private LocalDateTime closingDate;
    
    @JsonProperty("ClosingUserGUId")
    @Column(name = "closing_user_guid", length = 255)
    private String closingUserGuid;
    
    @JsonProperty("ClosingNotes")
    @Column(name = "closing_notes", columnDefinition = "TEXT")
    private String closingNotes;
    
    @JsonProperty("CreationUserGUId")
    @Column(name = "creation_user_guid", length = 255)
    private String creationUserGuid;
    
    @JsonProperty("CreationPositionGUId")
    @Column(name = "creation_position_guid", length = 255)
    private String creationPositionGuid;
    
    @JsonProperty("CreationDepartmentGUId")
    @Column(name = "creation_department_guid", length = 255)
    private String creationDepartmentGuid;
    
    @Column(name = "import_status", length = 255)
    private String importStatus = "PENDING";
    
    // Constructors
    public OutgoingCorrespondence() {}
    
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
    
    public LocalDateTime getSendDate() {
        return sendDate;
    }
    
    public void setSendDate(LocalDateTime sendDate) {
        this.sendDate = sendDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Boolean getIsClosed() {
        return isClosed;
    }
    
    public void setIsClosed(Boolean isClosed) {
        this.isClosed = isClosed;
    }
    
    public LocalDateTime getClosingDate() {
        return closingDate;
    }
    
    public void setClosingDate(LocalDateTime closingDate) {
        this.closingDate = closingDate;
    }
    
    public String getClosingUserGuid() {
        return closingUserGuid;
    }
    
    public void setClosingUserGuid(String closingUserGuid) {
        this.closingUserGuid = closingUserGuid;
    }
    
    public String getClosingNotes() {
        return closingNotes;
    }
    
    public void setClosingNotes(String closingNotes) {
        this.closingNotes = closingNotes;
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
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}