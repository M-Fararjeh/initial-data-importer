package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "correspondence_copy_tos")
public class CorrespondenceCopyTo extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 255)
    private String guid;
    
    @JsonProperty("DocGUId")
    @Column(name = "doc_guid", length = 255)
    private String docGuid;
    
    @JsonProperty("CopyToGUId")
    @Column(name = "copy_to_guid", length = 255)
    private String copyToGuid;
    
    @JsonProperty("CopyToType")
    @Column(name = "copy_to_type", length = 255)
    private String copyToType;
    
    @JsonProperty("Notes")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "import_status", length = 255)
    private String importStatus = "PENDING";
    
    // Constructors
    public CorrespondenceCopyTo() {}
    
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
    
    public String getCopyToGuid() {
        return copyToGuid;
    }
    
    public void setCopyToGuid(String copyToGuid) {
        this.copyToGuid = copyToGuid;
    }
    
    public String getCopyToType() {
        return copyToType;
    }
    
    public void setCopyToType(String copyToType) {
        this.copyToType = copyToType;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}