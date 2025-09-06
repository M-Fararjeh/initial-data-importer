package com.importservice.service.migration.incoming;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.IncomingCorrespondenceMigration;
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
        logger.info("Starting Phase 2: Creation with individual transactions per correspondence");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<IncomingCorrespondenceMigration> migrations = phaseService.getMigrationsForPhase("CREATION");
            logger.info("Found {} correspondences in CREATION phase", migrations.size());
            
            if (migrations.isEmpty()) {
                return phaseService.createResponse("SUCCESS", "No correspondences found in CREATION phase", 
                                                 0, 0, 0, new ArrayList<>());
            }
            
            // Process each correspondence in its own separate transaction
            for (int i = 0; i < migrations.size(); i++) {
                IncomingCorrespondenceMigration migration = migrations.get(i);
                String correspondenceGuid = migration.getCorrespondenceGuid();
                
                try {
                    logger.info("Processing correspondence: {} ({}/{})", 
                               correspondenceGuid, i + 1, migrations.size());
                    
                    boolean success = creationPhaseService.processCorrespondenceCreationInNewTransaction(correspondenceGuid);
                    
                    if (success) {
                        successfulImports++;
                        logger.info("✅ Successfully completed creation for correspondence: {}", correspondenceGuid);
                    } else {
                        failedImports++;
                        logger.warn("❌ Failed to complete creation for correspondence: {}", correspondenceGuid);
                    }
                    
                    // Add delay between correspondences to reduce system load and lock contention
                    if (i < migrations.size() - 1) {
                        Thread.sleep(500);
                    }
                    
                    // Log progress every 10 correspondences
                    if ((i + 1) % 10 == 0) {
                        logger.info("Progress: {}/{} correspondences processed (Success: {}, Failed: {})", 
                                   i + 1, migrations.size(), successfulImports, failedImports);
                    }
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error processing correspondence " + correspondenceGuid + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error("❌ " + errorMsg, e);
                    continue;
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Phase 2 completed. Created: %d, Failed: %d (Each correspondence processed in separate transaction)", 
                                         successfulImports, failedImports);
            
            logger.info("Phase 2 Creation completed - Total: {}, Success: {}, Failed: {}", 
                       migrations.size(), successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, migrations.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 2: Creation orchestration", e);
            return phaseService.createResponse("ERROR", "Phase 2 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    public ImportResponseDto executeCreationForSpecific(List<String> correspondenceGuids) {
        logger.info("Delegating creation for specific correspondences to CreationPhaseService");
        return creationPhaseService.executeCreationForSpecific(correspondenceGuids);
    }
    
    @Transactional(readOnly = true, timeout = 60)
    public List<IncomingCorrespondenceMigration> getCreationMigrations() {
        return creationPhaseService.getCreationMigrations();
    }
    
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getCreationMigrationsWithDetails() {
        return creationPhaseService.getCreationMigrationsWithDetails();
    }
    
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getCreationStatistics() {
        return creationPhaseService.getCreationStatistics();
    }
    
    // Phase 3: Assignment
    public ImportResponseDto executeAssignmentPhase() {
        return assignmentPhaseService.executeAssignmentPhase();
    }
    
    public ImportResponseDto executeAssignmentForSpecific(List<String> transactionGuids) {
        return assignmentPhaseService.executeAssignmentForSpecific(transactionGuids);
    }
    
    public Map<String, Object> getAssignmentMigrations(int page, int size, String status, String search) {
        return assignmentPhaseService.getAssignmentMigrations(page, size, status, search);
    }
    
    // Phase 4: Business Log
    public ImportResponseDto executeBusinessLogPhase() {
        return businessLogPhaseService.executeBusinessLogPhase();
    }
    
    public ImportResponseDto executeBusinessLogForSpecific(List<String> transactionGuids) {
        return businessLogPhaseService.executeBusinessLogForSpecific(transactionGuids);
    }
    
    public Map<String, Object> getBusinessLogMigrations(int page, int size, String status, String search) {
        return businessLogPhaseService.getBusinessLogMigrations(page, size, status, search);
    }
    
    // Phase 5: Comment
    public ImportResponseDto executeCommentPhase() {
        return commentPhaseService.executeCommentPhase();
    }
    
    public ImportResponseDto executeCommentForSpecific(List<String> commentGuids) {
        return commentPhaseService.executeCommentForSpecific(commentGuids);
    }
    
    public Map<String, Object> getCommentMigrations(int page, int size, String status, String commentType, String search) {
        return commentPhaseService.getCommentMigrations(page, size, status, commentType, search);
    }
    
    // Phase 6: Closing
    public ImportResponseDto executeClosingPhase() {
        return closingPhaseService.executeClosingPhase();
    }
    
    public ImportResponseDto executeClosingForSpecific(List<String> correspondenceGuids) {
        return closingPhaseService.executeClosingForSpecific(correspondenceGuids);
    }
    
    public Map<String, Object> getClosingMigrations(int page, int size, String status, String needToClose, String search) {
        return closingPhaseService.getClosingMigrations(page, size, status, needToClose, search);
    }
    
    // Statistics and Retry
    public Map<String, Object> getMigrationStatistics() {
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
    @Transactional(readOnly = false, timeout = 180)
    private boolean retryMigrationPhase(IncomingCorrespondenceMigration migration) {
        String currentPhase = migration.getCurrentPhase();
        
        switch (currentPhase) {
            case "CREATION":
                return creationPhaseService.executeCreationForSpecific(
                    Arrays.asList(migration.getCorrespondenceGuid())
                ).getStatus().equals("SUCCESS");
                
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
}