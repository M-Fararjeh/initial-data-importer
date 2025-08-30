package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "pos_roles")
public class PosRole extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 36)
    private String guid;
    
    @JsonProperty("RoleGUId")
    @Column(name = "role_guid", length = 36)
    private String roleGuid;
    
    @JsonProperty("PosGUId")
    @Column(name = "pos_guid", length = 36)
    private String posGuid;
    
    // Constructors
    public PosRole() {}
    
    // Getters and Setters
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }
    
    public String getRoleGuid() {
        return roleGuid;
    }
    
    public void setRoleGuid(String roleGuid) {
        this.roleGuid = roleGuid;
    }
    
    public String getPosGuid() {
        return posGuid;
    }
    
    public void setPosGuid(String posGuid) {
        this.posGuid = posGuid;
    }
}