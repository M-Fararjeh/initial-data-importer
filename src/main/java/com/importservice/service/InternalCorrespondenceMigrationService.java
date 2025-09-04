package com.importservice.service;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.InternalCorrespondenceMigration;
import com.importservice.service.migration.internal.InternalApprovalPhaseService;
import com.importservice.service.migration.internal.InternalAssignmentPhaseService;
import com.importservice.service.migration.internal.InternalBusinessLogPhaseService;
import com.importservice.service.migration.internal.InternalClosingPhaseService;
import com.importservice.service.migration.internal.InternalCreationPhaseService;
import com.importservice.service.migration.internal.InternalPrepareDataService;
import com.importservice.service.migration.MigrationPhaseService;
import com.importservice.service.migration.MigrationStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Main orchestration service for internal correspondence migration
 * Delegates to specialized phase services for better organization
 */
@Service
public class InternalCorrespondenceMigrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(InternalCorrespondenceMigrationService.class);
    
    @Autowired
    private InternalPrepareDataService prepareDataService;
    
    @Autowired
    private InternalCreationPhaseService creationPhaseService;
    
    @Autowired
    private InternalAssignmentPhaseService assignmentPhaseService;
    
    @Autowired
    private InternalApprovalPhaseService approvalPhaseService;
    
    @Autowired
    private InternalBusinessLogPhaseService businessLogPhaseService;
    
    @Autowired
    private InternalClosingPhaseService closingPhaseService;
    
    @Autowired
    private MigrationStatisticsService statisticsService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    // Phase 1: Prepare Data
    public ImportResponseDto prepareData() {
        logger.info("Delegating to InternalPrepareDataService");
        return prepareDataService.prepareData();
    }
    
    // Phase 2: Creation
    public ImportResponseDto executeCreationPhase() {
        logger.info("Delegating to InternalCreationPhaseService");
        return creationPhaseService.executeCreationPhase();
    }
    
    public ImportResponseDto executeCreationForSpecific(List<String> correspondenceGuids) {
        logger.info("Delegating internal creation for specific correspondences to InternalCreationPhaseService");
        return creationPhaseService.executeCreationForSpecific(correspondenceGuids);
    }
    
    @Transactional(readOnly = true, timeout = 60)
    public List<InternalCorrespondenceMigration> getCreationMigrations() {
        logger.info("Delegating to InternalCreationPhaseService for creation migrations");
        return creationPhaseService.getCreationMigrations();
    }
    
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getCreationMigrationsWithDetails() {
        logger.info("Delegating to InternalCreationPhaseService for creation migrations with details");
        return creationPhaseService.getCreationMigrationsWithDetails();
    }
    
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getCreationStatistics() {
        logger.info("Delegating to InternalCreationPhaseService for creation statistics");
        return creationPhaseService.getCreationStatistics();
    }
    
    // Phase 3: Assignment
    public ImportResponseDto executeAssignmentPhase() {
        logger.info("Delegating to InternalAssignmentPhaseService");
        return assignmentPhaseService.executeAssignmentPhase();
    }
    
    public ImportResponseDto executeAssignmentForSpecific(List<String> transactionGuids) {
        logger.info("Delegating internal assignment for specific transactions to InternalAssignmentPhaseService");
        return assignmentPhaseService.executeAssignmentForSpecific(transactionGuids);
    }
    
    public Map<String, Object> getAssignmentMigrations(int page, int size, String status, String search) {
        logger.info("Delegating to InternalAssignmentPhaseService for assignment migrations");
        return assignmentPhaseService.getInternalAssignmentMigrations(page, size, status, search);
    }
    
    // Phase 4: Approval
    public ImportResponseDto executeApprovalPhase() {
        logger.info("Delegating to InternalApprovalPhaseService");
        return approvalPhaseService.executeApprovalPhase();
    }
    
    public ImportResponseDto executeApprovalForSpecific(List<String> correspondenceGuids) {
        logger.info("Delegating internal approval for specific correspondences to InternalApprovalPhaseService");
        return approvalPhaseService.executeApprovalForSpecific(correspondenceGuids);
    }
    
    public Map<String, Object> getApprovalMigrations(int page, int size, String status, String step, String search) {
        logger.info("Delegating to InternalApprovalPhaseService for approval migrations");
        return approvalPhaseService.getInternalApprovalMigrations(page, size, status, step, search);
    }
    
    // Phase 5: Business Log
    public ImportResponseDto executeBusinessLogPhase() {
        logger.info("Delegating to InternalBusinessLogPhaseService");
        return businessLogPhaseService.executeBusinessLogPhase();
    }
    
    public ImportResponseDto executeBusinessLogForSpecific(List<String> transactionGuids) {
        logger.info("Delegating internal business log for specific transactions to InternalBusinessLogPhaseService");
        return businessLogPhaseService.executeBusinessLogForSpecific(transactionGuids);
    }
    
    public Map<String, Object> getBusinessLogMigrations(int page, int size, String status, String search) {
        logger.info("Delegating to InternalBusinessLogPhaseService for business log migrations");
        return businessLogPhaseService.getInternalBusinessLogMigrations(page, size, status, search);
    }
    
    // Phase 6: Closing
    public ImportResponseDto executeClosingPhase() {
        logger.info("Delegating to InternalClosingPhaseService");
        return closingPhaseService.executeClosingPhase();
    }
    
    public ImportResponseDto executeClosingForSpecific(List<String> correspondenceGuids) {
        logger.info("Delegating internal closing for specific correspondences to InternalClosingPhaseService");
        return closingPhaseService.executeClosingForSpecific(correspondenceGuids);
    }
    
    public Map<String, Object> getClosingMigrations(int page, int size, String status, String needToClose, String search) {
        logger.info("Delegating to InternalClosingPhaseService for closing migrations");
        return closingPhaseService.getInternalClosingMigrations(page, size, status, needToClose, search);
    }
    
    // Statistics and Retry
    public Map<String, Object> getMigrationStatistics() {
        logger.info("Delegating to MigrationStatisticsService for internal statistics");
        return getInternalMigrationStatistics();
    }
    
    /**
     * Gets internal-specific migration statistics
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getInternalMigrationStatistics() {
        try {
            // This would need to be implemented to get internal-specific statistics
            // For now, return basic structure
            Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("prepareData", 0L);
            statistics.put("creation", 0L);
            statistics.put("assignment", 0L);
            statistics.put("approval", 0L);
            statistics.put("businessLog", 0L);
            statistics.put("closing", 0L);
            statistics.put("completed", 0L);
            statistics.put("failed", 0L);
            statistics.put("inProgress", 0L);
            
            return statistics;
        } catch (Exception e) {
            logger.error("Error getting internal migration statistics", e);
            return new java.util.HashMap<>();
        }
    }
    
    @Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto retryFailedMigrations() {
        logger.info("Starting retry of failed internal migrations");
        
        try {
            // Get retryable internal migrations
            List<InternalCorrespondenceMigration> retryableMigrations = getRetryableInternalMigrations();
            
            if (retryableMigrations.isEmpty()) {
                return phaseService.createResponse("SUCCESS", "No failed internal migrations to retry", 
                                                 0, 0, 0, new ArrayList<>());
            }
            
            int totalRetried = 0;
            int successfulRetries = 0;
            int failedRetries = 0;
            List<String> errors = new ArrayList<>();
            
            for (InternalCorrespondenceMigration migration : retryableMigrations) {
                totalRetried++;
                
                try {
                    // Retry based on current phase
                    boolean success = retryInternalMigrationPhase(migration);
                    if (success) {
                        successfulRetries++;
                    } else {
                        failedRetries++;
                        errors.add("Failed to retry internal migration for correspondence: " + migration.getCorrespondenceGuid());
                    }
                } catch (Exception e) {
                    failedRetries++;
                    String errorMsg = "Error retrying internal migration " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulRetries, failedRetries);
            String message = String.format("Internal retry completed. Success: %d, Failed: %d", 
                                         successfulRetries, failedRetries);
            
            return phaseService.createResponse(status, message, totalRetried, 
                                             successfulRetries, failedRetries, errors);
            
        } catch (Exception e) {
            logger.error("Error in retry failed internal migrations", e);
            return phaseService.createResponse("ERROR", "Internal retry failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Gets retryable internal migrations
     */
    private List<InternalCorrespondenceMigration> getRetryableInternalMigrations() {
        // This would need to be implemented in the repository
        return new ArrayList<>(); // Placeholder
    }
    
    /**
     * Retries a specific internal migration phase
     */
    private boolean retryInternalMigrationPhase(InternalCorrespondenceMigration migration) {
        String currentPhase = migration.getCurrentPhase();
        
        switch (currentPhase) {
            case "CREATION":
                return creationPhaseService.executeCreationForSpecific(
                    Arrays.asList(migration.getCorrespondenceGuid())
                ).getStatus().equals("SUCCESS");
                
            case "ASSIGNMENT":
                logger.info("Internal assignment retry not implemented yet for correspondence: {}", 
                          migration.getCorrespondenceGuid());
                return false;
                
            case "APPROVAL":
                return approvalPhaseService.executeApprovalForSpecific(
                    Arrays.asList(migration.getCorrespondenceGuid())
                ).getStatus().equals("SUCCESS");
                
            case "BUSINESS_LOG":
                logger.info("Internal business log retry not implemented yet for correspondence: {}", 
                          migration.getCorrespondenceGuid());
                return false;
                
            case "CLOSING":
                return closingPhaseService.executeClosingForSpecific(
                    Arrays.asList(migration.getCorrespondenceGuid())
                ).getStatus().equals("SUCCESS");
                
            default:
                logger.warn("Unknown internal phase for retry: {}", currentPhase);
                return false;
        }
    }
}