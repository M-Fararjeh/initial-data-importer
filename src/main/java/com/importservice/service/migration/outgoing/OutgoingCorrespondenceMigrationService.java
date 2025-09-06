package com.importservice.service.migration.outgoing;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.OutgoingCorrespondenceMigration;
import com.importservice.service.migration.incoming.MigrationPhaseService;
import com.importservice.service.migration.incoming.MigrationStatisticsService;
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
 * Main orchestration service for outgoing correspondence migration
 * Delegates to specialized phase services for better organization
 */
@Service
public class OutgoingCorrespondenceMigrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(OutgoingCorrespondenceMigrationService.class);
    
    @Autowired
    private OutgoingPrepareDataService prepareDataService;
    
    @Autowired
    private OutgoingCreationPhaseService creationPhaseService;
    
    @Autowired
    private OutgoingAssignmentPhaseService assignmentPhaseService;
    
    @Autowired
    private OutgoingApprovalPhaseService approvalPhaseService;
    
    @Autowired
    private OutgoingBusinessLogPhaseService businessLogPhaseService;
    
    @Autowired
    private OutgoingClosingPhaseService closingPhaseService;
    
    @Autowired
    private MigrationStatisticsService statisticsService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    // Phase 1: Prepare Data
    public ImportResponseDto prepareData() {
        logger.info("Delegating to OutgoingPrepareDataService");
        return prepareDataService.prepareData();
    }
    
    // Phase 2: Creation
    public ImportResponseDto executeCreationPhase() {
        logger.info("Delegating to OutgoingCreationPhaseService");
        return creationPhaseService.executeCreationPhase();
    }
    
    public ImportResponseDto executeCreationForSpecific(List<String> correspondenceGuids) {
        logger.info("Delegating outgoing creation for specific correspondences to OutgoingCreationPhaseService");
        return creationPhaseService.executeCreationForSpecific(correspondenceGuids);
    }
    
    @Transactional(readOnly = true, timeout = 60)
    public List<OutgoingCorrespondenceMigration> getCreationMigrations() {
        logger.info("Delegating to OutgoingCreationPhaseService for creation migrations");
        return creationPhaseService.getCreationMigrations();
    }
    
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getCreationMigrationsWithDetails() {
        logger.info("Delegating to OutgoingCreationPhaseService for creation migrations with details");
        return creationPhaseService.getCreationMigrationsWithDetails();
    }
    
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getCreationStatistics() {
        logger.info("Delegating to OutgoingCreationPhaseService for creation statistics");
        return creationPhaseService.getCreationStatistics();
    }
    
    // Phase 3: Assignment
    public ImportResponseDto executeAssignmentPhase() {
        logger.info("Delegating to OutgoingAssignmentPhaseService");
        return assignmentPhaseService.executeAssignmentPhase();
    }
    
    public ImportResponseDto executeAssignmentForSpecific(List<String> transactionGuids) {
        logger.info("Delegating outgoing assignment for specific transactions to OutgoingAssignmentPhaseService");
        return assignmentPhaseService.executeAssignmentForSpecific(transactionGuids);
    }
    
    public Map<String, Object> getAssignmentMigrations(int page, int size, String status, String search) {
        logger.info("Delegating to OutgoingAssignmentPhaseService for assignment migrations");
        return assignmentPhaseService.getOutgoingAssignmentMigrations(page, size, status, search);
    }
    
    // Phase 4: Approval - Add missing method
    public Map<String, Object> getApprovalMigrations(int page, int size, String status, String step, String search) {
        logger.info("Delegating to OutgoingApprovalPhaseService for approval migrations");
        return approvalPhaseService.getOutgoingApprovalMigrations(page, size, status, step, search);
    }
    
    // Phase 4: Approval
    public ImportResponseDto executeApprovalPhase() {
        logger.info("Delegating to OutgoingApprovalPhaseService");
        return approvalPhaseService.executeApprovalPhase();
    }
    
    public ImportResponseDto executeApprovalForSpecific(List<String> correspondenceGuids) {
        logger.info("Delegating outgoing approval for specific correspondences to OutgoingApprovalPhaseService");
        return approvalPhaseService.executeApprovalForSpecific(correspondenceGuids);
    }
    
    // Phase 5: Business Log
    public ImportResponseDto executeBusinessLogPhase() {
        logger.info("Delegating to OutgoingBusinessLogPhaseService");
        return businessLogPhaseService.executeBusinessLogPhase();
    }
    
    public ImportResponseDto executeBusinessLogForSpecific(List<String> transactionGuids) {
        logger.info("Delegating outgoing business log for specific transactions to OutgoingBusinessLogPhaseService");
        return businessLogPhaseService.executeBusinessLogForSpecific(transactionGuids);
    }
    
    public Map<String, Object> getBusinessLogMigrations(int page, int size, String status, String search) {
        logger.info("Delegating to OutgoingBusinessLogPhaseService for business log migrations");
        return businessLogPhaseService.getOutgoingBusinessLogMigrations(page, size, status, search);
    }
    
    // Phase 6: Closing
    public ImportResponseDto executeClosingPhase() {
        logger.info("Delegating to OutgoingClosingPhaseService");
        return closingPhaseService.executeClosingPhase();
    }
    
    public ImportResponseDto executeClosingForSpecific(List<String> correspondenceGuids) {
        logger.info("Delegating outgoing closing for specific correspondences to OutgoingClosingPhaseService");
        return closingPhaseService.executeClosingForSpecific(correspondenceGuids);
    }
    
    public Map<String, Object> getClosingMigrations(int page, int size, String status, String needToClose, String search) {
        logger.info("Delegating to OutgoingClosingPhaseService for closing migrations");
        return closingPhaseService.getOutgoingClosingMigrations(page, size, status, needToClose, search);
    }
    
    // Statistics and Retry
    public Map<String, Object> getMigrationStatistics() {
        logger.info("Delegating to MigrationStatisticsService for outgoing statistics");
        return getOutgoingMigrationStatistics();
    }
    
    /**
     * Gets outgoing-specific migration statistics
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getOutgoingMigrationStatistics() {
        try {
            // This would need to be implemented to get outgoing-specific statistics
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
            logger.error("Error getting outgoing migration statistics", e);
            return new java.util.HashMap<>();
        }
    }
    
    @Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto retryFailedMigrations() {
        logger.info("Starting retry of failed outgoing migrations");
        
        try {
            // Get retryable outgoing migrations
            List<OutgoingCorrespondenceMigration> retryableMigrations = getRetryableOutgoingMigrations();
            
            if (retryableMigrations.isEmpty()) {
                return phaseService.createResponse("SUCCESS", "No failed outgoing migrations to retry", 
                                                 0, 0, 0, new ArrayList<>());
            }
            
            int totalRetried = 0;
            int successfulRetries = 0;
            int failedRetries = 0;
            List<String> errors = new ArrayList<>();
            
            for (OutgoingCorrespondenceMigration migration : retryableMigrations) {
                totalRetried++;
                
                try {
                    // Retry based on current phase
                    boolean success = retryOutgoingMigrationPhase(migration);
                    if (success) {
                        successfulRetries++;
                    } else {
                        failedRetries++;
                        errors.add("Failed to retry outgoing migration for correspondence: " + migration.getCorrespondenceGuid());
                    }
                } catch (Exception e) {
                    failedRetries++;
                    String errorMsg = "Error retrying outgoing migration " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulRetries, failedRetries);
            String message = String.format("Outgoing retry completed. Success: %d, Failed: %d", 
                                         successfulRetries, failedRetries);
            
            return phaseService.createResponse(status, message, totalRetried, 
                                             successfulRetries, failedRetries, errors);
            
        } catch (Exception e) {
            logger.error("Error in retry failed outgoing migrations", e);
            return phaseService.createResponse("ERROR", "Outgoing retry failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Gets retryable outgoing migrations
     */
    private List<OutgoingCorrespondenceMigration> getRetryableOutgoingMigrations() {
        // This would need to be implemented in the repository
        return new ArrayList<>(); // Placeholder
    }
    
    /**
     * Retries a specific outgoing migration phase
     */
    private boolean retryOutgoingMigrationPhase(OutgoingCorrespondenceMigration migration) {
        String currentPhase = migration.getCurrentPhase();
        
        switch (currentPhase) {
            case "CREATION":
                return creationPhaseService.executeCreationForSpecific(
                    Arrays.asList(migration.getCorrespondenceGuid())
                ).getStatus().equals("SUCCESS");
                
            case "ASSIGNMENT":
                logger.info("Outgoing assignment retry not implemented yet for correspondence: {}", 
                          migration.getCorrespondenceGuid());
                return false;
                
            case "APPROVAL":
                return approvalPhaseService.executeApprovalForSpecific(
                    Arrays.asList(migration.getCorrespondenceGuid())
                ).getStatus().equals("SUCCESS");
                
            case "BUSINESS_LOG":
                logger.info("Outgoing business log retry not implemented yet for correspondence: {}", 
                          migration.getCorrespondenceGuid());
                return false;
                
            case "COMMENT":
                logger.info("Outgoing comment retry not implemented yet for correspondence: {}", 
                          migration.getCorrespondenceGuid());
                return false;
                
            case "CLOSING":
                return closingPhaseService.executeClosingForSpecific(
                    Arrays.asList(migration.getCorrespondenceGuid())
                ).getStatus().equals("SUCCESS");
                
            default:
                logger.warn("Unknown outgoing phase for retry: {}", currentPhase);
                return false;
        }
    }
}