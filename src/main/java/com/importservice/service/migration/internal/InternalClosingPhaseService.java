package com.importservice.service.migration.internal;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.InternalCorrespondenceMigration;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.InternalCorrespondenceMigrationRepository;
import com.importservice.service.InternalDestinationSystemService;
import com.importservice.service.migration.MigrationPhaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for Internal Phase 6: Closing
 * Closes internal correspondences that need to be archived
 */
@Service
public class InternalClosingPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(InternalClosingPhaseService.class);
    
    @Autowired
    private InternalCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @Autowired
    private InternalDestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    /**
     * Phase 6: Closing
     * Closes internal correspondences that need to be closed
     */
    @Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto executeClosingPhase() {
        logger.info("Starting Internal Phase 6: Closing");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Only get internal correspondences that need to be closed (isNeedToClose = true)
            List<InternalCorrespondenceMigration> migrations = 
                migrationRepository.findByIsNeedToCloseAndCreatedDocumentIdIsNotNull(true);
            
            logger.info("Found {} internal correspondences that need to be closed", migrations.size());
            
            for (InternalCorrespondenceMigration migration : migrations) {
                try {
                    // Double-check that this correspondence actually needs to be closed
                    if (!migration.getIsNeedToClose()) {
                        logger.debug("Skipping correspondence {} - does not need to be closed", migration.getCorrespondenceGuid());
                        continue;
                    }
                    
                    boolean success = processInternalClosing(migration);
                    if (success) {
                        successfulImports++;
                        migration.setClosingStatus("COMPLETED");
                        // Status is already updated in the individual transaction method
                    } else {
                        failedImports++;
                        migration.setClosingStatus("FAILED");
                        migration.setRetryCount(migration.getRetryCount() + 1);
                        // Status is already updated in the individual transaction method
                    }
                    migrationRepository.save(migration);
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error processing internal closing " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    migration.setClosingStatus("FAILED");
                    migration.setClosingError(errorMsg);
                    migration.setRetryCount(migration.getRetryCount() + 1);
                    migrationRepository.save(migration);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Internal Phase 6 completed. Closed: %d, Failed: %d (Only processing correspondences with isNeedToClose = true)", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, migrations.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Internal Phase 6: Closing", e);
            return phaseService.createResponse("ERROR", "Internal Phase 6 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes closing for specific correspondences
     */
    public ImportResponseDto executeClosingForSpecific(List<String> correspondenceGuids) {
        logger.info("Starting internal closing for {} specific correspondences", correspondenceGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        // Process each closing in its own transaction for immediate status updates
        for (int i = 0; i < correspondenceGuids.size(); i++) {
            String correspondenceGuid = correspondenceGuids.get(i);
            try {
                logger.info("Processing internal closing: {} ({}/{})", 
                           correspondenceGuid, i + 1, correspondenceGuids.size());
                
                // Process in separate transaction for immediate status update
                boolean success = processInternalClosingInNewTransaction(correspondenceGuid);
                
                if (success) {
                    logger.info("Successfully completed internal closing for correspondence: {}", correspondenceGuid);
                    logger.info("Successfully completed internal closing for correspondence: {}", correspondenceGuid);
                } else {
                    failedImports++;
                    logger.warn("Failed to complete internal closing for correspondence: {}", correspondenceGuid);
                }
                
                // Add delay between closings to reduce system load
                if (i < correspondenceGuids.size() - 1) {
                    try {
                        Thread.sleep(200); // 200ms delay between closings
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("Thread interrupted during processing delay");
                        break;
                    }
                }
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing internal closing " + correspondenceGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = phaseService.determineFinalStatus(successfulImports, failedImports);
        String message = String.format("Specific internal closing completed. Closed: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, correspondenceGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Processes closing for a single correspondence in a new transaction for immediate status updates
     */
    @Transactional(readOnly = false, timeout = 60)
    public boolean processInternalClosingInNewTransaction(String correspondenceGuid) {
        try {
            logger.debug("Starting new transaction for internal closing: {}", correspondenceGuid);
            
            Optional<InternalCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (!migrationOpt.isPresent()) {
                logger.error("Internal migration record not found: {}", correspondenceGuid);
                return false;
            }
            
            InternalCorrespondenceMigration migration = migrationOpt.get();
            
            // Strict check: only process if isNeedToClose is explicitly true
            if (!migration.getIsNeedToClose()) {
                logger.info("Internal correspondence {} does not need to be closed (isNeedToClose = false), marking as success", correspondenceGuid);
                // Mark as completed since no action is needed
                migration.setClosingStatus("COMPLETED");
                updateInternalPhaseStatus(migration, "CLOSING", "COMPLETED", null);
                return true;
            }
            
            // Mark as in progress immediately
            migration.setClosingStatus("IN_PROGRESS");
            migration.setLastModifiedDate(LocalDateTime.now());
            migrationRepository.save(migration);
            
            logger.info("Processing internal correspondence {} for closing (isNeedToClose = true)", correspondenceGuid);
            
            // Process the closing
            boolean result = processInternalClosing(migration);
            
            // Update final status immediately
            if (result) {
                migration.setClosingStatus("COMPLETED");
                updateInternalPhaseStatus(migration, "CLOSING", "COMPLETED", null);
                logger.info("Successfully closed internal correspondence: {}", correspondenceGuid);
            } else {
                migration.setClosingStatus("FAILED");
                migration.setRetryCount(migration.getRetryCount() + 1);
                migration.setLastErrorAt(LocalDateTime.now());
                updateInternalPhaseStatus(migration, "CLOSING", "ERROR", "Closing process failed");
                logger.warn("Failed to close internal correspondence: {}", correspondenceGuid);
            }
            
            migrationRepository.save(migration);
            
            logger.debug("Completed internal closing transaction for: {} with result: {}", 
                        correspondenceGuid, result);
            return result;
            
        } catch (Exception e) {
            logger.error("Error in internal closing transaction for: {}", correspondenceGuid, e);
            
            // Update error status in separate try-catch to ensure it gets saved
            try {
                Optional<InternalCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
                if (migrationOpt.isPresent()) {
                    InternalCorrespondenceMigration migration = migrationOpt.get();
                    migration.setClosingStatus("FAILED");
                    migration.setClosingError("Transaction failed: " + e.getMessage());
                    migration.setRetryCount(migration.getRetryCount() + 1);
                    logger.warn("Failed to complete internal closing for correspondence: {}", correspondenceGuid);
                    migrationRepository.save(migration);
                }
            } catch (Exception statusError) {
                logger.error("Error updating error status for correspondence: {}", correspondenceGuid, statusError);
            }
            
            return false;
        }
    }
    
    /**
     * Processes closing for a single correspondence
     * @deprecated Use processInternalClosingInNewTransaction for better transaction handling
     */
    @Deprecated
    private boolean processInternalClosingForCorrespondence(String correspondenceGuid) {
        try {
            Optional<InternalCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (!migrationOpt.isPresent()) {
                logger.error("Internal migration record not found: {}", correspondenceGuid);
                return false;
            }
            
            InternalCorrespondenceMigration migration = migrationOpt.get();
            
            // Strict check: only process if isNeedToClose is explicitly true (1)
            if (!migration.getIsNeedToClose()) {
                logger.info("Internal correspondence {} does not need to be closed (isNeedToClose = false), skipping", correspondenceGuid);
                return true; // Count as success since no action needed
            }
            
            logger.info("Processing internal correspondence {} for closing (isNeedToClose = true)", correspondenceGuid);
            
            boolean success = processInternalClosing(migration);
            if (success) {
                migration.setClosingStatus("COMPLETED");
                logger.info("Successfully closed internal correspondence: {}", correspondenceGuid);
            } else {
                migration.setClosingStatus("FAILED");
                migration.setRetryCount(migration.getRetryCount() + 1);
                logger.warn("Failed to close internal correspondence: {}", correspondenceGuid);
            }
            migrationRepository.save(migration);
            
            return success;
        } catch (Exception e) {
            logger.error("Error processing internal closing for: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Processes closing for a single internal correspondence
     */
    private boolean processInternalClosing(InternalCorrespondenceMigration migration) {
        try {
            if (migration.getCreatedDocumentId() == null) {
                logger.error("No created document ID found for internal correspondence: {}", migration.getCorrespondenceGuid());
                return false;
            }
            
            // Verify again that this correspondence needs to be closed
            if (!migration.getIsNeedToClose()) {
                logger.warn("Internal correspondence {} marked for closing but isNeedToClose = false", migration.getCorrespondenceGuid());
                return false;
            }
            
            // Get the correspondence to extract the creation user
            Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(migration.getCorrespondenceGuid());
            String asUser = "itba-emp1"; // Default fallback
            if (correspondenceOpt.isPresent() && correspondenceOpt.get().getCreationUserName() != null) {
                asUser = correspondenceOpt.get().getCreationUserName();
            }
            
            logger.info("Closing internal correspondence {} in destination system with user: {}", 
                       migration.getCorrespondenceGuid(), asUser);
            
            // Close internal correspondence in destination system
            return destinationService.closeInternalCorrespondence(
                migration.getCorrespondenceGuid(),
                migration.getCreatedDocumentId(),
                asUser,
                LocalDateTime.now()
            );
            
        } catch (Exception e) {
            logger.error("Error processing internal closing: {}", migration.getCorrespondenceGuid(), e);
            return false;
        }
    }
    
    /**
     * Updates internal phase status - helper method for migration entity updates
     */
    private void updateInternalPhaseStatus(String correspondenceGuid, String phase, String status, String error) {
        try {
            Optional<InternalCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (migrationOpt.isPresent()) {
                InternalCorrespondenceMigration migration = migrationOpt.get();
                
                if ("ERROR".equals(status)) {
                    migration.markPhaseError(phase, error);
                    migration.incrementRetryCount();
                } else if ("COMPLETED".equals(status)) {
                    migration.markPhaseCompleted(phase);
                    migration.setRetryCount(0); // Reset retry count on success
                }
                
                migrationRepository.save(migration);
            }
        } catch (Exception e) {
            logger.error("Error updating internal phase status: {}", e.getMessage());
        }
    }
    
    /**
     * Gets internal closing migrations with pagination and search
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getInternalClosingMigrations(int page, int size, String status, String needToClose, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Object[]> closingPage;
            if ((status != null && !"all".equals(status)) || 
                (needToClose != null && !"all".equals(needToClose)) || 
                (search != null && !search.trim().isEmpty())) {
                
                String statusParam = "all".equals(status) ? null : status;
                Boolean needToCloseParam = "all".equals(needToClose) ? null : Boolean.valueOf(needToClose);
                String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();
                
                closingPage = migrationRepository.findClosingMigrationsWithSearchAndPagination(
                    statusParam, needToCloseParam, searchParam, pageable);
            } else {
                closingPage = migrationRepository.findClosingMigrationsWithPagination(pageable);
            }
            
            List<Map<String, Object>> closings = new ArrayList<>();
            for (Object[] row : closingPage.getContent()) {
                Map<String, Object> closing = new HashMap<>();
                closing.put("id", row[0] != null ? ((Number) row[0]).longValue() : null);
                closing.put("correspondenceGuid", row[1]);
                closing.put("isNeedToClose", row[2] != null ? (Boolean) row[2] : false);
                closing.put("closingStatus", row[3]);
                closing.put("closingError", row[4]);
                closing.put("createdDocumentId", row[5]);
                closing.put("retryCount", row[6] != null ? ((Number) row[6]).intValue() : 0);
                closing.put("lastModifiedDate", row[7] != null ? ((Timestamp) row[7]).toLocalDateTime() : null);
                closing.put("correspondenceSubject", row[8]);
                closing.put("correspondenceReferenceNo", row[9]);
                closing.put("correspondenceLastModifiedDate", row[10] != null ? ((Timestamp) row[10]).toLocalDateTime() : null);
                closing.put("creationUserName", row[11]);
                closings.add(closing);
            }
            
            // Count records that need to be closed
            Long needToCloseCount = migrationRepository.countByIsNeedToClose(true);
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", closings);
            result.put("totalElements", closingPage.getTotalElements());
            result.put("totalPages", closingPage.getTotalPages());
            result.put("currentPage", closingPage.getNumber());
            result.put("pageSize", closingPage.getSize());
            result.put("hasNext", closingPage.hasNext());
            result.put("hasPrevious", closingPage.hasPrevious());
            result.put("needToCloseCount", needToCloseCount);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting internal closing migrations", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("content", new ArrayList<>());
            errorResult.put("totalElements", 0L);
            errorResult.put("totalPages", 0);
            errorResult.put("currentPage", page);
            errorResult.put("pageSize", size);
            errorResult.put("hasNext", false);
            errorResult.put("hasPrevious", false);
            errorResult.put("needToCloseCount", 0L);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }
}