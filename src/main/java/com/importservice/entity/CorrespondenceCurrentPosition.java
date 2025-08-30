package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "correspondence_current_positions")
public class CorrespondenceCurrentPosition extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 36)
    private String guid;
    
    @JsonProperty("DocGUId")
    @Column(name = "doc_guid", length = 36)
    private String docGuid;
    
    @JsonProperty("PosGUId")
    @Column(name = "pos_guid", length = 36)
    private String posGuid;
    
    @Column(name = "import_status", length = 20)
    private String importStatus = "PENDING";
    
    // Constructors
    public CorrespondenceCurrentPosition() {}
    
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
    
    public String getPosGuid() {
        return posGuid;
    }
    
    public void setPosGuid(String posGuid) {
        this.posGuid = posGuid;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}