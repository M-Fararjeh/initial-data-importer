package com.importservice.service.migration.outgoing;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.OutgoingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.OutgoingCorrespondenceMigrationRepository;
import com.importservice.service.migration.incoming.MigrationPhaseService;
import com.importservice.util.CorrespondenceSubjectGenerator;
import com.importservice.util.CorrespondenceUtils;
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
    
    @Autowired
    private com.importservice.repository.CorrespondenceSendToRepository correspondenceSendToRepository;

    @Autowired
    private CorrespondenceSubjectGenerator subjectGenerator;
    
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
        //context.put("out_corr:signee", "SECTOR");
        context.put("corr:action", CorrespondenceUtils.mapAction(correspondence.getLastDecisionGuid()));
        String finalSubject = correspondence.getSubject();
        if (subjectGenerator.isRandomSubjectEnabled()) {
            String category = CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid());
            finalSubject = subjectGenerator.generateSubjectWithCategory(category);
            logger.info("Generated random subject for outgoing correspondence {}: {}", correspondence.getGuid(), finalSubject);
        }
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
        String fromDepartment = com.importservice.util.DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getCreationDepartmentGuid());
        if (fromDepartment == null) {
            fromDepartment = "COF"; // Default department
        }
        
        context.put("corr:from", fromDepartment);
        context.put("corr:to", "");
        context.put("corr:fromAgency", "ITBA");
        
        // Get toAgency from correspondence_send_tos table
        String toAgency = getToAgencyFromSendTos(correspondence.getGuid());
        context.put("corr:toAgency", toAgency);
        
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
    
    /**
     * Gets outgoing approval migrations with pagination and search
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getOutgoingApprovalMigrations(int page, int size, String status, String step, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Object[]> approvalPage;
            if ((status != null && !"all".equals(status)) || 
                (step != null && !"all".equals(step)) || 
                (search != null && !search.trim().isEmpty())) {
                
                String statusParam = "all".equals(status) ? null : status;
                String stepParam = "all".equals(step) ? null : step;
                String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();
                
                approvalPage = migrationRepository.findOutgoingApprovalMigrationsWithSearchAndPagination(
                    statusParam, stepParam, searchParam, pageable);
            } else {
                approvalPage = migrationRepository.findOutgoingApprovalMigrationsWithPagination(pageable);
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
            logger.error("Error getting outgoing approval migrations", e);
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
     * Gets the toAgency from correspondence_send_tos table
     * 
     * @param correspondenceGuid The correspondence GUID to look up
     * @return Agency code for the destination agency
     */
    private String getToAgencyFromSendTos(String correspondenceGuid) {
        try {
            logger.debug("Looking up toAgency for correspondence: {}", correspondenceGuid);
            
            // Get send_tos records for this correspondence
            List<com.importservice.entity.CorrespondenceSendTo> sendTos = 
                correspondenceSendToRepository.findByDocGuid(correspondenceGuid);
            
            if (sendTos == null || sendTos.isEmpty()) {
                logger.debug("No send_tos found for correspondence: {}, using default agency", correspondenceGuid);
                return "001"; // Default agency code
            }
            
            // Get the first send_to record (you might want to add business logic here for multiple recipients)
            com.importservice.entity.CorrespondenceSendTo firstSendTo = sendTos.get(0);
            String sendToGuid = firstSendTo.getSendToGuid();
            
            if (sendToGuid == null || sendToGuid.trim().isEmpty()) {
                logger.debug("Empty sendToGuid for correspondence: {}, using default agency", correspondenceGuid);
                return "001"; // Default agency code
            }
            
            // Map the send_to_guid to agency code using AgencyMappingUtils
            String agencyCode = com.importservice.util.AgencyMappingUtils.mapAgencyGuidToCode(sendToGuid);
            
            if (agencyCode == null) {
                logger.debug("No agency mapping found for sendToGuid: {}, using default agency", sendToGuid);
                return "001"; // Default agency code
            }
            
            logger.info("Found toAgency '{}' for correspondence '{}' from sendToGuid '{}'", 
                       agencyCode, correspondenceGuid, sendToGuid);
            return agencyCode;
            
        } catch (Exception e) {
            logger.error("Error getting toAgency from send_tos for correspondence: {}", correspondenceGuid, e);
            return "001"; // Default fallback
        }
    }
}