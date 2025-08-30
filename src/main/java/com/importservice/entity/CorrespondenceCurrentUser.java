package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "correspondence_current_users")
public class CorrespondenceCurrentUser extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 2147483647)
    private String guid;
    
    @JsonProperty("DocGUId")
    @Column(name = "doc_guid", length = 2147483647)
    private String docGuid;
    
    @JsonProperty("UserName")
    @Column(name = "user_name", length = 2147483647)
    private String userName;
    
    @Column(name = "import_status", length = 20)
    @Column(name = "import_status", length = 2147483647)
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