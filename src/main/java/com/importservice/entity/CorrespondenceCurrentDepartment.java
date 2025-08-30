package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "correspondence_current_departments")
public class CorrespondenceCurrentDepartment extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonProperty("DepGUID")
    @Column(name = "dep_guid", length = 255)
    private String depGuid;
    
    @Column(name = "doc_guid", length = 255)
    private String docGuid;
    
    @Column(name = "import_status", length = 255)
    private String importStatus = "PENDING";
    
    // Constructors
    public CorrespondenceCurrentDepartment() {}
    
    public CorrespondenceCurrentDepartment(String docGuid, String depGuid) {
        this.docGuid = docGuid;
        this.depGuid = depGuid;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDepGuid() {
        return depGuid;
    }
    
    public void setDepGuid(String depGuid) {
        this.depGuid = depGuid;
    }
    
    public String getDocGuid() {
        return docGuid;
    }
    
    public void setDocGuid(String docGuid) {
        this.docGuid = docGuid;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}