package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "correspondence_current_users", indexes = {
    @Index(name = "idx_corr_curr_user_doc_guid", columnList = "doc_guid"),
    @Index(name = "idx_corr_curr_user_import_status", columnList = "import_status")
})
public class CorrespondenceCurrentUser extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 255)
    private String guid;
    
    @JsonProperty("DocGUId")
    @Column(name = "doc_guid", length = 255)
    private String docGuid;
    
    @JsonProperty("UserName")
    @Column(name = "user_name", length = 500)
    private String userName;
    
    @Column(name = "import_status", length = 255)
    private String importStatus = "PENDING";
    
    // Constructors
    public CorrespondenceCurrentUser() {}
    
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
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}