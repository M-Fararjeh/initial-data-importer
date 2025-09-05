package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.CorrespondenceImportStatus;
import com.importservice.repository.CorrespondenceImportStatusRepository;
import com.importservice.repository.CorrespondenceRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/correspondence-import")
@Tag(name = "Correspondence Related Import Controller", description = "Operations for importing correspondence-related data with status tracking")
@CrossOrigin(origins = "*")
public class CorrespondenceRelatedImportController {
    
    private static final Logger logger = LoggerFactory.getLogger(CorrespondenceRelatedImportController.class);
    
    @Autowired
    private CorrespondenceRelatedImportService correspondenceRelatedImportService;
    
    @Autowired
    private CorrespondenceImportStatusRepository importStatusRepository;
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @PostMapping("/all-correspondences-with-related")
    @Operation(summary = "Import All Correspondences with Related Data", 
               description = "Import all correspondences with related data using status tracking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importAllCorrespondencesWithRelated() {
        logger.info("Received request to import all correspondences with related data");
        
        try {
            // Get all correspondences
            List<Correspondence> correspondences = correspondenceRepository.findAll();
            logger.info("Found {} correspondences to process", correspondences.size());
            
            int totalRecords = correspondences.size();
            int successfulImports = 0;
            int failedImports = 0;
            List<String> errors = new ArrayList<>();
            
            for (Correspondence correspondence : correspondences) {
                try {
                    boolean currentResult = correspondenceRelatedImportService.importRelatedDataForCorrespondence(correspondence.getGuid());
                    if (currentResult) {
                        successfulImports++;
                    } else {
                        failedImports++;
                        errors.add("Failed to import related data for correspondence: " + correspondence.getGuid());
                    }
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error importing correspondence " + correspondence.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            // Determine final status
            String status;
            if (failedImports == 0) {
                status = "SUCCESS";
            } else if (successfulImports > 0) {
                status = "PARTIAL_SUCCESS";
            } else {
                status = "ERROR";
            }
            
            String message = String.format("Bulk import completed. Success: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            ImportResponseDto response = new ImportResponseDto();
            response.setStatus(status);
            response.setMessage(message);
            response.setTotalRecords(totalRecords);
            response.setSuccessfulImports(successfulImports);
            response.setFailedImports(failedImports);
            response.setErrors(errors);
            
            return getResponseEntity(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error during bulk correspondence import", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/correspondence/{correspondenceGuid}/related")
    @Operation(summary = "Import Related Data for Specific Correspondence", 
               description = "Import all related data for a specific correspondence")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> importCorrespondenceRelated(
            @Parameter(description = "Correspondence GUID") @PathVariable String correspondenceGuid) {
        logger.info("Received request to import related data for correspondence: {}", correspondenceGuid);
        
        try {
            boolean serviceResult = correspondenceRelatedImportService.importRelatedDataForCorrespondence(correspondenceGuid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", serviceResult);
            response.put("correspondenceGuid", correspondenceGuid);
            response.put("message", serviceResult ? "Related data imported successfully" : "Failed to import related data");
            
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
    
    @GetMapping("/statistics")
    @Operation(summary = "Get Correspondence Import Statistics", 
               description = "Returns statistics about correspondence import status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCorrespondenceImportStatistics() {
        logger.info("Received request for correspondence import statistics");
        
        try {
            // Get statistics from import status table
            Object[] statsArray = importStatusRepository.getImportStatistics();
            
            Map<String, Object> statistics = new HashMap<>();
            
            if (statsArray != null && statsArray.length >= 5) {
                // Extract values safely
                Long pending = statsArray[0] != null ? ((Number) statsArray[0]).longValue() : 0L;
                Long inProgress = statsArray[1] != null ? ((Number) statsArray[1]).longValue() : 0L;
                Long completed = statsArray[2] != null ? ((Number) statsArray[2]).longValue() : 0L;
                Long failed = statsArray[3] != null ? ((Number) statsArray[3]).longValue() : 0L;
                Long total = statsArray[4] != null ? ((Number) statsArray[4]).longValue() : 0L;
                
                statistics.put("pending", pending);
                statistics.put("inProgress", inProgress);
                statistics.put("completed", completed);
                statistics.put("failed", failed);
                statistics.put("total", total);
            } else {
                // Fallback to manual count if query fails
                Long totalCount = importStatusRepository.count();
                Long completedCount = importStatusRepository.countByOverallStatus("COMPLETED");
                Long inProgressCount = importStatusRepository.countByOverallStatus("IN_PROGRESS");
                Long failedCount = importStatusRepository.countByOverallStatus("FAILED");
                Long pendingCount = importStatusRepository.countByOverallStatus("PENDING");
                
                statistics.put("total", totalCount);
                statistics.put("completed", completedCount);
                statistics.put("inProgress", inProgressCount);
                statistics.put("failed", failedCount);
                statistics.put("pending", pendingCount);
            }
            
            logger.info("Returning correspondence import statistics: {}", statistics);
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error getting correspondence import statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", "Failed to get statistics: " + e.getMessage());
            errorStats.put("total", 0L);
            errorStats.put("completed", 0L);
            errorStats.put("inProgress", 0L);
            errorStats.put("failed", 0L);
            errorStats.put("pending", 0L);
            return ResponseEntity.status(500).body(errorStats);
        }
    }
    
    @GetMapping("/status")
    @Operation(summary = "Get All Correspondence Import Statuses", 
               description = "Returns import status for all correspondences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statuses retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<List<Map<String, Object>>> getAllCorrespondenceImportStatuses() {
        logger.info("Received request for all correspondence import statuses");
        
        try {
            List<CorrespondenceImportStatus> statuses = importStatusRepository.findAll();
            List<Map<String, Object>> statusList = new ArrayList<>();
            
            for (CorrespondenceImportStatus status : statuses) {
                Map<String, Object> statusMap = new HashMap<>();
                statusMap.put("id", status.getId());
                statusMap.put("correspondenceGuid", status.getCorrespondenceGuid());
                statusMap.put("overallStatus", status.getOverallStatus());
                statusMap.put("attachmentsStatus", status.getAttachmentsStatus());
                statusMap.put("commentsStatus", status.getCommentsStatus());
                statusMap.put("copyTosStatus", status.getCopyTosStatus());
                statusMap.put("currentDepartmentsStatus", status.getCurrentDepartmentsStatus());
                statusMap.put("currentPositionsStatus", status.getCurrentPositionsStatus());
                statusMap.put("currentUsersStatus", status.getCurrentUsersStatus());
                statusMap.put("customFieldsStatus", status.getCustomFieldsStatus());
                statusMap.put("linksStatus", status.getLinksStatus());
                statusMap.put("sendTosStatus", status.getSendTosStatus());
                statusMap.put("transactionsStatus", status.getTransactionsStatus());
                statusMap.put("attachmentsCount", status.getAttachmentsCount());
                statusMap.put("commentsCount", status.getCommentsCount());
                statusMap.put("copyTosCount", status.getCopyTosCount());
                statusMap.put("currentDepartmentsCount", status.getCurrentDepartmentsCount());
                statusMap.put("currentPositionsCount", status.getCurrentPositionsCount());
                statusMap.put("currentUsersCount", status.getCurrentUsersCount());
                statusMap.put("customFieldsCount", status.getCustomFieldsCount());
                statusMap.put("linksCount", status.getLinksCount());
                statusMap.put("sendTosCount", status.getSendTosCount());
                statusMap.put("transactionsCount", status.getTransactionsCount());
                statusMap.put("totalEntitiesCount", status.getTotalEntitiesCount());
                statusMap.put("successfulEntitiesCount", status.getSuccessfulEntitiesCount());
                statusMap.put("failedEntitiesCount", status.getFailedEntitiesCount());
                statusMap.put("retryCount", status.getRetryCount());
                statusMap.put("startedAt", status.getStartedAt());
                statusMap.put("completedAt", status.getCompletedAt());
                statusMap.put("lastModifiedDate", status.getLastModifiedDate());
                
                // Get correspondence details
                try {
                    Correspondence correspondence = correspondenceRepository.findById(status.getCorrespondenceGuid()).orElse(null);
                    if (correspondence != null) {
                        statusMap.put("correspondenceSubject", correspondence.getSubject());
                        statusMap.put("correspondenceReferenceNo", correspondence.getReferenceNo());
                    }
                } catch (Exception e) {
                    logger.warn("Error getting correspondence details for: {}", status.getCorrespondenceGuid());
                }
                
                statusList.add(statusMap);
            }
            
            return ResponseEntity.ok(statusList);
            
        } catch (Exception e) {
            logger.error("Error getting all import statuses", e);
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
    
    @PostMapping("/retry-failed")
    @Operation(summary = "Retry Failed Correspondence Imports", 
               description = "Retries failed correspondence imports that haven't exceeded max retry count")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retry completed successfully"),
        @ApiResponse(responseCode = "400", description = "Retry failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> retryFailedImports() {
        logger.info("Received request to retry failed correspondence imports");
        
        try {
            List<CorrespondenceImportStatus> retryableImports = importStatusRepository.findRetryableImports();
            
            int totalRecords = retryableImports.size();
            int successfulImports = 0;
            int failedImports = 0;
            List<String> errors = new ArrayList<>();
            
            for (CorrespondenceImportStatus importStatus : retryableImports) {
                try {
                    boolean retryResult = correspondenceRelatedImportService.importRelatedDataForCorrespondence(importStatus.getCorrespondenceGuid());
                    if (retryResult) {
                        successfulImports++;
                    } else {
                        failedImports++;
                        errors.add("Failed to retry import for correspondence: " + importStatus.getCorrespondenceGuid());
                    }
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error retrying correspondence " + importStatus.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            // Determine final status
            String status;
            if (failedImports == 0) {
                status = "SUCCESS";
            } else if (successfulImports > 0) {
                status = "PARTIAL_SUCCESS";
            } else {
                status = "ERROR";
            }
            
            String message = String.format("Retry completed. Success: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            ImportResponseDto response = new ImportResponseDto();
            response.setStatus(status);
            response.setMessage(message);
            response.setTotalRecords(totalRecords);
            response.setSuccessfulImports(successfulImports);
            response.setFailedImports(failedImports);
            response.setErrors(errors);
            
            return getResponseEntity(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error during retry failed imports", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/reset/{correspondenceGuid}")
    @Operation(summary = "Reset Import Status for Correspondence", 
               description = "Resets import status for a specific correspondence to allow re-import")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reset completed successfully"),
        @ApiResponse(responseCode = "400", description = "Reset failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> resetImportStatus(
            @Parameter(description = "Correspondence GUID") @PathVariable String correspondenceGuid) {
        logger.info("Received request to reset import status for correspondence: {}", correspondenceGuid);
        
        try {
            boolean resetResult = correspondenceRelatedImportService.resetImportStatus(correspondenceGuid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", resetResult);
            response.put("correspondenceGuid", correspondenceGuid);
            response.put("message", resetResult ? "Import status reset successfully" : "Failed to reset import status");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Unexpected error during reset for: {}", correspondenceGuid, e);
            Map<String, Object> errorResponse = new HashMap<>();
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
        errorResponse.setErrors(Arrays.asList(errorMessage));
        return errorResponse;
    }
}