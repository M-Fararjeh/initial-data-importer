package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

@Entity
@Table(name = "user_positions", indexes = {
    @Index(name = "idx_user_pos_user_guid", columnList = "user_guid"),
    @Index(name = "idx_user_pos_pos_guid", columnList = "pos_guid"),
    @Index(name = "idx_user_pos_is_deleted", columnList = "is_deleted"),
    @Index(name = "idx_user_pos_lookup", columnList = "user_guid, pos_guid")
})
public class UserPosition extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 255)
    private String guid;
    
    @JsonProperty("PosGUId")
    @Column(name = "pos_guid", length = 255)
    private String posGuid;
    
    @JsonProperty("UserGUId")
    @Column(name = "user_guid", length = 255)
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