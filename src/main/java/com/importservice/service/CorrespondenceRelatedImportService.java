package com.importservice.service;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.CorrespondenceImportStatus;
import com.importservice.repository.CorrespondenceImportStatusRepository;
import com.importservice.repository.CorrespondenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing correspondence-related data import with status tracking
 */
@Service
public class CorrespondenceRelatedImportService {
    
    private static final Logger logger = LoggerFactory.getLogger(CorrespondenceRelatedImportService.class);
    
    @Autowired
    private CorrespondenceImportStatusRepository importStatusRepository;
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @Autowired
    private DataImportService dataImportService;
    
    /**
     * Imports all correspondence-related data with comprehensive status tracking
     */
    @Transactional
    public ImportResponseDto importAllCorrespondencesWithRelatedData() {
        logger.info("Starting comprehensive import of all correspondences with related data");
        
        List<String> errors = new ArrayList<>();
        int totalCorrespondences = 0;
        int successfulCorrespondences = 0;
        int failedCorrespondences = 0;
        
        try {
            // Get all correspondences from database
            List<Correspondence> correspondences = correspondenceRepository.findAll();
            totalCorrespondences = correspondences.size();
            
            logger.info("Found {} correspondences to process for related data import", totalCorrespondences);
            
            if (correspondences.isEmpty()) {
                return createResponse("SUCCESS", "No correspondences found in database", 0, 0, 0, new ArrayList<>());
            }
            
            for (Correspondence correspondence : correspondences) {
                try {
                    boolean success = importRelatedDataForCorrespondence(correspondence.getGuid());
                    if (success) {
                        successfulCorrespondences++;
                    } else {
                        failedCorrespondences++;
                        errors.add("Failed to import related data for correspondence: " + correspondence.getGuid());
                    }
                } catch (Exception e) {
                    failedCorrespondences++;
                    String errorMsg = "Error processing correspondence " + correspondence.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = determineFinalStatus(successfulCorrespondences, failedCorrespondences);
            String message = String.format("Correspondence related data import completed. Success: %d, Failed: %d", 
                                         successfulCorrespondences, failedCorrespondences);
            
            return createResponse(status, message, totalCorrespondences, 
                                successfulCorrespondences, failedCorrespondences, errors);
            
        } catch (Exception e) {
            logger.error("Error in comprehensive correspondence import", e);
            return createResponse("ERROR", "Import failed: " + e.getMessage(), 
                                0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Imports related data for a specific correspondence with status tracking
     */
    @Transactional
    public boolean importRelatedDataForCorrespondence(String correspondenceGuid) {
        logger.info("Importing related data for correspondence: {}", correspondenceGuid);
        
        try {
            // Get or create import status record
            CorrespondenceImportStatus importStatus = getOrCreateImportStatus(correspondenceGuid);
            
            // Skip if already completed
            if (importStatus.isCompleted()) {
                logger.info("Related data already imported for correspondence: {}", correspondenceGuid);
                return true;
            }
            
            importStatus.setOverallStatus("IN_PROGRESS");
            importStatus.setStartedAt(LocalDateTime.now());
            importStatusRepository.save(importStatus);
            
            // Import each entity type with status tracking
            boolean overallSuccess = true;
            
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "attachments", importStatus);
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "comments", importStatus);
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "copytos", importStatus);
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "currentdepartments", importStatus);
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "currentpositions", importStatus);
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "currentusers", importStatus);
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "customfields", importStatus);
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "links", importStatus);
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "sendtos", importStatus);
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "transactions", importStatus);
            
            // Update final status
            if (overallSuccess) {
                importStatus.setOverallStatus("COMPLETED");
                importStatus.setCompletedAt(LocalDateTime.now());
            } else {
                importStatus.setOverallStatus("FAILED");
                importStatus.incrementRetryCount();
            }
            
            importStatusRepository.save(importStatus);
            
            logger.info("Completed related data import for correspondence: {} with status: {}", 
                       correspondenceGuid, importStatus.getOverallStatus());
            
            return overallSuccess;
            
        } catch (Exception e) {
            logger.error("Error importing related data for correspondence: {}", correspondenceGuid, e);
            
            // Update status to failed
            try {
                CorrespondenceImportStatus importStatus = getOrCreateImportStatus(correspondenceGuid);
                importStatus.setOverallStatus("FAILED");
                importStatus.incrementRetryCount();
                importStatusRepository.save(importStatus);
            } catch (Exception statusUpdateError) {
                logger.error("Error updating import status for correspondence: {}", correspondenceGuid, statusUpdateError);
            }
            
            return false;
        }
    }
    
    /**
     * Imports a specific entity type with status tracking
     */
    private boolean importEntityWithTracking(String correspondenceGuid, String entityType, CorrespondenceImportStatus importStatus) {
        try {
            logger.debug("Importing {} for correspondence: {}", entityType, correspondenceGuid);
            
            // Mark as in progress
            importStatus.markEntityInProgress(entityType);
            importStatusRepository.save(importStatus);
            
            // Call appropriate import method
            ImportResponseDto result = callImportMethod(correspondenceGuid, entityType);
            
            if ("SUCCESS".equals(result.getStatus()) || "PARTIAL_SUCCESS".equals(result.getStatus())) {
                importStatus.markEntitySuccess(entityType, result.getSuccessfulImports());
                logger.debug("Successfully imported {} for correspondence: {}", entityType, correspondenceGuid);
                return true;
            } else {
                importStatus.markEntityFailed(entityType, result.getMessage());
                logger.warn("Failed to import {} for correspondence: {} - {}", entityType, correspondenceGuid, result.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            String errorMsg = "Error importing " + entityType + " for correspondence " + correspondenceGuid + ": " + e.getMessage();
            importStatus.markEntityFailed(entityType, errorMsg);
            logger.error(errorMsg, e);
            return false;
        } finally {
            importStatusRepository.save(importStatus);
        }
    }
    
    /**
     * Calls the appropriate import method based on entity type
     */
    private ImportResponseDto callImportMethod(String correspondenceGuid, String entityType) {
        switch (entityType.toLowerCase()) {
            case "attachments":
                return dataImportService.importCorrespondenceAttachments(correspondenceGuid);
            case "comments":
                return dataImportService.importCorrespondenceComments(correspondenceGuid);
            case "copytos":
                return dataImportService.importCorrespondenceCopyTos(correspondenceGuid);
            case "currentdepartments":
                return dataImportService.importCorrespondenceCurrentDepartments(correspondenceGuid);
            case "currentpositions":
                return dataImportService.importCorrespondenceCurrentPositions(correspondenceGuid);
            case "currentusers":
                return dataImportService.importCorrespondenceCurrentUsers(correspondenceGuid);
            case "customfields":
                return dataImportService.importCorrespondenceCustomFields(correspondenceGuid);
            case "links":
                return dataImportService.importCorrespondenceLinks(correspondenceGuid);
            case "sendtos":
                return dataImportService.importCorrespondenceSendTos(correspondenceGuid);
            case "transactions":
                return dataImportService.importCorrespondenceTransactions(correspondenceGuid);
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }
    
    /**
     * Gets or creates import status record for correspondence
     */
    private CorrespondenceImportStatus getOrCreateImportStatus(String correspondenceGuid) {
        Optional<CorrespondenceImportStatus> existingStatus = 
            importStatusRepository.findByCorrespondenceGuid(correspondenceGuid);
        
        if (existingStatus.isPresent()) {
            return existingStatus.get();
        } else {
            CorrespondenceImportStatus newStatus = new CorrespondenceImportStatus(correspondenceGuid);
            return importStatusRepository.save(newStatus);
        }
    }
    
    /**
     * Retries failed imports for correspondences that haven't exceeded max retries
     */
    @Transactional
    public ImportResponseDto retryFailedImports() {
        logger.info("Starting retry of failed correspondence imports");
        
        List<String> errors = new ArrayList<>();
        int successfulRetries = 0;
        int failedRetries = 0;
        
        try {
            List<CorrespondenceImportStatus> retryableImports = importStatusRepository.findRetryableImports();
            
            if (retryableImports.isEmpty()) {
                return createResponse("SUCCESS", "No failed imports to retry", 0, 0, 0, new ArrayList<>());
            }
            
            for (CorrespondenceImportStatus importStatus : retryableImports) {
                try {
                    boolean success = retryFailedEntitiesForCorrespondence(importStatus);
                    if (success) {
                        successfulRetries++;
                    } else {
                        failedRetries++;
                        errors.add("Failed to retry import for correspondence: " + importStatus.getCorrespondenceGuid());
                    }
                } catch (Exception e) {
                    failedRetries++;
                    String errorMsg = "Error retrying import for correspondence " + importStatus.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = determineFinalStatus(successfulRetries, failedRetries);
            String message = String.format("Retry completed. Success: %d, Failed: %d", successfulRetries, failedRetries);
            
            return createResponse(status, message, retryableImports.size(), successfulRetries, failedRetries, errors);
            
        } catch (Exception e) {
            logger.error("Error in retry failed imports", e);
            return createResponse("ERROR", "Retry failed: " + e.getMessage(), 0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Retries only the failed entities for a specific correspondence
     */
    private boolean retryFailedEntitiesForCorrespondence(CorrespondenceImportStatus importStatus) {
        String correspondenceGuid = importStatus.getCorrespondenceGuid();
        logger.info("Retrying failed entities for correspondence: {}", correspondenceGuid);
        
        boolean overallSuccess = true;
        
        // Only retry failed entities
        if ("FAILED".equals(importStatus.getAttachmentsStatus())) {
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "attachments", importStatus);
        }
        if ("FAILED".equals(importStatus.getCommentsStatus())) {
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "comments", importStatus);
        }
        if ("FAILED".equals(importStatus.getCopyTosStatus())) {
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "copytos", importStatus);
        }
        if ("FAILED".equals(importStatus.getCurrentDepartmentsStatus())) {
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "currentdepartments", importStatus);
        }
        if ("FAILED".equals(importStatus.getCurrentPositionsStatus())) {
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "currentpositions", importStatus);
        }
        if ("FAILED".equals(importStatus.getCurrentUsersStatus())) {
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "currentusers", importStatus);
        }
        if ("FAILED".equals(importStatus.getCustomFieldsStatus())) {
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "customfields", importStatus);
        }
        if ("FAILED".equals(importStatus.getLinksStatus())) {
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "links", importStatus);
        }
        if ("FAILED".equals(importStatus.getSendTosStatus())) {
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "sendtos", importStatus);
        }
        if ("FAILED".equals(importStatus.getTransactionsStatus())) {
            overallSuccess &= importEntityWithTracking(correspondenceGuid, "transactions", importStatus);
        }
        
        return overallSuccess;
    }
    
    /**
     * Gets import statistics for all correspondences
     */
    public Map<String, Object> getImportStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // Overall statistics
            Object[] overallStats = importStatusRepository.getImportStatistics();
            if (overallStats != null && overallStats.length >= 5) {
                statistics.put("pending", overallStats[0] != null ? ((Number) overallStats[0]).longValue() : 0L);
                statistics.put("inProgress", overallStats[1] != null ? ((Number) overallStats[1]).longValue() : 0L);
                statistics.put("completed", overallStats[2] != null ? ((Number) overallStats[2]).longValue() : 0L);
                statistics.put("failed", overallStats[3] != null ? ((Number) overallStats[3]).longValue() : 0L);
                statistics.put("total", overallStats[4] != null ? ((Number) overallStats[4]).longValue() : 0L);
            }
            
            // Entity-specific statistics
            Object[] entityStats = importStatusRepository.getEntityStatistics();
            if (entityStats != null && entityStats.length >= 10) {
                Map<String, Object> entityStatistics = new HashMap<>();
                entityStatistics.put("attachments", entityStats[0] != null ? ((Number) entityStats[0]).longValue() : 0L);
                entityStatistics.put("comments", entityStats[1] != null ? ((Number) entityStats[1]).longValue() : 0L);
                entityStatistics.put("copyTos", entityStats[2] != null ? ((Number) entityStats[2]).longValue() : 0L);
                entityStatistics.put("currentDepartments", entityStats[3] != null ? ((Number) entityStats[3]).longValue() : 0L);
                entityStatistics.put("currentPositions", entityStats[4] != null ? ((Number) entityStats[4]).longValue() : 0L);
                entityStatistics.put("currentUsers", entityStats[5] != null ? ((Number) entityStats[5]).longValue() : 0L);
                entityStatistics.put("customFields", entityStats[6] != null ? ((Number) entityStats[6]).longValue() : 0L);
                entityStatistics.put("links", entityStats[7] != null ? ((Number) entityStats[7]).longValue() : 0L);
                entityStatistics.put("sendTos", entityStats[8] != null ? ((Number) entityStats[8]).longValue() : 0L);
                entityStatistics.put("transactions", entityStats[9] != null ? ((Number) entityStats[9]).longValue() : 0L);
                statistics.put("entityDetails", entityStatistics);
            }
            
            return statistics;
            
        } catch (Exception e) {
            logger.error("Error getting import statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", "Failed to get statistics: " + e.getMessage());
            errorStats.put("pending", 0L);
            errorStats.put("inProgress", 0L);
            errorStats.put("completed", 0L);
            errorStats.put("failed", 0L);
            errorStats.put("total", 0L);
            return errorStats;
        }
    }
    
    /**
     * Gets detailed import status for all correspondences
     */
    public List<CorrespondenceImportStatus> getAllImportStatuses() {
        try {
            return importStatusRepository.findAll();
        } catch (Exception e) {
            logger.error("Error getting all import statuses", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gets import status for a specific correspondence
     */
    public Optional<CorrespondenceImportStatus> getImportStatus(String correspondenceGuid) {
        try {
            return importStatusRepository.findByCorrespondenceGuid(correspondenceGuid);
        } catch (Exception e) {
            logger.error("Error getting import status for correspondence: {}", correspondenceGuid, e);
            return Optional.empty();
        }
    }
    
    /**
     * Resets import status for a correspondence (for manual retry)
     */
    @Transactional
    public boolean resetImportStatus(String correspondenceGuid) {
        try {
            Optional<CorrespondenceImportStatus> statusOpt = importStatusRepository.findByCorrespondenceGuid(correspondenceGuid);
            if (statusOpt.isPresent()) {
                CorrespondenceImportStatus status = statusOpt.get();
                
                // Reset all statuses to PENDING
                status.setOverallStatus("PENDING");
                status.setAttachmentsStatus("PENDING");
                status.setCommentsStatus("PENDING");
                status.setCopyTosStatus("PENDING");
                status.setCurrentDepartmentsStatus("PENDING");
                status.setCurrentPositionsStatus("PENDING");
                status.setCurrentUsersStatus("PENDING");
                status.setCustomFieldsStatus("PENDING");
                status.setLinksStatus("PENDING");
                status.setSendTosStatus("PENDING");
                status.setTransactionsStatus("PENDING");
                
                // Clear errors
                status.setAttachmentsError(null);
                status.setCommentsError(null);
                status.setCopyTosError(null);
                status.setCurrentDepartmentsError(null);
                status.setCurrentPositionsError(null);
                status.setCurrentUsersError(null);
                status.setCustomFieldsError(null);
                status.setLinksError(null);
                status.setSendTosError(null);
                status.setTransactionsError(null);
                
                // Reset counts and retry info
                status.setRetryCount(0);
                status.setCompletedAt(null);
                status.setLastErrorAt(null);
                
                importStatusRepository.save(status);
                logger.info("Reset import status for correspondence: {}", correspondenceGuid);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error resetting import status for correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    // Helper methods
    private ImportResponseDto createResponse(String status, String message, int total, int success, int failed, List<String> errors) {
        ImportResponseDto response = new ImportResponseDto();
        response.setStatus(status);
        response.setMessage(message);
        response.setTotalRecords(total);
        response.setSuccessfulImports(success);
        response.setFailedImports(failed);
        response.setErrors(errors != null ? errors : new ArrayList<>());
        return response;
    }
    
    private String determineFinalStatus(int successfulImports, int failedImports) {
        if (failedImports == 0) {
            return "SUCCESS";
        } else if (successfulImports > 0) {
            return "PARTIAL_SUCCESS";
        } else {
            return "ERROR";
        }
    }
}