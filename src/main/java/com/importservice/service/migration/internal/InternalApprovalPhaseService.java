package com.importservice.service.migration.internal;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.InternalCorrespondenceMigration;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.InternalCorrespondenceMigrationRepository;
import com.importservice.service.migration.incoming.MigrationPhaseService;
import com.importservice.util.CorrespondenceSubjectGenerator;
import com.importservice.util.CorrespondenceUtils;
import com.importservice.util.DepartmentUtils;
import com.importservice.util.HijriDateUtils;
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
 * Service for Internal Phase 4: Approval
 * Approves internal correspondences and registers them with reference
 */
@Service
public class InternalApprovalPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(InternalApprovalPhaseService.class);
    
    @Autowired
    private InternalCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @Autowired
    private InternalDestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    @Autowired
    private CorrespondenceSubjectGenerator subjectGenerator;
    
    /**
     * Phase 4: Approval
     * Approves internal correspondences and processes them for sending
     */
    @Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto executeApprovalPhase() {
        logger.info("Starting Internal Phase 4: Approval");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<InternalCorrespondenceMigration> migrations = migrationRepository.findByCurrentPhase("APPROVAL");
            
            for (InternalCorrespondenceMigration migration : migrations) {
                try {
                    boolean success = processApproval(migration);
                    if (success) {
                        successfulImports++;
                        updateApprovalSuccess(migration);
                    } else {
                        failedImports++;
                        updateApprovalError(migration, "Approval process failed");
                    }
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error processing internal approval " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    updateApprovalError(migration, errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Internal Phase 4 completed. Approved: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, migrations.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Internal Phase 4: Approval", e);
            return phaseService.createResponse("ERROR", "Internal Phase 4 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes approval for specific correspondences
     */
    public ImportResponseDto executeApprovalForSpecific(List<String> correspondenceGuids) {
        logger.info("Starting approval for {} specific internal correspondences", correspondenceGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        // Process each approval in its own transaction for immediate status updates
        for (int i = 0; i < correspondenceGuids.size(); i++) {
            String correspondenceGuid = correspondenceGuids.get(i);
            try {
                logger.info("Processing internal approval: {} ({}/{})", 
                           correspondenceGuid, i + 1, correspondenceGuids.size());
                
                // Process in separate transaction for immediate status update
                boolean success = processInternalApprovalInNewTransaction(correspondenceGuid);
                
                if (success) {
                    successfulImports++;
                    logger.info("Successfully completed internal approval for correspondence: {}", correspondenceGuid);
                } else {
                    failedImports++;
                    logger.warn("Failed to complete internal approval for correspondence: {}", correspondenceGuid);
                }
                
                // Add delay between approvals to reduce system load
                if (i < correspondenceGuids.size() - 1) {
                    try {
                        Thread.sleep(250); // 250ms delay between approvals (longer due to complexity)
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("Thread interrupted during processing delay");
                        break;
                    }
                }
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing internal approval " + correspondenceGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = phaseService.determineFinalStatus(successfulImports, failedImports);
        String message = String.format("Specific internal approval completed. Approved: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, correspondenceGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Processes approval for a single correspondence in a new transaction for immediate status updates
     */
    @Transactional(readOnly = false, timeout = 180)
    public boolean processInternalApprovalInNewTransaction(String correspondenceGuid) {
        try {
            logger.debug("Starting new transaction for internal approval: {}", correspondenceGuid);
            
            Optional<InternalCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (!migrationOpt.isPresent()) {
                logger.error("Internal migration record not found: {}", correspondenceGuid);
                return false;
            }
            
            InternalCorrespondenceMigration migration = migrationOpt.get();
            
            // Mark as in progress immediately
            migration.setApprovalStatus("IN_PROGRESS");
            migration.setLastModifiedDate(LocalDateTime.now());
            migrationRepository.save(migration);
            
            // Process the approval
            boolean result = processApproval(migration);
            
            // Update final status immediately
            if (result) {
                updateApprovalSuccess(migration);
                logger.info("Successfully processed internal approval: {}", correspondenceGuid);
            } else {
                updateApprovalError(migration, "Approval process failed");
                logger.warn("Failed to process internal approval: {}", correspondenceGuid);
            }
            
            logger.debug("Completed internal approval transaction for: {} with result: {}", 
                        correspondenceGuid, result);
            return result;
            
        } catch (Exception e) {
            logger.error("Error in internal approval transaction for: {}", correspondenceGuid, e);
            
            // Update error status in separate try-catch to ensure it gets saved
            try {
                Optional<InternalCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
                if (migrationOpt.isPresent()) {
                    InternalCorrespondenceMigration migration = migrationOpt.get();
                    updateApprovalError(migration, "Transaction failed: " + e.getMessage());
                }
            } catch (Exception statusError) {
                logger.error("Error updating error status for correspondence: {}", correspondenceGuid, statusError);
            }
            
            return false;
        }
    }
    
    /**
     * Processes approval for a single correspondence
     * @deprecated Use processInternalApprovalInNewTransaction for better transaction handling
     */
    @Deprecated
    private boolean processApprovalForCorrespondence(String correspondenceGuid) {
        try {
            Optional<InternalCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (!migrationOpt.isPresent()) {
                logger.error("Internal migration record not found: {}", correspondenceGuid);
                return false;
            }
            
            InternalCorrespondenceMigration migration = migrationOpt.get();
            boolean success = processApproval(migration);
            
            if (success) {
                updateApprovalSuccess(migration);
            } else {
                updateApprovalError(migration, "Approval process failed");
            }
            
            return success;
        } catch (Exception e) {
            logger.error("Error processing internal approval for: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Processes the complete approval workflow for a single internal correspondence
     * Implements step-based processing for fault tolerance
     */
    private boolean processApproval(InternalCorrespondenceMigration migration) {
        String correspondenceGuid = migration.getCorrespondenceGuid();
        logger.info("Processing approval for internal correspondence: {}", correspondenceGuid);
        
        try {
            if (migration.getCreatedDocumentId() == null) {
                logger.error("No created document ID found for internal correspondence: {}", correspondenceGuid);
                return false;
            }
            
            String documentId = migration.getCreatedDocumentId();
            
            // Get correspondence details
            Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(correspondenceGuid);
            if (!correspondenceOpt.isPresent()) {
                logger.error("Internal correspondence not found: {}", correspondenceGuid);
                return false;
            }
            Correspondence correspondence = correspondenceOpt.get();
            
            String asUser = correspondence.getCreationUserName() != null ? 
                          correspondence.getCreationUserName() : "itba-emp1";
            
            // Step 1: Approve correspondence
            if ("APPROVE_CORRESPONDENCE".equals(migration.getApprovalStep())) {
                logger.info("Approval Step 1: Approving internal correspondence: {}", correspondenceGuid);
                boolean approvalSuccess = destinationService.approveInternalCorrespondence(documentId, asUser);
                if (!approvalSuccess) {
                    logger.error("Approval Step 1 failed: Failed to approve internal correspondence: {}", correspondenceGuid);
                    return false;
                }
                
                updateApprovalStep(migration, "REGISTER_WITH_REFERENCE");
                migrationRepository.save(migration); // Save progress
            }
            
            // Step 2: Register with reference
            if ("REGISTER_WITH_REFERENCE".equals(migration.getApprovalStep())) {
                logger.info("Approval Step 2: Registering internal with reference: {}", correspondenceGuid);
                Map<String, Object> interCorrespondenceContext = buildInternalCorrespondenceContext(correspondence);
                boolean registerSuccess = destinationService.registerInternalWithReference(documentId, asUser, interCorrespondenceContext);
                if (!registerSuccess) {
                    logger.error("Approval Step 2 failed: Failed to register internal correspondence with reference: {}", correspondenceGuid);
                    return false;
                }
                
                updateApprovalStep(migration, "SEND_CORRESPONDENCE");
                migrationRepository.save(migration); // Save progress
            }
            
            // Step 3: Send correspondence
            if ("SEND_CORRESPONDENCE".equals(migration.getApprovalStep())) {
                logger.info("Approval Step 3: Sending internal correspondence: {}", correspondenceGuid);
                boolean sendSuccess = destinationService.sendInternalCorrespondence(documentId, asUser);
                if (!sendSuccess) {
                    logger.error("Approval Step 3 failed: Failed to send internal correspondence: {}", correspondenceGuid);
                    return false;
                }
                
                updateApprovalStep(migration, "SET_OWNER");
                migrationRepository.save(migration); // Save progress
            }
            
            // Step 4: Set owner
            if ("SET_OWNER".equals(migration.getApprovalStep())) {
                logger.info("Approval Step 4: Setting owner for internal correspondence: {}", correspondenceGuid);
                boolean setOwnerSuccess = destinationService.setInternalCorrespondenceOwner(documentId, asUser);
                if (!setOwnerSuccess) {
                    logger.error("Approval Step 4 failed: Failed to set owner for internal correspondence: {}", correspondenceGuid);
                    return false;
                }
                
                updateApprovalStep(migration, "COMPLETED");
                migrationRepository.save(migration); // Save final progress
                logger.info("Successfully completed approval for internal correspondence: {}", correspondenceGuid);
                return true;
            }
            
            logger.warn("Approval process reached end without completion for internal correspondence: {}", correspondenceGuid);
            return false;
        } catch (Exception e) {
            logger.error("Error in approval process for internal correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Updates the approval step for tracking progress
     */
    private void updateApprovalStep(InternalCorrespondenceMigration migration, String step) {
        try {
            migration.setApprovalStep(step);
            migration.setLastModifiedDate(LocalDateTime.now());
            logger.debug("Updated approval step to {} for internal correspondence: {}", step, migration.getCorrespondenceGuid());
        } catch (Exception e) {
            logger.warn("Error updating approval step to {}: {}", step, e.getMessage());
        }
    }
    
    /**
     * Builds internal correspondence context for API calls
     */
    private Map<String, Object> buildInternalCorrespondenceContext(Correspondence correspondence) {
        Map<String, Object> context = new HashMap<>();
        
        // Document dates
        String gDocumentDate = correspondence.getCorrespondenceCreationDate() != null ?
                correspondence.getCorrespondenceCreationDate().toString() + "Z" :
                LocalDateTime.now().toString() + "Z";
        String hDocumentDate = correspondence.getCorrespondenceCreationDate() != null ?
                HijriDateUtils.convertToHijri(correspondence.getCorrespondenceCreationDate()) :
                HijriDateUtils.getCurrentHijriDate();
        
        // Due dates
        String gDueDate = correspondence.getDueDate() != null ? 
                        correspondence.getDueDate().toString() + "Z" : 
                        LocalDateTime.now().plusDays(30).toString() + "Z";
        String hDueDate = correspondence.getDueDate() != null ? 
                        HijriDateUtils.convertToHijri(correspondence.getDueDate()) : 
                        HijriDateUtils.convertToHijri(LocalDateTime.now().plusDays(30));
        
        // Department mapping
        String fromDepartment = DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getCreationDepartmentGuid());
        if (fromDepartment == null) {
            fromDepartment = "COF"; // Default department
        }
        
        String toDepartment = DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getFromDepartmentGuid());
        if (toDepartment == null) {
            toDepartment = "CTS"; // Default to department
        }
        
        context.put("corr:gDocumentDate", gDocumentDate);
        context.put("corr:hDocumentDate", hDocumentDate);
        context.put("corr:action", CorrespondenceUtils.mapAction(correspondence.getLastDecisionGuid()));
        
        String finalSubject = correspondence.getSubject();
        if (subjectGenerator.isRandomSubjectEnabled()) {
            String category = CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid());
            finalSubject = subjectGenerator.generateSubjectWithCategory(category);
            logger.info("Generated random subject for internal correspondence {}: {}", correspondence.getGuid(), finalSubject);
        }
        
        context.put("corr:subject", finalSubject != null ? finalSubject : "");
        context.put("corr:remarks", correspondence.getNotes() != null ? CorrespondenceUtils.cleanHtmlTags(correspondence.getNotes()) : "");
        context.put("corr:referenceNumber", correspondence.getReferenceNo() != null ? correspondence.getReferenceNo() : "");
        context.put("corr:category", CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid()));
        context.put("corr:secrecyLevel", CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId()));
        context.put("corr:priority", CorrespondenceUtils.mapPriority(correspondence.getPriorityId()));
        context.put("corr:gDueDate", gDueDate);
        context.put("corr:hDueDate", hDueDate);
        context.put("corr:requireReply", CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus()));
        context.put("corr:from", fromDepartment);
        context.put("corr:to", toDepartment);
        context.put("corr:fromAgency", "ITBA");
        context.put("corr:toAgency", "ITBA");
        
        return context;
    }
    
    /**
     * Updates approval success status
     */
    private void updateApprovalSuccess(InternalCorrespondenceMigration migration) {
        try {
            migration.setApprovalStatus("COMPLETED");
            migration.setApprovalStep("COMPLETED");
            migration.setCurrentPhase("BUSINESS_LOG");
            migration.setNextPhase("CLOSING");
            migration.setPhaseStatus("PENDING");
            migrationRepository.save(migration);
        } catch (Exception e) {
            logger.error("Error updating internal approval success status: {}", e.getMessage());
        }
    }
    
    /**
     * Updates approval error status
     */
    private void updateApprovalError(InternalCorrespondenceMigration migration, String errorMsg) {
        try {
            migration.setApprovalStatus("ERROR");
            migration.setApprovalError(errorMsg);
            migration.setRetryCount(migration.getRetryCount() + 1);
            migration.setLastErrorAt(LocalDateTime.now());
            migrationRepository.save(migration);
        } catch (Exception e) {
            logger.error("Error updating internal approval error status: {}", e.getMessage());
        }
    }
    
    /**
     * Gets internal approval migrations with pagination and search
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getInternalApprovalMigrations(int page, int size, String status, String step, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Object[]> approvalPage;
            if ((status != null && !"all".equals(status)) || 
                (step != null && !"all".equals(step)) || 
                (search != null && !search.trim().isEmpty())) {
                
                String statusParam = "all".equals(status) ? null : status;
                String stepParam = "all".equals(step) ? null : step;
                String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();
                
                approvalPage = migrationRepository.findInternalApprovalMigrationsWithSearchAndPagination(
                    statusParam, stepParam, searchParam, pageable);
            } else {
                approvalPage = migrationRepository.findInternalApprovalMigrationsWithPagination(pageable);
            }
            
            List<Map<String, Object>> approvals = new ArrayList<>();
            for (Object[] row : approvalPage.getContent()) {
                Map<String, Object> approval = new HashMap<>();
                approval.put("id", row[0] != null ? ((Number) row[0]).longValue() : null);
                approval.put("correspondenceGuid", row[1]);
                approval.put("createdDocumentId", row[2]);
                approval.put("approvalStatus", row[3]);
                approval.put("approvalStep", row[4]);
                approval.put("approvalError", row[5]);
                approval.put("retryCount", row[6] != null ? ((Number) row[6]).intValue() : 0);
                approval.put("lastModifiedDate", row[7] != null ? ((Timestamp) row[7]).toLocalDateTime() : null);
                approval.put("correspondenceSubject", row[8]);
                approval.put("correspondenceReferenceNo", row[9]);
                approval.put("creationUserName", row[10]);
                approvals.add(approval);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", approvals);
            result.put("totalElements", approvalPage.getTotalElements());
            result.put("totalPages", approvalPage.getTotalPages());
            result.put("currentPage", approvalPage.getNumber());
            result.put("pageSize", approvalPage.getSize());
            result.put("hasNext", approvalPage.hasNext());
            result.put("hasPrevious", approvalPage.hasPrevious());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting internal approval migrations", e);
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