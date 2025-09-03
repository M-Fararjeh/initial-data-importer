package com.importservice.service;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.service.migration.AssignmentPhaseService;
import com.importservice.service.migration.BusinessLogPhaseService;
import com.importservice.service.migration.ClosingPhaseService;
import com.importservice.service.migration.CommentPhaseService;
import com.importservice.service.migration.CreationPhaseService;
import com.importservice.service.migration.MigrationPhaseService;
import com.importservice.service.migration.MigrationStatisticsService;
import com.importservice.service.migration.PrepareDataService;
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
 * Main orchestration service for incoming correspondence migration
 * Delegates to specialized phase services for better organization
 */
@Service
public class IncomingCorrespondenceMigrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(IncomingCorrespondenceMigrationService.class);
    
    @Autowired
    private PrepareDataService prepareDataService;
    
    @Autowired
    private CreationPhaseService creationPhaseService;
    
    @Autowired
    private AssignmentPhaseService assignmentPhaseService;
    
    @Autowired
    private BusinessLogPhaseService businessLogPhaseService;
    
    @Autowired
    private CommentPhaseService commentPhaseService;
    
    @Autowired
    private ClosingPhaseService closingPhaseService;
    
    @Autowired
    private MigrationStatisticsService statisticsService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    // Phase 1: Prepare Data
    public ImportResponseDto prepareData() {
        logger.info("Delegating to PrepareDataService");
        return prepareDataService.prepareData();
    }
    
    // Phase 2: Creation
    public ImportResponseDto executeCreationPhase() {
        logger.info("Delegating to CreationPhaseService");
        return creationPhaseService.executeCreationPhase();
    }
    
    public ImportResponseDto executeCreationForSpecific(List<String> correspondenceGuids) {
        logger.info("Delegating creation for specific correspondences to CreationPhaseService");
        return creationPhaseService.executeCreationForSpecific(correspondenceGuids);
    }
    
    public List<IncomingCorrespondenceMigration> getCreationMigrations() {
        logger.info("Delegating to CreationPhaseService for creation migrations");
        return creationPhaseService.getCreationMigrations();
    }
    
    public Map<String, Object> getCreationMigrationsWithDetails() {
        logger.info("Delegating to CreationPhaseService for creation migrations with details");
        return creationPhaseService.getCreationMigrationsWithDetails();
    }
    
    // Phase 3: Assignment
    public ImportResponseDto executeAssignmentPhase() {
        logger.info("Delegating to AssignmentPhaseService");
        return assignmentPhaseService.executeAssignmentPhase();
    }
    
    public ImportResponseDto executeAssignmentForSpecific(List<String> transactionGuids) {
        logger.info("Delegating assignment for specific transactions to AssignmentPhaseService");
        return assignmentPhaseService.executeAssignmentForSpecific(transactionGuids);
    }
    
    public Map<String, Object> getAssignmentMigrations(int page, int size, String status, String search) {
        logger.info("Delegating to AssignmentPhaseService for assignment migrations");
        return assignmentPhaseService.getAssignmentMigrations(page, size, status, search);
    }
    
    // Phase 4: Business Log
    public ImportResponseDto executeBusinessLogPhase() {
        logger.info("Delegating to BusinessLogPhaseService");
        return businessLogPhaseService.executeBusinessLogPhase();
    }
    
    public ImportResponseDto executeBusinessLogForSpecific(List<String> transactionGuids) {
        logger.info("Delegating business log for specific transactions to BusinessLogPhaseService");
        return businessLogPhaseService.executeBusinessLogForSpecific(transactionGuids);
    }
    
    public Map<String, Object> getBusinessLogMigrations(int page, int size, String status, String search) {
        logger.info("Delegating to BusinessLogPhaseService for business log migrations");
        return businessLogPhaseService.getBusinessLogMigrations(page, size, status, search);
    }
    
    // Phase 5: Comment
    public ImportResponseDto executeCommentPhase() {
        logger.info("Delegating to CommentPhaseService");
        return commentPhaseService.executeCommentPhase();
    }
    
    public ImportResponseDto executeCommentForSpecific(List<String> commentGuids) {
        logger.info("Delegating comment for specific comments to CommentPhaseService");
        return commentPhaseService.executeCommentForSpecific(commentGuids);
    }
    
    public Map<String, Object> getCommentMigrations(int page, int size, String status, String commentType, String search) {
        logger.info("Delegating to CommentPhaseService for comment migrations");
        return commentPhaseService.getCommentMigrations(page, size, status, commentType, search);
    }
    
    // Phase 6: Closing
    public ImportResponseDto executeClosingPhase() {
        logger.info("Delegating to ClosingPhaseService");
        return closingPhaseService.executeClosingPhase();
    }
    
    public ImportResponseDto executeClosingForSpecific(List<String> correspondenceGuids) {
        logger.info("Delegating closing for specific correspondences to ClosingPhaseService");
        return closingPhaseService.executeClosingForSpecific(correspondenceGuids);
    }
    
    public Map<String, Object> getClosingMigrations(int page, int size, String status, String needToClose, String search) {
        logger.info("Delegating to ClosingPhaseService for closing migrations");
        return closingPhaseService.getClosingMigrations(page, size, status, needToClose, search);
    }
    
    // Statistics and Retry
    public Map<String, Object> getMigrationStatistics() {
        logger.info("Delegating to MigrationStatisticsService");
        return statisticsService.getMigrationStatistics();
    }
    
    @Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto retryFailedMigrations() {
        logger.info("Starting retry of failed migrations");
        
        try {
            List<IncomingCorrespondenceMigration> retryableMigrations = phaseService.getRetryableMigrations();
            
            if (retryableMigrations.isEmpty()) {
                return phaseService.createResponse("SUCCESS", "No failed migrations to retry", 
                                                 0, 0, 0, new ArrayList<>());
            }
            
            int totalRetried = 0;
            int successfulRetries = 0;
            int failedRetries = 0;
            List<String> errors = new ArrayList<>();
            
            for (IncomingCorrespondenceMigration migration : retryableMigrations) {
                totalRetried++;
                
                try {
                    // Retry based on current phase
                    boolean success = retryMigrationPhase(migration);
                    if (success) {
                        successfulRetries++;
                    } else {
                        failedRetries++;
                        errors.add("Failed to retry migration for correspondence: " + migration.getCorrespondenceGuid());
                    }
                } catch (Exception e) {
                    failedRetries++;
                    String errorMsg = "Error retrying migration " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulRetries, failedRetries);
            String message = String.format("Retry completed. Success: %d, Failed: %d", 
                                         successfulRetries, failedRetries);
            
            return phaseService.createResponse(status, message, totalRetried, 
                                             successfulRetries, failedRetries, errors);
            
        } catch (Exception e) {
            logger.error("Error in retry failed migrations", e);
            return phaseService.createResponse("ERROR", "Retry failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Retries a specific migration phase
     */
    private boolean retryMigrationPhase(IncomingCorrespondenceMigration migration) {
        String currentPhase = migration.getCurrentPhase();
        
        switch (currentPhase) {
            case "CREATION":
                return creationPhaseService.executeCreationForSpecific(
                    Arrays.asList(migration.getCorrespondenceGuid())
                ).getStatus().equals("SUCCESS");
                
            case "ASSIGNMENT":
                // For assignment retry, we need to find the transaction GUIDs
                // This would require additional logic to get transaction GUIDs for the correspondence
                logger.info("Assignment retry not implemented yet for correspondence: {}", 
                          migration.getCorrespondenceGuid());
                return false;
                
            case "BUSINESS_LOG":
                // Similar to assignment, needs transaction GUIDs
                logger.info("Business log retry not implemented yet for correspondence: {}", 
                          migration.getCorrespondenceGuid());
                return false;
                
            case "COMMENT":
                // Similar to others, needs comment GUIDs
                logger.info("Comment retry not implemented yet for correspondence: {}", 
                          migration.getCorrespondenceGuid());
                return false;
                
            case "CLOSING":
                return closingPhaseService.executeClosingForSpecific(
                    Arrays.asList(migration.getCorrespondenceGuid())
                ).getStatus().equals("SUCCESS");
                
            default:
                logger.warn("Unknown phase for retry: {}", currentPhase);
                return false;
        }
    }
}