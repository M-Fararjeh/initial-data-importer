package com.importservice.service.migration;

import java.math.BigInteger;
import java.util.Arrays;
import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.CorrespondenceTransaction;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceTransactionRepository;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
import com.importservice.service.DestinationSystemService;
import com.importservice.util.DepartmentUtils;
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
 * Service for Phase 3: Assignment
 * Assigns correspondences to users and departments
 */
@Service
public class AssignmentPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(AssignmentPhaseService.class);
    
    @Autowired
    private CorrespondenceTransactionRepository transactionRepository;
    
    @Autowired
    private IncomingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private DestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    @Autowired
    private DepartmentUtils departmentUtils;
    
    /**
     * Phase 3: Assignment
     * Assigns correspondences to users and departments
     */
    @Transactional
    public ImportResponseDto executeAssignmentPhase() {
        logger.info("Starting Phase 3: Assignment");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<CorrespondenceTransaction> assignments = transactionRepository.findAssignmentsNeedingProcessing();
            
            for (CorrespondenceTransaction assignment : assignments) {
                try {
                    boolean success = processAssignment(assignment);
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
                    String errorMsg = "Error processing assignment " + assignment.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    assignment.setMigrateStatus("FAILED");
                    assignment.setRetryCount(assignment.getRetryCount() + 1);
                    transactionRepository.save(assignment);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Phase 3 completed. Assigned: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, assignments.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 3: Assignment", e);
            return phaseService.createResponse("ERROR", "Phase 3 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes assignment for specific transactions
     */
    @Transactional(timeout = 300)
    public ImportResponseDto executeAssignmentForSpecific(List<String> transactionGuids) {
        logger.info("Starting assignment for {} specific transactions", transactionGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String transactionGuid : transactionGuids) {
            try {
                boolean success = processAssignmentForTransaction(transactionGuid);
                
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
        String message = String.format("Specific assignment completed. Assigned: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, transactionGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Processes assignment for a single transaction
     */
    private boolean processAssignmentForTransaction(String transactionGuid) {
        try {
            Optional<CorrespondenceTransaction> transactionOpt = 
                transactionRepository.findById(transactionGuid);
            
            if (!transactionOpt.isPresent()) {
                logger.error("Transaction not found: {}", transactionGuid);
                return false;
            }
            
            CorrespondenceTransaction transaction = transactionOpt.get();
            boolean success = processAssignment(transaction);
            
            if (success) {
                transaction.setMigrateStatus("SUCCESS");
            } else {
                transaction.setMigrateStatus("FAILED");
                transaction.setRetryCount(transaction.getRetryCount() + 1);
            }
            transactionRepository.save(transaction);
            
            return success;
        } catch (Exception e) {
            logger.error("Error processing assignment for: {}", transactionGuid, e);
            return false;
        }
    }
    
    /**
     * Processes a single assignment transaction
     */
    private boolean processAssignment(CorrespondenceTransaction assignment) {
        try {
            // Get the created document ID from migration record
            Optional<IncomingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(assignment.getDocGuid());
            
            if (!migrationOpt.isPresent() || migrationOpt.get().getCreatedDocumentId() == null) {
                logger.error("No created document ID found for correspondence: {}", assignment.getDocGuid());
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
            return destinationService.createAssignment(
                assignment.getGuid(),
                fromUser,
                documentId,
                assignment.getActionDate(),
                toUser,
                departmentCode,
                assignment.getDecisionGuid()
            );
            
        } catch (Exception e) {
            logger.error("Error processing assignment: {}", assignment.getGuid(), e);
            return false;
        }
    }
    
    /**
     * Gets assignment migrations with pagination and search
     */
    public Map<String, Object> getAssignmentMigrations(int page, int size, String status, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Object[]> assignmentPage;
            if ((status != null && !"all".equals(status)) || (search != null && !search.trim().isEmpty())) {
                String statusParam = "all".equals(status) ? null : status;
                String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();
                assignmentPage = transactionRepository.findAssignmentMigrationsWithSearchAndPagination(
                    statusParam, searchParam, pageable);
            } else {
                assignmentPage = transactionRepository.findAssignmentMigrationsWithPagination(pageable);
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
            logger.error("Error getting assignment migrations", e);
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