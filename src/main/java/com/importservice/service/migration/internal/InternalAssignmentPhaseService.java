package com.importservice.service.migration.internal;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.CorrespondenceTransaction;
import com.importservice.entity.InternalCorrespondenceMigration;
import com.importservice.repository.CorrespondenceTransactionRepository;
import com.importservice.repository.InternalCorrespondenceMigrationRepository;
import com.importservice.service.InternalDestinationSystemService;
import com.importservice.service.migration.MigrationPhaseService;
import com.importservice.util.DepartmentUtils;
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
 * Service for Internal Phase 3: Assignment
 * Assigns internal correspondences to users and departments
 */
@Service
public class InternalAssignmentPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(InternalAssignmentPhaseService.class);
    
    @Autowired
    private CorrespondenceTransactionRepository transactionRepository;
    
    @Autowired
    private InternalCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private InternalDestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    @Autowired
    private DepartmentUtils departmentUtils;
    
    /**
     * Phase 3: Assignment
     * Assigns internal correspondences to users and departments
     */
    @Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto executeAssignmentPhase() {
        logger.info("Starting Internal Phase 3: Assignment");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Get internal correspondence assignments (action_id = 12 for internal correspondences)
            List<CorrespondenceTransaction> assignments = getInternalAssignmentsNeedingProcessing();
            
            for (CorrespondenceTransaction assignment : assignments) {
                try {
                    boolean success = processInternalAssignment(assignment);
                    if (success) {
                        successfulImports++;
                        assignment.setMigrateStatus("SUCCESS");
                    } else {
                        failedImports++;
                        assignment.setMigrateStatus("FAILED");
                        assignment.setRetryCount(assignment.getRetryCount() + 1);
                    }
                    transactionRepository.save(assignment);
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error processing internal assignment " + assignment.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    assignment.setMigrateStatus("FAILED");
                    assignment.setRetryCount(assignment.getRetryCount() + 1);
                    transactionRepository.save(assignment);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Internal Phase 3 completed. Assigned: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, assignments.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Internal Phase 3: Assignment", e);
            return phaseService.createResponse("ERROR", "Internal Phase 3 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes assignment for specific transactions
     */
    public ImportResponseDto executeAssignmentForSpecific(List<String> transactionGuids) {
        logger.info("Starting internal assignment for {} specific transactions", transactionGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        List<String> successfulCorrespondenceGuids = new ArrayList<>();
        
        // Process each assignment in its own transaction for immediate status updates
        for (int i = 0; i < transactionGuids.size(); i++) {
            String transactionGuid = transactionGuids.get(i);
            try {
                logger.info("Processing internal assignment: {} ({}/{})", 
                           transactionGuid, i + 1, transactionGuids.size());
                
                // Process in separate transaction for immediate status update
                ProcessResult result = processInternalAssignmentInNewTransaction(transactionGuid);
                
                if (result.success) {
                    successfulImports++;
                    if (result.correspondenceGuid != null) {
                        successfulCorrespondenceGuids.add(result.correspondenceGuid);
                    }
                    logger.info("Successfully completed internal assignment for transaction: {}", transactionGuid);
                } else {
                    failedImports++;
                    logger.warn("Failed to complete internal assignment for transaction: {}", transactionGuid);
                }
                
                // Add delay between assignments to reduce system load
                if (i < transactionGuids.size() - 1) {
                    try {
                        Thread.sleep(150); // 150ms delay between assignments
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("Thread interrupted during processing delay");
                        break;
                    }
                }
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing internal transaction " + transactionGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        // After all individual transactions are committed, check assignment completion status
        // This runs in separate transactions to see the committed changes
        for (String correspondenceGuid : successfulCorrespondenceGuids) {
            try {
                checkAndUpdateAssignmentStatusInSeparateTransaction(correspondenceGuid);
            } catch (Exception e) {
                logger.warn("Error checking assignment completion status for correspondence: {}", correspondenceGuid, e);
                // Don't fail the overall process for status check errors
            }
        }
        
        String status = phaseService.determineFinalStatus(successfulImports, failedImports);
        String message = String.format("Specific internal assignment completed. Assigned: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, transactionGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Helper class to return both success status and correspondence GUID
     */
    private static class ProcessResult {
        final boolean success;
        final String correspondenceGuid;
        
        ProcessResult(boolean success, String correspondenceGuid) {
            this.success = success;
            this.correspondenceGuid = correspondenceGuid;
        }
    }
    
    /**
     * Processes assignment for a single transaction in a new transaction for immediate status updates
     */
    @Transactional(readOnly = false, timeout = 60)
    public ProcessResult processInternalAssignmentInNewTransaction(String transactionGuid) {
        try {
            logger.debug("Starting new transaction for internal assignment: {}", transactionGuid);
            
            Optional<CorrespondenceTransaction> transactionOpt = 
                transactionRepository.findById(transactionGuid);
            
            if (!transactionOpt.isPresent()) {
                logger.error("Internal transaction not found: {}", transactionGuid);
                return new ProcessResult(false, null);
            }
            
            CorrespondenceTransaction transaction = transactionOpt.get();
            String correspondenceGuid = transaction.getDocGuid();
            
            // Mark as in progress immediately
            transaction.setMigrateStatus("IN_PROGRESS");
            transaction.setLastModifiedDate(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            // Process the assignment
            boolean result = processInternalAssignment(transaction);
            
            // Update final status immediately
            if (result) {
                transaction.setMigrateStatus("SUCCESS");
                transaction.setRetryCount(0); // Reset retry count on success
                logger.info("Successfully processed internal assignment: {}", transactionGuid);
            } else {
                transaction.setMigrateStatus("FAILED");
                transaction.setRetryCount(transaction.getRetryCount() + 1);
                logger.warn("Failed to process internal assignment: {}", transactionGuid);
            }
            
            transaction.setLastModifiedDate(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            logger.debug("Completed internal assignment transaction for: {} with result: {}", 
                        transactionGuid, result);
            return new ProcessResult(result, correspondenceGuid);
            
        } catch (Exception e) {
            logger.error("Error in internal assignment transaction for: {}", transactionGuid, e);
            
            // Update error status in separate try-catch to ensure it gets saved
            String correspondenceGuid = null;
            try {
                Optional<CorrespondenceTransaction> transactionOpt = 
                    transactionRepository.findById(transactionGuid);
                if (transactionOpt.isPresent()) {
                    CorrespondenceTransaction transaction = transactionOpt.get();
                    correspondenceGuid = transaction.getDocGuid();
                    transaction.setMigrateStatus("FAILED");
                    transaction.setRetryCount(transaction.getRetryCount() + 1);
                    transaction.setLastModifiedDate(LocalDateTime.now());
                    transactionRepository.save(transaction);
                }
            } catch (Exception statusError) {
                logger.error("Error updating error status for transaction: {}", transactionGuid, statusError);
            }
            
            return new ProcessResult(false, correspondenceGuid);
        }
    }
    
    /**
     * Checks and updates assignment status in a separate transaction
     * This ensures we see the committed changes from individual assignment transactions
     */
    @Transactional(readOnly = false, timeout = 30)
    public void checkAndUpdateAssignmentStatusInSeparateTransaction(String correspondenceGuid) {
        try {
            logger.debug("Checking assignment completion status for internal correspondence in separate transaction: {}", correspondenceGuid);
            
            // Get all assignment transactions for this correspondence (action_id = 12)
            List<CorrespondenceTransaction> assignmentTransactions = transactionRepository
                .findByDocGuidAndActionId(correspondenceGuid, 12);
            
            if (assignmentTransactions.isEmpty()) {
                logger.debug("No assignment transactions found for correspondence: {}", correspondenceGuid);
                return;
            }
            
            // Check if all assignment transactions are successfully migrated
            boolean allTransactionsMigrated = assignmentTransactions.stream()
                .allMatch(transaction -> "SUCCESS".equals(transaction.getMigrateStatus()));
            
            if (allTransactionsMigrated) {
                logger.info("All assignment transactions migrated for internal correspondence: {}, updating assignment status", correspondenceGuid);
                
                // Update the assignment status in internal migration table
                Optional<InternalCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
                
                if (migrationOpt.isPresent()) {
                    InternalCorrespondenceMigration migration = migrationOpt.get();
                    migration.setAssignmentStatus("COMPLETED");
                    migration.setCurrentPhase("APPROVAL");
                    migration.setNextPhase("BUSINESS_LOG");
                    migration.setPhaseStatus("PENDING");
                    migrationRepository.save(migration);
                    
                    logger.info("Updated assignment status to COMPLETED for internal correspondence: {}", correspondenceGuid);
                } else {
                    logger.warn("Internal migration record not found for correspondence: {}", correspondenceGuid);
                }
            } else {
                logger.debug("Not all assignment transactions are migrated yet for internal correspondence: {}", correspondenceGuid);
                
                // Log the status of each transaction for debugging
                for (CorrespondenceTransaction transaction : assignmentTransactions) {
                    logger.debug("Transaction {} status: {}", transaction.getGuid(), transaction.getMigrateStatus());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error checking assignment completion status for internal correspondence: {}", correspondenceGuid, e);
        }
    }
    
    /**
     * Gets internal assignments that need processing
     */
    private List<CorrespondenceTransaction> getInternalAssignmentsNeedingProcessing() {
        // Get internal correspondences (correspondence_type_id = 3) with assignment transactions
        return transactionRepository.findInternalAssignmentsNeedingProcessing();
    }
    
    /**
     * Processes assignment for a single transaction
     * @deprecated Use processInternalAssignmentInNewTransaction for better transaction handling
     */
    @Deprecated
    private boolean processInternalAssignmentForTransaction(String transactionGuid) {
        try {
            Optional<CorrespondenceTransaction> transactionOpt = 
                transactionRepository.findById(transactionGuid);
            
            if (!transactionOpt.isPresent()) {
                logger.error("Internal transaction not found: {}", transactionGuid);
                return false;
            }
            
            CorrespondenceTransaction transaction = transactionOpt.get();
            boolean success = processInternalAssignment(transaction);
            
            if (success) {
                transaction.setMigrateStatus("SUCCESS");
            } else {
                transaction.setMigrateStatus("FAILED");
                transaction.setRetryCount(transaction.getRetryCount() + 1);
            }
            transactionRepository.save(transaction);
            
            // Check if all transactions for this correspondence are migrated
            if (success) {
                checkAndUpdateAssignmentStatusForCorrespondence(transaction.getDocGuid());
            }
            
            return success;
        } catch (Exception e) {
            logger.error("Error processing internal assignment for: {}", transactionGuid, e);
            return false;
        }
    }
    
    /**
     * Processes a single internal assignment transaction
     */
    private boolean processInternalAssignment(CorrespondenceTransaction assignment) {
        try {
            // Get the created document ID from migration record
            Optional<InternalCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(assignment.getDocGuid());
            
            if (!migrationOpt.isPresent() || migrationOpt.get().getCreatedDocumentId() == null) {
                logger.error("No created document ID found for internal correspondence: {}", assignment.getDocGuid());
                return false;
            }
            
            String documentId = migrationOpt.get().getCreatedDocumentId();
            
            // Map department GUID to department code
            String departmentCode = DepartmentUtils.getDepartmentCodeByOldGuid(assignment.getToDepartmentGuid());
            if (departmentCode == null) {
                departmentCode = "COF"; // Default department
            }
            
            // Use actual user names from transaction data
            String fromUser = assignment.getFromUserName() != null ? assignment.getFromUserName() : "itba-emp1";
            String toUser = assignment.getToUserName() != null ? assignment.getToUserName() : "itba-emp1";
            
            // Create assignment in destination system
            return destinationService.createInternalAssignment(
                assignment.getGuid(),
                fromUser,
                documentId,
                assignment.getActionDate(),
                toUser,
                departmentCode,
                assignment.getDecisionGuid()
            );
            
        } catch (Exception e) {
            logger.error("Error processing internal assignment: {}", assignment.getGuid(), e);
            return false;
        }
    }
    
    /**
     * Gets internal assignment migrations with pagination and search
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getInternalAssignmentMigrations(int page, int size, String status, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // Use internal-specific assignment query methods
            Page<Object[]> assignmentPage;
            if ((status != null && !"all".equals(status)) || (search != null && !search.trim().isEmpty())) {
                String statusParam = "all".equals(status) ? null : status;
                String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();
                assignmentPage = migrationRepository.findInternalAssignmentMigrationsWithSearchAndPagination(
                    statusParam, searchParam, pageable);
            } else {
                assignmentPage = migrationRepository.findInternalAssignmentMigrationsWithPagination(pageable);
            }
            
            List<Map<String, Object>> assignments = new ArrayList<>();
            for (Object[] row : assignmentPage.getContent()) {
                Map<String, Object> assignment = new HashMap<>();
                assignment.put("transactionGuid", row[0]);
                assignment.put("correspondenceGuid", row[1]);
                assignment.put("fromUserName", row[2]);
                assignment.put("toUserName", row[3]);
                assignment.put("actionDate", row[4] != null ? ((Timestamp) row[4]).toLocalDateTime() : null);
                assignment.put("decisionGuid", row[5]);
                assignment.put("notes", row[6]);
                assignment.put("migrateStatus", row[7]);
                assignment.put("retryCount", row[8] != null ? ((Number) row[8]).intValue() : 0);
                assignment.put("lastModifiedDate", row[9] != null ? ((Timestamp) row[9]).toLocalDateTime() : null);
                assignment.put("correspondenceSubject", row[10]);
                assignment.put("correspondenceReferenceNo", row[11]);
                assignment.put("createdDocumentId", row[12]);
                assignment.put("creationUserName", row[13]);
                
                // Map department code from department GUID
                String departmentCode = DepartmentUtils.getDepartmentCodeByOldGuid((String) row[5]); // decision_guid used as dept mapping
                assignment.put("departmentCode", departmentCode != null ? departmentCode : "COF");
                
                assignments.add(assignment);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", assignments);
            result.put("totalElements", assignmentPage.getTotalElements());
            result.put("totalPages", assignmentPage.getTotalPages());
            result.put("currentPage", assignmentPage.getNumber());
            result.put("pageSize", assignmentPage.getSize());
            result.put("hasNext", assignmentPage.hasNext());
            result.put("hasPrevious", assignmentPage.hasPrevious());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting internal assignment migrations", e);
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