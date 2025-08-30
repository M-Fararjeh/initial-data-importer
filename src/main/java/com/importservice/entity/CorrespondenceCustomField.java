package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "correspondence_custom_fields")
public class CorrespondenceCustomField extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 2147483647)
    private String guid;
    
    @JsonProperty("DocGUId")
    @Column(name = "doc_guid", length = 2147483647)
    private String docGuid;
    
    @JsonProperty("EvDate")
    @Column(name = "ev_date", length = 2147483647)
    private String evDate;
    
    @JsonProperty("EVToDate")
    @Column(name = "ev_to_date", length = 2147483647)
    private String evToDate;
    
    @JsonProperty("DADate")
    @Column(name = "da_date", length = 2147483647)
    private String daDate;
    
    @JsonProperty("EMName")
    @Column(name = "em_name", length = 2147483647)
    private String emName;
    
    @JsonProperty("CLOrd")
    @Column(name = "cl_ord", length = 2147483647)
    private String clOrd;
    
    @JsonProperty("ESalary")
    @Column(name = "e_salary", length = 2147483647)
    private String eSalary;
    
    @JsonProperty("EMPER")
    @Column(name = "emper", length = 2147483647)
    private String emper;
    
    @JsonProperty("WorkH")
    @Column(name = "work_h", length = 2147483647)
    private String workH;
    
    @JsonProperty("DMaking")
    @Column(name = "d_making", length = 2147483647)
    private String dMaking;
    
    @JsonProperty("ENTNUM")
    @Column(name = "entnum", length = 2147483647)
    private String entnum;
    
    @JsonProperty("EPer")
    @Column(name = "e_per", length = 2147483647)
    private String ePer;
    
    @JsonProperty("RProc")
    @Column(name = "r_proc", length = 2147483647)
    private String rProc;
    
    @JsonProperty("Comdate")
    @Column(name = "comdate", length = 2147483647)
    private String comdate;
    
    @Column(name = "import_status", length = 2147483647)
    private String importStatus = "PENDING";
    
    // Constructors
    public CorrespondenceCustomField() {}
    
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
    
    public String getEvDate() {
        return evDate;
    }
    
    public void setEvDate(String evDate) {
        this.evDate = evDate;
    }
    
    public String getEvToDate() {
        return evToDate;
    }
    
    public void setEvToDate(String evToDate) {
        this.evToDate = evToDate;
    }
    
    public String getDaDate() {
        return daDate;
    }
    
    public void setDaDate(String daDate) {
        this.daDate = daDate;
    }
    
    public String getEmName() {
        return emName;
    }
    
    public void setEmName(String emName) {
        this.emName = emName;
    }
    
    public String getClOrd() {
        return clOrd;
    }
    
    public void setClOrd(String clOrd) {
        this.clOrd = clOrd;
    }
    
    public String geteSalary() {
        return eSalary;
    }
    
    public void seteSalary(String eSalary) {
        this.eSalary = eSalary;
    }
    
    public String getEmper() {
        return emper;
    }
    
    public void setEmper(String emper) {
        this.emper = emper;
    }
    
    public String getWorkH() {
        return workH;
    }
    
    public void setWorkH(String workH) {
        this.workH = workH;
    }
    
    public String getdMaking() {
        return dMaking;
    }
    
    public void setdMaking(String dMaking) {
        this.dMaking = dMaking;
    }
    
    public String getEntnum() {
        return entnum;
    }
    
    public void setEntnum(String entnum) {
        this.entnum = entnum;
    }
    
    public String getePer() {
        return ePer;
    }
    
    public void setePer(String ePer) {
        this.ePer = ePer;
    }
    
    public String getrProc() {
        return rProc;
    }
    
    public void setrProc(String rProc) {
        this.rProc = rProc;
    }
    
    public String getComdate() {
        return comdate;
    }
    
    public void setComdate(String comdate) {
        this.comdate = comdate;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}