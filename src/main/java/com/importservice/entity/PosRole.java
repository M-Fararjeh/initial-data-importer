package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "pos_roles", indexes = {
    @Index(name = "idx_pos_roles_role_guid", columnList = "role_guid"),
    @Index(name = "idx_pos_roles_pos_guid", columnList = "pos_guid")
})
public class PosRole extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 255)
    private String guid;
    
    @JsonProperty("RoleGUId")
    @Column(name = "role_guid", length = 255)
    private String roleGuid;
    
    @JsonProperty("PosGUId")
    @Column(name = "pos_guid", length = 255)
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