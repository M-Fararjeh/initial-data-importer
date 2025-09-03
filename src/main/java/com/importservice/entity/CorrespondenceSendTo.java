package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "correspondence_send_tos", indexes = {
    @Index(name = "idx_corr_send_to_doc_guid", columnList = "doc_guid"),
    @Index(name = "idx_corr_send_to_import_status", columnList = "import_status")
})
public class CorrespondenceSendTo extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 255)
    private String guid;
    
    @JsonProperty("DocGUId")
    @Column(name = "doc_guid", length = 255)
    private String docGuid;
    
    @JsonProperty("SendToGUId")
    @Column(name = "send_to_guid", length = 255)
    private String sendToGuid;
    
    @JsonProperty("SendToType")
    @Column(name = "send_to_type", length = 255)
    private String sendToType;
    
    @JsonProperty("IsBCC")
    @Column(name = "is_bcc")
    private Boolean isBcc;
    
    @JsonProperty("DecisionGUId")
    @Column(name = "decision_guid", length = 255)
    private String decisionGuid;
    
    @Column(name = "import_status", length = 255)
    private String importStatus = "PENDING";
    
    // Constructors
    public CorrespondenceSendTo() {}
    
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
    
    public String getSendToGuid() {
        return sendToGuid;
    }
    
    public void setSendToGuid(String sendToGuid) {
        this.sendToGuid = sendToGuid;
    }
    
    public String getSendToType() {
        return sendToType;
    }
    
    public void setSendToType(String sendToType) {
        this.sendToType = sendToType;
    }
    
    public Boolean getIsBcc() {
        return isBcc;
    }
    
    public void setIsBcc(Boolean isBcc) {
        this.isBcc = isBcc;
    }
    
    public String getDecisionGuid() {
        return decisionGuid;
    }
    
    public void setDecisionGuid(String decisionGuid) {
        this.decisionGuid = decisionGuid;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}