package com.importservice.service.migration;

import java.math.BigInteger;
import java.util.Arrays;
import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.entity.Correspondence;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
import com.importservice.service.DestinationSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for Phase 6: Closing
 * Closes correspondences that need to be archived
 */
@Service
public class ClosingPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClosingPhaseService.class);
    
    @Autowired
    private IncomingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @Autowired
    private DestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    /**
     * Phase 6: Closing
     * Closes correspondences that need to be closed
     */
    @Transactional
    public ImportResponseDto executeClosingPhase() {
        logger.info("Starting Phase 6: Closing");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<IncomingCorrespondenceMigration> migrations = 
                migrationRepository.findByIsNeedToCloseAndCreatedDocumentIdIsNotNull(true);
            
            for (IncomingCorrespondenceMigration migration : migrations) {
                try {
                    boolean success = processClosing(migration);
                    if (success) {
                        successfulImports++;
                        migration.setClosingStatus("COMPLETED");
                        phaseService.updatePhaseStatus(migration.getCorrespondenceGuid(), "CLOSING", "COMPLETED", null);
                    } else {
                        failedImports++;
                        migration.setClosingStatus("FAILED");
                        migration.setRetryCount(migration.getRetryCount() + 1);
                        phaseService.updatePhaseStatus(migration.getCorrespondenceGuid(), "CLOSING", "ERROR", 
                                                     "Closing process failed");
                    }
                    migrationRepository.save(migration);
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error processing closing " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    migration.setClosingStatus("FAILED");
                    migration.setClosingError(errorMsg);
                    migration.setRetryCount(migration.getRetryCount() + 1);
                    migrationRepository.save(migration);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Phase 6 completed. Closed: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, migrations.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 6: Closing", e);
            return phaseService.createResponse("ERROR", "Phase 6 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes closing for specific correspondences
     */
    @Transactional
    public ImportResponseDto executeClosingForSpecific(List<String> correspondenceGuids) {
        logger.info("Starting closing for {} specific correspondences", correspondenceGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String correspondenceGuid : correspondenceGuids) {
            try {
                Optional<IncomingCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
                
                if (!migrationOpt.isPresent()) {
                    failedImports++;
                    errors.add("Migration record not found: " + correspondenceGuid);
                    continue;
                }
                
                IncomingCorrespondenceMigration migration = migrationOpt.get();
                
                if (!migration.getIsNeedToClose()) {
                    logger.info("Correspondence {} does not need to be closed, skipping", correspondenceGuid);
                    successfulImports++; // Count as success since no action needed
                    continue;
                }
                
                boolean success = processClosing(migration);
                if (success) {
                    successfulImports++;
                    migration.setClosingStatus("COMPLETED");
                    phaseService.updatePhaseStatus(correspondenceGuid, "CLOSING", "COMPLETED", null);
                } else {
                    failedImports++;
                    migration.setClosingStatus("FAILED");
                    migration.setRetryCount(migration.getRetryCount() + 1);
                    phaseService.updatePhaseStatus(correspondenceGuid, "CLOSING", "ERROR", "Closing process failed");
                }
                migrationRepository.save(migration);
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing closing " + correspondenceGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = phaseService.determineFinalStatus(successfulImports, failedImports);
        String message = String.format("Specific closing completed. Closed: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, correspondenceGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Processes closing for a single correspondence
     */
    private boolean processClosing(IncomingCorrespondenceMigration migration) {
        try {
            if (migration.getCreatedDocumentId() == null) {
                logger.error("No created document ID found for correspondence: {}", migration.getCorrespondenceGuid());
                return false;
            }
            
            // Get the correspondence to extract the creation user
            Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(migration.getCorrespondenceGuid());
            String asUser = "itba-emp1"; // Default fallback
            if (correspondenceOpt.isPresent() && correspondenceOpt.get().getCreationUserName() != null) {
                asUser = correspondenceOpt.get().getCreationUserName();
            }
            
            // Close correspondence in destination system
            return destinationService.closeCorrespondence(
                migration.getCorrespondenceGuid(),
                migration.getCreatedDocumentId(),
                asUser,
                LocalDateTime.now()
            );
            
        } catch (Exception e) {
            logger.error("Error processing closing: {}", migration.getCorrespondenceGuid(), e);
            return false;
        }
    }
    
    /**
     * Gets closing migrations with pagination and search
     */
    public Map<String, Object> getClosingMigrations(int page, int size, String status, String needToClose, String search) {
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
                closing.put("id", row[0] != null ? ((BigInteger) row[0]).longValue() : null);
                closing.put("correspondenceGuid", row[1]);
                closing.put("isNeedToClose", row[2] != null ? (Boolean) row[2] : false);
                closing.put("closingStatus", row[3]);
                closing.put("closingError", row[4]);
                closing.put("createdDocumentId", row[5]);
                closing.put("retryCount", row[6] != null ? ((BigInteger) row[6]).intValue() : 0);
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
            logger.error("Error getting closing migrations", e);
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