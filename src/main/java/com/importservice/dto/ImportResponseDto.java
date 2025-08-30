package com.importservice.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

@ApiModel(description = "Response object for import operations")
public class ImportResponseDto {
    
    @ApiModelProperty(value = "Import operation status", example = "SUCCESS")
    private String status;
    
    @ApiModelProperty(value = "Descriptive message about the import result", example = "Import completed successfully")
    private String message;
    
    @ApiModelProperty(value = "Total number of records processed", example = "4")
    private Integer totalRecords;
    
    @ApiModelProperty(value = "Number of successfully imported records", example = "4")
    private Integer successfulImports;
    
    @ApiModelProperty(value = "Number of failed import attempts", example = "0")
    private Integer failedImports;
    
    @ApiModelProperty(value = "List of error messages for failed imports")
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