package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 36)
    private String guid;
    
    @JsonProperty("LoginName")
    @Column(name = "login_name", length = 100)
    private String loginName;
    
    @JsonProperty("CellPhonNumber")
    @Column(name = "cell_phone_number", length = 50)
    private String cellPhoneNumber;
    
    @JsonProperty("Description")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @JsonProperty("EMail")
    @Column(name = "email", length = 100)
    private String email;
    
    @JsonProperty("EnglishFirstName")
    @Column(name = "english_first_name", length = 200)
    private String englishFirstName;
    
    @JsonProperty("EnglishFamilyName")
    @Column(name = "english_family_name", length = 200)
    private String englishFamilyName;
    
    @JsonProperty("LocalFirstName")
    @Column(name = "local_first_name", length = 200)
    private String localFirstName;
    
    @JsonProperty("LocalFamilyName")
    @Column(name = "local_family_name", length = 200)
    private String localFamilyName;
    
    @JsonProperty("EnglishTitle")
    @Column(name = "english_title", length = 200)
    private String englishTitle;
    
    @JsonProperty("EnglishLocalTitle")
    @Column(name = "english_local_title", length = 200)
    private String englishLocalTitle;
    
    @JsonProperty("DisplayName")
    @Column(name = "display_name", length = 200)
    private String displayName;
    
    @JsonProperty("UserCode")
    @Column(name = "user_code", length = 50)
    private String userCode;
    
    @JsonProperty("NickName")
    @Column(name = "nick_name", length = 100)
    private String nickName;
    
    @JsonProperty("Notes")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @JsonProperty("IsBlocked")
    @Column(name = "is_blocked")
    private Boolean isBlocked;
    
    @JsonProperty("UserStatus")
    @Column(name = "user_status")
    private Integer userStatus;
    
    @JsonProperty("ExpiryDate")
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    // Constructors
    public User() {}
    
    // Getters and Setters
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }
    
    public String getLoginName() {
        return loginName;
    }
    
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
    
    public String getCellPhoneNumber() {
        return cellPhoneNumber;
    }
    
    public void setCellPhoneNumber(String cellPhoneNumber) {
        this.cellPhoneNumber = cellPhoneNumber;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getEnglishFirstName() {
        return englishFirstName;
    }
    
    public void setEnglishFirstName(String englishFirstName) {
        this.englishFirstName = englishFirstName;
    }
    
    public String getEnglishFamilyName() {
        return englishFamilyName;
    }
    
    public void setEnglishFamilyName(String englishFamilyName) {
        this.englishFamilyName = englishFamilyName;
    }
    
    public String getLocalFirstName() {
        return localFirstName;
    }
    
    public void setLocalFirstName(String localFirstName) {
        this.localFirstName = localFirstName;
    }
    
    public String getLocalFamilyName() {
        return localFamilyName;
    }
    
    public void setLocalFamilyName(String localFamilyName) {
        this.localFamilyName = localFamilyName;
    }
    
    public String getEnglishTitle() {
        return englishTitle;
    }
    
    public void setEnglishTitle(String englishTitle) {
        this.englishTitle = englishTitle;
    }
    
    public String getEnglishLocalTitle() {
        return englishLocalTitle;
    }
    
    public void setEnglishLocalTitle(String englishLocalTitle) {
        this.englishLocalTitle = englishLocalTitle;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getUserCode() {
        return userCode;
    }
    
    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
    
    public String getNickName() {
        return nickName;
    }
    
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Boolean getIsBlocked() {
        return isBlocked;
    }
    
    public void setIsBlocked(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
    
    public Integer getUserStatus() {
        return userStatus;
    }
    
    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}