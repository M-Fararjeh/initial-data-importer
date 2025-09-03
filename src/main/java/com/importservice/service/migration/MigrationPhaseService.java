package com.importservice.service.migration;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service for managing migration phases and overall migration lifecycle
 */
@Service
public class MigrationPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(MigrationPhaseService.class);
    
    @Autowired
    private IncomingCorrespondenceMigrationRepository migrationRepository;
    
    /**
     * Updates migration phase status
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, 
                   timeout = 30, 
                   isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void updatePhaseStatus(String correspondenceGuid, String phase, String status, String error) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                updatePhaseStatusInternal(correspondenceGuid, phase, status, error);
                return; // Success, exit retry loop
            } catch (org.springframework.dao.PessimisticLockingFailureException e) {
                retryCount++;
                logger.warn("[NEW_TRANSACTION] Lock timeout on attempt {} for correspondence: {} - {}", 
                           retryCount, correspondenceGuid, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    logger.error("[NEW_TRANSACTION] Failed to update phase status after {} attempts for correspondence: {}", 
                               maxRetries, correspondenceGuid);
                    throw new RuntimeException("Failed to update phase status after " + maxRetries + " attempts: " + e.getMessage(), e);
                }
                
                // Exponential backoff
                try {
                    Thread.sleep(1000 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry backoff", ie);
                }
            }
        }
    }
    
    /**
     * Internal method for updating phase status
     */
    private void updatePhaseStatusInternal(String correspondenceGuid, String phase, String status, String error) {
        try {
            logger.info("[NEW_TRANSACTION] Updating phase status for correspondence: {} - Phase: {}, Status: {}", 
                       correspondenceGuid, phase, status);
            
            IncomingCorrespondenceMigration migration = migrationRepository
                .findByCorrespondenceGuid(correspondenceGuid)
                .orElse(null);
            
            if (migration == null) {
                logger.warn("Migration record not found for correspondence: {}", correspondenceGuid);
                return;
            }
            
            // Create a new instance to avoid stale data issues
            IncomingCorrespondenceMigration freshMigration = new IncomingCorrespondenceMigration();
            freshMigration.setId(migration.getId());
            freshMigration.setCorrespondenceGuid(migration.getCorrespondenceGuid());
            freshMigration.setCurrentPhase(migration.getCurrentPhase());
            freshMigration.setNextPhase(migration.getNextPhase());
            freshMigration.setPhaseStatus(migration.getPhaseStatus());
            freshMigration.setIsNeedToClose(migration.getIsNeedToClose());
            freshMigration.setCreatedDocumentId(migration.getCreatedDocumentId());
            freshMigration.setBatchId(migration.getBatchId());
            freshMigration.setRetryCount(migration.getRetryCount());
            freshMigration.setMaxRetries(migration.getMaxRetries());
            freshMigration.setStartedAt(migration.getStartedAt());
            freshMigration.setCompletedAt(migration.getCompletedAt());
            freshMigration.setLastErrorAt(migration.getLastErrorAt());
            freshMigration.setCreationDate(migration.getCreationDate());
            
            // Copy all phase statuses
            freshMigration.setPrepareDataStatus(migration.getPrepareDataStatus());
            freshMigration.setPrepareDataError(migration.getPrepareDataError());
            freshMigration.setCreationStatus(migration.getCreationStatus());
            freshMigration.setCreationError(migration.getCreationError());
            freshMigration.setCreationStep(migration.getCreationStep());
            freshMigration.setAssignmentStatus(migration.getAssignmentStatus());
            freshMigration.setAssignmentError(migration.getAssignmentError());
            freshMigration.setBusinessLogStatus(migration.getBusinessLogStatus());
            freshMigration.setBusinessLogError(migration.getBusinessLogError());
            freshMigration.setCommentStatus(migration.getCommentStatus());
            freshMigration.setCommentError(migration.getCommentError());
            freshMigration.setClosingStatus(migration.getClosingStatus());
            freshMigration.setClosingError(migration.getClosingError());
            freshMigration.setOverallStatus(migration.getOverallStatus());
            
            if ("ERROR".equals(status)) {
                freshMigration.markPhaseError(phase, error);
                freshMigration.incrementRetryCount();
            } else if ("COMPLETED".equals(status)) {
                freshMigration.markPhaseCompleted(phase);
                freshMigration.setRetryCount(0); // Reset retry count on success
            }
            
            migrationRepository.save(freshMigration);
            migrationRepository.flush(); // Ensure immediate persistence
            logger.info("[NEW_TRANSACTION] Successfully updated and committed phase {} status to {} for correspondence: {}", 
                       phase, status, correspondenceGuid);
            
        } catch (Exception e) {
            logger.error("[NEW_TRANSACTION] Error updating phase status for correspondence: {}", correspondenceGuid, e);
            throw new RuntimeException("Failed to update phase status: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets migrations that need processing for a specific phase
     */
    @Transactional(readOnly = true, timeout = 60)
    public List<IncomingCorrespondenceMigration> getMigrationsForPhase(String phase) {
        try {
            List<IncomingCorrespondenceMigration> migrations = migrationRepository.findByCurrentPhase(phase);
            logger.info("Found {} migrations for phase: {}", migrations.size(), phase);
            return migrations;
        } catch (Exception e) {
            logger.error("Error getting migrations for phase: {}", phase, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gets retryable migrations (failed with retry count < max)
     */
    @Transactional(readOnly = true, timeout = 60)
    public List<IncomingCorrespondenceMigration> getRetryableMigrations() {
        try {
            List<IncomingCorrespondenceMigration> migrations = migrationRepository.findRetryableMigrations();
            logger.info("Found {} retryable migrations", migrations.size());
            return migrations;
        } catch (Exception e) {
            logger.error("Error getting retryable migrations", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Creates migration statistics response
     */
    public ImportResponseDto createResponse(String status, String message, int total, int success, int failed, List<String> errors) {
        ImportResponseDto response = new ImportResponseDto();
        response.setStatus(status);
        response.setMessage(message);
        response.setTotalRecords(total);
        response.setSuccessfulImports(success);
        response.setFailedImports(failed);
        response.setErrors(errors != null ? errors : new ArrayList<>());
        return response;
    }
    
    /**
     * Determines final status based on success/failure counts
     */
    public String determineFinalStatus(int successfulImports, int failedImports) {
        if (failedImports == 0) {
            return "SUCCESS";
        } else if (successfulImports > 0) {
            return "PARTIAL_SUCCESS";
        } else {
            return "ERROR";
        }
    }
}