package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.CorrespondenceImportStatus;
import com.importservice.service.CorrespondenceRelatedImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/correspondence-import")
@Tag(name = "Correspondence Related Import Controller", description = "Operations for importing correspondence-related data with status tracking")
@CrossOrigin(origins = "*")
public class CorrespondenceRelatedImportController {

    private static final Logger logger = LoggerFactory.getLogger(CorrespondenceRelatedImportController.class);

    @Autowired
    private CorrespondenceRelatedImportService correspondenceRelatedImportService;

    @PostMapping("/all-correspondences-with-related")
    @Operation(summary = "Import All Correspondences with Related Data", 
               description = "Imports all correspondence-related data with comprehensive status tracking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importAllCorrespondencesWithRelated() {
        logger.info("Received request to import all correspondences with related data");
        
        try {
            ImportResponseDto response = correspondenceRelatedImportService.importAllCorrespondencesWithRelatedData();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error during correspondence related import", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }

    @PostMapping("/correspondence/{correspondenceGuid}/related")
    @Operation(summary = "Import Related Data for Specific Correspondence", 
               description = "Imports all related data for a specific correspondence with status tracking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> importRelatedDataForCorrespondence(
            @Parameter(description = "Correspondence GUID") @PathVariable String correspondenceGuid) {
        logger.info("Received request to import related data for correspondence: {}", correspondenceGuid);
        
        try {
            boolean success = correspondenceRelatedImportService.importRelatedDataForCorrespondence(correspondenceGuid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("correspondenceGuid", correspondenceGuid);
            response.put("message", success ? "Import completed successfully" : "Import failed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Unexpected error during correspondence related import for: {}", correspondenceGuid, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("correspondenceGuid", correspondenceGuid);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/correspondence/{correspondenceGuid}/reset")
    @Operation(summary = "Reset Import Status for Correspondence", 
               description = "Resets import status for a correspondence to allow re-import")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status reset successfully"),
        @ApiResponse(responseCode = "404", description = "Correspondence not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> resetImportStatusForCorrespondence(
            @Parameter(description = "Correspondence GUID") @PathVariable String correspondenceGuid) {
        logger.info("Received request to reset import status for correspondence: {}", correspondenceGuid);
        
        try {
            boolean success = correspondenceRelatedImportService.resetImportStatus(correspondenceGuid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("correspondenceGuid", correspondenceGuid);
            response.put("message", success ? "Status reset successfully" : "Failed to reset status");
            
            if (success) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error resetting import status for correspondence: {}", correspondenceGuid, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("correspondenceGuid", correspondenceGuid);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/retry-failed")
    @Operation(summary = "Retry Failed Imports", 
               description = "Retries failed correspondence-related imports that haven't exceeded max retry count")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retry completed successfully"),
        @ApiResponse(responseCode = "400", description = "Retry failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> retryFailedImports() {
        logger.info("Received request to retry failed correspondence imports");
        
        try {
            ImportResponseDto response = correspondenceRelatedImportService.retryFailedImports();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error during retry failed imports", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get Import Statistics", 
               description = "Returns statistics about correspondence-related data import progress")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getImportStatistics() {
        logger.info("Received request for correspondence import statistics");
        
        try {
            Map<String, Object> statistics = correspondenceRelatedImportService.getImportStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting correspondence import statistics", e);
            Map<String, Object> errorMap = new java.util.HashMap<>();
            errorMap.put("error", "Failed to get statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(errorMap);
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Get All Import Statuses", 
               description = "Returns detailed import status for all correspondences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statuses retrieved successfully")
    })
    public ResponseEntity<List<CorrespondenceImportStatus>> getAllImportStatuses() {
        logger.info("Received request for all correspondence import statuses");
        
        try {
            List<CorrespondenceImportStatus> statuses = correspondenceRelatedImportService.getAllImportStatuses();
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            logger.error("Error getting all import statuses", e);
            return ResponseEntity.status(500).body(new java.util.ArrayList<>());
        }
    }

    @GetMapping("/status/{correspondenceGuid}")
    @Operation(summary = "Get Import Status for Specific Correspondence", 
               description = "Returns detailed import status for a specific correspondence")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Correspondence not found")
    })
    public ResponseEntity<CorrespondenceImportStatus> getImportStatus(
            @Parameter(description = "Correspondence GUID") @PathVariable String correspondenceGuid) {
        logger.info("Received request for import status of correspondence: {}", correspondenceGuid);
        
        try {
            Optional<CorrespondenceImportStatus> status = correspondenceRelatedImportService.getImportStatus(correspondenceGuid);
            if (status.isPresent()) {
                return ResponseEntity.ok(status.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting import status for correspondence: {}", correspondenceGuid, e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/reset/{correspondenceGuid}")
    @Operation(summary = "Reset Import Status", 
               description = "Resets import status for a correspondence to allow re-import")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status reset successfully"),
        @ApiResponse(responseCode = "404", description = "Correspondence not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> resetImportStatus(
            @Parameter(description = "Correspondence GUID") @PathVariable String correspondenceGuid) {
        logger.info("Received request to reset import status for correspondence: {}", correspondenceGuid);
        
        try {
            boolean success = correspondenceRelatedImportService.resetImportStatus(correspondenceGuid);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", success);
            response.put("correspondenceGuid", correspondenceGuid);
            response.put("message", success ? "Status reset successfully" : "Failed to reset status");
            
            if (success) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error resetting import status for correspondence: {}", correspondenceGuid, e);
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("correspondenceGuid", correspondenceGuid);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    private ResponseEntity<ImportResponseDto> getResponseEntity(ImportResponseDto response) {
        if ("ERROR".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    private ImportResponseDto createErrorResponse(String errorMessage) {
        ImportResponseDto errorResponse = new ImportResponseDto();
        errorResponse.setStatus("ERROR");
        errorResponse.setMessage(errorMessage);
        errorResponse.setTotalRecords(0);
        errorResponse.setSuccessfulImports(0);
        errorResponse.setFailedImports(0);
        errorResponse.setErrors(java.util.Arrays.asList(errorMessage));
        return errorResponse;
    }
}