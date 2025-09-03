package com.importservice.service.migration.outgoing;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.OutgoingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.OutgoingCorrespondenceMigrationRepository;
import com.importservice.service.OutgoingDestinationSystemService;
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
 * Service for Outgoing Phase 7: Closing
 * Closes outgoing correspondences that need to be archived
 */
@Service
public class OutgoingClosingPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(OutgoingClosingPhaseService.class);
    
    @Autowired
    private OutgoingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @Autowired
    private OutgoingDestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    /**
     * Phase 7: Closing
     * Closes outgoing correspondences that need to be closed
     */
    @Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto executeClosingPhase() {
        logger.info("Starting Outgoing Phase 7: Closing");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<OutgoingCorrespondenceMigration> migrations = 
                migrationRepository.findByIsNeedToCloseAndCreatedDocumentIdIsNotNull(true);
            
            for (OutgoingCorrespondenceMigration migration : migrations) {
                try {
                    boolean success = processOutgoingClosing(migration);
                    if (success) {
                        successfulImports++;
                        migration.setClosingStatus("COMPLETED");
                        updateOutgoingPhaseStatus(migration.getCorrespondenceGuid(), "CLOSING", "COMPLETED", null);
                    } else {
                        failedImports++;
                        migration.setClosingStatus("FAILED");
                        migration.setRetryCount(migration.getRetryCount() + 1);
                        updateOutgoingPhaseStatus(migration.getCorrespondenceGuid(), "CLOSING", "ERROR", 
                                                "Closing process failed");
                    }
                    migrationRepository.save(migration);
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error processing outgoing closing " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    migration.setClosingStatus("FAILED");
                    migration.setClosingError(errorMsg);
                    migration.setRetryCount(migration.getRetryCount() + 1);
                    migrationRepository.save(migration);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Outgoing Phase 7 completed. Closed: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, migrations.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Outgoing Phase 7: Closing", e);
            return phaseService.createResponse("ERROR", "Outgoing Phase 7 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes closing for specific correspondences
     */
    @Transactional(readOnly = false, timeout = 300)
    public ImportResponseDto executeClosingForSpecific(List<String> correspondenceGuids) {
        logger.info("Starting outgoing closing for {} specific correspondences", correspondenceGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String correspondenceGuid : correspondenceGuids) {
            try {
                boolean success = processOutgoingClosingForCorrespondence(correspondenceGuid);
                
                // Add small delay between correspondences to reduce lock contention
                try {
                    Thread.sleep(100); // 100ms delay
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
                if (success) {
                    successfulImports++;
                    updateOutgoingPhaseStatus(correspondenceGuid, "CLOSING", "COMPLETED", null);
                } else {
                    failedImports++;
                    updateOutgoingPhaseStatus(correspondenceGuid, "CLOSING", "ERROR", "Closing process failed");
                }
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing outgoing closing " + correspondenceGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = phaseService.determineFinalStatus(successfulImports, failedImports);
        String message = String.format("Specific outgoing closing completed. Closed: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, correspondenceGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Processes closing for a single correspondence
     */
    private boolean processOutgoingClosingForCorrespondence(String correspondenceGuid) {
        try {
            Optional<OutgoingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (!migrationOpt.isPresent()) {
                logger.error("Outgoing migration record not found: {}", correspondenceGuid);
                return false;
            }
            
            OutgoingCorrespondenceMigration migration = migrationOpt.get();
            
            if (!migration.getIsNeedToClose()) {
                logger.info("Outgoing correspondence {} does not need to be closed, skipping", correspondenceGuid);
                return true; // Count as success since no action needed
            }
            
            boolean success = processOutgoingClosing(migration);
            if (success) {
                migration.setClosingStatus("COMPLETED");
            } else {
                migration.setClosingStatus("FAILED");
                migration.setRetryCount(migration.getRetryCount() + 1);
            }
            migrationRepository.save(migration);
            
            return success;
        } catch (Exception e) {
            logger.error("Error processing outgoing closing for: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Processes closing for a single outgoing correspondence
     */
    private boolean processOutgoingClosing(OutgoingCorrespondenceMigration migration) {
        try {
            if (migration.getCreatedDocumentId() == null) {
                logger.error("No created document ID found for outgoing correspondence: {}", migration.getCorrespondenceGuid());
                return false;
            }
            
            // Get the correspondence to extract the creation user
            Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(migration.getCorrespondenceGuid());
            String asUser = "itba-emp1"; // Default fallback
            if (correspondenceOpt.isPresent() && correspondenceOpt.get().getCreationUserName() != null) {
                asUser = correspondenceOpt.get().getCreationUserName();
            }
            
            // Close outgoing correspondence in destination system
            return destinationService.closeOutgoingCorrespondence(
                migration.getCorrespondenceGuid(),
                migration.getCreatedDocumentId(),
                asUser,
                LocalDateTime.now()
            );
            
        } catch (Exception e) {
            logger.error("Error processing outgoing closing: {}", migration.getCorrespondenceGuid(), e);
            return false;
        }
    }
    
    /**
     * Updates outgoing phase status
     */
    private void updateOutgoingPhaseStatus(String correspondenceGuid, String phase, String status, String error) {
        try {
            Optional<OutgoingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (migrationOpt.isPresent()) {
                OutgoingCorrespondenceMigration migration = migrationOpt.get();
                
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
            logger.error("Error updating outgoing phase status: {}", e.getMessage());
        }
    }
    
    /**
     * Gets outgoing closing migrations with pagination and search
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getOutgoingClosingMigrations(int page, int size, String status, String needToClose, String search) {
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
            logger.error("Error getting outgoing closing migrations", e);
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