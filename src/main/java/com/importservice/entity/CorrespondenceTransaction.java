package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "correspondence_transactions")
public class CorrespondenceTransaction extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 36)
    private String guid;
    
    @JsonProperty("DocGUId")
    @Column(name = "doc_guid", length = 36)
    private String docGuid;
    
    @JsonProperty("ActionId")
    @Column(name = "action_id")
    private Integer actionId;
    
    @JsonProperty("ActionEnglishName")
    @Column(name = "action_english_name", length = 200)
    private String actionEnglishName;
    
    @JsonProperty("ActionLocalName")
    @Column(name = "action_local_name", length = 200)
    private String actionLocalName;
    
    @JsonProperty("ActionDate")
    @Column(name = "action_date")
    private LocalDateTime actionDate;
    
    @JsonProperty("OrderActionDate")
    @Column(name = "order_action_date", length = 50)
    private String orderActionDate;
    
    @JsonProperty("FromUserName")
    @Column(name = "from_user_name", length = 100)
    private String fromUserName;
    
    @JsonProperty("FromPosGUId")
    @Column(name = "from_pos_guid", length = 36)
    private String fromPosGuid;
    
    @JsonProperty("FromDepartmentGUId")
    @Column(name = "from_department_guid", length = 36)
    private String fromDepartmentGuid;
    
    @JsonProperty("FromRoleGUId")
    @Column(name = "from_role_guid", length = 36)
    private String fromRoleGuid;
    
    @JsonProperty("Notes")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @JsonProperty("Reason")
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @JsonProperty("DecisionGUId")
    @Column(name = "decision_guid", length = 36)
    private String decisionGuid;
    
    @JsonProperty("ToUserName")
    @Column(name = "to_user_name", length = 100)
    private String toUserName;
    
    @JsonProperty("ToPosGUid")
    @Column(name = "to_pos_guid", length = 36)
    private String toPosGuid;
    
    @JsonProperty("ToDepartmentGUId")
    @Column(name = "to_department_guid", length = 36)
    private String toDepartmentGuid;
    
    @JsonProperty("ForwardToGUId")
    @Column(name = "forward_to_guid", length = 36)
    private String forwardToGuid;
    
    @JsonProperty("ForwardToType")
    @Column(name = "forward_to_type", length = 50)
    private String forwardToType;
    
    @JsonProperty("IsMultiForward")
    @Column(name = "is_multi_forward")
    private Boolean isMultiForward;
    
    @JsonProperty("IsPrivateNotes")
    @Column(name = "is_private_notes")
    private Boolean isPrivateNotes;
    
    @JsonProperty("IsHidden")
    @Column(name = "is_hidden")
    private Boolean isHidden;
    
    @Column(name = "import_status", length = 20)
    private String importStatus = "PENDING";
    
    // Constructors
    public CorrespondenceTransaction() {}
    
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
    
    public Integer getActionId() {
        return actionId;
    }
    
    public void setActionId(Integer actionId) {
        this.actionId = actionId;
    }
    
    public String getActionEnglishName() {
        return actionEnglishName;
    }
    
    public void setActionEnglishName(String actionEnglishName) {
        this.actionEnglishName = actionEnglishName;
    }
    
    public String getActionLocalName() {
        return actionLocalName;
    }
    
    public void setActionLocalName(String actionLocalName) {
        this.actionLocalName = actionLocalName;
    }
    
    public LocalDateTime getActionDate() {
        return actionDate;
    }
    
    public void setActionDate(LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }
    
    public String getOrderActionDate() {
        return orderActionDate;
    }
    
    public void setOrderActionDate(String orderActionDate) {
        this.orderActionDate = orderActionDate;
    }
    
    public String getFromUserName() {
        return fromUserName;
    }
    
    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }
    
    public String getFromPosGuid() {
        return fromPosGuid;
    }
    
    public void setFromPosGuid(String fromPosGuid) {
        this.fromPosGuid = fromPosGuid;
    }
    
    public String getFromDepartmentGuid() {
        return fromDepartmentGuid;
    }
    
    public void setFromDepartmentGuid(String fromDepartmentGuid) {
        this.fromDepartmentGuid = fromDepartmentGuid;
    }
    
    public String getFromRoleGuid() {
        return fromRoleGuid;
    }
    
    public void setFromRoleGuid(String fromRoleGuid) {
        this.fromRoleGuid = fromRoleGuid;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getDecisionGuid() {
        return decisionGuid;
    }
    
    public void setDecisionGuid(String decisionGuid) {
        this.decisionGuid = decisionGuid;
    }
    
    public String getToUserName() {
        return toUserName;
    }
    
    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }
    
    public String getToPosGuid() {
        return toPosGuid;
    }
    
    public void setToPosGuid(String toPosGuid) {
        this.toPosGuid = toPosGuid;
    }
    
    public String getToDepartmentGuid() {
        return toDepartmentGuid;
    }
    
    public void setToDepartmentGuid(String toDepartmentGuid) {
        this.toDepartmentGuid = toDepartmentGuid;
    }
    
    public String getForwardToGuid() {
        return forwardToGuid;
    }
    
    public void setForwardToGuid(String forwardToGuid) {
        this.forwardToGuid = forwardToGuid;
    }
    
    public String getForwardToType() {
        return forwardToType;
    }
    
    public void setForwardToType(String forwardToType) {
        this.forwardToType = forwardToType;
    }
    
    public Boolean getIsMultiForward() {
        return isMultiForward;
    }
    
    public void setIsMultiForward(Boolean isMultiForward) {
        this.isMultiForward = isMultiForward;
    }
    
    public Boolean getIsPrivateNotes() {
        return isPrivateNotes;
    }
    
    public void setIsPrivateNotes(Boolean isPrivateNotes) {
        this.isPrivateNotes = isPrivateNotes;
    }
    
    public Boolean getIsHidden() {
        return isHidden;
    }
    
    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}