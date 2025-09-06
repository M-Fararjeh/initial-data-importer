package com.importservice.service.migration;

import java.util.Arrays;
import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.CorrespondenceAttachment;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceAttachmentRepository;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
import com.importservice.service.DestinationSystemService;
import com.importservice.util.AgencyMappingUtils;
import com.importservice.util.AttachmentUtils;
import com.importservice.util.CorrespondenceUtils;
import com.importservice.util.DepartmentUtils;
import com.importservice.util.HijriDateUtils;
import com.importservice.util.CorrespondenceSubjectGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for Phase 2: Creation
 * Creates correspondences in destination system with attachments
 */
@Service
public class CreationPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(CreationPhaseService.class);
    
    @Autowired
    private IncomingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @Autowired
    private CorrespondenceAttachmentRepository attachmentRepository;
    
    @Autowired
    private DestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    @Autowired
    private DepartmentUtils departmentUtils;
    
    @Autowired
    private CorrespondenceSubjectGenerator subjectGenerator;

    /**
     * Phase 2: Creation
     * Creates correspondences in destination system
     */
    //@Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto executeCreationPhase() {
        logger.info("Starting Phase 2: Creation");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<IncomingCorrespondenceMigration> migrations = phaseService.getMigrationsForPhase("CREATION");
            
            for (IncomingCorrespondenceMigration migration : migrations) {
                try {
                    boolean success = processCorrespondenceCreation(migration);
                    if (success) {
                        successfulImports++;
                        phaseService.updatePhaseStatus(migration.getCorrespondenceGuid(), "CREATION", "COMPLETED", null);
                    } else {
                        failedImports++;
                        // Don't update phase status here as it's already updated in processCorrespondenceCreation
                    }
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error processing correspondence " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    phaseService.updatePhaseStatus(migration.getCorrespondenceGuid(), "CREATION", "ERROR", errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Phase 2 completed. Created: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, migrations.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 2: Creation", e);
            return phaseService.createResponse("ERROR", "Phase 2 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes creation for specific correspondences
     */
    @Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto executeCreationForSpecific(List<String> correspondenceGuids) {
        logger.info("Starting creation for {} specific correspondences", correspondenceGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        // Process correspondences one by one with individual transactions for better timeout handling
        for (int i = 0; i < correspondenceGuids.size(); i++) {
            String correspondenceGuid = correspondenceGuids.get(i);
            try {
                logger.info("Processing correspondence: {} ({}/{})", 
                           correspondenceGuid, i + 1, correspondenceGuids.size());
                
                // Process in separate transaction for better timeout handling
                boolean success = processCorrespondenceCreationInNewTransaction(correspondenceGuid);
                
                if (success) {
                    successfulImports++;
                    logger.info("Successfully completed creation for correspondence: {}", correspondenceGuid);
                } else {
                    failedImports++;
                    logger.warn("Failed to complete creation for correspondence: {}", correspondenceGuid);
                }
                
                // Add small delay between correspondences to reduce system load
                if (i < correspondenceGuids.size() - 1) {
                    try {
                        Thread.sleep(200); // 200ms delay between correspondences
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("Thread interrupted during processing delay");
                        break;
                    }
                }
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing correspondence " + correspondenceGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = phaseService.determineFinalStatus(successfulImports, failedImports);
        String message = String.format("Specific creation completed. Created: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, correspondenceGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Processes correspondence creation in a new transaction for better timeout handling
     */
    @Transactional(readOnly = false, timeout = 120, propagation = Propagation.REQUIRES_NEW, 
                   isolation = Isolation.READ_COMMITTED)
    public boolean processCorrespondenceCreationInNewTransaction(String correspondenceGuid) {
        try {
            logger.debug("Starting new transaction for correspondence: {}", correspondenceGuid);
            
            Optional<IncomingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (!migrationOpt.isPresent()) {
                logger.error("Migration record not found for correspondence: {}", correspondenceGuid);
                return false;
            }
            
            IncomingCorrespondenceMigration migration = migrationOpt.get();
            
            // Mark as in progress immediately
            migration.setCreationStatus("IN_PROGRESS");
            migration.setLastModifiedDate(LocalDateTime.now());
            
            // Save status change without forcing immediate commit
            migrationRepository.save(migration);
            
            // Process the creation
            boolean result = processCorrespondenceCreation(migration);
            
            // Update final status
            if (result) {
                migration.setCreationStatus("COMPLETED");
                migration.setCreationStep("COMPLETED");
                migration.setCurrentPhase("ASSIGNMENT");
                migration.setNextPhase("BUSINESS_LOG");
                migration.setPhaseStatus("PENDING");
                migration.setRetryCount(0); // Reset retry count on success
            } else {
                migration.setCreationStatus("ERROR");
                migration.setCreationError("Creation process failed");
                migration.setRetryCount(migration.getRetryCount() + 1);
                migration.setLastErrorAt(LocalDateTime.now());
            }
            
            // Save final status
            migrationRepository.save(migration);
            
            logger.info("Completed creation transaction for correspondence: {} with result: {}", 
                       correspondenceGuid, result);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error in creation transaction for correspondence: {}", correspondenceGuid, e);
            
            // Update error status in separate try-catch to ensure it gets saved
            try {
                Optional<IncomingCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
                if (migrationOpt.isPresent()) {
                    IncomingCorrespondenceMigration migration = migrationOpt.get();
                    migration.setCreationStatus("ERROR");
                    migration.setCreationError("Transaction failed: " + e.getMessage());
                    migration.setRetryCount(migration.getRetryCount() + 1);
                    migration.setLastErrorAt(LocalDateTime.now());
                    migrationRepository.save(migration);
                }
            } catch (Exception statusError) {
                logger.error("Error updating error status for correspondence: {}", correspondenceGuid, statusError);
            }
            
            return false;
        }
    }
    
    /**
     * Processes correspondence creation for a single correspondence without nested transactions
     * @deprecated Use processCorrespondenceCreationInNewTransaction for better transaction handling
     */
    @Deprecated
    private boolean processCorrespondenceCreationSimple(String correspondenceGuid) {
        try {
            logger.info("Starting creation process for correspondence: {}", correspondenceGuid);
            
            Optional<IncomingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (!migrationOpt.isPresent()) {
                logger.error("Migration record not found for correspondence: {}", correspondenceGuid);
                return false;
            }
            
            IncomingCorrespondenceMigration migration = migrationOpt.get();
            boolean result = processCorrespondenceCreation(migration);
            
            // Final status update
            if (result) {
                migration.setCreationStatus("COMPLETED");
                migration.setCreationStep("COMPLETED");
                migration.setCurrentPhase("ASSIGNMENT");
                migration.setNextPhase("BUSINESS_LOG");
                migration.setPhaseStatus("PENDING");
            } else {
                migration.setCreationStatus("ERROR");
                migration.setRetryCount(migration.getRetryCount() + 1);
                migration.setLastErrorAt(LocalDateTime.now());
            }
            
            migrationRepository.save(migration);
            
            logger.info("Completed creation process for correspondence: {} with result: {}", 
                       correspondenceGuid, result);
            return result;
        } catch (Exception e) {
            logger.error("Error in creation process for correspondence: {}", correspondenceGuid, e);
            
            // Update error status
            try {
                Optional<IncomingCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
                if (migrationOpt.isPresent()) {
                    IncomingCorrespondenceMigration migration = migrationOpt.get();
                    migration.setCreationStatus("ERROR");
                    migration.setCreationError(e.getMessage());
                    migration.setRetryCount(migration.getRetryCount() + 1);
                    migration.setLastErrorAt(LocalDateTime.now());
                    migrationRepository.save(migration);
                }
            } catch (Exception statusError) {
                logger.error("Error updating error status: {}", statusError.getMessage());
            }
            
            return false;
        }
    }
    
    /**
     * Updates the creation step for tracking progress with immediate commit
     */
    private void updateCreationStep(IncomingCorrespondenceMigration migration, String step) {
        try {
            migration.setCreationStep(step);
            migration.setLastModifiedDate(LocalDateTime.now());
            updateMigrationStatusImmediately(migration);
            logger.debug("Updated creation step to {} for correspondence: {}", step, migration.getCorrespondenceGuid());
        } catch (Exception e) {
            logger.warn("Error updating creation step to {}: {}", step, e.getMessage());
        }
    }
    
    /**
     * Processes the complete creation workflow for a single correspondence
     * This method runs within the new transaction created by processCorrespondenceCreationInNewTransaction
     */
    private boolean processCorrespondenceCreation(IncomingCorrespondenceMigration migration) {
        String correspondenceGuid = migration.getCorrespondenceGuid();
        logger.info("Processing creation for correspondence: {}", correspondenceGuid);
        
        try {
            // Step 1: Get correspondence details and attachments
            Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(correspondenceGuid);
            if (!correspondenceOpt.isPresent()) {
                logger.error("Correspondence not found: {}", correspondenceGuid);
                return false;
            }
            Correspondence correspondence = correspondenceOpt.get();
            
            CorrespondenceAttachment primaryAttachment = null;
            List<CorrespondenceAttachment> attachments = null;
            String documentId = null;
            
            // Step 1: Get Details and Attachments
            if ("GET_DETAILS".equals(migration.getCreationStep())) {
                logger.info("Step 1: Getting details and attachments for correspondence: {}", correspondenceGuid);
                attachments = attachmentRepository.findByDocGuid(correspondenceGuid);
                primaryAttachment = AttachmentUtils.findPrimaryAttachment(attachments);
                
                updateCreationStep(migration, "GET_ATTACHMENTS");
                migrationRepository.save(migration); // Save step progress
            }
            
            // Step 2: Get Attachments (already done above, move to upload)
            if ("GET_ATTACHMENTS".equals(migration.getCreationStep())) {
                if (attachments == null) {
                    attachments = attachmentRepository.findByDocGuid(correspondenceGuid);
                    primaryAttachment = AttachmentUtils.findPrimaryAttachment(attachments);
                }
                
                String batchId = null;
                
                // Step 3: Upload main attachment if exists
                if (primaryAttachment != null && AttachmentUtils.isValidForUpload(primaryAttachment)) {
                    logger.info("Step 3: Uploading main attachment for correspondence: {}", correspondenceGuid);
                    updateCreationStep(migration, "UPLOAD_MAIN_ATTACHMENT");
                    migrationRepository.saveAndFlush(migration); // Save step progress
                    
                    batchId = destinationService.createBatch();
                    if (batchId != null) {
                        migration.setBatchId(batchId);
                        migrationRepository.save(migration); // Save batch ID
                        
                        String fileData = AttachmentUtils.getFileDataForUpload(
                                primaryAttachment.getFileData(),
                                primaryAttachment.getName(),
                                true
                        );
                        
                        boolean uploaded = destinationService.uploadBase64FileToBatch(
                                batchId, "0", fileData,
                                AttachmentUtils.getFileNameForUpload(primaryAttachment.getName(), true)
                        );
                        
                        if (!uploaded) {
                            logger.error("Step 3 failed: Failed to upload primary attachment for correspondence: {}", correspondenceGuid);
                            return false;
                        }
                    } else {
                        logger.error("Step 3 failed: Failed to create batch for primary attachment upload: {}", correspondenceGuid);
                        return false;
                    }
                } else {
                    logger.info("No primary attachment found for correspondence: {}, proceeding without batch", correspondenceGuid);
                }
                
                // Move to next step
                updateCreationStep(migration, "CREATE_CORRESPONDENCE");
                migrationRepository.save(migration); // Save progress
            }
            
            // Step 4: Create correspondence
            if ("CREATE_CORRESPONDENCE".equals(migration.getCreationStep())) {
                logger.info("Step 4: Creating correspondence in destination: {}", correspondenceGuid);
                
                String batchId = migration.getBatchId(); // Get saved batch ID
                documentId = createCorrespondenceInDestination(correspondence, batchId);
                if (documentId == null) {
                    logger.error("Step 4 failed: Failed to create correspondence in destination system: {}", correspondenceGuid);
                    return false;
                }
                
                // CRITICAL: Save the created document ID immediately
                migration.setCreatedDocumentId(documentId);
                updateCreationStep(migration, "UPLOAD_OTHER_ATTACHMENTS");
                migrationRepository.save(migration); // Save with document ID
                
                logger.info("Successfully created correspondence with document ID: {} for correspondence: {}", documentId, correspondenceGuid);
            }
            
            // Resume processing: Get document ID if not available
            if (documentId == null) {
                documentId = migration.getCreatedDocumentId();
                if (documentId == null) {
                    logger.error("No document ID available for correspondence: {}", correspondenceGuid);
                    return false;
                }
            }
            
            // Step 5: Upload other attachments
            if ("UPLOAD_OTHER_ATTACHMENTS".equals(migration.getCreationStep())) {
                logger.info("Step 5: Uploading other attachments for correspondence: {}", correspondenceGuid);
                
                if (attachments == null) {
                    attachments = attachmentRepository.findByDocGuid(correspondenceGuid);
                    primaryAttachment = AttachmentUtils.findPrimaryAttachment(attachments);
                }
                
                boolean otherAttachmentsSuccess = uploadOtherAttachments(attachments, primaryAttachment, documentId);
                if (!otherAttachmentsSuccess) {
                    logger.error("Step 5 failed: Failed to upload other attachments for correspondence: {}", correspondenceGuid);
                    return false;
                }
                
                updateCreationStep(migration, "CREATE_PHYSICAL_ATTACHMENT");
                migrationRepository.save(migration); // Save progress
            }
            
            // Step 6: Create physical attachment
            if ("CREATE_PHYSICAL_ATTACHMENT".equals(migration.getCreationStep())) {
                logger.info("Step 6: Creating physical attachment for correspondence: {}", correspondenceGuid);
                boolean physicalAttachmentSuccess = createPhysicalAttachment(correspondence, documentId);
                if (!physicalAttachmentSuccess) {
                    logger.error("Step 6 failed: Failed to create physical attachment for correspondence: {}", correspondenceGuid);
                    return false;
                }
                
                updateCreationStep(migration, "SET_READY_TO_REGISTER");
                migrationRepository.save(migration); // Save progress
            }
            
            // Step 7: Set ready to register
            if ("SET_READY_TO_REGISTER".equals(migration.getCreationStep())) {
                logger.info("Step 7: Setting ready to register for correspondence: {}", correspondenceGuid);
                String asUserForRegister = correspondence.getCreationUserName() != null ?
                        correspondence.getCreationUserName() : "itba-emp1";
                
                boolean readyToRegisterSuccess = destinationService.setIncomingReadyToRegister(documentId, asUserForRegister);
                if (!readyToRegisterSuccess) {
                    logger.error("Step 7 failed: Failed to set ready to register for correspondence: {}", correspondenceGuid);
                    return false;
                }
                
                updateCreationStep(migration, "REGISTER_WITH_REFERENCE");
                migrationRepository.save(migration); // Save progress
            }
            
            // Step 8: Register with reference
            if ("REGISTER_WITH_REFERENCE".equals(migration.getCreationStep())) {
                logger.info("Step 8: Registering with reference for correspondence: {}", correspondenceGuid);
                boolean registerSuccess = registerCorrespondenceWithReference(correspondence, documentId);
                if (!registerSuccess) {
                    logger.error("Step 8 failed: Failed to register correspondence with reference: {}", correspondenceGuid);
                    return false;
                }
                
                updateCreationStep(migration, "START_WORK");
                migrationRepository.save(migration); // Save progress
                
                // Add delay for destination system processing
                try {
                    Thread.sleep(1000); // Reduced to 1 second
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Step 9: Start work
            if ("START_WORK".equals(migration.getCreationStep())) {
                logger.info("Step 9: Starting work for correspondence: {}", correspondenceGuid);
                String asUserForWork = correspondence.getCreationUserName() != null ?
                        correspondence.getCreationUserName() : "itba-emp1";
                
                boolean startWorkSuccess = destinationService.startIncomingCorrespondenceWork(documentId, asUserForWork);
                if (!startWorkSuccess) {
                    logger.error("Step 9 failed: Failed to start work for correspondence: {}", correspondenceGuid);
                    return false;
                }
                
                updateCreationStep(migration, "SET_OWNER");
                migrationRepository.save(migration); // Save progress
            }
            
            // Step 10: Set owner
            if ("SET_OWNER".equals(migration.getCreationStep())) {
                logger.info("Step 10: Setting owner for correspondence: {}", correspondenceGuid);
                String asUserForOwner = correspondence.getCreationUserName() != null ?
                        correspondence.getCreationUserName() : "itba-emp1";
                
                boolean setOwnerSuccess = destinationService.setCorrespondenceOwner(documentId, asUserForOwner);
                if (!setOwnerSuccess) {
                    logger.error("Step 10 failed: Failed to set owner for correspondence: {}", correspondenceGuid);
                    return false;
                }
                
                // Final step: Mark as completed
                updateCreationStep(migration, "COMPLETED");
                migrationRepository.save(migration); // Save final progress
                logger.info("Successfully completed creation for correspondence: {}", correspondenceGuid);
                return true;
            }
            
            logger.warn("Creation process reached end without completion for correspondence: {}", correspondenceGuid);
            return false;
        } catch (Exception e) {
            logger.error("Error in creation process for correspondence: {}", correspondenceGuid, e);
            
            // Update error status immediately
            try {
                migration.setCreationStatus("ERROR");
                migration.setCreationError("Step failed: " + e.getMessage());
                migration.setRetryCount(migration.getRetryCount() + 1);
                migration.setLastErrorAt(LocalDateTime.now());
                migrationRepository.save(migration); // Save error status
            } catch (Exception saveError) {
                logger.error("Error saving error status for correspondence: {}", correspondenceGuid, saveError);
            }
            
            return false;
        }
    }
    
    /**
     * Creates correspondence in destination system
     */
    private String createCorrespondenceInDestination(Correspondence correspondence, String batchId) {
        try {
            // Get the actual user from correspondence data
            String asUser = correspondence.getCreationUserName() != null ? 
                          correspondence.getCreationUserName() : "itba-emp1";
            
            // Map correspondence data for destination system
            String originalSubject = correspondence.getSubject();
            String subject = subjectGenerator.generateSubject(originalSubject);
            
            String externalRef = correspondence.getExternalReferenceNumber() != null ? 
                               correspondence.getExternalReferenceNumber() : "";
            String notes = correspondence.getNotes() != null ? CorrespondenceUtils.cleanHtmlTags(correspondence.getNotes()) : "";
            String referenceNo = correspondence.getReferenceNo() != null ? correspondence.getReferenceNo() : "";
            
            // Map category, priority, secrecy
            String category = CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid());
            
            // If random generation is enabled, always generate new subject
            if (subjectGenerator.isRandomSubjectEnabled()) {
                subject = subjectGenerator.generateSubjectWithCategory(category);
                logger.info("Generated random subject for correspondence {}: {}", correspondence.getGuid(), subject);
            }
            
            String priority = CorrespondenceUtils.mapPriority(correspondence.getPriorityId());
            String secrecyLevel = CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId());
            
            // Map agency
            String fromAgency = AgencyMappingUtils.mapAgencyGuidToCode(correspondence.getComingFromGuid());
            
            // Map department
            String toDepartment = "COF"; // Default department
/*            DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getFromDepartmentGuid());
            if (toDepartment == null) {
                toDepartment = "COF"; // Default department
            }*/
            
            // Convert dates to Hijri
            String gDueDate = correspondence.getDueDate() != null ? 
                            correspondence.getDueDate().toString() + "Z" : 
                            LocalDateTime.now().toString() + "Z";

            String hDueDate = correspondence.getDueDate() != null ? 
                            HijriDateUtils.convertToHijri(correspondence.getDueDate()) : 
                            HijriDateUtils.getCurrentHijriDate();
            
            String gDocumentDate = correspondence.getIncomingDate() != null ? 
                                 correspondence.getIncomingDate().toString() + "Z" :
                    correspondence.getCorrespondenceCreationDate().toString() + "Z";

            String hDocumentDate = correspondence.getIncomingDate() != null ? 
                                 HijriDateUtils.convertToHijri(correspondence.getIncomingDate()) : 
                                 HijriDateUtils.convertToHijri(correspondence.getCorrespondenceCreationDate());
            
            String gDate = gDocumentDate;
            String hDate = hDocumentDate;
            
            Boolean requireReply = CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus());
            String action = CorrespondenceUtils.mapAction(correspondence.getLastDecisionGuid());
            
            return destinationService.createIncomingCorrespondence(
                correspondence.getGuid(),
                asUser,
                gDate,
                subject,
                externalRef,
                notes,
                referenceNo,
                category,
                secrecyLevel,
                priority,
                gDueDate,
                hDueDate,
                requireReply,
                fromAgency,
                gDocumentDate,
                hDocumentDate,
                gDate,
                hDate,
                toDepartment,
                batchId,
                action
            );
            
        } catch (Exception e) {
            logger.error("Error creating correspondence in destination: {}", correspondence.getGuid(), e);
            return null;
        }
    }
    
    /**
     * Uploads other (non-primary) attachments
     */
    private boolean uploadOtherAttachments(List<CorrespondenceAttachment> allAttachments,
                                      CorrespondenceAttachment primaryAttachment, 
                                      String documentId) {
        try {
            List<CorrespondenceAttachment> otherAttachments = 
                AttachmentUtils.getNonPrimaryAttachments(allAttachments, primaryAttachment);
            
            for (CorrespondenceAttachment attachment : otherAttachments) {
                if (!AttachmentUtils.isValidForUpload(attachment)) {
                    logger.debug("Skipping invalid attachment: {}", attachment.getGuid());
                    continue;
                }
                
                // Create batch for this attachment
                String batchId = destinationService.createBatch();
                if (batchId == null) {
                    logger.warn("Failed to create batch for attachment: {}", attachment.getGuid());
                    return false;
                }
                
                // Upload file
                String fileData = AttachmentUtils.getFileDataForUpload(
                    attachment.getFileData(), 
                    attachment.getName(), 
                    false
                );
                
                boolean uploaded = destinationService.uploadBase64FileToBatch(
                    batchId, "0", fileData, 
                    AttachmentUtils.getFileNameForUpload(attachment.getName(), false)
                );
                
                if (!uploaded) {
                    logger.warn("Failed to upload attachment: {}", attachment.getGuid());
                    return false;
                }
                
                // Create attachment in destination
                boolean attachmentCreated = destinationService.createAttachment(attachment, batchId, documentId);
                if (!attachmentCreated) {
                    logger.warn("Failed to create attachment in destination: {}", attachment.getGuid());
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error uploading other attachments", e);
            return false;
        }
    }
    
    /**
     * Creates physical attachment if manual attachments count exists
     */
    private boolean createPhysicalAttachment(Correspondence correspondence, String documentId) {
        try {
            if (correspondence.getManualAttachmentsCount() != null && 
                !correspondence.getManualAttachmentsCount().trim().isEmpty()) {
                
                boolean success = destinationService.createPhysicalAttachment(
                    documentId,
                    correspondence.getCreationUserName(),
                    correspondence.getManualAttachmentsCount()
                );
                
                if (!success) {
                    logger.warn("Failed to create physical attachment for correspondence: {}", correspondence.getGuid());
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Error creating physical attachment", e);
            return false;
        }
    }
    
    /**
     * Registers correspondence with reference including required parameters
     */
    private boolean registerCorrespondenceWithReference(Correspondence correspondence, String documentId) {
        try {
            String asUser = correspondence.getCreationUserName() != null ? 
                          correspondence.getCreationUserName() : "itba-emp1";
            
            // Build the same context that was used in correspondence creation
            Map<String, Object> incCorrespondenceContext = buildCorrespondenceContext(correspondence);
            
            // Get action and department for the required parameters
            String action = CorrespondenceUtils.mapAction(correspondence.getLastDecisionGuid());
            String toDepartment = DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getFromDepartmentGuid());
            if (toDepartment == null) {
                toDepartment = "COF"; // Default department
            }
            
            boolean success = destinationService.registerWithReference(
                documentId, 
                asUser, 
                incCorrespondenceContext,
                action,
                toDepartment
            );
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error registering correspondence with reference", e);
            return false;
        }
    }
    
    /**
     * Builds correspondence context for API calls
     */
    private Map<String, Object> buildCorrespondenceContext(Correspondence correspondence) {
        Map<String, Object> context = new HashMap<>();
        String finalSubject = correspondence.getSubject() ;
        if (subjectGenerator.isRandomSubjectEnabled()) {
            finalSubject = subjectGenerator.generateSubjectWithCategory("General");
        }

        context.put("corr:subject", finalSubject);
        context.put("corr:externalCorrespondenceNumber", 
                   correspondence.getExternalReferenceNumber() != null ? correspondence.getExternalReferenceNumber() : "");
        context.put("corr:remarks", correspondence.getNotes() != null ? correspondence.getNotes() : "");
        context.put("corr:referenceNumber", correspondence.getReferenceNo() != null ? correspondence.getReferenceNo() : "");
        context.put("corr:category", CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid()));
        context.put("corr:secrecyLevel", CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId()));
        context.put("corr:priority", CorrespondenceUtils.mapPriority(correspondence.getPriorityId()));
        
        // Convert dates
        String gDueDate = correspondence.getDueDate() != null ? 
                        correspondence.getDueDate().toString() + "Z" : 
                        LocalDateTime.now().toString() + "Z";
        String hDueDate = correspondence.getDueDate() != null ? 
                        HijriDateUtils.convertToHijri(correspondence.getDueDate()) : 
                        HijriDateUtils.getCurrentHijriDate();
        
        context.put("corr:gDueDate", gDueDate);
        context.put("corr:hDueDate", hDueDate);
        context.put("corr:requireReply", CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus()));
        context.put("corr:fromAgency", AgencyMappingUtils.mapAgencyGuidToCode(correspondence.getComingFromGuid()));

        String gDocumentDate = correspondence.getIncomingDate() != null ?
                correspondence.getIncomingDate().toString() + "Z" :
                correspondence.getCorrespondenceCreationDate().toString() + "Z";

        String hDocumentDate = correspondence.getIncomingDate() != null ?
                HijriDateUtils.convertToHijri(correspondence.getIncomingDate()) :
                HijriDateUtils.convertToHijri(correspondence.getCorrespondenceCreationDate());

        String gDate = gDocumentDate;
        String hDate = hDocumentDate;

        context.put("corr:gDocumentDate", gDocumentDate);
        context.put("corr:hDocumentDate", hDocumentDate);
        context.put("corr:gDate", gDate);
        context.put("corr:hDate", hDate);
        context.put("corr:delivery", "unknown");
        context.put("corr:toAgency", "ITBA");
        
        return context;
    }
    
    /**
     * Gets creation migrations for UI display
     */
    @Transactional(readOnly = true, timeout = 60)
    public List<IncomingCorrespondenceMigration> getCreationMigrations() {
        try {
            List<IncomingCorrespondenceMigration> migrations = migrationRepository.findAll();
            logger.info("Retrieved {} creation migrations", migrations.size());
            return migrations;
        } catch (Exception e) {
            logger.error("Error getting creation migrations", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gets creation migrations with correspondence details for UI display
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getCreationMigrationsWithDetails() {
        try {
            List<IncomingCorrespondenceMigration> migrations = migrationRepository.findAll();
            List<Map<String, Object>> migrationsWithDetails = new ArrayList<>();
            
            for (IncomingCorrespondenceMigration migration : migrations) {
                Map<String, Object> migrationData = new HashMap<>();
                
                // Copy migration fields
                migrationData.put("id", migration.getId());
                migrationData.put("correspondenceGuid", migration.getCorrespondenceGuid());
                migrationData.put("currentPhase", migration.getCurrentPhase());
                migrationData.put("phaseStatus", migration.getPhaseStatus());
                migrationData.put("creationStep", migration.getCreationStep());
                migrationData.put("creationStatus", migration.getCreationStatus());
                migrationData.put("creationError", migration.getCreationError());
                migrationData.put("createdDocumentId", migration.getCreatedDocumentId());
                migrationData.put("batchId", migration.getBatchId());
                migrationData.put("retryCount", migration.getRetryCount());
                migrationData.put("startedAt", migration.getStartedAt());
                migrationData.put("lastModifiedDate", migration.getLastModifiedDate());
                
                // Get correspondence details
                Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(migration.getCorrespondenceGuid());
                if (correspondenceOpt.isPresent()) {
                    Correspondence correspondence = correspondenceOpt.get();
                    migrationData.put("correspondenceSubject", correspondence.getSubject());
                    migrationData.put("correspondenceReferenceNo", correspondence.getReferenceNo());
                    migrationData.put("creationUserName", correspondence.getCreationUserName());
                } else {
                    migrationData.put("correspondenceSubject", null);
                    migrationData.put("correspondenceReferenceNo", null);
                    migrationData.put("creationUserName", null);
                }
                
                migrationsWithDetails.add(migrationData);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", migrationsWithDetails);
            result.put("totalElements", migrations.size());
            
            logger.info("Retrieved {} creation migrations with details", migrations.size());
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting creation migrations with details", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("content", new ArrayList<>());
            errorResult.put("totalElements", 0);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * Gets creation statistics for UI display
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getCreationStatistics() {
        try {
            List<IncomingCorrespondenceMigration> migrations = migrationRepository.findAll();
            
            Map<String, Object> statistics = new HashMap<>();
            
            // Calculate status counts
            long total = migrations.size();
            long completed = migrations.stream().mapToLong(m -> "COMPLETED".equals(m.getCreationStatus()) ? 1 : 0).sum();
            long pending = migrations.stream().mapToLong(m -> "PENDING".equals(m.getCreationStatus()) ? 1 : 0).sum();
            long error = migrations.stream().mapToLong(m -> "ERROR".equals(m.getCreationStatus()) ? 1 : 0).sum();
            
            statistics.put("total", total);
            statistics.put("completed", completed);
            statistics.put("pending", pending);
            statistics.put("error", error);
            
            // Calculate step statistics
            Map<String, Long> stepCounts = new HashMap<>();
            for (IncomingCorrespondenceMigration migration : migrations) {
                String step = migration.getCreationStep() != null ? migration.getCreationStep() : "UNKNOWN";
                stepCounts.put(step, stepCounts.getOrDefault(step, 0L) + 1);
            }
            
            List<Map<String, Object>> stepStatistics = new ArrayList<>();
            for (Map.Entry<String, Long> entry : stepCounts.entrySet()) {
                Map<String, Object> stepStat = new HashMap<>();
                stepStat.put("step", entry.getKey());
                stepStat.put("count", entry.getValue());
                stepStatistics.add(stepStat);
            }
            
            statistics.put("stepStatistics", stepStatistics);
            
            logger.info("Generated creation statistics: total={}, completed={}, pending={}, error={}", 
                       total, completed, pending, error);
            
            return statistics;
            
        } catch (Exception e) {
            logger.error("Error getting creation statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("total", 0L);
            errorStats.put("completed", 0L);
            errorStats.put("pending", 0L);
            errorStats.put("error", 0L);
            errorStats.put("stepStatistics", new ArrayList<>());
            errorStats.put("error", e.getMessage());
            return errorStats;
        }
    }
    
    /**
     * Updates migration status with immediate database commit using a separate transaction
     */
    private void updateMigrationStatusImmediately(IncomingCorrespondenceMigration migration) {
        try {
            // Use simple save without forcing immediate commit to reduce lock contention
            migrationRepository.save(migration);
            logger.debug("Updated migration status for correspondence: {} - Status: {}, Step: {}", 
                        migration.getCorrespondenceGuid(), migration.getCreationStatus(), migration.getCreationStep());
        } catch (Exception e) {
            logger.warn("Error updating migration status for correspondence: {} - {}", 
                       migration.getCorrespondenceGuid(), e.getMessage());
            // Don't throw exception to prevent cascading failures
        }
    }
}