package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "user_positions")
public class UserPosition extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 2147483647)
    private String guid;
    
    @JsonProperty("PosGUId")
    @Column(name = "pos_guid", length = 2147483647)
    private String posGuid;
    
    @JsonProperty("UserGUId")
    @Column(name = "user_guid", length = 2147483647)
    private String userGuid;
    
    @JsonProperty("IsDeleted")
    @Column(name = "is_deleted")
    private Integer isDeleted;
    
    // Constructors
    public UserPosition() {}
    
    // Getters and Setters
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }
    
    public String getPosGuid() {
        return posGuid;
    }
    
    public void setPosGuid(String posGuid) {
        this.posGuid = posGuid;
    }
    
    public String getUserGuid() {
        return userGuid;
    }
    
    public void setUserGuid(String userGuid) {
        this.userGuid = userGuid;
    }
    
    public Integer getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}