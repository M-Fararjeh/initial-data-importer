package com.importservice.service.migration;

import java.math.BigInteger;
import java.util.Arrays;
import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.CorrespondenceTransaction;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceTransactionRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for Phase 4: Business Log
 * Processes business logic and workflows
 */
@Service
public class BusinessLogPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(BusinessLogPhaseService.class);
    
    @Autowired
    private CorrespondenceTransactionRepository transactionRepository;
    
    @Autowired
    private IncomingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private DestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    /**
     * Phase 4: Business Log
     * Processes business logic for correspondences
     */
    @Transactional
    public ImportResponseDto executeBusinessLogPhase() {
        logger.info("Starting Phase 4: Business Log");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<CorrespondenceTransaction> businessLogs = transactionRepository.findBusinessLogsNeedingProcessing();
            
            for (CorrespondenceTransaction businessLog : businessLogs) {
                try {
                    boolean success = processBusinessLog(businessLog);
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
                    String errorMsg = "Error processing business log " + businessLog.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    businessLog.setMigrateStatus("FAILED");
                    businessLog.setRetryCount(businessLog.getRetryCount() + 1);
                    transactionRepository.save(businessLog);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Phase 4 completed. Processed: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, businessLogs.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 4: Business Log", e);
            return phaseService.createResponse("ERROR", "Phase 4 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes business log for specific transactions
     */
    @Transactional(timeout = 300)
    public ImportResponseDto executeBusinessLogForSpecific(List<String> transactionGuids) {
        logger.info("Starting business log for {} specific transactions", transactionGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String transactionGuid : transactionGuids) {
            try {
                boolean success = processBusinessLogForTransaction(transactionGuid);
                
                if (success) {
                    successfulImports++;
                } else {
                    failedImports++;
                }
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing transaction " + transactionGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = phaseService.determineFinalStatus(successfulImports, failedImports);
        String message = String.format("Specific business log completed. Processed: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, transactionGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Processes business log for a single transaction
     */
    private boolean processBusinessLogForTransaction(String transactionGuid) {
        try {
            Optional<CorrespondenceTransaction> transactionOpt = 
                transactionRepository.findById(transactionGuid);
            
            if (!transactionOpt.isPresent()) {
                logger.error("Transaction not found: {}", transactionGuid);
                return false;
            }
            
            CorrespondenceTransaction transaction = transactionOpt.get();
            boolean success = processBusinessLog(transaction);
            
            if (success) {
                transaction.setMigrateStatus("SUCCESS");
            } else {
                transaction.setMigrateStatus("FAILED");
                transaction.setRetryCount(transaction.getRetryCount() + 1);
            }
            transactionRepository.save(transaction);
            
            return success;
        } catch (Exception e) {
            logger.error("Error processing business log for: {}", transactionGuid, e);
            return false;
        }
    }
    
    /**
     * Processes a single business log transaction
     */
    private boolean processBusinessLog(CorrespondenceTransaction transaction) {
        try {
            // Get the created document ID from migration record
            Optional<IncomingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(transaction.getDocGuid());
            
            if (!migrationOpt.isPresent() || migrationOpt.get().getCreatedDocumentId() == null) {
                logger.error("No created document ID found for correspondence: {}", transaction.getDocGuid());
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
            logger.error("Error processing business log: {}", transaction.getGuid(), e);
            return false;
        }
    }
    
    /**
     * Gets business log migrations with pagination and search
     */
    public Map<String, Object> getBusinessLogMigrations(int page, int size, String status, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Object[]> businessLogPage;
            if ((status != null && !"all".equals(status)) || (search != null && !search.trim().isEmpty())) {
                String statusParam = "all".equals(status) ? null : status;
                String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();
                businessLogPage = transactionRepository.findBusinessLogMigrationsWithSearchAndPagination(
                    statusParam, searchParam, pageable);
            } else {
                businessLogPage = transactionRepository.findBusinessLogMigrationsWithPagination(pageable);
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
            logger.error("Error getting business log migrations", e);
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