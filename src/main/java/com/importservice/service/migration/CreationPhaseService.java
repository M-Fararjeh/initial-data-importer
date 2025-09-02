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
    @Transactional(timeout = 600)
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
    @Transactional(timeout = 300)
    public ImportResponseDto executeCreationForSpecific(List<String> correspondenceGuids) {
        logger.info("Starting creation for {} specific correspondences", correspondenceGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String correspondenceGuid : correspondenceGuids) {
            try {
                boolean success = processCorrespondenceCreation(correspondenceGuid);
                if (success) {
                    successfulImports++;
                    phaseService.updatePhaseStatus(correspondenceGuid, "CREATION", "COMPLETED", null);
                } else {
                    failedImports++;
                }
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing correspondence " + correspondenceGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                phaseService.updatePhaseStatus(correspondenceGuid, "CREATION", "ERROR", errorMsg);
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
     * Processes correspondence creation for a single correspondence
     */
    private boolean processCorrespondenceCreation(String correspondenceGuid) {
        try {
            Optional<IncomingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (!migrationOpt.isPresent()) {
                logger.error("Migration record not found for correspondence: {}", correspondenceGuid);
                return false;
            }
            
            return processCorrespondenceCreationWorkflow(migrationOpt.get());
        } catch (Exception e) {
            logger.error("Error in new transaction for correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Processes the complete creation workflow for a single correspondence
     */
    private boolean processCorrespondenceCreationWorkflow(IncomingCorrespondenceMigration migration) {
        String correspondenceGuid = migration.getCorrespondenceGuid();
        logger.info("Processing creation for correspondence: {}", correspondenceGuid);
        
        try {
            // Step 1: Get correspondence details
            updateCreationStep(migration, "GET_DETAILS");
            Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(correspondenceGuid);
            if (!correspondenceOpt.isPresent()) {
                logger.error("Correspondence not found: {}", correspondenceGuid);
                migration.setCreationStatus("ERROR");
                migration.setCreationError("Correspondence not found: " + correspondenceGuid);
                migrationRepository.save(migration);
                return false;
            }
            Correspondence correspondence = correspondenceOpt.get();
            
            // Step 2: Get attachments
            updateCreationStep(migration, "GET_ATTACHMENTS");
            List<CorrespondenceAttachment> attachments = attachmentRepository.findByDocGuid(correspondenceGuid);
            CorrespondenceAttachment primaryAttachment = AttachmentUtils.findPrimaryAttachment(attachments);
            
            String batchId = null;
            
            // Step 3: Upload main attachment if exists
            if (primaryAttachment != null && AttachmentUtils.isValidForUpload(primaryAttachment)) {
                updateCreationStep(migration, "UPLOAD_MAIN_ATTACHMENT");
                batchId = destinationService.createBatch();
                if (batchId != null) {
                    migration.setBatchId(batchId);
                    migrationRepository.save(migration);
                    
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
                        logger.warn("Failed to upload primary attachment for correspondence: {}", correspondenceGuid);
                        migration.setCreationStatus("ERROR");
                        migration.setCreationError("Failed to upload primary attachment");
                        migrationRepository.save(migration);
                        return false;
                    }
                } else {
                    logger.error("Failed to create batch for primary attachment upload: {}", correspondenceGuid);
                    migration.setCreationStatus("ERROR");
                    migration.setCreationError("Failed to create batch for primary attachment");
                    migrationRepository.save(migration);
                    return false;
                }
            }
            
            // Step 4: Create correspondence
            updateCreationStep(migration, "CREATE_CORRESPONDENCE");
            String documentId = createCorrespondenceInDestination(correspondence, batchId);
            if (documentId == null) {
                logger.error("Failed to create correspondence in destination system: {}", correspondenceGuid);
                migration.setCreationStatus("ERROR");
                migration.setCreationError("Failed to create correspondence in destination system");
                migrationRepository.save(migration);
                return false;
            }
            
            migration.setCreatedDocumentId(documentId);
            migrationRepository.save(migration);
            
            // Step 5: Upload other attachments
            updateCreationStep(migration, "UPLOAD_OTHER_ATTACHMENTS");
            boolean otherAttachmentsSuccess = uploadOtherAttachments(attachments, primaryAttachment, documentId);
            if (!otherAttachmentsSuccess) {
                logger.error("Failed to upload other attachments for correspondence: {}", correspondenceGuid);
                migration.setCreationStatus("ERROR");
                migration.setCreationError("Failed to upload other attachments");
                migrationRepository.save(migration);
                return false;
            }
            
            // Step 6: Create physical attachment
            updateCreationStep(migration, "CREATE_PHYSICAL_ATTACHMENT");
            boolean physicalAttachmentSuccess = createPhysicalAttachment(correspondence, documentId);
            if (!physicalAttachmentSuccess) {
                logger.error("Failed to create physical attachment for correspondence: {}", correspondenceGuid);
                migration.setCreationStatus("ERROR");
                migration.setCreationError("Failed to create physical attachment");
                migrationRepository.save(migration);
                return false;
            }
            
            // Step 7: Set ready to register
            updateCreationStep(migration, "SET_READY_TO_REGISTER");
            String asUserForRegister = correspondence.getCreationUserName() != null ? 
                                     correspondence.getCreationUserName() : "itba-emp1";
            boolean readyToRegisterSuccess = destinationService.setIncomingReadyToRegister(documentId, asUserForRegister);
            if (!readyToRegisterSuccess) {
                String errorMsg = "Failed to set ready to register for correspondence: " + correspondenceGuid;
                logger.error(errorMsg);
                migration.setCreationStatus("ERROR");
                migration.setCreationError(errorMsg);
                migration.setCreationStep("SET_READY_TO_REGISTER");
                migrationRepository.save(migration);
                return false;
            }
            
            // Step 8: Register with reference
            updateCreationStep(migration, "REGISTER_WITH_REFERENCE");
            boolean registerSuccess = registerCorrespondenceWithReference(correspondence, documentId);
            if (!registerSuccess) {
                String errorMsg = "Failed to register correspondence with reference: " + correspondenceGuid;
                logger.error(errorMsg);
                migration.setCreationStatus("ERROR");
                migration.setCreationError(errorMsg);
                migration.setCreationStep("REGISTER_WITH_REFERENCE");
                migrationRepository.save(migration);
                return false;
            }
            
            // Step 9: Start work
            updateCreationStep(migration, "START_WORK");
            String asUserForWork = correspondence.getCreationUserName() != null ? 
                                 correspondence.getCreationUserName() : "itba-emp1";
            boolean startWorkSuccess = destinationService.startIncomingCorrespondenceWork(documentId, asUserForWork);
            if (!startWorkSuccess) {
                String errorMsg = "Failed to start work for correspondence: " + correspondenceGuid;
                logger.error(errorMsg);
                migration.setCreationStatus("ERROR");
                migration.setCreationError(errorMsg);
                migration.setCreationStep("START_WORK");
                migrationRepository.save(migration);
                return false;
            }
            
            // Step 10: Set owner
            updateCreationStep(migration, "SET_OWNER");
            String asUserForOwner = correspondence.getCreationUserName() != null ? 
                                  correspondence.getCreationUserName() : "itba-emp1";
            boolean setOwnerSuccess = destinationService.setCorrespondenceOwner(documentId, asUserForOwner);
            if (!setOwnerSuccess) {
                String errorMsg = "Failed to set owner for correspondence: " + correspondenceGuid;
                logger.error(errorMsg);
                migration.setCreationStatus("ERROR");
                migration.setCreationError(errorMsg);
                migration.setCreationStep("SET_OWNER");
                migrationRepository.save(migration);
                return false;
            }
            
            // Mark as completed
            updateCreationStep(migration, "COMPLETED");
            migration.setCreationStatus("COMPLETED");
            migrationRepository.save(migration);
            
            logger.info("Successfully completed creation for correspondence: {}", correspondenceGuid);
            return true;
            
        } catch (Exception e) {
            logger.error("Error in creation process for correspondence: {}", correspondenceGuid, e);
            migration.setCreationStatus("ERROR");
            migration.setCreationError(e.getMessage());
            migrationRepository.save(migration);
            return false;
        }
    }
    
    /**
     * Updates the creation step for tracking progress
     */
    private void updateCreationStep(IncomingCorrespondenceMigration migration, String step) {
        migration.setCreationStep(step);
        migrationRepository.save(migration);
        logger.debug("Updated creation step to {} for correspondence: {}", step, migration.getCorrespondenceGuid());
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
}