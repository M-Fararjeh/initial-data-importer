package com.importservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Response object for import operations")
public class ImportResponseDto {
    
    @Schema(description = "Import operation status", example = "SUCCESS")
    private String status;
    
    @Schema(description = "Descriptive message about the import result", example = "Import completed successfully")
    private String message;
    
    @Schema(description = "Total number of records processed", example = "4")
    private Integer totalRecords;
    
    @Schema(description = "Number of successfully imported records", example = "4")
    private Integer successfulImports;
    
    @Schema(description = "Number of failed import attempts", example = "0")
    private Integer failedImports;
    
    @Schema(description = "List of error messages for failed imports")
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