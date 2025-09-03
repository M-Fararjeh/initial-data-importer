package com.importservice.service.migration.outgoing;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.OutgoingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.OutgoingCorrespondenceMigrationRepository;
import com.importservice.service.OutgoingDestinationSystemService;
import com.importservice.service.migration.MigrationPhaseService;
import com.importservice.util.CorrespondenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for Outgoing Phase 4: Approval
 * Approves outgoing correspondences and registers them with reference
 */
@Service
public class OutgoingApprovalPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(OutgoingApprovalPhaseService.class);
    
    @Autowired
    private OutgoingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @Autowired
    private OutgoingDestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    /**
     * Phase 4: Approval
     * Approves outgoing correspondences and processes them for sending
     */
    @Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto executeApprovalPhase() {
        logger.info("Starting Outgoing Phase 4: Approval");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<OutgoingCorrespondenceMigration> migrations = migrationRepository.findByCurrentPhase("APPROVAL");
            
            for (OutgoingCorrespondenceMigration migration : migrations) {
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
                    String errorMsg = "Error processing approval " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    updateApprovalError(migration, errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Outgoing Phase 4 completed. Approved: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, migrations.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Outgoing Phase 4: Approval", e);
            return phaseService.createResponse("ERROR", "Outgoing Phase 4 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes approval for specific correspondences
     */
    @Transactional(readOnly = false, timeout = 300)
    public ImportResponseDto executeApprovalForSpecific(List<String> correspondenceGuids) {
        logger.info("Starting approval for {} specific outgoing correspondences", correspondenceGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String correspondenceGuid : correspondenceGuids) {
            try {
                boolean success = processApprovalForCorrespondence(correspondenceGuid);
                
                // Add small delay between correspondences to reduce lock contention
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
                String errorMsg = "Error processing approval " + correspondenceGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = phaseService.determineFinalStatus(successfulImports, failedImports);
        String message = String.format("Specific approval completed. Approved: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, correspondenceGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Processes approval for a single correspondence
     */
    private boolean processApprovalForCorrespondence(String correspondenceGuid) {
        try {
            Optional<OutgoingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (!migrationOpt.isPresent()) {
                logger.error("Outgoing migration record not found: {}", correspondenceGuid);
                return false;
            }
            
            OutgoingCorrespondenceMigration migration = migrationOpt.get();
            boolean success = processApproval(migration);
            
            if (success) {
                updateApprovalSuccess(migration);
            } else {
                updateApprovalError(migration, "Approval process failed");
            }
            
            return success;
        } catch (Exception e) {
            logger.error("Error processing approval for: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Processes the complete approval workflow for a single outgoing correspondence
     * Implements step-based processing for fault tolerance
     */
    private boolean processApproval(OutgoingCorrespondenceMigration migration) {
        String correspondenceGuid = migration.getCorrespondenceGuid();
        logger.info("Processing approval for outgoing correspondence: {}", correspondenceGuid);
        
        try {
            if (migration.getCreatedDocumentId() == null) {
                logger.error("No created document ID found for outgoing correspondence: {}", correspondenceGuid);
                return false;
            }
            
            String documentId = migration.getCreatedDocumentId();
            
            // Get correspondence details
            Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(correspondenceGuid);
            if (!correspondenceOpt.isPresent()) {
                logger.error("Outgoing correspondence not found: {}", correspondenceGuid);
                return false;
            }
            Correspondence correspondence = correspondenceOpt.get();
            
            String asUser = correspondence.getCreationUserName() != null ? 
                          correspondence.getCreationUserName() : "itba-emp1";
            
            // Step 1: Approve correspondence
            if ("APPROVE_CORRESPONDENCE".equals(migration.getApprovalStep())) {
                logger.info("Approval Step 1: Approving correspondence: {}", correspondenceGuid);
                boolean approvalSuccess = destinationService.approveOutgoingCorrespondence(documentId, asUser);
                if (!approvalSuccess) {
                    logger.error("Approval Step 1 failed: Failed to approve outgoing correspondence: {}", correspondenceGuid);
                    return false;
                }
                
                updateApprovalStep(migration, "REGISTER_WITH_REFERENCE");
                migrationRepository.save(migration); // Save progress
            }
            
            // Step 2: Register with reference
            if ("REGISTER_WITH_REFERENCE".equals(migration.getApprovalStep())) {
                logger.info("Approval Step 2: Registering with reference: {}", correspondenceGuid);
                Map<String, Object> outCorrespondenceContext = buildOutgoingCorrespondenceContext(correspondence);
                boolean registerSuccess = destinationService.registerOutgoingWithReference(documentId, asUser, outCorrespondenceContext);
                if (!registerSuccess) {
                    logger.error("Approval Step 2 failed: Failed to register outgoing correspondence with reference: {}", correspondenceGuid);
                    return false;
                }
                
                updateApprovalStep(migration, "SEND_CORRESPONDENCE");
                migrationRepository.save(migration); // Save progress
            }
            
            // Step 3: Send correspondence
            if ("SEND_CORRESPONDENCE".equals(migration.getApprovalStep())) {
                logger.info("Approval Step 3: Sending correspondence: {}", correspondenceGuid);
                boolean sendSuccess = destinationService.sendOutgoingCorrespondence(documentId, asUser);
                if (!sendSuccess) {
                    logger.error("Approval Step 3 failed: Failed to send outgoing correspondence: {}", correspondenceGuid);
                    return false;
                }
                
                updateApprovalStep(migration, "COMPLETED");
                migrationRepository.save(migration); // Save final progress
                logger.info("Successfully completed approval for outgoing correspondence: {}", correspondenceGuid);
                return true;
            }
            
            logger.warn("Approval process reached end without completion for outgoing correspondence: {}", correspondenceGuid);
            return false;
            
        } catch (Exception e) {
            logger.error("Error in approval process for outgoing correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Updates the approval step for tracking progress
     */
    private void updateApprovalStep(OutgoingCorrespondenceMigration migration, String step) {
        try {
            migration.setApprovalStep(step);
            migration.setLastModifiedDate(LocalDateTime.now());
            logger.debug("Updated approval step to {} for outgoing correspondence: {}", step, migration.getCorrespondenceGuid());
        } catch (Exception e) {
            logger.warn("Error updating approval step to {}: {}", step, e.getMessage());
        }
    }
    
    /**
     * Builds outgoing correspondence context for API calls
     */
    private Map<String, Object> buildOutgoingCorrespondenceContext(Correspondence correspondence) {
        Map<String, Object> context = new HashMap<>();
        
        // Document dates
        String gDocumentDate = correspondence.getCorrespondenceCreationDate() != null ?
                correspondence.getCorrespondenceCreationDate().toString() + "Z" :
                LocalDateTime.now().toString() + "Z";
        String hDocumentDate = correspondence.getCorrespondenceCreationDate() != null ?
                com.importservice.util.HijriDateUtils.convertToHijri(correspondence.getCorrespondenceCreationDate()) :
                com.importservice.util.HijriDateUtils.getCurrentHijriDate();
        
        // Due dates
        String gDueDate = correspondence.getDueDate() != null ? 
                        correspondence.getDueDate().toString() + "Z" : 
                        LocalDateTime.now().plusDays(30).toString() + "Z";
        String hDueDate = correspondence.getDueDate() != null ? 
                        com.importservice.util.HijriDateUtils.convertToHijri(correspondence.getDueDate()) : 
                        com.importservice.util.HijriDateUtils.convertToHijri(LocalDateTime.now().plusDays(30));
        
        context.put("corr:gDocumentDate", gDocumentDate);
        context.put("corr:hDocumentDate", hDocumentDate);
        context.put("out_corr:signee", "SECTOR");
        context.put("corr:action", CorrespondenceUtils.mapAction(correspondence.getLastDecisionGuid()));
        context.put("corr:subject", correspondence.getSubject() != null ? correspondence.getSubject() : "");
        context.put("corr:remarks", correspondence.getNotes() != null ? CorrespondenceUtils.cleanHtmlTags(correspondence.getNotes()) : "");
        context.put("corr:referenceNumber", correspondence.getReferenceNo() != null ? correspondence.getReferenceNo() : "");
        context.put("corr:category", CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid()));
        context.put("corr:secrecyLevel", CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId()));
        context.put("corr:priority", CorrespondenceUtils.mapPriority(correspondence.getPriorityId()));
        context.put("corr:gDueDate", gDueDate);
        context.put("corr:hDueDate", hDueDate);
        context.put("corr:requireReply", CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus()));
        
        // Department mapping
        String fromDepartment = com.importservice.util.DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getFromDepartmentGuid());
        if (fromDepartment == null) {
            fromDepartment = "COF"; // Default department
        }
        
        context.put("corr:from", fromDepartment);
        context.put("corr:to", "");
        context.put("corr:fromAgency", "ITBA");
        context.put("corr:toAgency", com.importservice.util.AgencyMappingUtils.mapAgencyGuidToCode(correspondence.getComingFromGuid()));
        context.put("out_corr:multiRecivers", Arrays.asList());
        
        return context;
    }
    
    /**
     * Updates approval success status
     */
    private void updateApprovalSuccess(OutgoingCorrespondenceMigration migration) {
        try {
            migration.setApprovalStatus("COMPLETED");
            migration.setApprovalStep("COMPLETED");
            migration.setCurrentPhase("BUSINESS_LOG");
            migration.setNextPhase("COMMENT");
            migration.setPhaseStatus("PENDING");
            migrationRepository.save(migration);
        } catch (Exception e) {
            logger.error("Error updating approval success status: {}", e.getMessage());
        }
    }
    
    /**
     * Updates approval error status
     */
    private void updateApprovalError(OutgoingCorrespondenceMigration migration, String errorMsg) {
        try {
            migration.setApprovalStatus("ERROR");
            migration.setApprovalError(errorMsg);
            migration.setRetryCount(migration.getRetryCount() + 1);
            migration.setLastErrorAt(LocalDateTime.now());
            migrationRepository.save(migration);
        } catch (Exception e) {
            logger.error("Error updating approval error status: {}", e.getMessage());
        }
    }
}