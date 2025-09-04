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

import java.util.*;

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
    
    @Autowired
    private com.importservice.repository.CorrespondenceTransactionRepository transactionRepository;
    
    @Autowired
    private com.importservice.repository.InternalCorrespondenceMigrationRepository migrationRepository;
    
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
            Map<String, Object> statistics = new HashMap<>();
            
            // Phase-specific counts - use safe defaults
            Long prepareDataCount = 0L;
            Long creationCount = 0L;
            Long assignmentCount = 0L;
            Long approvalCount = 0L;
            Long businessLogCount = 0L;
            Long closingCount = 0L;
            
            try {
                prepareDataCount = migrationRepository.countByCurrentPhase("PREPARE_DATA");
                creationCount = migrationRepository.countByCurrentPhase("CREATION");
                assignmentCount = migrationRepository.countByCurrentPhase("ASSIGNMENT");
                approvalCount = migrationRepository.countByCurrentPhase("APPROVAL");
                businessLogCount = migrationRepository.countByCurrentPhase("BUSINESS_LOG");
                closingCount = migrationRepository.countByCurrentPhase("CLOSING");
            } catch (Exception e) {
                logger.warn("Error getting internal phase counts, using defaults: {}", e.getMessage());
            }
            
            statistics.put("prepareData", prepareDataCount != null ? prepareDataCount : 0L);
            statistics.put("creation", creationCount != null ? creationCount : 0L);
            statistics.put("assignment", assignmentCount != null ? assignmentCount : 0L);
            statistics.put("approval", approvalCount != null ? approvalCount : 0L);
            statistics.put("businessLog", businessLogCount != null ? businessLogCount : 0L);
            statistics.put("closing", closingCount != null ? closingCount : 0L);
            
            // Overall status counts - use safe defaults
            Long completedCount = 0L;
            Long failedCount = 0L;
            Long inProgressCount = 0L;
            
            try {
                completedCount = migrationRepository.countByOverallStatus("COMPLETED");
                failedCount = migrationRepository.countByOverallStatus("FAILED");
                inProgressCount = migrationRepository.countByOverallStatus("IN_PROGRESS");
            } catch (Exception e) {
                logger.warn("Error getting internal overall status counts, using defaults: {}", e.getMessage());
            }
            
            statistics.put("completed", completedCount != null ? completedCount : 0L);
            statistics.put("failed", failedCount != null ? failedCount : 0L);
            statistics.put("inProgress", inProgressCount != null ? inProgressCount : 0L);
            
            // Internal assignment statistics - handle potential ambiguity
            try {
                Object[] assignmentStats = transactionRepository.getInternalAssignmentStatistics();
                if (assignmentStats != null && assignmentStats.length >= 4) {
                    Map<String, Object> assignmentStatistics = new HashMap<>();
                    assignmentStatistics.put("pending", assignmentStats[0] != null ? ((Number) assignmentStats[0]).longValue() : 0L);
                    assignmentStatistics.put("success", assignmentStats[1] != null ? ((Number) assignmentStats[1]).longValue() : 0L);
                    assignmentStatistics.put("failed", assignmentStats[2] != null ? ((Number) assignmentStats[2]).longValue() : 0L);
                    assignmentStatistics.put("total", assignmentStats[3] != null ? ((Number) assignmentStats[3]).longValue() : 0L);
                    statistics.put("assignmentDetails", assignmentStatistics);
                }
            } catch (Exception e) {
                logger.warn("Error getting internal assignment statistics: {}", e.getMessage());
            }
            
            // Internal business log statistics - handle potential ambiguity
            try {
                Object[] businessLogStats = transactionRepository.getInternalBusinessLogStatistics();
                if (businessLogStats != null && businessLogStats.length >= 4) {
                    Map<String, Object> businessLogStatistics = new HashMap<>();
                    businessLogStatistics.put("pending", businessLogStats[0] != null ? ((Number) businessLogStats[0]).longValue() : 0L);
                    businessLogStatistics.put("success", businessLogStats[1] != null ? ((Number) businessLogStats[1]).longValue() : 0L);
                    businessLogStatistics.put("failed", businessLogStats[2] != null ? ((Number) businessLogStats[2]).longValue() : 0L);
                    businessLogStatistics.put("total", businessLogStats[3] != null ? ((Number) businessLogStats[3]).longValue() : 0L);
                    statistics.put("businessLogDetails", businessLogStatistics);
                }
            } catch (Exception e) {
                logger.warn("Error getting internal business log statistics: {}", e.getMessage());
            }
            
            // Internal closing statistics - use safe defaults
            Long needToCloseCount = 0L;
            Long closingCompletedCount = 0L;
            Long closingFailedCount = 0L;
            
            try {
                needToCloseCount = migrationRepository.countByIsNeedToClose(true);
                closingCompletedCount = migrationRepository.countByClosingStatus("COMPLETED");
                closingFailedCount = migrationRepository.countByClosingStatus("FAILED");
            } catch (Exception e) {
                logger.warn("Error getting internal closing statistics, using defaults: {}", e.getMessage());
            }
            
            statistics.put("needToCloseCount", needToCloseCount != null ? needToCloseCount : 0L);
            statistics.put("closingCompleted", closingCompletedCount != null ? closingCompletedCount : 0L);
            statistics.put("closingFailed", closingFailedCount != null ? closingFailedCount : 0L);
            
            logger.debug("Generated internal migration statistics: {}", statistics);
            
            return statistics;
        } catch (Exception e) {
            logger.error("Error getting internal migration statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", "Failed to get internal statistics: " + e.getMessage());
            
            // Add default values for all expected fields
            errorStats.put("prepareData", 0L);
            errorStats.put("creation", 0L);
            errorStats.put("assignment", 0L);
            errorStats.put("approval", 0L);
            errorStats.put("businessLog", 0L);
            errorStats.put("closing", 0L);
            errorStats.put("completed", 0L);
            errorStats.put("failed", 0L);
            errorStats.put("inProgress", 0L);
            errorStats.put("needToCloseCount", 0L);
            errorStats.put("closingCompleted", 0L);
            errorStats.put("closingFailed", 0L);
            
            return errorStats;
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