package com.importservice.dto;

import java.util.List;

public class ImportResponseDto {
    
    private String status;
    private String message;
    private Integer totalRecords;
    private Integer successfulImports;
    private Integer failedImports;
    private List<String> errors;

    public ImportResponseDto() {
    }

    public ImportResponseDto(String status, String message, Integer totalRecords, 
                           Integer successfulImports, Integer failedImports, List<String> errors) {
        this.status = status;
        this.message = message;
        this.totalRecords = totalRecords;
        this.successfulImports = successfulImports;
        this.failedImports = failedImports;
        this.errors = errors;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getSuccessfulImports() {
        return successfulImports;
    }

    public void setSuccessfulImports(Integer successfulImports) {
        this.successfulImports = successfulImports;
    }

    public Integer getFailedImports() {
        return failedImports;
    }

    public void setFailedImports(Integer failedImports) {
        this.failedImports = failedImports;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "ImportResponseDto{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", totalRecords=" + totalRecords +
                ", successfulImports=" + successfulImports +
                ", failedImports=" + failedImports +
                ", errors=" + errors +
                '}';
    }
}