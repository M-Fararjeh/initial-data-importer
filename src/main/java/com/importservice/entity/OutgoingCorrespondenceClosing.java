package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outgoing_correspondence_closings")
public class OutgoingCorrespondenceClosing extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 255)
    private String guid;
    
    @JsonProperty("DocGUId")
    @Column(name = "doc_guid", length = 255)
    private String docGuid;
    
    @JsonProperty("ClosingDate")
    @Column(name = "closing_date")
    private LocalDateTime closingDate;
    
    @JsonProperty("ClosingType")
    @Column(name = "closing_type", length = 255)
    private String closingType;
    
    @JsonProperty("ClosingReason")
    @Column(name = "closing_reason", columnDefinition = "TEXT")
    private String closingReason;
    
    @JsonProperty("ClosingNotes")
    @Column(name = "closing_notes", columnDefinition = "TEXT")
    private String closingNotes;
    
    @JsonProperty("ClosingUserGUId")
    @Column(name = "closing_user_guid", length = 255)
    private String closingUserGuid;
    
    @JsonProperty("ClosingUserName")
    @Column(name = "closing_user_name", length = 500)
    private String closingUserName;
    
    @JsonProperty("ClosingPositionGUId")
    @Column(name = "closing_position_guid", length = 255)
    private String closingPositionGuid;
    
    @JsonProperty("ClosingDepartmentGUId")
    @Column(name = "closing_department_guid", length = 255)
    private String closingDepartmentGuid;
    
    @JsonProperty("ApprovalUserGUId")
    @Column(name = "approval_user_guid", length = 255)
    private String approvalUserGuid;
    
    @JsonProperty("ApprovalDate")
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @JsonProperty("ApprovalNotes")
    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;
    
    @JsonProperty("IsApproved")
    @Column(name = "is_approved")
    private Boolean isApproved;
    
    @JsonProperty("IsRejected")
    @Column(name = "is_rejected")
    private Boolean isRejected;
    
    @JsonProperty("IsPending")
    @Column(name = "is_pending")
    private Boolean isPending;
    
    @JsonProperty("Status")
    @Column(name = "status", length = 255)
    private String status;
    
    @Column(name = "import_status", length = 255)
    private String importStatus = "PENDING";
    
    // Constructors
    public OutgoingCorrespondenceClosing() {}
    
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
    
    public LocalDateTime getClosingDate() {
        return closingDate;
    }
    
    public void setClosingDate(LocalDateTime closingDate) {
        this.closingDate = closingDate;
    }
    
    public String getClosingType() {
        return closingType;
    }
    
    public void setClosingType(String closingType) {
        this.closingType = closingType;
    }
    
    public String getClosingReason() {
        return closingReason;
    }
    
    public void setClosingReason(String closingReason) {
        this.closingReason = closingReason;
    }
    
    public String getClosingNotes() {
        return closingNotes;
    }
    
    public void setClosingNotes(String closingNotes) {
        this.closingNotes = closingNotes;
    }
    
    public String getClosingUserGuid() {
        return closingUserGuid;
    }
    
    public void setClosingUserGuid(String closingUserGuid) {
        this.closingUserGuid = closingUserGuid;
    }
    
    public String getClosingUserName() {
        return closingUserName;
    }
    
    public void setClosingUserName(String closingUserName) {
        this.closingUserName = closingUserName;
    }
    
    public String getClosingPositionGuid() {
        return closingPositionGuid;
    }
    
    public void setClosingPositionGuid(String closingPositionGuid) {
        this.closingPositionGuid = closingPositionGuid;
    }
    
    public String getClosingDepartmentGuid() {
        return closingDepartmentGuid;
    }
    
    public void setClosingDepartmentGuid(String closingDepartmentGuid) {
        this.closingDepartmentGuid = closingDepartmentGuid;
    }
    
    public String getApprovalUserGuid() {
        return approvalUserGuid;
    }
    
    public void setApprovalUserGuid(String approvalUserGuid) {
        this.approvalUserGuid = approvalUserGuid;
    }
    
    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }
    
    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }
    
    public String getApprovalNotes() {
        return approvalNotes;
    }
    
    public void setApprovalNotes(String approvalNotes) {
        this.approvalNotes = approvalNotes;
    }
    
    public Boolean getIsApproved() {
        return isApproved;
    }
    
    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }
    
    public Boolean getIsRejected() {
        return isRejected;
    }
    
    public void setIsRejected(Boolean isRejected) {
        this.isRejected = isRejected;
    }
    
    public Boolean getIsPending() {
        return isPending;
    }
    
    public void setIsPending(Boolean isPending) {
        this.isPending = isPending;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}