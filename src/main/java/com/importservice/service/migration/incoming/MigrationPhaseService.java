package com.importservice.service.migration.incoming;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    @Transactional(readOnly = false, timeout = 15, isolation = Isolation.READ_COMMITTED)
    public void updatePhaseStatus(String correspondenceGuid, String phase, String status, String error) {
        try {
            updatePhaseStatusInternal(correspondenceGuid, phase, status, error);
        } catch (Exception e) {
            logger.warn("Failed to update phase status for correspondence: {} - {}", correspondenceGuid, e.getMessage());
            // Don't throw exception to prevent cascading failures
        }
    }
    
    /**
     * Internal method for updating phase status
     */
    private void updatePhaseStatusInternal(String correspondenceGuid, String phase, String status, String error) {
        try {
            logger.info("Updating phase status for correspondence: {} - Phase: {}, Status: {}", 
                       correspondenceGuid, phase, status);
            
            IncomingCorrespondenceMigration migration = migrationRepository
                .findByCorrespondenceGuid(correspondenceGuid)
                .orElse(null);
            
            if (migration == null) {
                logger.warn("Migration record not found for correspondence: {}", correspondenceGuid);
                return;
            }
            
            if ("ERROR".equals(status)) {
                migration.markPhaseError(phase, error);
                migration.incrementRetryCount();
            } else if ("COMPLETED".equals(status)) {
                migration.markPhaseCompleted(phase);
                migration.setRetryCount(0); // Reset retry count on success
            }
            
            migrationRepository.save(migration);
            logger.info("Successfully updated phase {} status to {} for correspondence: {}", 
                       phase, status, correspondenceGuid);
            
        } catch (Exception e) {
            logger.error("Error updating phase status for correspondence: {}", correspondenceGuid, e);
            // Don't throw exception to prevent cascading failures
        }
    }
    
    /**
     * Gets migrations that need processing for a specific phase
     */
    //@Transactional(readOnly = true, timeout = 60)
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