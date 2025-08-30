package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "correspondence_links")
public class CorrespondenceLink extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 2147483647)
    private String guid;
    
    @JsonProperty("DocGUId")
    @Column(name = "doc_guid", length = 2147483647)
    private String docGuid;
    
    @JsonProperty("LinkDocGUId")
    @Column(name = "link_doc_guid", length = 2147483647)
    private String linkDocGuid;
    
    @JsonProperty("LinkTypeEnglishName")
    @Column(name = "link_type_english_name", length = 2147483647)
    private String linkTypeEnglishName;
    
    @JsonProperty("LinkTypeLocalName")
    @Column(name = "link_type_local_name", length = 2147483647)
    private String linkTypeLocalName;
    
    @Column(name = "import_status", length = 20)
    @Column(name = "import_status", length = 2147483647)
    private String importStatus = "PENDING";
    
    // Constructors
    public CorrespondenceLink() {}
    
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
    
    public String getLinkDocGuid() {
        return linkDocGuid;
    }
    
    public void setLinkDocGuid(String linkDocGuid) {
        this.linkDocGuid = linkDocGuid;
    }
    
    public String getLinkTypeEnglishName() {
        return linkTypeEnglishName;
    }
    
    public void setLinkTypeEnglishName(String linkTypeEnglishName) {
        this.linkTypeEnglishName = linkTypeEnglishName;
    }
    
    public String getLinkTypeLocalName() {
        return linkTypeLocalName;
    }
    
    public void setLinkTypeLocalName(String linkTypeLocalName) {
        this.linkTypeLocalName = linkTypeLocalName;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}