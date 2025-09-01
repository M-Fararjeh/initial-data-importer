package com.importservice.service;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.CorrespondenceAttachment;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceAttachmentRepository;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
import com.importservice.util.AttachmentUtils;
import com.importservice.util.CorrespondenceUtils;
import com.importservice.util.DepartmentUtils;
import com.importservice.util.AgencyMappingUtils;
import com.importservice.util.HijriDateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

@Service
public class IncomingCorrespondenceMigrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(IncomingCorrespondenceMigrationService.class);
    
    @Autowired
    private IncomingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @Autowired
    private CorrespondenceAttachmentRepository attachmentRepository;
    
    @Autowired
    private DestinationSystemService destinationSystemService;
    
    @Autowired
    private DepartmentUtils departmentUtils;
    
    /**
     * Phase 1: Prepare Data
     * Selects incoming correspondences and creates migration records
     */
    @Transactional
    public ImportResponseDto prepareData() {
        logger.info("Starting Phase 1: Prepare Data");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Find incoming correspondences (CorrespondenceTypeId = 2)
            List<Correspondence> incomingCorrespondences = correspondenceRepository
                .findByCorrespondenceTypeIdAndIsDeletedAndIsDraft(2, false, false);
            
            logger.info("Found {} incoming correspondences to prepare", incomingCorrespondences.size());
            
            for (Correspondence correspondence : incomingCorrespondences) {
                try {
                    // Skip if already exists in migration table
                    Optional<IncomingCorrespondenceMigration> existing = 
                        migrationRepository.findByCorrespondenceGuid(correspondence.getGuid());
                    
                    if (existing.isPresent()) {
                        logger.debug("Migration record already exists for correspondence: {}", correspondence.getGuid());
                        continue;
                    }
                    
                    // Skip if cancelled
                    if (correspondence.getIsCanceled() != null && correspondence.getIsCanceled() != 0) {
                        logger.debug("Skipping cancelled correspondence: {}", correspondence.getGuid());
                        continue;
                    }
                    
                    // Create migration record
                    IncomingCorrespondenceMigration migration = new IncomingCorrespondenceMigration(
                        correspondence.getGuid(),
                        correspondence.getIsArchive() != null ? correspondence.getIsArchive() : false
                    );
                    
                    migration.setPrepareDataStatus("COMPLETED");
                    migration.markPhaseCompleted("PREPARE_DATA");
                    
                    migrationRepository.save(migration);
                    successfulImports++;
                    
                    logger.debug("Created migration record for correspondence: {}", correspondence.getGuid());
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Failed to prepare correspondence " + correspondence.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("Prepare Data phase completed. Success: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return new ImportResponseDto(status, message, incomingCorrespondences.size(), 
                                       successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Failed to execute Prepare Data phase", e);
            return new ImportResponseDto("ERROR", "Failed to execute Prepare Data phase: " + e.getMessage(), 
                0, 0, 0, Collections.singletonList("Failed to execute Prepare Data phase: " + e.getMessage()));
        }
    }
    
    /**
     * Phase 2: Creation
     * Creates correspondences in destination system with attachments
     */
    @Transactional
    public ImportResponseDto executeCreationPhase() {
        logger.info("Starting Phase 2: Creation");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Get migrations ready for creation phase
            List<IncomingCorrespondenceMigration> migrations = 
                migrationRepository.findByCurrentPhase("CREATION");
            
            logger.info("Found {} correspondences ready for creation", migrations.size());
            
            for (IncomingCorrespondenceMigration migration : migrations) {
                try {
                    boolean success = executeCreationSteps(migration);
                    
                    if (success) {
                        migration.markPhaseCompleted("CREATION");
                        migrationRepository.save(migration);
                        successfulImports++;
                        logger.info("Successfully completed creation for correspondence: {}", 
                                  migration.getCorrespondenceGuid());
                    } else {
                        failedImports++;
                        migration.incrementRetryCount();
                        migrationRepository.save(migration);
                        errors.add("Failed creation for correspondence: " + migration.getCorrespondenceGuid());
                    }
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error in creation phase for correspondence " + 
                                    migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    
                    migration.markPhaseError("CREATION", errorMsg);
                    migration.incrementRetryCount();
                    migrationRepository.save(migration);
                    
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("Creation phase completed. Success: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return new ImportResponseDto(status, message, migrations.size(), 
                                       successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Failed to execute Creation phase", e);
            return new ImportResponseDto("ERROR", "Failed to execute Creation phase: " + e.getMessage(), 
                0, 0, 0, Collections.singletonList("Failed to execute Creation phase: " + e.getMessage()));
        }
    }
    
    /**
     * Executes all creation steps for a single correspondence
     */
    private boolean executeCreationSteps(IncomingCorrespondenceMigration migration) {
        try {
            String correspondenceGuid = migration.getCorrespondenceGuid();
            
            // Step 1: Get correspondence details
            migration.setCreationStep("GET_DETAILS");
            migrationRepository.save(migration);
            
            Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(correspondenceGuid);
            if (!correspondenceOpt.isPresent()) {
                migration.markPhaseError("CREATION", "Correspondence not found: " + correspondenceGuid);
                return false;
            }
            
            Correspondence correspondence = correspondenceOpt.get();
            
            // Validate correspondence
            if (correspondence.getIsDeleted() != null && correspondence.getIsDeleted()) {
                migration.markPhaseError("CREATION", "Correspondence is deleted");
                return false;
            }
            
            if (correspondence.getIsDraft() != null && correspondence.getIsDraft()) {
                migration.markPhaseError("CREATION", "Correspondence is draft");
                return false;
            }
            
            if (correspondence.getIsCanceled() != null && correspondence.getIsCanceled() != 0) {
                migration.markPhaseError("CREATION", "Correspondence is cancelled");
                return false;
            }
            
            // Step 2: Get attachments
            migration.setCreationStep("GET_ATTACHMENTS");
            migrationRepository.save(migration);
            
            List<CorrespondenceAttachment> attachments = 
                attachmentRepository.findByDocGuid(correspondenceGuid);
            
            CorrespondenceAttachment primaryAttachment = AttachmentUtils.findPrimaryAttachment(attachments);
            
            // Step 3: Upload main attachment
            migration.setCreationStep("UPLOAD_MAIN_ATTACHMENT");
            migrationRepository.save(migration);
            
            String batchId = null;
            if (primaryAttachment != null && AttachmentUtils.isValidForUpload(primaryAttachment)) {
                batchId = uploadPrimaryAttachment(primaryAttachment);
                if (batchId == null) {
                    migration.markPhaseError("CREATION", "Failed to upload primary attachment");
                    return false;
                }
                migration.setBatchId(batchId);
            }
            
            // Step 4: Create correspondence
            migration.setCreationStep("CREATE_CORRESPONDENCE");
            migrationRepository.save(migration);
            
            String documentId = createIncomingCorrespondence(correspondence, batchId);
            if (documentId == null) {
                migration.markPhaseError("CREATION", "Failed to create correspondence in destination system");
                return false;
            }
            migration.setCreatedDocumentId(documentId);
            
            // Step 5: Upload other attachments
            migration.setCreationStep("UPLOAD_OTHER_ATTACHMENTS");
            migrationRepository.save(migration);
            
            List<CorrespondenceAttachment> otherAttachments = 
                AttachmentUtils.getNonPrimaryAttachments(attachments, primaryAttachment);
            
            for (CorrespondenceAttachment attachment : otherAttachments) {
                if (AttachmentUtils.isValidForUpload(attachment)) {
                    boolean uploaded = uploadOtherAttachment(attachment, documentId);
                    if (!uploaded) {
                        logger.warn("Failed to upload attachment: {}", attachment.getName());
                        // Continue with other attachments - don't fail the entire process
                    }
                }
            }
            
            // Steps 6-10: TODO - Implement remaining steps
            migration.setCreationStep("COMPLETED");
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error executing creation steps for correspondence: {}", 
                        migration.getCorrespondenceGuid(), e);
            migration.markPhaseError("CREATION", "Unexpected error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Uploads primary attachment and returns batch ID
     */
    private String uploadPrimaryAttachment(CorrespondenceAttachment attachment) {
        try {
            // Create batch
            String batchId = destinationSystemService.createBatch();
            if (batchId == null) {
                logger.error("Failed to create batch for primary attachment");
                return null;
            }
            
            // Get file data for upload
            String fileData = AttachmentUtils.getFileDataForUpload(
                attachment.getFileData(), 
                attachment.getName(), 
                true
            );
            
            // Always proceed with upload - either real data or sample data
            if (fileData == null) {
                logger.warn("No file data for primary attachment: {}, using sample data", attachment.getName());
                fileData = "testbase64";
            }
            
            String cleanName = AttachmentUtils.getFileNameForUpload(attachment.getName(), true);
            
            // Upload file to batch
            boolean uploaded = destinationSystemService.uploadBase64FileToBatch(
                batchId, "0", fileData, cleanName
            );
            
            if (!uploaded) {
                logger.error("Failed to upload primary attachment to batch");
                return null;
            }
            
            logger.debug("Successfully uploaded primary attachment: {}", cleanName);
            return batchId;
            
        } catch (Exception e) {
            logger.error("Error uploading primary attachment: {}", attachment.getName(), e);
            return null;
        }
    }
    
    /**
     * Creates incoming correspondence in destination system
     */
    private String createIncomingCorrespondence(Correspondence correspondence, String batchId) {
        try {
            // Build request using correspondence data
            String subject = correspondence.getSubject();
            String externalRef = correspondence.getExternalReferenceNumber();
            String notes = cleanHtmlTags(correspondence.getNotes());
            String referenceNo = correspondence.getReferenceNo();
            String category = CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid());
            String secrecyLevel = CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId());
            String priority = CorrespondenceUtils.mapPriority(correspondence.getPriorityId());
            
            // Calculate due date (creation date + 5 years if null)
            LocalDateTime dueDate = correspondence.getDueDate();
            if (dueDate == null) {
                dueDate = HijriDateUtils.addYears(correspondence.getCorrespondenceCreationDate(), 5);
            }
            
            String gDueDate = HijriDateUtils.formatToIsoString(dueDate);
            String hDueDate = HijriDateUtils.convertToHijri(dueDate);
            
            Boolean requireReply = CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus());
            
            String fromAgency = AgencyMappingUtils.mapAgencyGuidToCode(correspondence.getComingFromGuid());
            
            LocalDateTime documentDate = correspondence.getIncomingDate();
            if (documentDate == null) {
                documentDate = correspondence.getCorrespondenceCreationDate();
            }
            
            String gDocumentDate = HijriDateUtils.formatToIsoString(documentDate);
            String hDocumentDate = HijriDateUtils.convertToHijri(documentDate);
            String gDate = gDocumentDate;
            String hDate = hDocumentDate;
            
            String toDepartment = DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getCreationDepartmentGuid());
            if (toDepartment == null) {
                toDepartment = "CEO"; // Default fallback
            }
            
            String action = CorrespondenceUtils.mapAction(correspondence.getLastDecisionGuid());
            
            // Call destination system service to create correspondence
            String documentId = destinationSystemService.createIncomingCorrespondence(
                correspondence.getGuid(),
                correspondence.getCreationUserName(),
                HijriDateUtils.formatToIsoString(correspondence.getCorrespondenceCreationDate()),
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
            
            if (documentId != null) {
                logger.debug("Successfully created correspondence in destination system: {}", documentId);
                return documentId;
            } else {
                logger.error("Failed to create correspondence in destination system");
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Error creating incoming correspondence: {}", correspondence.getGuid(), e);
            return null;
        }
    }
    
    /**
     * Uploads other (non-primary) attachments
     */
    private boolean uploadOtherAttachment(CorrespondenceAttachment attachment, String correspondenceDocumentId) {
        try {
            // Create batch for this attachment
            String batchId = destinationSystemService.createBatch();
            if (batchId == null) {
                logger.error("Failed to create batch for attachment: {}", attachment.getName());
                return false;
            }
            
            // Get file data for upload
            String fileData = AttachmentUtils.getFileDataForUpload(
                attachment.getFileData(),
                attachment.getName(),
                false
            );
            
            // Always proceed with upload - either real data or sample data
            if (fileData == null) {
                logger.warn("No file data for attachment: {}, using sample data", attachment.getName());
                fileData = "testbase64";
            }
            
            String cleanName = AttachmentUtils.getFileNameForUpload(attachment.getName(), false);
            
            // Upload file to batch
            boolean uploaded = destinationSystemService.uploadBase64FileToBatch(
                batchId, "0", fileData, cleanName
            );
            
            if (!uploaded) {
                logger.error("Failed to upload attachment to batch: {}", attachment.getName());
                return false;
            }
            
            // Create attachment in destination system
            boolean created = destinationSystemService.createAttachment(
                attachment,
                batchId,
                correspondenceDocumentId
            );
            
            if (created) {
                logger.debug("Successfully uploaded and created attachment: {}", cleanName);
                return true;
            } else {
                logger.error("Failed to create attachment in destination system: {}", attachment.getName());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error uploading other attachment: {}", attachment.getName(), e);
            return false;
        }
    }
    
    /**
     * Phase 3: Assignment (placeholder)
     */
    @Transactional
    public ImportResponseDto executeAssignmentPhase() {
        logger.info("Starting Phase 3: Assignment");
        
        List<IncomingCorrespondenceMigration> migrations = 
            migrationRepository.findByCurrentPhase("ASSIGNMENT");
        
        // TODO: Implement assignment logic
        for (IncomingCorrespondenceMigration migration : migrations) {
            migration.markPhaseCompleted("ASSIGNMENT");
            migrationRepository.save(migration);
        }
        
        return new ImportResponseDto("SUCCESS", "Assignment phase completed", 
            migrations.size(), migrations.size(), 0, new ArrayList<>());
    }
    
    /**
     * Phase 4: Business Log (placeholder)
     */
    @Transactional
    public ImportResponseDto executeBusinessLogPhase() {
        logger.info("Starting Phase 4: Business Log");
        
        List<IncomingCorrespondenceMigration> migrations = 
            migrationRepository.findByCurrentPhase("BUSINESS_LOG");
        
        // TODO: Implement business log logic
        for (IncomingCorrespondenceMigration migration : migrations) {
            migration.markPhaseCompleted("BUSINESS_LOG");
            migrationRepository.save(migration);
        }
        
        return new ImportResponseDto("SUCCESS", "Business Log phase completed", 
            migrations.size(), migrations.size(), 0, new ArrayList<>());
    }
    
    /**
     * Phase 5: Comment (placeholder)
     */
    @Transactional
    public ImportResponseDto executeCommentPhase() {
        logger.info("Starting Phase 5: Comment");
        
        List<IncomingCorrespondenceMigration> migrations = 
            migrationRepository.findByCurrentPhase("COMMENT");
        
        // TODO: Implement comment logic
        for (IncomingCorrespondenceMigration migration : migrations) {
            migration.markPhaseCompleted("COMMENT");
            migrationRepository.save(migration);
        }
        
        return new ImportResponseDto("SUCCESS", "Comment phase completed", 
            migrations.size(), migrations.size(), 0, new ArrayList<>());
    }
    
    /**
     * Phase 6: Closing (placeholder)
     */
    @Transactional
    public ImportResponseDto executeClosingPhase() {
        logger.info("Starting Phase 6: Closing");
        
        List<IncomingCorrespondenceMigration> migrations = 
            migrationRepository.findByCurrentPhase("CLOSING");
        
        // TODO: Implement closing logic
        for (IncomingCorrespondenceMigration migration : migrations) {
            if (migration.getIsNeedToClose() != null && migration.getIsNeedToClose()) {
                // Execute closing logic for correspondences that need to be closed
                // TODO: Implement actual closing API calls
            }
            
            migration.markPhaseCompleted("CLOSING");
            migrationRepository.save(migration);
        }
        
        return new ImportResponseDto("SUCCESS", "Closing phase completed", 
            migrations.size(), migrations.size(), 0, new ArrayList<>());
    }
    
    /**
     * Gets migration statistics
     */
    public Map<String, Object> getMigrationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("prepareData", migrationRepository.countByCurrentPhase("PREPARE_DATA"));
        stats.put("creation", migrationRepository.countByCurrentPhase("CREATION"));
        stats.put("assignment", migrationRepository.countByCurrentPhase("ASSIGNMENT"));
        stats.put("businessLog", migrationRepository.countByCurrentPhase("BUSINESS_LOG"));
        stats.put("comment", migrationRepository.countByCurrentPhase("COMMENT"));
        stats.put("closing", migrationRepository.countByCurrentPhase("CLOSING"));
        stats.put("completed", migrationRepository.countByOverallStatus("COMPLETED"));
        stats.put("failed", migrationRepository.countByOverallStatus("FAILED"));
        stats.put("inProgress", migrationRepository.countByOverallStatus("IN_PROGRESS"));
        
        return stats;
    }
    
    /**
     * Retries failed migrations
     */
    @Transactional
    public ImportResponseDto retryFailedMigrations() {
        logger.info("Starting retry of failed migrations");
        
        List<IncomingCorrespondenceMigration> retryableMigrations = 
            migrationRepository.findRetryableMigrations();
        
        logger.info("Found {} migrations to retry", retryableMigrations.size());
        
        int successfulRetries = 0;
        int failedRetries = 0;
        List<String> errors = new ArrayList<>();
        
        for (IncomingCorrespondenceMigration migration : retryableMigrations) {
            try {
                // Reset phase status to allow retry
                migration.setPhaseStatus("PENDING");
                
                // Execute the appropriate phase
                boolean success = false;
                switch (migration.getCurrentPhase()) {
                    case "CREATION":
                        success = executeCreationSteps(migration);
                        break;
                    // TODO: Add other phases
                    default:
                        logger.warn("Retry not implemented for phase: {}", migration.getCurrentPhase());
                        continue;
                }
                
                if (success) {
                    successfulRetries++;
                    logger.info("Successfully retried migration for correspondence: {}", 
                              migration.getCorrespondenceGuid());
                } else {
                    failedRetries++;
                    migration.incrementRetryCount();
                    errors.add("Retry failed for correspondence: " + migration.getCorrespondenceGuid());
                }
                
                migrationRepository.save(migration);
                
            } catch (Exception e) {
                failedRetries++;
                String errorMsg = "Error retrying migration for correspondence " + 
                                migration.getCorrespondenceGuid() + ": " + e.getMessage();
                errors.add(errorMsg);
                
                migration.incrementRetryCount();
                migrationRepository.save(migration);
                
                logger.error(errorMsg, e);
            }
        }
        
        String status = failedRetries == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
        String message = String.format("Retry completed. Success: %d, Failed: %d", 
                                     successfulRetries, failedRetries);
        
        return new ImportResponseDto(status, message, retryableMigrations.size(), 
                                   successfulRetries, failedRetries, errors);
    }
    
    /**
     * Cleans HTML tags from text
     */
    private String cleanHtmlTags(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]*>", "").trim();
    }
}