package com.importservice.service.migration.outgoing;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.CorrespondenceTransaction;
import com.importservice.entity.OutgoingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceTransactionRepository;
import com.importservice.repository.OutgoingCorrespondenceMigrationRepository;
import com.importservice.service.migration.incoming.MigrationPhaseService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for Outgoing Phase 3: Assignment
 * Assigns outgoing correspondences to users and departments
 */
@Service
public class OutgoingAssignmentPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(OutgoingAssignmentPhaseService.class);
    
    @Autowired
    private CorrespondenceTransactionRepository transactionRepository;
    
    @Autowired
    private OutgoingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private OutgoingDestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    @Autowired
    private DepartmentUtils departmentUtils;
    
    /**
     * Phase 3: Assignment
     * Assigns outgoing correspondences to users and departments
     */
    @Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto executeAssignmentPhase() {
        logger.info("Starting Outgoing Phase 3: Assignment");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Get outgoing correspondence assignments (action_id = 12 for outgoing correspondences)
            List<CorrespondenceTransaction> assignments = getOutgoingAssignmentsNeedingProcessing();
            
            for (CorrespondenceTransaction assignment : assignments) {
                try {
                    boolean success = processOutgoingAssignment(assignment);
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
                    String errorMsg = "Error processing outgoing assignment " + assignment.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    assignment.setMigrateStatus("FAILED");
                    assignment.setRetryCount(assignment.getRetryCount() + 1);
                    transactionRepository.save(assignment);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Outgoing Phase 3 completed. Assigned: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, assignments.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Outgoing Phase 3: Assignment", e);
            return phaseService.createResponse("ERROR", "Outgoing Phase 3 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes assignment for specific transactions
     */
    @Transactional(readOnly = false, timeout = 300)
    public ImportResponseDto executeAssignmentForSpecific(List<String> transactionGuids) {
        logger.info("Starting outgoing assignment for {} specific transactions", transactionGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String transactionGuid : transactionGuids) {
            try {
                boolean success = processOutgoingAssignmentForTransaction(transactionGuid);
                
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
        String message = String.format("Specific outgoing assignment completed. Assigned: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, transactionGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Gets outgoing assignments that need processing
     */
    private List<CorrespondenceTransaction> getOutgoingAssignmentsNeedingProcessing() {
        // Get outgoing migrations that have completed creation and are pending approval
        List<OutgoingCorrespondenceMigration> migrations = migrationRepository.findOutgoingAssignmentsNeedingProcessing();
        
        // Convert to transactions for processing (this might need adjustment based on your business logic)
        List<CorrespondenceTransaction> transactions = new ArrayList<>();
        for (OutgoingCorrespondenceMigration migration : migrations) {
            // Get transactions for this correspondence
            List<CorrespondenceTransaction> corrTransactions = transactionRepository.findByDocGuid(migration.getCorrespondenceGuid());
            transactions.addAll(corrTransactions.stream()
                .filter(t -> t.getActionId() == 12) // Only assignment transactions
                .filter(t -> "PENDING".equals(t.getMigrateStatus()) || 
                           ("FAILED".equals(t.getMigrateStatus()) && t.getRetryCount() < 3))
                .collect(java.util.stream.Collectors.toList()));
        }
        
        return transactions;
    }
    
    /**
     * Processes assignment for a single transaction
     */
    private boolean processOutgoingAssignmentForTransaction(String transactionGuid) {
        try {
            Optional<CorrespondenceTransaction> transactionOpt = 
                transactionRepository.findById(transactionGuid);
            
            if (!transactionOpt.isPresent()) {
                logger.error("Outgoing transaction not found: {}", transactionGuid);
                return false;
            }
            
            CorrespondenceTransaction transaction = transactionOpt.get();
            boolean success = processOutgoingAssignment(transaction);
            
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
            
            // Check if all transactions for this correspondence are migrated
            if (success) {
                checkAndUpdateAssignmentStatusForCorrespondence(transaction.getDocGuid());
            }
            
            return success;
        } catch (Exception e) {
            logger.error("Error processing outgoing assignment for: {}", transactionGuid, e);
            return false;
        }
    }
    
    /**
     * Processes a single outgoing assignment transaction
     */
    private boolean processOutgoingAssignment(CorrespondenceTransaction assignment) {
        try {
            // Get the created document ID from migration record
            Optional<OutgoingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(assignment.getDocGuid());
            
            if (!migrationOpt.isPresent() || migrationOpt.get().getCreatedDocumentId() == null) {
                logger.error("No created document ID found for outgoing correspondence: {}", assignment.getDocGuid());
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
            return destinationService.createOutgoingAssignment(
                assignment.getGuid(),
                fromUser,
                documentId,
                assignment.getActionDate(),
                toUser,
                departmentCode,
                assignment.getDecisionGuid()
            );
            
        } catch (Exception e) {
            logger.error("Error processing outgoing assignment: {}", assignment.getGuid(), e);
            return false;
        }
    }
    
    /**
     * Gets outgoing assignment migrations with pagination and search
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getOutgoingAssignmentMigrations(int page, int size, String status, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // Use actual transaction queries for outgoing assignments
            Page<Object[]> assignmentPage;
            if ((status != null && !"all".equals(status)) || (search != null && !search.trim().isEmpty())) {
                String statusParam = "all".equals(status) ? null : status;
                String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();
                assignmentPage = migrationRepository.findOutgoingAssignmentMigrationsWithSearchAndPagination(
                    statusParam, searchParam, pageable);
            } else {
                assignmentPage = migrationRepository.findOutgoingAssignmentMigrationsWithPagination(pageable);
            }
            
            List<Map<String, Object>> assignments = new ArrayList<>();
            for (Object[] row : assignmentPage.getContent()) {
                Map<String, Object> assignment = new HashMap<>();
                assignment.put("transactionGuid", row[0]); // ct.guid
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
            logger.error("Error getting outgoing assignment migrations", e);
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
    
    /**
     * Checks if all assignment transactions for a correspondence are migrated
     * and updates the assignment status in outgoing migration table
     */
    private void checkAndUpdateAssignmentStatusForCorrespondence(String correspondenceGuid) {
        try {
            logger.debug("Checking assignment completion status for outgoing correspondence: {}", correspondenceGuid);
            
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
                logger.info("All assignment transactions migrated for correspondence: {}, updating assignment status", correspondenceGuid);
                
                // Update the assignment status in outgoing migration table
                Optional<OutgoingCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
                
                if (migrationOpt.isPresent()) {
                    OutgoingCorrespondenceMigration migration = migrationOpt.get();
                    migration.setAssignmentStatus("COMPLETED");
                    migration.setCurrentPhase("APPROVAL");
                    migration.setNextPhase("BUSINESS_LOG");
                    migration.setPhaseStatus("PENDING");
                    migrationRepository.save(migration);
                    
                    logger.info("Updated assignment status to COMPLETED for outgoing correspondence: {}", correspondenceGuid);
                } else {
                    logger.warn("Outgoing migration record not found for correspondence: {}", correspondenceGuid);
                }
            } else {
                logger.debug("Not all assignment transactions are migrated yet for correspondence: {}", correspondenceGuid);
                
                // Log the status of each transaction for debugging
                for (CorrespondenceTransaction transaction : assignmentTransactions) {
                    logger.debug("Transaction {} status: {}", transaction.getGuid(), transaction.getMigrateStatus());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error checking assignment completion status for correspondence: {}", correspondenceGuid, e);
        }
    }
}