package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.CorrespondenceRelatedImportService;
import com.importservice.repository.CorrespondenceImportStatusRepository;
import com.importservice.entity.CorrespondenceImportStatus;
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

    @PostMapping("/all-correspondences-with-related")
    @Operation(summary = "Import All Correspondences with Related Data", 
               description = "Import all correspondences with related data using status tracking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importAllCorrespondencesWithRelated() {
        logger.info("Received request to import all correspondences with related data (with status tracking)");
        
        try {
            ImportResponseDto response = correspondenceRelatedImportService.importAllCorrespondencesWithRelated();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error during correspondence related import", e);
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
            ImportResponseDto result = correspondenceRelatedImportService.importRelatedDataForCorrespondence(correspondenceGuid);
            boolean success = "SUCCESS".equals(result.getStatus()) || "PARTIAL_SUCCESS".equals(result.getStatus());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("correspondenceGuid", correspondenceGuid);
            response.put("message", result.getMessage());
            response.put("details", result);
            
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
               description = "Returns comprehensive statistics about correspondence import status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCorrespondenceImportStatistics() {
        logger.info("Received request for correspondence import statistics");
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // Get statistics from import status table
            Object[] importStats = importStatusRepository.getImportStatistics();
            
            if (importStats != null && importStats.length >= 5) {
                // Extract values safely
                Long pending = importStats[0] != null ? ((Number) importStats[0]).longValue() : 0L;
                Long inProgress = importStats[1] != null ? ((Number) importStats[1]).longValue() : 0L;
                Long completed = importStats[2] != null ? ((Number) importStats[2]).longValue() : 0L;
                Long failed = importStats[3] != null ? ((Number) importStats[3]).longValue() : 0L;
                Long total = importStats[4] != null ? ((Number) importStats[4]).longValue() : 0L;
                
                statistics.put("pending", pending);
                statistics.put("inProgress", inProgress);
                statistics.put("completed", completed);
                statistics.put("failed", failed);
                statistics.put("total", total);
                
                logger.info("Import statistics: total={}, completed={}, inProgress={}, failed={}, pending={}", 
                           total, completed, inProgress, failed, pending);
            } else {
                // Fallback to zero values if query fails
                logger.warn("Import statistics query returned null or insufficient data, using defaults");
                statistics.put("pending", 0L);
                statistics.put("inProgress", 0L);
                statistics.put("completed", 0L);
                statistics.put("failed", 0L);
                statistics.put("total", 0L);
            }
            
            // Get entity-specific statistics
            try {
                Object[] entityStats = importStatusRepository.getEntityStatistics();
                if (entityStats != null && entityStats.length >= 10) {
                    Map<String, Object> entityStatistics = new HashMap<>();
                    entityStatistics.put("attachmentsSuccess", entityStats[0] != null ? ((Number) entityStats[0]).longValue() : 0L);
                    entityStatistics.put("commentsSuccess", entityStats[1] != null ? ((Number) entityStats[1]).longValue() : 0L);
                    entityStatistics.put("copyTosSuccess", entityStats[2] != null ? ((Number) entityStats[2]).longValue() : 0L);
                    entityStatistics.put("currentDepartmentsSuccess", entityStats[3] != null ? ((Number) entityStats[3]).longValue() : 0L);
                    entityStatistics.put("currentPositionsSuccess", entityStats[4] != null ? ((Number) entityStats[4]).longValue() : 0L);
                    entityStatistics.put("currentUsersSuccess", entityStats[5] != null ? ((Number) entityStats[5]).longValue() : 0L);
                    entityStatistics.put("customFieldsSuccess", entityStats[6] != null ? ((Number) entityStats[6]).longValue() : 0L);
                    entityStatistics.put("linksSuccess", entityStats[7] != null ? ((Number) entityStats[7]).longValue() : 0L);
                    entityStatistics.put("sendTosSuccess", entityStats[8] != null ? ((Number) entityStats[8]).longValue() : 0L);
                    entityStatistics.put("transactionsSuccess", entityStats[9] != null ? ((Number) entityStats[9]).longValue() : 0L);
                    statistics.put("entityStatistics", entityStatistics);
                }
            } catch (Exception e) {
                logger.warn("Error getting entity statistics: {}", e.getMessage());
            }
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting correspondence import statistics", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Failed to get statistics: " + e.getMessage());
            errorMap.put("pending", 0L);
            errorMap.put("inProgress", 0L);
            errorMap.put("completed", 0L);
            errorMap.put("failed", 0L);
            errorMap.put("total", 0L);
            return ResponseEntity.status(500).body(errorMap);
        }
    }
    
    @GetMapping("/status")
    @Operation(summary = "Get All Correspondence Import Statuses", 
               description = "Returns detailed import status for all correspondences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statuses retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<List<Map<String, Object>>> getAllCorrespondenceImportStatuses() {
        logger.info("Received request for all correspondence import statuses");
        
        try {
            List<CorrespondenceImportStatus> importStatuses = importStatusRepository.findAll();
            List<Map<String, Object>> statusList = new ArrayList<>();
            
            for (CorrespondenceImportStatus status : importStatuses) {
                Map<String, Object> statusInfo = new HashMap<>();
                statusInfo.put("id", status.getId());
                statusInfo.put("correspondenceGuid", status.getCorrespondenceGuid());
                statusInfo.put("overallStatus", status.getOverallStatus());
                
                // Individual entity statuses
                statusInfo.put("attachmentsStatus", status.getAttachmentsStatus());
                statusInfo.put("commentsStatus", status.getCommentsStatus());
                statusInfo.put("copyTosStatus", status.getCopyTosStatus());
                statusInfo.put("currentDepartmentsStatus", status.getCurrentDepartmentsStatus());
                statusInfo.put("currentPositionsStatus", status.getCurrentPositionsStatus());
                statusInfo.put("currentUsersStatus", status.getCurrentUsersStatus());
                statusInfo.put("customFieldsStatus", status.getCustomFieldsStatus());
                statusInfo.put("linksStatus", status.getLinksStatus());
                statusInfo.put("sendTosStatus", status.getSendTosStatus());
                statusInfo.put("transactionsStatus", status.getTransactionsStatus());
                
                // Entity counts
                statusInfo.put("attachmentsCount", status.getAttachmentsCount());
                statusInfo.put("commentsCount", status.getCommentsCount());
                statusInfo.put("copyTosCount", status.getCopyTosCount());
                statusInfo.put("currentDepartmentsCount", status.getCurrentDepartmentsCount());
                statusInfo.put("currentPositionsCount", status.getCurrentPositionsCount());
                statusInfo.put("currentUsersCount", status.getCurrentUsersCount());
                statusInfo.put("customFieldsCount", status.getCustomFieldsCount());
                statusInfo.put("linksCount", status.getLinksCount());
                statusInfo.put("sendTosCount", status.getSendTosCount());
                statusInfo.put("transactionsCount", status.getTransactionsCount());
                
                // Summary counts
                statusInfo.put("totalEntitiesCount", status.getTotalEntitiesCount());
                statusInfo.put("successfulEntitiesCount", status.getSuccessfulEntitiesCount());
                statusInfo.put("failedEntitiesCount", status.getFailedEntitiesCount());
                
                // Retry and timing info
                statusInfo.put("retryCount", status.getRetryCount());
                statusInfo.put("startedAt", status.getStartedAt());
                statusInfo.put("completedAt", status.getCompletedAt());
                statusInfo.put("lastModifiedDate", status.getLastModifiedDate());
                
                // Get correspondence details if needed (you might want to join this in a query for better performance)
                // For now, we'll leave these as null and let the frontend handle missing data
                statusInfo.put("correspondenceSubject", null);
                statusInfo.put("correspondenceReferenceNo", null);
                
                statusList.add(statusInfo);
            }
            
            logger.info("Retrieved {} correspondence import statuses", statusList.size());
            return ResponseEntity.ok(statusList);
        } catch (Exception e) {
            logger.error("Error getting all correspondence import statuses", e);
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
    public ResponseEntity<ImportResponseDto> retryFailedCorrespondenceImports() {
        logger.info("Received request to retry failed correspondence imports");
        
        try {
            ImportResponseDto response = correspondenceRelatedImportService.retryFailedImports();
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
    public ResponseEntity<Map<String, Object>> resetCorrespondenceImportStatus(
            @Parameter(description = "Correspondence GUID") @PathVariable String correspondenceGuid) {
        logger.info("Received request to reset import status for correspondence: {}", correspondenceGuid);
        
        try {
            boolean success = correspondenceRelatedImportService.resetImportStatus(correspondenceGuid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("correspondenceGuid", correspondenceGuid);
            response.put("message", success ? "Import status reset successfully" : "Failed to reset import status");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Unexpected error during reset import status for: {}", correspondenceGuid, e);
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
        errorResponse.setErrors(java.util.Arrays.asList(errorMessage));
        return errorResponse;
    }
}