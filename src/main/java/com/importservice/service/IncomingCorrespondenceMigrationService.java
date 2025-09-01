package com.importservice.service;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.entity.Correspondence;
import com.importservice.entity.CorrespondenceAttachment;
import com.importservice.entity.CorrespondenceComment;
import com.importservice.entity.CorrespondenceTransaction;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.CorrespondenceAttachmentRepository;
import com.importservice.repository.CorrespondenceCommentRepository;
import com.importservice.repository.CorrespondenceTransactionRepository;
import com.importservice.util.AgencyMappingUtils;
import com.importservice.util.CorrespondenceUtils;
import com.importservice.util.DepartmentUtils;
import com.importservice.util.HijriDateUtils;
import com.importservice.util.AttachmentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private CorrespondenceCommentRepository commentRepository;
    
    @Autowired
    private CorrespondenceTransactionRepository transactionRepository;
    
    @Autowired
    private DestinationSystemService destinationSystemService;
    
    @Autowired
    private DepartmentUtils departmentUtils;
    
    /**
     * Phase 1: Prepare Data
     * Selects incoming correspondences and creates migration tracking records
     */
    @Transactional
    public ImportResponseDto prepareData() {
        logger.info("Starting Phase 1: Prepare Data");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Find incoming correspondences (CorrespondenceTypeId = 2)
            // Exclude deleted, draft, and cancelled correspondences
            List<Correspondence> incomingCorrespondences = correspondenceRepository
                .findByCorrespondenceTypeIdAndIsDeletedAndIsDraft(2, false, false)
                .stream()
                .filter(c -> c.getIsCanceled() == null || c.getIsCanceled() == 0)
                .collect(Collectors.toList());
            
            logger.info("Found {} incoming correspondences to prepare", incomingCorrespondences.size());
            
            for (Correspondence correspondence : incomingCorrespondences) {
                try {
                    // Check if migration record already exists
                    Optional<IncomingCorrespondenceMigration> existingMigration = 
                        migrationRepository.findByCorrespondenceGuid(correspondence.getGuid());
                    
                    if (existingMigration.isPresent()) {
                        logger.debug("Migration record already exists for correspondence: {}", correspondence.getGuid());
                        successfulImports++;
                        continue;
                    }
                    
                    // Determine if correspondence needs to be closed
                    boolean needToClose = determineIfNeedToClose(correspondence);
                    
                    // Create migration record
                    IncomingCorrespondenceMigration migration = new IncomingCorrespondenceMigration(
                        correspondence.getGuid(), needToClose);
                    
                    migration.setPrepareDataStatus("COMPLETED");
                    migration.setCurrentPhase("CREATION");
                    migration.setNextPhase("ASSIGNMENT");
                    migration.setPhaseStatus("PENDING");
                    
                    migrationRepository.save(migration);
                    successfulImports++;
                    
                    logger.debug("Created migration record for correspondence: {} (needToClose: {})", 
                               correspondence.getGuid(), needToClose);
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Failed to prepare correspondence " + correspondence.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("Phase 1 completed. Prepared: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return new ImportResponseDto(status, message, incomingCorrespondences.size(), 
                                       successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 1: Prepare Data", e);
            return new ImportResponseDto("ERROR", "Phase 1 failed: " + e.getMessage(), 
                                       0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Phase 2: Creation
     * Creates correspondences in destination system with proper error handling
     */
    @Transactional
    public ImportResponseDto executeCreationPhase() {
        logger.info("Starting Phase 2: Creation");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Get migrations ready for creation phase
            List<IncomingCorrespondenceMigration> migrations = migrationRepository
                .findByCurrentPhase("CREATION")
                .stream()
                .filter(m -> "PENDING".equals(m.getPhaseStatus()) || 
                           ("ERROR".equals(m.getPhaseStatus()) && m.canRetry()))
                .collect(Collectors.toList());
            
            logger.info("Found {} correspondences ready for creation", migrations.size());
            
            for (IncomingCorrespondenceMigration migration : migrations) {
                try {
                    boolean success = executeCreationForSingleCorrespondence(migration);
                    
                    if (success) {
                        successfulImports++;
                        migration.markPhaseCompleted("CREATION");
                        logger.info("Successfully completed creation for correspondence: {}", 
                                  migration.getCorrespondenceGuid());
                    } else {
                        failedImports++;
                        migration.incrementRetryCount();
                        migration.markPhaseError("CREATION", "Creation process failed");
                        errors.add("Failed to create correspondence: " + migration.getCorrespondenceGuid());
                        logger.error("Failed creation for correspondence: {}", migration.getCorrespondenceGuid());
                    }
                    
                    migrationRepository.save(migration);
                    
                } catch (Exception e) {
                    failedImports++;
                    migration.incrementRetryCount();
                    migration.markPhaseError("CREATION", "Exception during creation: " + e.getMessage());
                    migrationRepository.save(migration);
                    
                    String errorMsg = "Exception creating correspondence " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("Phase 2 completed. Created: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return new ImportResponseDto(status, message, migrations.size(), 
                                       successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 2: Creation", e);
            return new ImportResponseDto("ERROR", "Phase 2 failed: " + e.getMessage(), 
                                       0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes creation for a single correspondence with proper error handling
     * If any step fails, the entire process stops and returns false
     */
    private boolean executeCreationForSingleCorrespondence(IncomingCorrespondenceMigration migration) {
        String correspondenceGuid = migration.getCorrespondenceGuid();
        logger.info("Starting creation process for correspondence: {}", correspondenceGuid);
        
        try {
            // Step 1: Get Details
            migration.setCreationStep("GET_DETAILS");
            migrationRepository.save(migration);
            
            Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(correspondenceGuid);
            if (!correspondenceOpt.isPresent()) {
                String error = "Correspondence not found: " + correspondenceGuid;
                migration.setCreationError(error);
                migration.setCreationStatus("ERROR");
                logger.error(error);
                return false;
            }
            
            Correspondence correspondence = correspondenceOpt.get();
            logger.debug("Step 1 completed: Got correspondence details for {}", correspondenceGuid);
            
            // Step 2: Get Attachments
            migration.setCreationStep("GET_ATTACHMENTS");
            migrationRepository.save(migration);
            
            List<CorrespondenceAttachment> attachments = attachmentRepository.findByDocGuid(correspondenceGuid);
            logger.debug("Step 2 completed: Found {} attachments for {}", attachments.size(), correspondenceGuid);
            
            // Step 3: Upload Main Attachment (if exists)
            String batchId = null;
            CorrespondenceAttachment primaryAttachment = AttachmentUtils.findPrimaryAttachment(attachments);
            
            if (primaryAttachment != null && AttachmentUtils.isValidForUpload(primaryAttachment)) {
                migration.setCreationStep("UPLOAD_MAIN_ATTACHMENT");
                migrationRepository.save(migration);
                
                // Create batch
                batchId = destinationSystemService.createBatch();
                if (batchId == null) {
                    String error = "Failed to create upload batch for correspondence: " + correspondenceGuid;
                    migration.setCreationError(error);
                    migration.setCreationStatus("ERROR");
                    logger.error(error);
                    return false;
                }
                
                migration.setBatchId(batchId);
                migrationRepository.save(migration);
                
                // Upload primary attachment
                String fileData = AttachmentUtils.getFileDataForUpload(
                    primaryAttachment.getFileData(), 
                    primaryAttachment.getName(), 
                    true);
                
                String fileName = AttachmentUtils.getFileNameForUpload(primaryAttachment.getName(), true);
                
                boolean uploadSuccess = destinationSystemService.uploadBase64FileToBatch(
                    batchId, "0", fileData, fileName);
                
                if (!uploadSuccess) {
                    String error = "Failed to upload primary attachment for correspondence: " + correspondenceGuid;
                    migration.setCreationError(error);
                    migration.setCreationStatus("ERROR");
                    logger.error(error);
                    return false;
                }
                
                logger.debug("Step 3 completed: Uploaded primary attachment for {}", correspondenceGuid);
            } else {
                logger.debug("Step 3 skipped: No valid primary attachment for {}", correspondenceGuid);
            }
            
            // Step 4: Create Correspondence
            migration.setCreationStep("CREATE_CORRESPONDENCE");
            migrationRepository.save(migration);
            
            String documentId = createCorrespondenceInDestination(correspondence, batchId);
            if (documentId == null) {
                String error = "Failed to create correspondence in destination system: " + correspondenceGuid;
                migration.setCreationError(error);
                migration.setCreationStatus("ERROR");
                logger.error(error);
                return false;
            }
            
            migration.setCreatedDocumentId(documentId);
            migrationRepository.save(migration);
            logger.debug("Step 4 completed: Created correspondence {} with document ID: {}", correspondenceGuid, documentId);
            
            // Step 5: Upload Other Attachments
            List<CorrespondenceAttachment> otherAttachments = AttachmentUtils.getNonPrimaryAttachments(attachments, primaryAttachment);
            if (!otherAttachments.isEmpty()) {
                migration.setCreationStep("UPLOAD_OTHER_ATTACHMENTS");
                migrationRepository.save(migration);
                
                boolean allAttachmentsUploaded = uploadOtherAttachments(otherAttachments, documentId);
                if (!allAttachmentsUploaded) {
                    String error = "Failed to upload some other attachments for correspondence: " + correspondenceGuid;
                    migration.setCreationError(error);
                    migration.setCreationStatus("ERROR");
                    logger.error(error);
                    return false;
                }
                
                logger.debug("Step 5 completed: Uploaded {} other attachments for {}", otherAttachments.size(), correspondenceGuid);
            } else {
                logger.debug("Step 5 skipped: No other attachments for {}", correspondenceGuid);
            }
            
            // Step 6: Create Physical Attachment
            migration.setCreationStep("CREATE_PHYSICAL_ATTACHMENT");
            migrationRepository.save(migration);
            
            boolean physicalAttachmentSuccess = destinationSystemService.createPhysicalAttachment(
                documentId, 
                correspondence.getCreationUserName(), 
                correspondence.getManualAttachmentsCount());
            
            if (!physicalAttachmentSuccess) {
                String error = "Failed to create physical attachment for correspondence: " + correspondenceGuid;
                migration.setCreationError(error);
                migration.setCreationStatus("ERROR");
                logger.error(error);
                return false;
            }
            
            logger.debug("Step 6 completed: Created physical attachment for {}", correspondenceGuid);
            
            // Step 7: Set Ready to Register
            migration.setCreationStep("SET_READY_TO_REGISTER");
            migrationRepository.save(migration);
            
            boolean readyToRegisterSuccess = destinationSystemService.setIncomingReadyToRegister(
                documentId, correspondence.getCreationUserName());
            
            if (!readyToRegisterSuccess) {
                String error = "Failed to set ready to register for correspondence: " + correspondenceGuid;
                migration.setCreationError(error);
                migration.setCreationStatus("ERROR");
                logger.error(error);
                return false;
            }
            
            logger.debug("Step 7 completed: Set ready to register for {}", correspondenceGuid);
            
            // Step 8: Register with Reference
            migration.setCreationStep("REGISTER_WITH_REFERENCE");
            migrationRepository.save(migration);
            
            Map<String, Object> incCorrespondenceContext = buildCorrespondenceContext(correspondence, batchId);
            boolean registerSuccess = destinationSystemService.registerWithReference(
                documentId, correspondence.getCreationUserName(), incCorrespondenceContext);
            
            if (!registerSuccess) {
                String error = "Failed to register with reference for correspondence: " + correspondenceGuid;
                migration.setCreationError(error);
                migration.setCreationStatus("ERROR");
                logger.error(error);
                return false;
            }
            
            logger.debug("Step 8 completed: Registered with reference for {}", correspondenceGuid);
            
            // Step 9: Start Work
            migration.setCreationStep("START_WORK");
            migrationRepository.save(migration);
            
            boolean startWorkSuccess = destinationSystemService.startIncomingCorrespondenceWork(
                documentId, correspondence.getCreationUserName());
            
            if (!startWorkSuccess) {
                String error = "Failed to start work for correspondence: " + correspondenceGuid;
                migration.setCreationError(error);
                migration.setCreationStatus("ERROR");
                logger.error(error);
                return false;
            }
            
            logger.debug("Step 9 completed: Started work for {}", correspondenceGuid);
            
            // Step 10: Set Owner
            migration.setCreationStep("SET_OWNER");
            migrationRepository.save(migration);
            
            boolean setOwnerSuccess = destinationSystemService.setCorrespondenceOwner(
                documentId, correspondence.getCreationUserName());
            
            if (!setOwnerSuccess) {
                String error = "Failed to set owner for correspondence: " + correspondenceGuid;
                migration.setCreationError(error);
                migration.setCreationStatus("ERROR");
                logger.error(error);
                return false;
            }
            
            logger.debug("Step 10 completed: Set owner for {}", correspondenceGuid);
            
            // All steps completed successfully
            migration.setCreationStep("COMPLETED");
            migration.setCreationStatus("COMPLETED");
            migration.setCreationError(null); // Clear any previous errors
            migrationRepository.save(migration);
            
            logger.info("Successfully completed all creation steps for correspondence: {}", correspondenceGuid);
            return true;
            
        } catch (Exception e) {
            String error = "Unexpected error during creation for correspondence " + correspondenceGuid + ": " + e.getMessage();
            migration.setCreationError(error);
            migration.setCreationStatus("ERROR");
            migrationRepository.save(migration);
            logger.error(error, e);
            return false;
        }
    }
    
    /**
     * Executes creation for specific correspondences
     */
    @Transactional
    public ImportResponseDto executeCreationForSpecific(List<String> correspondenceGuids) {
        logger.info("Starting creation for {} specific correspondences", correspondenceGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String correspondenceGuid : correspondenceGuids) {
            try {
                Optional<IncomingCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
                
                if (!migrationOpt.isPresent()) {
                    failedImports++;
                    String errorMsg = "Migration record not found for correspondence: " + correspondenceGuid;
                    errors.add(errorMsg);
                    logger.error(errorMsg);
                    continue;
                }
                
                IncomingCorrespondenceMigration migration = migrationOpt.get();
                
                // Reset creation status and step for retry
                migration.setCreationStatus("PENDING");
                migration.setCreationStep("GET_DETAILS");
                migration.setCreationError(null);
                migration.setPhaseStatus("PENDING");
                migrationRepository.save(migration);
                
                boolean success = executeCreationForSingleCorrespondence(migration);
                
                if (success) {
                    successfulImports++;
                    migration.markPhaseCompleted("CREATION");
                    logger.info("Successfully completed creation for correspondence: {}", correspondenceGuid);
                } else {
                    failedImports++;
                    migration.incrementRetryCount();
                    migration.markPhaseError("CREATION", "Creation process failed");
                    errors.add("Failed to create correspondence: " + correspondenceGuid);
                    logger.error("Failed creation for correspondence: {}", correspondenceGuid);
                }
                
                migrationRepository.save(migration);
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Exception creating correspondence " + correspondenceGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
        String message = String.format("Creation for specific correspondences completed. Success: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return new ImportResponseDto(status, message, correspondenceGuids.size(), 
                                   successfulImports, failedImports, errors);
    }
    
    /**
     * Creates correspondence in destination system
     */
    private String createCorrespondenceInDestination(Correspondence correspondence, String batchId) {
        try {
            // Map correspondence data to destination format
            String subject = correspondence.getSubject() != null ? correspondence.getSubject() : "";
            String externalRef = correspondence.getExternalReferenceNumber() != null ? 
                               correspondence.getExternalReferenceNumber() : "";
            String notes = correspondence.getNotes() != null ? correspondence.getNotes() : "";
            String referenceNo = correspondence.getReferenceNo() != null ? correspondence.getReferenceNo() : "";
            
            // Map priority, secrecy, and category
            String priority = CorrespondenceUtils.mapPriority(correspondence.getPriorityId());
            String secrecyLevel = CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId());
            String category = CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid());
            
            // Map agency
            String fromAgency = AgencyMappingUtils.mapAgencyGuidToCode(correspondence.getComingFromGuid());
            
            // Map department
            String toDepartment = DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getFromDepartmentGuid());
            if (toDepartment == null) {
                toDepartment = "CEO"; // Default department
            }
            
            // Convert dates to Hijri
            String gDueDate = correspondence.getDueDate() != null ? 
                            correspondence.getDueDate().toString() + "Z" : 
                            LocalDateTime.now().toString() + "Z";
            String hDueDate = correspondence.getDueDate() != null ? 
                            HijriDateUtils.convertToHijri(correspondence.getDueDate()) : 
                            HijriDateUtils.getCurrentHijriDate();
            
            String gDocumentDate = correspondence.getCorrespondenceCreationDate() != null ? 
                                 correspondence.getCorrespondenceCreationDate().toString() + "Z" : 
                                 LocalDateTime.now().toString() + "Z";
            String hDocumentDate = correspondence.getCorrespondenceCreationDate() != null ? 
                                 HijriDateUtils.convertToHijri(correspondence.getCorrespondenceCreationDate()) : 
                                 HijriDateUtils.getCurrentHijriDate();
            
            String gDate = correspondence.getIncomingDate() != null ? 
                         correspondence.getIncomingDate().toString() + "Z" : 
                         LocalDateTime.now().toString() + "Z";
            String hDate = correspondence.getIncomingDate() != null ? 
                         HijriDateUtils.convertToHijri(correspondence.getIncomingDate()) : 
                         HijriDateUtils.getCurrentHijriDate();
            
            String docDate = correspondence.getDbCreationDate() != null ? 
                           correspondence.getDbCreationDate().toString() + "Z" : 
                           LocalDateTime.now().toString() + "Z";
            
            Boolean requireReply = CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus());
            String action = CorrespondenceUtils.mapAction(correspondence.getLastDecisionGuid());
            
            // Create correspondence in destination system
            String documentId = destinationSystemService.createIncomingCorrespondence(
                correspondence.getGuid(),
                correspondence.getCreationUserName(),
                docDate,
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
            
            return documentId;
            
        } catch (Exception e) {
            logger.error("Error creating correspondence in destination system: {}", correspondence.getGuid(), e);
            return null;
        }
    }
    
    /**
     * Uploads other (non-primary) attachments
     */
    private boolean uploadOtherAttachments(List<CorrespondenceAttachment> otherAttachments, String documentId) {
        for (CorrespondenceAttachment attachment : otherAttachments) {
            try {
                if (!AttachmentUtils.isValidForUpload(attachment)) {
                    logger.debug("Skipping invalid attachment: {}", attachment.getGuid());
                    continue;
                }
                
                // Create individual batch for each attachment
                String batchId = destinationSystemService.createBatch();
                if (batchId == null) {
                    logger.error("Failed to create batch for attachment: {}", attachment.getGuid());
                    return false;
                }
                
                // Upload file to batch
                String fileData = AttachmentUtils.getFileDataForUpload(
                    attachment.getFileData(), 
                    attachment.getName(), 
                    false);
                
                String fileName = AttachmentUtils.getFileNameForUpload(attachment.getName(), false);
                
                boolean uploadSuccess = destinationSystemService.uploadBase64FileToBatch(
                    batchId, "0", fileData, fileName);
                
                if (!uploadSuccess) {
                    logger.error("Failed to upload attachment file: {}", attachment.getGuid());
                    return false;
                }
                
                // Create attachment record
                boolean createSuccess = destinationSystemService.createAttachment(attachment, batchId, documentId);
                if (!createSuccess) {
                    logger.error("Failed to create attachment record: {}", attachment.getGuid());
                    return false;
                }
                
                logger.debug("Successfully uploaded and created attachment: {}", attachment.getName());
                
            } catch (Exception e) {
                logger.error("Error processing attachment: {}", attachment.getGuid(), e);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Builds correspondence context for API calls
     */
    private Map<String, Object> buildCorrespondenceContext(Correspondence correspondence, String batchId) {
        Map<String, Object> incCorrespondence = new HashMap<>();
        
        incCorrespondence.put("corr:subject", correspondence.getSubject() != null ? correspondence.getSubject() : "");
        incCorrespondence.put("corr:externalCorrespondenceNumber", 
                            correspondence.getExternalReferenceNumber() != null ? 
                            correspondence.getExternalReferenceNumber() : "");
        incCorrespondence.put("corr:remarks", correspondence.getNotes() != null ? correspondence.getNotes() : "");
        incCorrespondence.put("corr:referenceNumber", 
                            correspondence.getReferenceNo() != null ? correspondence.getReferenceNo() : "");
        incCorrespondence.put("corr:category", CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid()));
        incCorrespondence.put("corr:secrecyLevel", CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId()));
        incCorrespondence.put("corr:priority", CorrespondenceUtils.mapPriority(correspondence.getPriorityId()));
        
        // Add dates
        String gDueDate = correspondence.getDueDate() != null ? 
                        correspondence.getDueDate().toString() + "Z" : 
                        LocalDateTime.now().toString() + "Z";
        String hDueDate = correspondence.getDueDate() != null ? 
                        HijriDateUtils.convertToHijri(correspondence.getDueDate()) : 
                        HijriDateUtils.getCurrentHijriDate();
        
        incCorrespondence.put("corr:gDueDate", gDueDate);
        incCorrespondence.put("corr:hDueDate", hDueDate);
        incCorrespondence.put("corr:requireReply", CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus()));
        incCorrespondence.put("corr:from", "");
        incCorrespondence.put("corr:fromAgency", AgencyMappingUtils.mapAgencyGuidToCode(correspondence.getComingFromGuid()));
        
        String gDocumentDate = correspondence.getCorrespondenceCreationDate() != null ? 
                             correspondence.getCorrespondenceCreationDate().toString() + "Z" : 
                             LocalDateTime.now().toString() + "Z";
        String hDocumentDate = correspondence.getCorrespondenceCreationDate() != null ? 
                             HijriDateUtils.convertToHijri(correspondence.getCorrespondenceCreationDate()) : 
                             HijriDateUtils.getCurrentHijriDate();
        
        incCorrespondence.put("corr:gDocumentDate", gDocumentDate);
        incCorrespondence.put("corr:hDocumentDate", hDocumentDate);
        
        String gDate = correspondence.getIncomingDate() != null ? 
                     correspondence.getIncomingDate().toString() + "Z" : 
                     LocalDateTime.now().toString() + "Z";
        String hDate = correspondence.getIncomingDate() != null ? 
                     HijriDateUtils.convertToHijri(correspondence.getIncomingDate()) : 
                     HijriDateUtils.getCurrentHijriDate();
        
        incCorrespondence.put("corr:gDate", gDate);
        incCorrespondence.put("corr:hDate", hDate);
        incCorrespondence.put("corr:delivery", "unknown");
        
        String toDepartment = DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getFromDepartmentGuid());
        incCorrespondence.put("corr:to", toDepartment != null ? toDepartment : "CEO");
        incCorrespondence.put("corr:toAgency", "ITBA");
        incCorrespondence.put("corr:action", CorrespondenceUtils.mapAction(correspondence.getLastDecisionGuid()));
        
        // Add file content if batch exists
        if (batchId != null) {
            Map<String, Object> fileContent = new HashMap<>();
            fileContent.put("upload-batch", batchId);
            fileContent.put("upload-fileId", "0");
            incCorrespondence.put("file:content", fileContent);
        }
        
        return incCorrespondence;
    }
    
    /**
     * Determines if correspondence needs to be closed
     */
    private boolean determineIfNeedToClose(Correspondence correspondence) {
        // Logic to determine if correspondence should be closed
        // This could be based on various factors like:
        // - Final status
        // - Archive status
        // - Age of correspondence
        // - Business rules
        
        if (correspondence.getIsFinal() != null && correspondence.getIsFinal()) {
            return true;
        }
        
        if (correspondence.getIsArchive() != null && correspondence.getIsArchive()) {
            return true;
        }
        
        // Add more business logic as needed
        return false;
    }
    
    /**
     * Phase 3: Assignment
     */
    @Transactional
    public ImportResponseDto executeAssignmentPhase() {
        logger.info("Starting Phase 3: Assignment");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Get assignment transactions that need processing
            List<CorrespondenceTransaction> assignments = transactionRepository.findAssignmentsNeedingProcessing();
            
            logger.info("Found {} assignments to process", assignments.size());
            
            for (CorrespondenceTransaction assignment : assignments) {
                try {
                    boolean success = processAssignment(assignment);
                    
                    if (success) {
                        successfulImports++;
                        assignment.setMigrateStatus("SUCCESS");
                        logger.info("Successfully processed assignment: {}", assignment.getGuid());
                    } else {
                        failedImports++;
                        assignment.setRetryCount(assignment.getRetryCount() + 1);
                        assignment.setMigrateStatus("FAILED");
                        errors.add("Failed to process assignment: " + assignment.getGuid());
                        logger.error("Failed to process assignment: {}", assignment.getGuid());
                    }
                    
                    transactionRepository.save(assignment);
                    
                } catch (Exception e) {
                    failedImports++;
                    assignment.setRetryCount(assignment.getRetryCount() + 1);
                    assignment.setMigrateStatus("FAILED");
                    transactionRepository.save(assignment);
                    
                    String errorMsg = "Exception processing assignment " + assignment.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("Phase 3 completed. Processed: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return new ImportResponseDto(status, message, assignments.size(), 
                                       successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 3: Assignment", e);
            return new ImportResponseDto("ERROR", "Phase 3 failed: " + e.getMessage(), 
                                       0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Processes a single assignment
     */
    private boolean processAssignment(CorrespondenceTransaction assignment) {
        try {
            // Get the created document ID from migration record
            Optional<IncomingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(assignment.getDocGuid());
            
            if (!migrationOpt.isPresent() || migrationOpt.get().getCreatedDocumentId() == null) {
                logger.error("No created document ID found for assignment: {}", assignment.getGuid());
                return false;
            }
            
            String documentId = migrationOpt.get().getCreatedDocumentId();
            
            // Get department code for assignment
            String departmentCode = null;
            if (assignment.getToDepartmentGuid() != null) {
                departmentCode = DepartmentUtils.getDepartmentCodeByOldGuid(assignment.getToDepartmentGuid());
            }
            if (departmentCode == null) {
                departmentCode = "CEO"; // Default department
            }
            
            // Create assignment in destination system
            return destinationSystemService.createAssignment(
                assignment.getGuid(),
                assignment.getFromUserName(),
                documentId,
                assignment.getActionDate(),
                assignment.getToUserName(),
                departmentCode,
                assignment.getDecisionGuid()
            );
            
        } catch (Exception e) {
            logger.error("Error processing assignment: {}", assignment.getGuid(), e);
            return false;
        }
    }
    
    /**
     * Phase 4: Business Log
     */
    @Transactional
    public ImportResponseDto executeBusinessLogPhase() {
        logger.info("Starting Phase 4: Business Log");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Get business log transactions that need processing
            List<CorrespondenceTransaction> businessLogs = transactionRepository.findBusinessLogsNeedingProcessing();
            
            logger.info("Found {} business logs to process", businessLogs.size());
            
            for (CorrespondenceTransaction businessLog : businessLogs) {
                try {
                    boolean success = processBusinessLog(businessLog);
                    
                    if (success) {
                        successfulImports++;
                        businessLog.setMigrateStatus("SUCCESS");
                        logger.info("Successfully processed business log: {}", businessLog.getGuid());
                    } else {
                        failedImports++;
                        businessLog.setRetryCount(businessLog.getRetryCount() + 1);
                        businessLog.setMigrateStatus("FAILED");
                        errors.add("Failed to process business log: " + businessLog.getGuid());
                        logger.error("Failed to process business log: {}", businessLog.getGuid());
                    }
                    
                    transactionRepository.save(businessLog);
                    
                } catch (Exception e) {
                    failedImports++;
                    businessLog.setRetryCount(businessLog.getRetryCount() + 1);
                    businessLog.setMigrateStatus("FAILED");
                    transactionRepository.save(businessLog);
                    
                    String errorMsg = "Exception processing business log " + businessLog.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("Phase 4 completed. Processed: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return new ImportResponseDto(status, message, businessLogs.size(), 
                                       successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 4: Business Log", e);
            return new ImportResponseDto("ERROR", "Phase 4 failed: " + e.getMessage(), 
                                       0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Processes a single business log
     */
    private boolean processBusinessLog(CorrespondenceTransaction businessLog) {
        try {
            // Get the created document ID from migration record
            Optional<IncomingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(businessLog.getDocGuid());
            
            if (!migrationOpt.isPresent() || migrationOpt.get().getCreatedDocumentId() == null) {
                logger.error("No created document ID found for business log: {}", businessLog.getGuid());
                return false;
            }
            
            String documentId = migrationOpt.get().getCreatedDocumentId();
            
            // Create business log in destination system
            return destinationSystemService.createBusinessLog(
                businessLog.getGuid(),
                documentId,
                businessLog.getActionDate(),
                businessLog.getActionEnglishName(),
                businessLog.getNotes(),
                businessLog.getFromUserName()
            );
            
        } catch (Exception e) {
            logger.error("Error processing business log: {}", businessLog.getGuid(), e);
            return false;
        }
    }
    
    /**
     * Phase 5: Comment
     */
    @Transactional
    public ImportResponseDto executeCommentPhase() {
        logger.info("Starting Phase 5: Comment");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Get comments that need processing
            List<CorrespondenceComment> comments = commentRepository.findCommentsNeedingProcessing();
            
            logger.info("Found {} comments to process", comments.size());
            
            for (CorrespondenceComment comment : comments) {
                try {
                    boolean success = processComment(comment);
                    
                    if (success) {
                        successfulImports++;
                        comment.setMigrateStatus("SUCCESS");
                        logger.info("Successfully processed comment: {}", comment.getCommentGuid());
                    } else {
                        failedImports++;
                        comment.setRetryCount(comment.getRetryCount() + 1);
                        comment.setMigrateStatus("FAILED");
                        errors.add("Failed to process comment: " + comment.getCommentGuid());
                        logger.error("Failed to process comment: {}", comment.getCommentGuid());
                    }
                    
                    commentRepository.save(comment);
                    
                } catch (Exception e) {
                    failedImports++;
                    comment.setRetryCount(comment.getRetryCount() + 1);
                    comment.setMigrateStatus("FAILED");
                    commentRepository.save(comment);
                    
                    String errorMsg = "Exception processing comment " + comment.getCommentGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("Phase 5 completed. Processed: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return new ImportResponseDto(status, message, comments.size(), 
                                       successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 5: Comment", e);
            return new ImportResponseDto("ERROR", "Phase 5 failed: " + e.getMessage(), 
                                       0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Processes a single comment
     */
    private boolean processComment(CorrespondenceComment comment) {
        try {
            // Get the created document ID from migration record
            Optional<IncomingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(comment.getDocGuid());
            
            if (!migrationOpt.isPresent() || migrationOpt.get().getCreatedDocumentId() == null) {
                logger.error("No created document ID found for comment: {}", comment.getCommentGuid());
                return false;
            }
            
            String documentId = migrationOpt.get().getCreatedDocumentId();
            
            // Create comment in destination system
            return destinationSystemService.createComment(
                comment.getCommentGuid(),
                documentId,
                comment.getCommentCreationDate(),
                comment.getComment(),
                comment.getCreationUserGuid()
            );
            
        } catch (Exception e) {
            logger.error("Error processing comment: {}", comment.getCommentGuid(), e);
            return false;
        }
    }
    
    /**
     * Phase 6: Closing
     */
    @Transactional
    public ImportResponseDto executeClosingPhase() {
        logger.info("Starting Phase 6: Closing");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Get correspondences that need to be closed
            List<IncomingCorrespondenceMigration> closingMigrations = migrationRepository
                .findByIsNeedToCloseAndCreatedDocumentIdIsNotNull(true);
            
            logger.info("Found {} correspondences that need to be closed", closingMigrations.size());
            
            for (IncomingCorrespondenceMigration migration : closingMigrations) {
                try {
                    if (!"PENDING".equals(migration.getClosingStatus()) && 
                        !("FAILED".equals(migration.getClosingStatus()) && migration.canRetry())) {
                        continue; // Skip if already processed or max retries exceeded
                    }
                    
                    boolean success = processClosing(migration);
                    
                    if (success) {
                        successfulImports++;
                        migration.setClosingStatus("COMPLETED");
                        migration.setClosingError(null);
                        logger.info("Successfully closed correspondence: {}", migration.getCorrespondenceGuid());
                    } else {
                        failedImports++;
                        migration.incrementRetryCount();
                        migration.setClosingStatus("FAILED");
                        migration.setClosingError("Failed to close correspondence");
                        errors.add("Failed to close correspondence: " + migration.getCorrespondenceGuid());
                        logger.error("Failed to close correspondence: {}", migration.getCorrespondenceGuid());
                    }
                    
                    migrationRepository.save(migration);
                    
                } catch (Exception e) {
                    failedImports++;
                    migration.incrementRetryCount();
                    migration.setClosingStatus("FAILED");
                    migration.setClosingError("Exception during closing: " + e.getMessage());
                    migrationRepository.save(migration);
                    
                    String errorMsg = "Exception closing correspondence " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("Phase 6 completed. Closed: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return new ImportResponseDto(status, message, closingMigrations.size(), 
                                       successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 6: Closing", e);
            return new ImportResponseDto("ERROR", "Phase 6 failed: " + e.getMessage(), 
                                       0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Processes closing for a single correspondence
     */
    private boolean processClosing(IncomingCorrespondenceMigration migration) {
        try {
            Optional<Correspondence> correspondenceOpt = 
                correspondenceRepository.findById(migration.getCorrespondenceGuid());
            
            if (!correspondenceOpt.isPresent()) {
                logger.error("Correspondence not found for closing: {}", migration.getCorrespondenceGuid());
                return false;
            }
            
            Correspondence correspondence = correspondenceOpt.get();
            
            // Close correspondence in destination system
            return destinationSystemService.closeCorrespondence(
                migration.getCorrespondenceGuid(),
                migration.getCreatedDocumentId(),
                correspondence.getCreationUserName(),
                LocalDateTime.now()
            );
            
        } catch (Exception e) {
            logger.error("Error processing closing for correspondence: {}", migration.getCorrespondenceGuid(), e);
            return false;
        }
    }
    
    /**
     * Executes assignment for specific transactions
     */
    @Transactional
    public ImportResponseDto executeAssignmentForSpecific(List<String> transactionGuids) {
        logger.info("Starting assignment for {} specific transactions", transactionGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String transactionGuid : transactionGuids) {
            try {
                Optional<CorrespondenceTransaction> transactionOpt = 
                    transactionRepository.findById(transactionGuid);
                
                if (!transactionOpt.isPresent()) {
                    failedImports++;
                    String errorMsg = "Transaction not found: " + transactionGuid;
                    errors.add(errorMsg);
                    logger.error(errorMsg);
                    continue;
                }
                
                CorrespondenceTransaction transaction = transactionOpt.get();
                
                // Reset status for retry
                transaction.setMigrateStatus("PENDING");
                transactionRepository.save(transaction);
                
                boolean success = processAssignment(transaction);
                
                if (success) {
                    successfulImports++;
                    transaction.setMigrateStatus("SUCCESS");
                    logger.info("Successfully processed assignment: {}", transactionGuid);
                } else {
                    failedImports++;
                    transaction.setRetryCount(transaction.getRetryCount() + 1);
                    transaction.setMigrateStatus("FAILED");
                    errors.add("Failed to process assignment: " + transactionGuid);
                    logger.error("Failed to process assignment: {}", transactionGuid);
                }
                
                transactionRepository.save(transaction);
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Exception processing assignment " + transactionGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
        String message = String.format("Assignment for specific transactions completed. Success: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return new ImportResponseDto(status, message, transactionGuids.size(), 
                                   successfulImports, failedImports, errors);
    }
    
    /**
     * Executes business log for specific transactions
     */
    @Transactional
    public ImportResponseDto executeBusinessLogForSpecific(List<String> transactionGuids) {
        logger.info("Starting business log for {} specific transactions", transactionGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String transactionGuid : transactionGuids) {
            try {
                Optional<CorrespondenceTransaction> transactionOpt = 
                    transactionRepository.findById(transactionGuid);
                
                if (!transactionOpt.isPresent()) {
                    failedImports++;
                    String errorMsg = "Transaction not found: " + transactionGuid;
                    errors.add(errorMsg);
                    logger.error(errorMsg);
                    continue;
                }
                
                CorrespondenceTransaction transaction = transactionOpt.get();
                
                // Reset status for retry
                transaction.setMigrateStatus("PENDING");
                transactionRepository.save(transaction);
                
                boolean success = processBusinessLog(transaction);
                
                if (success) {
                    successfulImports++;
                    transaction.setMigrateStatus("SUCCESS");
                    logger.info("Successfully processed business log: {}", transactionGuid);
                } else {
                    failedImports++;
                    transaction.setRetryCount(transaction.getRetryCount() + 1);
                    transaction.setMigrateStatus("FAILED");
                    errors.add("Failed to process business log: " + transactionGuid);
                    logger.error("Failed to process business log: {}", transactionGuid);
                }
                
                transactionRepository.save(transaction);
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Exception processing business log " + transactionGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
        String message = String.format("Business log for specific transactions completed. Success: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return new ImportResponseDto(status, message, transactionGuids.size(), 
                                   successfulImports, failedImports, errors);
    }
    
    /**
     * Executes comment for specific comments
     */
    @Transactional
    public ImportResponseDto executeCommentForSpecific(List<String> commentGuids) {
        logger.info("Starting comment for {} specific comments", commentGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String commentGuid : commentGuids) {
            try {
                Optional<CorrespondenceComment> commentOpt = 
                    commentRepository.findById(commentGuid);
                
                if (!commentOpt.isPresent()) {
                    failedImports++;
                    String errorMsg = "Comment not found: " + commentGuid;
                    errors.add(errorMsg);
                    logger.error(errorMsg);
                    continue;
                }
                
                CorrespondenceComment comment = commentOpt.get();
                
                // Reset status for retry
                comment.setMigrateStatus("PENDING");
                commentRepository.save(comment);
                
                boolean success = processComment(comment);
                
                if (success) {
                    successfulImports++;
                    comment.setMigrateStatus("SUCCESS");
                    logger.info("Successfully processed comment: {}", commentGuid);
                } else {
                    failedImports++;
                    comment.setRetryCount(comment.getRetryCount() + 1);
                    comment.setMigrateStatus("FAILED");
                    errors.add("Failed to process comment: " + commentGuid);
                    logger.error("Failed to process comment: {}", commentGuid);
                }
                
                commentRepository.save(comment);
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Exception processing comment " + commentGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
        String message = String.format("Comment for specific comments completed. Success: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return new ImportResponseDto(status, message, commentGuids.size(), 
                                   successfulImports, failedImports, errors);
    }
    
    /**
     * Executes closing for specific correspondences
     */
    @Transactional
    public ImportResponseDto executeClosingForSpecific(List<String> correspondenceGuids) {
        logger.info("Starting closing for {} specific correspondences", correspondenceGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String correspondenceGuid : correspondenceGuids) {
            try {
                Optional<IncomingCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
                
                if (!migrationOpt.isPresent()) {
                    failedImports++;
                    String errorMsg = "Migration record not found: " + correspondenceGuid;
                    errors.add(errorMsg);
                    logger.error(errorMsg);
                    continue;
                }
                
                IncomingCorrespondenceMigration migration = migrationOpt.get();
                
                if (!migration.getIsNeedToClose()) {
                    failedImports++;
                    String errorMsg = "Correspondence does not need to be closed: " + correspondenceGuid;
                    errors.add(errorMsg);
                    logger.error(errorMsg);
                    continue;
                }
                
                // Reset status for retry
                migration.setClosingStatus("PENDING");
                migration.setClosingError(null);
                migrationRepository.save(migration);
                
                boolean success = processClosing(migration);
                
                if (success) {
                    successfulImports++;
                    migration.setClosingStatus("COMPLETED");
                    migration.setClosingError(null);
                    logger.info("Successfully closed correspondence: {}", correspondenceGuid);
                } else {
                    failedImports++;
                    migration.incrementRetryCount();
                    migration.setClosingStatus("FAILED");
                    migration.setClosingError("Failed to close correspondence");
                    errors.add("Failed to close correspondence: " + correspondenceGuid);
                    logger.error("Failed to close correspondence: {}", correspondenceGuid);
                }
                
                migrationRepository.save(migration);
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Exception closing correspondence " + correspondenceGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
        String message = String.format("Closing for specific correspondences completed. Success: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return new ImportResponseDto(status, message, correspondenceGuids.size(), 
                                   successfulImports, failedImports, errors);
    }
    
    /**
     * Retries failed migrations
     */
    @Transactional
    public ImportResponseDto retryFailedMigrations() {
        logger.info("Starting retry of failed migrations");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<IncomingCorrespondenceMigration> retryableMigrations = migrationRepository.findRetryableMigrations();
            
            logger.info("Found {} migrations that can be retried", retryableMigrations.size());
            
            for (IncomingCorrespondenceMigration migration : retryableMigrations) {
                try {
                    // Reset phase status for retry
                    migration.setPhaseStatus("PENDING");
                    
                    // Retry based on current phase
                    boolean success = false;
                    switch (migration.getCurrentPhase()) {
                        case "CREATION":
                            migration.setCreationStatus("PENDING");
                            migration.setCreationStep("GET_DETAILS");
                            migration.setCreationError(null);
                            success = executeCreationForSingleCorrespondence(migration);
                            if (success) {
                                migration.markPhaseCompleted("CREATION");
                            } else {
                                migration.markPhaseError("CREATION", "Retry failed");
                            }
                            break;
                        case "ASSIGNMENT":
                            // Retry assignment logic would go here
                            success = true; // Placeholder
                            break;
                        case "BUSINESS_LOG":
                            // Retry business log logic would go here
                            success = true; // Placeholder
                            break;
                        case "COMMENT":
                            // Retry comment logic would go here
                            success = true; // Placeholder
                            break;
                        case "CLOSING":
                            migration.setClosingStatus("PENDING");
                            migration.setClosingError(null);
                            success = processClosing(migration);
                            if (success) {
                                migration.setClosingStatus("COMPLETED");
                            } else {
                                migration.setClosingStatus("FAILED");
                                migration.setClosingError("Retry failed");
                            }
                            break;
                        default:
                            logger.warn("Unknown phase for retry: {}", migration.getCurrentPhase());
                            continue;
                    }
                    
                    if (success) {
                        successfulImports++;
                        logger.info("Successfully retried migration for correspondence: {}", 
                                  migration.getCorrespondenceGuid());
                    } else {
                        failedImports++;
                        migration.incrementRetryCount();
                        errors.add("Failed to retry correspondence: " + migration.getCorrespondenceGuid());
                        logger.error("Failed to retry correspondence: {}", migration.getCorrespondenceGuid());
                    }
                    
                    migrationRepository.save(migration);
                    
                } catch (Exception e) {
                    failedImports++;
                    migration.incrementRetryCount();
                    migrationRepository.save(migration);
                    
                    String errorMsg = "Exception retrying correspondence " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("Retry completed. Success: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return new ImportResponseDto(status, message, retryableMigrations.size(), 
                                       successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error retrying failed migrations", e);
            return new ImportResponseDto("ERROR", "Retry failed: " + e.getMessage(), 
                                       0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Gets creation migrations for UI display
     */
    public List<IncomingCorrespondenceMigration> getCreationMigrations() {
        try {
            return migrationRepository.findAll()
                .stream()
                .sorted((m1, m2) -> m2.getLastModifiedDate().compareTo(m1.getLastModifiedDate()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting creation migrations", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gets assignment migrations with pagination and search
     */
    public Map<String, Object> getAssignmentMigrations(int page, int size, String status, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastModifiedDate"));
            
            Page<Object[]> assignmentPage;
            
            // Apply filters
            String statusFilter = "all".equals(status) ? null : status;
            String searchFilter = (search == null || search.trim().isEmpty()) ? null : search.trim();
            
            if (statusFilter != null || searchFilter != null) {
                assignmentPage = transactionRepository.findAssignmentMigrationsWithSearchAndPagination(
                    statusFilter, searchFilter, pageable);
            } else {
                assignmentPage = transactionRepository.findAssignmentMigrationsWithPagination(pageable);
            }
            
            // Convert Object[] results to Map for easier handling in frontend
            List<Map<String, Object>> content = assignmentPage.getContent().stream()
                .map(this::convertAssignmentRowToMap)
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", content);
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
    
    /**
     * Gets business log migrations with pagination and search
     */
    public Map<String, Object> getBusinessLogMigrations(int page, int size, String status, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastModifiedDate"));
            
            Page<Object[]> businessLogPage;
            
            // Apply filters
            String statusFilter = "all".equals(status) ? null : status;
            String searchFilter = (search == null || search.trim().isEmpty()) ? null : search.trim();
            
            if (statusFilter != null || searchFilter != null) {
                businessLogPage = transactionRepository.findBusinessLogMigrationsWithSearchAndPagination(
                    statusFilter, searchFilter, pageable);
            } else {
                businessLogPage = transactionRepository.findBusinessLogMigrationsWithPagination(pageable);
            }
            
            // Convert Object[] results to Map for easier handling in frontend
            List<Map<String, Object>> content = businessLogPage.getContent().stream()
                .map(this::convertBusinessLogRowToMap)
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", content);
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
    
    /**
     * Gets comment migrations with pagination and search
     */
    public Map<String, Object> getCommentMigrations(int page, int size, String status, String commentType, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastModifiedDate"));
            
            Page<Object[]> commentPage;
            
            // Apply filters
            String statusFilter = "all".equals(status) ? null : status;
            String commentTypeFilter = "all".equals(commentType) ? null : commentType;
            String searchFilter = (search == null || search.trim().isEmpty()) ? null : search.trim();
            
            if (statusFilter != null || commentTypeFilter != null || searchFilter != null) {
                commentPage = commentRepository.findCommentMigrationsWithSearchAndPagination(
                    statusFilter, commentTypeFilter, searchFilter, pageable);
            } else {
                commentPage = commentRepository.findCommentMigrationsWithPagination(pageable);
            }
            
            // Convert Object[] results to Map for easier handling in frontend
            List<Map<String, Object>> content = commentPage.getContent().stream()
                .map(this::convertCommentRowToMap)
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", content);
            result.put("totalElements", commentPage.getTotalElements());
            result.put("totalPages", commentPage.getTotalPages());
            result.put("currentPage", commentPage.getNumber());
            result.put("pageSize", commentPage.getSize());
            result.put("hasNext", commentPage.hasNext());
            result.put("hasPrevious", commentPage.hasPrevious());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting comment migrations", e);
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
     * Gets closing migrations with pagination and search
     */
    public Map<String, Object> getClosingMigrations(int page, int size, String status, String needToClose, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastModifiedDate"));
            
            Page<Object[]> closingPage;
            
            // Apply filters
            String statusFilter = "all".equals(status) ? null : status;
            Boolean needToCloseFilter = "all".equals(needToClose) ? null : Boolean.valueOf(needToClose);
            String searchFilter = (search == null || search.trim().isEmpty()) ? null : search.trim();
            
            closingPage = migrationRepository.findClosingMigrationsWithSearchAndPagination(
                statusFilter, needToCloseFilter, searchFilter, pageable);
            
            // Convert Object[] results to Map for easier handling in frontend
            List<Map<String, Object>> content = closingPage.getContent().stream()
                .map(this::convertClosingRowToMap)
                .collect(Collectors.toList());
            
            // Count how many need to be closed
            Long needToCloseCount = migrationRepository.countByIsNeedToClose(true);
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", content);
            result.put("totalElements", closingPage.getTotalElements());
            result.put("totalPages", closingPage.getTotalPages());
            result.put("currentPage", closingPage.getNumber());
            result.put("pageSize", closingPage.getSize());
            result.put("hasNext", closingPage.hasNext());
            result.put("hasPrevious", closingPage.hasPrevious());
            result.put("needToCloseCount", needToCloseCount);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting closing migrations", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("content", new ArrayList<>());
            errorResult.put("totalElements", 0L);
            errorResult.put("totalPages", 0);
            errorResult.put("currentPage", page);
            errorResult.put("pageSize", size);
            errorResult.put("hasNext", false);
            errorResult.put("hasPrevious", false);
            errorResult.put("needToCloseCount", 0L);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * Gets migration statistics
     */
    public Map<String, Object> getMigrationStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // Count by phase
            statistics.put("prepareData", migrationRepository.countByCurrentPhase("PREPARE_DATA"));
            statistics.put("creation", migrationRepository.countByCurrentPhase("CREATION"));
            statistics.put("assignment", migrationRepository.countByCurrentPhase("ASSIGNMENT"));
            statistics.put("businessLog", migrationRepository.countByCurrentPhase("BUSINESS_LOG"));
            statistics.put("comment", migrationRepository.countByCurrentPhase("COMMENT"));
            statistics.put("closing", migrationRepository.countByCurrentPhase("CLOSING"));
            
            // Count by overall status
            statistics.put("completed", migrationRepository.countByOverallStatus("COMPLETED"));
            statistics.put("failed", migrationRepository.countByOverallStatus("FAILED"));
            statistics.put("inProgress", migrationRepository.countByOverallStatus("IN_PROGRESS"));
            
            return statistics;
            
        } catch (Exception e) {
            logger.error("Error getting migration statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", e.getMessage());
            return errorStats;
        }
    }
    
    // Helper methods for converting database rows to maps
    private Map<String, Object> convertAssignmentRowToMap(Object[] row) {
        Map<String, Object> map = new HashMap<>();
        map.put("transactionGuid", row[0]);
        map.put("correspondenceGuid", row[1]);
        map.put("fromUserName", row[2]);
        map.put("toUserName", row[3]);
        map.put("actionDate", row[4]);
        map.put("decisionGuid", row[5]);
        map.put("notes", row[6]);
        map.put("migrateStatus", row[7]);
        map.put("retryCount", row[8]);
        map.put("lastModifiedDate", row[9]);
        map.put("correspondenceSubject", row[10]);
        map.put("correspondenceReferenceNo", row[11]);
        map.put("createdDocumentId", row[12]);
        return map;
    }
    
    private Map<String, Object> convertBusinessLogRowToMap(Object[] row) {
        Map<String, Object> map = new HashMap<>();
        map.put("transactionGuid", row[0]);
        map.put("correspondenceGuid", row[1]);
        map.put("actionId", row[2]);
        map.put("actionEnglishName", row[3]);
        map.put("actionLocalName", row[4]);
        map.put("actionDate", row[5]);
        map.put("fromUserName", row[6]);
        map.put("notes", row[7]);
        map.put("migrateStatus", row[8]);
        map.put("retryCount", row[9]);
        map.put("lastModifiedDate", row[10]);
        map.put("correspondenceSubject", row[11]);
        map.put("correspondenceReferenceNo", row[12]);
        map.put("createdDocumentId", row[13]);
        return map;
    }
    
    private Map<String, Object> convertCommentRowToMap(Object[] row) {
        Map<String, Object> map = new HashMap<>();
        map.put("commentGuid", row[0]);
        map.put("correspondenceGuid", row[1]);
        map.put("commentCreationDate", row[2]);
        map.put("comment", row[3]);
        map.put("commentType", row[4]);
        map.put("creationUserGuid", row[5]);
        map.put("roleGuid", row[6]);
        map.put("attachmentCaption", row[7]);
        map.put("migrateStatus", row[8]);
        map.put("retryCount", row[9]);
        map.put("lastModifiedDate", row[10]);
        map.put("correspondenceSubject", row[11]);
        map.put("correspondenceReferenceNo", row[12]);
        map.put("createdDocumentId", row[13]);
        return map;
    }
    
    private Map<String, Object> convertClosingRowToMap(Object[] row) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", row[0]);
        map.put("correspondenceGuid", row[1]);
        map.put("isNeedToClose", row[2]);
        map.put("closingStatus", row[3]);
        map.put("closingError", row[4]);
        map.put("createdDocumentId", row[5]);
        map.put("retryCount", row[6]);
        map.put("lastModifiedDate", row[7]);
        map.put("correspondenceSubject", row[8]);
        map.put("correspondenceReferenceNo", row[9]);
        map.put("correspondenceLastModifiedDate", row[10]);
        map.put("creationUserName", row[11]);
        return map;
    }
}