package com.importservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outgoing_correspondence_business_logs")
public class OutgoingCorrespondenceBusinessLog extends BaseEntity {
    
    @Id
    @JsonProperty("GUId")
    @Column(name = "guid", length = 255)
    private String guid;
    
    @JsonProperty("DocGUId")
    @Column(name = "doc_guid", length = 255)
    private String docGuid;
    
    @JsonProperty("LogDate")
    @Column(name = "log_date")
    private LocalDateTime logDate;
    
    @JsonProperty("LogType")
    @Column(name = "log_type", length = 255)
    private String logType;
    
    @JsonProperty("LogMessage")
    @Column(name = "log_message", columnDefinition = "TEXT")
    private String logMessage;
    
    @JsonProperty("LogDetails")
    @Column(name = "log_details", columnDefinition = "TEXT")
    private String logDetails;
    
    @JsonProperty("UserGUId")
    @Column(name = "user_guid", length = 255)
    private String userGuid;
    
    @JsonProperty("UserName")
    @Column(name = "user_name", length = 500)
    private String userName;
    
    @JsonProperty("PositionGUId")
    @Column(name = "position_guid", length = 255)
    private String positionGuid;
    
    @JsonProperty("DepartmentGUId")
    @Column(name = "department_guid", length = 255)
    private String departmentGuid;
    
    @JsonProperty("ActionType")
    @Column(name = "action_type", length = 255)
    private String actionType;
    
    @JsonProperty("ActionResult")
    @Column(name = "action_result", length = 255)
    private String actionResult;
    
    @JsonProperty("IPAddress")
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @JsonProperty("SessionId")
    @Column(name = "session_id", length = 255)
    private String sessionId;
    
    @Column(name = "import_status", length = 255)
    private String importStatus = "PENDING";
    
    // Constructors
    public OutgoingCorrespondenceBusinessLog() {}
    
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
    
    public LocalDateTime getLogDate() {
        return logDate;
    }
    
    public void setLogDate(LocalDateTime logDate) {
        this.logDate = logDate;
    }
    
    public String getLogType() {
        return logType;
    }
    
    public void setLogType(String logType) {
        this.logType = logType;
    }
    
    public String getLogMessage() {
        return logMessage;
    }
    
    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }
    
    public String getLogDetails() {
        return logDetails;
    }
    
    public void setLogDetails(String logDetails) {
        this.logDetails = logDetails;
    }
    
    public String getUserGuid() {
        return userGuid;
    }
    
    public void setUserGuid(String userGuid) {
        this.userGuid = userGuid;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPositionGuid() {
        return positionGuid;
    }
    
    public void setPositionGuid(String positionGuid) {
        this.positionGuid = positionGuid;
    }
    
    public String getDepartmentGuid() {
        return departmentGuid;
    }
    
    public void setDepartmentGuid(String departmentGuid) {
        this.departmentGuid = departmentGuid;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public String getActionResult() {
        return actionResult;
    }
    
    public void setActionResult(String actionResult) {
        this.actionResult = actionResult;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getImportStatus() {
        return importStatus;
    }
    
    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }
}