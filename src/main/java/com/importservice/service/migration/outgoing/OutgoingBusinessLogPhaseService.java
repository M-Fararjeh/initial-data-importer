package com.importservice.service.migration.outgoing;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.CorrespondenceTransaction;
import com.importservice.entity.OutgoingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceTransactionRepository;
import com.importservice.repository.OutgoingCorrespondenceMigrationRepository;
import com.importservice.service.migration.incoming.MigrationPhaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for Outgoing Phase 5: Business Log
 * Processes business logic and workflows for outgoing correspondences
 */
@Service
public class OutgoingBusinessLogPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(OutgoingBusinessLogPhaseService.class);
    
    @Autowired
    private CorrespondenceTransactionRepository transactionRepository;
    
    @Autowired
    private OutgoingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private OutgoingDestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    /**
     * Phase 5: Business Log
     * Processes business logic for outgoing correspondences
     */
    @Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto executeBusinessLogPhase() {
        logger.info("Starting Outgoing Phase 5: Business Log");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Get business logs for outgoing correspondences (non-assignment transactions)
            List<CorrespondenceTransaction> businessLogs = getOutgoingBusinessLogsNeedingProcessing();
            
            for (CorrespondenceTransaction businessLog : businessLogs) {
                try {
                    boolean success = processOutgoingBusinessLog(businessLog);
                    if (success) {
                        successfulImports++;
                        businessLog.setMigrateStatus("SUCCESS");
                    } else {
                        failedImports++;
                        businessLog.setMigrateStatus("FAILED");
                        businessLog.setRetryCount(businessLog.getRetryCount() + 1);
                    }
                    transactionRepository.save(businessLog);
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error processing outgoing business log " + businessLog.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    businessLog.setMigrateStatus("FAILED");
                    businessLog.setRetryCount(businessLog.getRetryCount() + 1);
                    transactionRepository.save(businessLog);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Outgoing Phase 5 completed. Processed: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, businessLogs.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Outgoing Phase 5: Business Log", e);
            return phaseService.createResponse("ERROR", "Outgoing Phase 5 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes business log for specific transactions
     */
    @Transactional(readOnly = false, timeout = 300)
    public ImportResponseDto executeBusinessLogForSpecific(List<String> transactionGuids) {
        logger.info("Starting outgoing business log for {} specific transactions", transactionGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String transactionGuid : transactionGuids) {
            try {
                boolean success = processOutgoingBusinessLogForTransaction(transactionGuid);
                
                // Add small delay between transactions to reduce lock contention
                try {
                    Thread.sleep(100); // 100ms delay
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
                if (success) {
                    successfulImports++;
                } else {
                    failedImports++;
                }
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing outgoing transaction " + transactionGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = phaseService.determineFinalStatus(successfulImports, failedImports);
        String message = String.format("Specific outgoing business log completed. Processed: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, transactionGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Gets outgoing business logs that need processing
     */
    private List<CorrespondenceTransaction> getOutgoingBusinessLogsNeedingProcessing() {
        // Use the new method that filters by outgoing correspondence type
        return transactionRepository.findOutgoingBusinessLogsNeedingProcessing();
    }
    
    /**
     * Processes business log for a single transaction
     */
    private boolean processOutgoingBusinessLogForTransaction(String transactionGuid) {
        try {
            Optional<CorrespondenceTransaction> transactionOpt = 
                transactionRepository.findById(transactionGuid);
            
            if (!transactionOpt.isPresent()) {
                logger.error("Outgoing transaction not found: {}", transactionGuid);
                return false;
            }
            
            CorrespondenceTransaction transaction = transactionOpt.get();
            boolean success = processOutgoingBusinessLog(transaction);
            
            if (success) {
                transaction.setMigrateStatus("SUCCESS");
            } else {
                transaction.setMigrateStatus("FAILED");
                transaction.setRetryCount(transaction.getRetryCount() + 1);
            }
            transactionRepository.save(transaction);
            
            return success;
        } catch (Exception e) {
            logger.error("Error processing outgoing business log for: {}", transactionGuid, e);
            return false;
        }
    }
    
    /**
     * Processes a single outgoing business log transaction
     */
    private boolean processOutgoingBusinessLog(CorrespondenceTransaction transaction) {
        try {
            // Get the created document ID from migration record
            Optional<OutgoingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(transaction.getDocGuid());
            
            if (!migrationOpt.isPresent() || migrationOpt.get().getCreatedDocumentId() == null) {
                logger.error("No created document ID found for outgoing correspondence: {}", transaction.getDocGuid());
                return false;
            }
            
            String documentId = migrationOpt.get().getCreatedDocumentId();
            
            // Use actual user name from transaction data
            String fromUser = transaction.getFromUserName() != null ? transaction.getFromUserName() : "itba-emp1";
            
            // Create business log in destination system
            return destinationService.createBusinessLog(
                transaction.getGuid(),
                documentId,
                transaction.getActionDate(),
                transaction.getActionEnglishName(),
                transaction.getNotes(),
                fromUser
            );
            
        } catch (Exception e) {
            logger.error("Error processing outgoing business log: {}", transaction.getGuid(), e);
            return false;
        }
    }
    
    /**
     * Gets outgoing business log migrations with pagination and search
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getOutgoingBusinessLogMigrations(int page, int size, String status, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // Use outgoing-specific business log query methods
            Page<Object[]> businessLogPage;
            if ((status != null && !"all".equals(status)) || (search != null && !search.trim().isEmpty())) {
                String statusParam = "all".equals(status) ? null : status;
                String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();
                businessLogPage = transactionRepository.findOutgoingBusinessLogMigrationsWithSearchAndPagination(
                    statusParam, searchParam, pageable);
            } else {
                businessLogPage = transactionRepository.findOutgoingBusinessLogMigrationsWithPagination(pageable);
            }
            
            List<Map<String, Object>> businessLogs = new ArrayList<>();
            for (Object[] row : businessLogPage.getContent()) {
                Map<String, Object> businessLog = new HashMap<>();
                businessLog.put("transactionGuid", row[0]);
                businessLog.put("correspondenceGuid", row[1]);
                businessLog.put("actionId", row[2] != null ? ((Number) row[2]).intValue() : null);
                businessLog.put("actionEnglishName", row[3]);
                businessLog.put("actionLocalName", row[4]);
                businessLog.put("actionDate", row[5] != null ? ((Timestamp) row[5]).toLocalDateTime() : null);
                businessLog.put("fromUserName", row[6]);
                businessLog.put("notes", row[7]);
                businessLog.put("migrateStatus", row[8]);
                businessLog.put("retryCount", row[9] != null ? ((Number) row[9]).intValue() : 0);
                businessLog.put("lastModifiedDate", row[10] != null ? ((Timestamp) row[10]).toLocalDateTime() : null);
                businessLog.put("correspondenceSubject", row[11]);
                businessLog.put("correspondenceReferenceNo", row[12]);
                businessLog.put("createdDocumentId", row[13]);
                businessLogs.add(businessLog);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", businessLogs);
            result.put("totalElements", businessLogPage.getTotalElements());
            result.put("totalPages", businessLogPage.getTotalPages());
            result.put("currentPage", businessLogPage.getNumber());
            result.put("pageSize", businessLogPage.getSize());
            result.put("hasNext", businessLogPage.hasNext());
            result.put("hasPrevious", businessLogPage.hasPrevious());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting outgoing business log migrations", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("content", new ArrayList<>());
            errorResult.put("totalElements", 0L);
            errorResult.put("totalPages", 0);
            errorResult.put("currentPage", page);
            errorResult.put("pageSize", size);
            errorResult.put("hasNext", false);
            errorResult.put("hasPrevious", false);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }
}