package com.importservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserImportDto {
    
    @JsonProperty("#")
    private Integer number;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("EmployeeNumber")
    private Integer employeeNumber;
    
    @JsonProperty("Full Arabic name")
    private String fullArabicName;
    
    @JsonProperty("Full english name")
    private String fullEnglishName;
    
    @JsonProperty("MobileNumber")
    private String mobileNumber;
    
    @JsonProperty("ERP Arabic name")
    private String erpArabicName;
    
    @JsonProperty("Department Arabic name")
    private String departmentArabicName;
    
    @JsonProperty("Department Name ERP")
    private String departmentNameErp;
    
    @JsonProperty("Department code")
    private String departmentCode;
    
    @JsonProperty("Arabic Position name")
    private String arabicPositionName;
    
    @JsonProperty("English position name")
    private String englishPositionName;
    
    @JsonProperty("isManager")
    private String isManager;
    
    @JsonProperty("Secret")
    private String secret;
    
    @JsonProperty("Top Secret")
    private String topSecret;
    
    // Constructors
    public UserImportDto() {}
    
    // Getters and Setters
    public Integer getNumber() {
        return number;
    }
    
    public void setNumber(Integer number) {
        this.number = number;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Integer getEmployeeNumber() {
        return employeeNumber;
    }
    
    public void setEmployeeNumber(Integer employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
    
    public String getFullArabicName() {
        return fullArabicName;
    }
    
    public void setFullArabicName(String fullArabicName) {
        this.fullArabicName = fullArabicName;
    }
    
    public String getFullEnglishName() {
        return fullEnglishName;
    }
    
    public void setFullEnglishName(String fullEnglishName) {
        this.fullEnglishName = fullEnglishName;
    }
    
    public String getMobileNumber() {
        return mobileNumber;
    }
    
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
    
    public String getErpArabicName() {
        return erpArabicName;
    }
    
    public void setErpArabicName(String erpArabicName) {
        this.erpArabicName = erpArabicName;
    }
    
    public String getDepartmentArabicName() {
        return departmentArabicName;
    }
    
    public void setDepartmentArabicName(String departmentArabicName) {
        this.departmentArabicName = departmentArabicName;
    }
    
    public String getDepartmentNameErp() {
        return departmentNameErp;
    }
    
    public void setDepartmentNameErp(String departmentNameErp) {
        this.departmentNameErp = departmentNameErp;
    }
    
    public String getDepartmentCode() {
        return departmentCode;
    }
    
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
    
    public String getArabicPositionName() {
        return arabicPositionName;
    }
    
    public void setArabicPositionName(String arabicPositionName) {
        this.arabicPositionName = arabicPositionName;
    }
    
    public String getEnglishPositionName() {
        return englishPositionName;
    }
    
    public void setEnglishPositionName(String englishPositionName) {
        this.englishPositionName = englishPositionName;
    }
    
    public String getIsManager() {
        return isManager;
    }
    
    public void setIsManager(String isManager) {
        this.isManager = isManager;
    }
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public String getTopSecret() {
        return topSecret;
    }
    
    public void setTopSecret(String topSecret) {
        this.topSecret = topSecret;
    }
    
    // Helper methods
    public boolean isManagerUser() {
        return "Yes".equalsIgnoreCase(isManager);
    }
    
    public boolean hasSecret() {
        return "Yes".equalsIgnoreCase(secret);
    }
    
    public boolean hasTopSecret() {
        return "Yes".equalsIgnoreCase(topSecret);
    }
    
    public boolean isCeoDepartment() {
        return "CEO".equalsIgnoreCase(departmentCode);
    }
    
    public String getUsernameFromEmail() {
        if (email == null || !email.contains("@")) {
            return email;
        }
        return email.substring(0, email.indexOf("@"));
    }
    
    @Override
    public String toString() {
        return "UserImportDto{" +
                "number=" + number +
                ", email='" + email + '\'' +
                ", employeeNumber=" + employeeNumber +
                ", fullArabicName='" + fullArabicName + '\'' +
                ", fullEnglishName='" + fullEnglishName + '\'' +
                ", departmentCode='" + departmentCode + '\'' +
                ", isManager='" + isManager + '\'' +
                ", secret='" + secret + '\'' +
                ", topSecret='" + topSecret + '\'' +
                '}';
    }
}