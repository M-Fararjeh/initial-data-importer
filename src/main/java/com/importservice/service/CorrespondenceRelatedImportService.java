package com.importservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.importservice.dto.ImportResponseDto;
import com.importservice.dto.ApiResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.CorrespondenceImportStatus;
import com.importservice.entity.*;
import com.importservice.repository.CorrespondenceImportStatusRepository;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    
    // Correspondence-related repositories
    @Autowired
    private CorrespondenceAttachmentRepository correspondenceAttachmentRepository;
    
    @Autowired
    private CorrespondenceCommentRepository correspondenceCommentRepository;
    
    @Autowired
    private CorrespondenceCopyToRepository correspondenceCopyToRepository;
    
    @Autowired
    private CorrespondenceCurrentDepartmentRepository correspondenceCurrentDepartmentRepository;
    
    @Autowired
    private CorrespondenceCurrentPositionRepository correspondenceCurrentPositionRepository;
    
    @Autowired
    private CorrespondenceCurrentUserRepository correspondenceCurrentUserRepository;
    
    @Autowired
    private CorrespondenceCustomFieldRepository correspondenceCustomFieldRepository;
    
    @Autowired
    private CorrespondenceLinkRepository correspondenceLinkRepository;
    
    @Autowired
    private CorrespondenceSendToRepository correspondenceSendToRepository;
    
    @Autowired
    private CorrespondenceTransactionRepository correspondenceTransactionRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${source.api.base-url}")
    private String sourceApiBaseUrl;
    
    @Value("${source.api.key}")
    private String sourceApiKey;
    
    /**
     * Imports all correspondence-related data with comprehensive status tracking
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 600)
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
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public boolean importRelatedDataForCorrespondence(String correspondenceGuid) {
        logger.info("Importing related data for correspondence: {}", correspondenceGuid);
        
        try {
            // Get or create import status record
            CorrespondenceImportStatus importStatus = createOrGetImportStatusInNewTransaction(correspondenceGuid);
            
            // Skip if already completed
            if (importStatus.isCompleted()) {
                logger.info("Related data already imported for correspondence: {}", correspondenceGuid);
                return true;
            }
            
            importStatus.setOverallStatus("IN_PROGRESS");
            importStatus.setStartedAt(LocalDateTime.now());
            updateImportStatusInNewTransaction(importStatus);
            
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
            
            updateImportStatusInNewTransaction(importStatus);
            
            logger.info("Completed related data import for correspondence: {} with status: {}", 
                       correspondenceGuid, importStatus.getOverallStatus());
            
            return overallSuccess;
            
        } catch (Exception e) {
            logger.error("Error importing related data for correspondence: {}", correspondenceGuid, e);
            
            // Update status to failed
            try {
                CorrespondenceImportStatus importStatus = createOrGetImportStatusInNewTransaction(correspondenceGuid);
                importStatus.setOverallStatus("FAILED");
                importStatus.incrementRetryCount();
                updateImportStatusInNewTransaction(importStatus);
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
            updateImportStatusInNewTransaction(importStatus);
            
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
            updateImportStatusInNewTransaction(importStatus);
        }
    }
    
    /**
     * Calls the appropriate import method based on entity type
     */
    private ImportResponseDto callImportMethod(String correspondenceGuid, String entityType) {
        switch (entityType.toLowerCase()) {
            case "attachments":
                return importCorrespondenceAttachmentsWithDirectCall(correspondenceGuid);
            case "comments":
                return importCorrespondenceCommentsWithDirectCall(correspondenceGuid);
            case "copytos":
                return importCorrespondenceCopyTosWithDirectCall(correspondenceGuid);
            case "currentdepartments":
                return importCorrespondenceCurrentDepartmentsWithDirectCall(correspondenceGuid);
            case "currentpositions":
                return importCorrespondenceCurrentPositionsWithDirectCall(correspondenceGuid);
            case "currentusers":
                return importCorrespondenceCurrentUsersWithDirectCall(correspondenceGuid);
            case "customfields":
                return importCorrespondenceCustomFieldsWithDirectCall(correspondenceGuid);
            case "links":
                return importCorrespondenceLinksWithDirectCall(correspondenceGuid);
            case "sendtos":
                return importCorrespondenceSendTosWithDirectCall(correspondenceGuid);
            case "transactions":
                return importCorrespondenceTransactionsWithDirectCall(correspondenceGuid);
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }
    
    /**
     * Gets or creates import status record for correspondence
     */
    private CorrespondenceImportStatus getOrCreateImportStatus(String correspondenceGuid) {
        return createOrGetImportStatusInNewTransaction(correspondenceGuid);
    }
    
    /**
     * Creates or gets import status in a completely separate transaction
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 30)
    public CorrespondenceImportStatus createOrGetImportStatusInNewTransaction(String correspondenceGuid) {
        logger.info("Creating or getting import status for correspondence: {}", correspondenceGuid);
        
        try {
            // First check if it exists
            Optional<CorrespondenceImportStatus> existingStatus = 
                importStatusRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (existingStatus.isPresent()) {
                logger.info("Found existing import status for correspondence: {}", correspondenceGuid);
                return existingStatus.get();
            }
            
            // Create new status record
            logger.info("Creating new import status record for correspondence: {}", correspondenceGuid);
            CorrespondenceImportStatus newStatus = new CorrespondenceImportStatus(correspondenceGuid);
            newStatus.setOverallStatus("PENDING");
            newStatus.setStartedAt(LocalDateTime.now());
            
            // Save and flush immediately
            CorrespondenceImportStatus savedStatus = importStatusRepository.save(newStatus);
            importStatusRepository.flush();
            
            logger.info("Successfully created import status record with ID: {} for correspondence: {}", 
                       savedStatus.getId(), correspondenceGuid);
            
            return savedStatus;
            
        } catch (Exception e) {
            logger.error("Error creating import status for correspondence: {}", correspondenceGuid, e);
            throw new RuntimeException("Failed to create import status: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates import status in a separate transaction
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 30)
    public void updateImportStatusInNewTransaction(CorrespondenceImportStatus importStatus) {
        try {
            logger.debug("Updating import status for correspondence: {}", importStatus.getCorrespondenceGuid());
            importStatusRepository.save(importStatus);
            importStatusRepository.flush();
            logger.debug("Successfully updated import status for correspondence: {}", importStatus.getCorrespondenceGuid());
        } catch (Exception e) {
            logger.error("Error updating import status for correspondence: {}", importStatus.getCorrespondenceGuid(), e);
            throw new RuntimeException("Failed to update import status: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets or creates import status record for correspondence (old method - kept for compatibility)
     */
    private CorrespondenceImportStatus getOrCreateImportStatusOld(String correspondenceGuid) {
        Optional<CorrespondenceImportStatus> existingStatus = 
            importStatusRepository.findByCorrespondenceGuid(correspondenceGuid);
        
        if (existingStatus.isPresent()) {
            return existingStatus.get();
        } else {
            CorrespondenceImportStatus newStatus = new CorrespondenceImportStatus(correspondenceGuid);
            return importStatusRepository.save(newStatus);
        }
    }
            logger.debug("Created new import status record for correspondence: {}", correspondenceGuid);
            return savedStatus;
        }
    }
    
    /**
     * Retries failed imports for correspondences that haven't exceeded max retries
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 600)
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
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 30)
    public boolean resetImportStatus(String correspondenceGuid) {
        logger.info("Resetting import status for correspondence: {}", correspondenceGuid);
        
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
                importStatusRepository.flush();
                logger.info("Reset import status for correspondence: {}", correspondenceGuid);
                return true;
            }
            logger.warn("No import status found to reset for correspondence: {}", correspondenceGuid);
            return false;
        } catch (Exception e) {
            logger.error("Error resetting import status for correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    // Helper methods
    
    /**
     * Direct call methods for importing specific entity types with API calls
     */
    private ImportResponseDto importCorrespondenceAttachmentsWithDirectCall(String docGuid) {
        return importCorrespondenceRelatedDataDirect("/CorrespondenceAttachments/docGuid/" + docGuid, 
                                                   CorrespondenceAttachment.class, 
                                                   correspondenceAttachmentRepository, 
                                                   "CorrespondenceAttachments");
    }
    
    private ImportResponseDto importCorrespondenceCommentsWithDirectCall(String docGuid) {
        return importCorrespondenceRelatedDataDirect("/CorrespondenceComments/docGuid/" + docGuid, 
                                                   CorrespondenceComment.class, 
                                                   correspondenceCommentRepository, 
                                                   "CorrespondenceComments");
    }
    
    private ImportResponseDto importCorrespondenceCopyTosWithDirectCall(String docGuid) {
        return importCorrespondenceRelatedDataDirect("/CorrespondenceCopyTo/docGUId/" + docGuid, 
                                                   CorrespondenceCopyTo.class, 
                                                   correspondenceCopyToRepository, 
                                                   "CorrespondenceCopyTos");
    }
    
    private ImportResponseDto importCorrespondenceCurrentDepartmentsWithDirectCall(String docGuid) {
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        int totalRecords = 0;

        try {
            String url = sourceApiBaseUrl + "/CorrespondenceCurrentDepartments/docGuid/" + docGuid;
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                logger.warn("Empty response body for correspondence current departments, docGuid: {}", docGuid);
                return createResponse("SUCCESS", "No correspondence current departments found for document", 
                    0, 0, 0, new ArrayList<>());
            }
            
            TypeReference<ApiResponseDto<CorrespondenceCurrentDepartment>> typeRef = 
                new TypeReference<ApiResponseDto<CorrespondenceCurrentDepartment>>() {};
            ApiResponseDto<CorrespondenceCurrentDepartment> apiResponse = objectMapper.readValue(responseBody, typeRef);

            if (apiResponse == null || !Boolean.TRUE.equals(apiResponse.getSuccess())) {
                String message = apiResponse != null ? apiResponse.getMessage() : "Unknown API error";
                return createResponse("ERROR", "API returned failure: " + message, 
                    0, 0, 0, Arrays.asList("API returned failure: " + message));
            }

            List<CorrespondenceCurrentDepartment> departments = apiResponse.getData();
            if (departments == null || departments.isEmpty()) {
                logger.info("No correspondence current departments found for docGuid: {}", docGuid);
                return createResponse("SUCCESS", "No correspondence current departments found for document", 
                    0, 0, 0, new ArrayList<>());
            }
            
            totalRecords = departments.size();
            logger.info("Found {} correspondence current departments for docGuid: {}", totalRecords, docGuid);

            for (CorrespondenceCurrentDepartment dept : departments) {
                try {
                    if (dept == null) {
                        failedImports++;
                        errors.add("Null department object received");
                        continue;
                    }
                    dept.setDocGuid(docGuid);
                    correspondenceCurrentDepartmentRepository.save(dept);
                    successfulImports++;
                    logger.debug("Successfully saved correspondence current department for docGuid: {}", docGuid);
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Failed to save correspondence current department: " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("CorrespondenceCurrentDepartments import completed. Success: %d, Failed: %d", 
                                         successfulImports, failedImports);

            return createResponse(status, message, totalRecords, successfulImports, failedImports, errors);

        } catch (Exception e) {
            logger.error("Failed to import correspondence current departments", e);
            return createResponse("ERROR", "Failed to import correspondence current departments: " + e.getMessage(), 
                0, 0, 0, Arrays.asList("Failed to import correspondence current departments: " + e.getMessage()));
        }
    }
    
    private ImportResponseDto importCorrespondenceCurrentPositionsWithDirectCall(String docGuid) {
        return importCorrespondenceRelatedDataDirect("/CorrespondenceCurrentPositions/docGuid/" + docGuid, 
                                                   CorrespondenceCurrentPosition.class, 
                                                   correspondenceCurrentPositionRepository, 
                                                   "CorrespondenceCurrentPositions");
    }
    
    private ImportResponseDto importCorrespondenceCurrentUsersWithDirectCall(String docGuid) {
        return importCorrespondenceRelatedDataDirect("/CorrespondenceCurrentUsers/docGuid/" + docGuid, 
                                                   CorrespondenceCurrentUser.class, 
                                                   correspondenceCurrentUserRepository, 
                                                   "CorrespondenceCurrentUsers");
    }
    
    private ImportResponseDto importCorrespondenceCustomFieldsWithDirectCall(String docGuid) {
        return importCorrespondenceRelatedDataDirect("/CorrespondenceCustomFields/docGuid/" + docGuid, 
                                                   CorrespondenceCustomField.class, 
                                                   correspondenceCustomFieldRepository, 
                                                   "CorrespondenceCustomFields");
    }
    
    private ImportResponseDto importCorrespondenceLinksWithDirectCall(String docGuid) {
        return importCorrespondenceRelatedDataDirect("/CorrespondenceLinks/docGuid/" + docGuid, 
                                                   CorrespondenceLink.class, 
                                                   correspondenceLinkRepository, 
                                                   "CorrespondenceLinks");
    }
    
    private ImportResponseDto importCorrespondenceSendTosWithDirectCall(String docGuid) {
        return importCorrespondenceRelatedDataDirect("/CorrespondenceSendTo/docGUId/" + docGuid, 
                                                   CorrespondenceSendTo.class, 
                                                   correspondenceSendToRepository, 
                                                   "CorrespondenceSendTos");
    }
    
    private ImportResponseDto importCorrespondenceTransactionsWithDirectCall(String docGuid) {
        return importCorrespondenceRelatedDataDirect("/CorrespondenceTransactions/docGuid/" + docGuid, 
                                                   CorrespondenceTransaction.class, 
                                                   correspondenceTransactionRepository, 
                                                   "CorrespondenceTransactions");
    }
    
    /**
     * Generic method for importing correspondence-related data with direct API calls
     */
    private <T, ID> ImportResponseDto importCorrespondenceRelatedDataDirect(String endpoint, Class<T> entityClass, 
                                                                           JpaRepository<T, ID> repository, String entityName) {
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        int totalRecords = 0;

        try {
            String url;
            if (endpoint.contains("/CorrespondenceAttachments/docGuid/")) {
                // Special case for CorrespondenceAttachments - use different base URL
                String docGuid = endpoint.substring(endpoint.lastIndexOf("/") + 1);
                url = "https://itba.tarasol.cloud/Tarasol4ExtractorApi/docGuid/" + docGuid;
            } else {
                url = sourceApiBaseUrl + endpoint;
            }
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                logger.warn("Empty response body for {}, endpoint: {}", entityName, endpoint);
                return createResponse("SUCCESS", "No " + entityName + " found", 
                    0, 0, 0, new ArrayList<>());
            }
            
            ApiResponseDto<Object> genericResponse = objectMapper.readValue(responseBody, 
                new TypeReference<ApiResponseDto<Object>>() {});
            
            if (genericResponse == null || !Boolean.TRUE.equals(genericResponse.getSuccess())) {
                String message = genericResponse != null ? genericResponse.getMessage() : "Unknown API error";
                return createResponse("ERROR", "API returned failure: " + message, 
                    0, 0, 0, Arrays.asList("API returned failure: " + message));
            }
            
            List<T> entities = new ArrayList<>();
            if (genericResponse.getData() != null) {
                for (Object item : genericResponse.getData()) {
                    try {
                        if (item == null) {
                            logger.warn("Null item found in {} data", entityName);
                            continue;
                        }
                        T entityData = objectMapper.convertValue(item, entityClass);
                        if (entityData != null) {
                            entities.add(entityData);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to convert item to {}: {}", entityClass.getSimpleName(), e.getMessage());
                        failedImports++;
                        errors.add("Failed to parse " + entityName + " item: " + e.getMessage());
                    }
                }
            } else {
                logger.info("No data found for {}, endpoint: {}", entityName, endpoint);
                return createResponse("SUCCESS", "No " + entityName + " found", 
                    0, 0, 0, new ArrayList<>());
            }

            totalRecords = entities.size();
            logger.info("Found {} {} to import", totalRecords, entityName);

            for (T entityData : entities) {
                try {
                    if (entityData == null) {
                        failedImports++;
                        errors.add("Null " + entityName + " object received");
                        continue;
                    }
                    
                    // Special handling for CorrespondenceAttachment with large file data
                    if (entityData instanceof CorrespondenceAttachment) {
                        CorrespondenceAttachment attachment = (CorrespondenceAttachment) entityData;
                        if (attachment.getFileData() != null && attachment.getFileData().length() > 200_000_000) {
                            logger.warn("Skipping attachment {} due to large file size: {} bytes", 
                                      attachment.getGuid(), attachment.getFileData().length());
                            attachment.setFileData(null);
                            attachment.setFileDataErrorMessage("File too large for import (>200MB)");
                        }
                    }
                    
                    repository.save(entityData);
                    successfulImports++;
                    logger.debug("Successfully saved {}", entityName);
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Failed to save " + entityName + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }

            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("%s import completed. Success: %d, Failed: %d", 
                                         entityName, successfulImports, failedImports);

            return createResponse(status, message, totalRecords, successfulImports, failedImports, errors);

        } catch (Exception e) {
            logger.error("Failed to import {}", entityName, e);
            return createResponse("ERROR", "Failed to import " + entityName + ": " + e.getMessage(), 
                0, 0, 0, Arrays.asList("Failed to import " + entityName + ": " + e.getMessage()));
        }
    }
    
    /**
     * Creates HTTP headers for API requests
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "*/*");
        headers.set("X-API-KEY", sourceApiKey);
        return headers;
    }
    
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