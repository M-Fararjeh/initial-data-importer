package com.importservice.service.migration.internal;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.CorrespondenceAttachment;
import com.importservice.entity.InternalCorrespondenceMigration;
import com.importservice.repository.CorrespondenceAttachmentRepository;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.InternalCorrespondenceMigrationRepository;
import com.importservice.service.InternalDestinationSystemService;
import com.importservice.service.migration.MigrationPhaseService;
import com.importservice.util.AttachmentUtils;
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
 * Service for Internal Phase 2: Creation
 * Creates internal correspondences in destination system with attachments
 */
@Service
public class InternalCreationPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(InternalCreationPhaseService.class);
    
    @Autowired
    private InternalCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @Autowired
    private CorrespondenceAttachmentRepository attachmentRepository;
    
    @Autowired
    private InternalDestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    @Autowired
    private CorrespondenceSubjectGenerator subjectGenerator;

    /**
     * Phase 2: Creation
     * Creates internal correspondences in destination system
     */
    @Transactional(readOnly = false, timeout = 600)
    public ImportResponseDto executeCreationPhase() {
        logger.info("Starting Internal Phase 2: Creation");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<InternalCorrespondenceMigration> migrations = migrationRepository.findByCurrentPhase("CREATION");
            logger.info("Found {} internal correspondences in CREATION phase", migrations.size());
            
            // Process each correspondence in its own transaction for immediate status updates
            for (int i = 0; i < migrations.size(); i++) {
                InternalCorrespondenceMigration migration = migrations.get(i);
                try {
                    logger.info("Processing internal correspondence: {} ({}/{})", 
                               migration.getCorrespondenceGuid(), i + 1, migrations.size());
                    
                    // Process in separate transaction for immediate status update
                    boolean success = processCorrespondenceCreationInNewTransaction(migration.getCorrespondenceGuid());
                    
                    if (success) {
                        successfulImports++;
                        logger.info("Successfully completed internal creation for correspondence: {}", migration.getCorrespondenceGuid());
                    } else {
                        failedImports++;
                        logger.warn("Failed to complete internal creation for correspondence: {}", migration.getCorrespondenceGuid());
                    }
                    
                    // Add small delay between correspondences to reduce system load
                    if (i < migrations.size() - 1) {
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
                    String errorMsg = "Error processing internal correspondence " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Internal Phase 2 completed. Created: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, migrations.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Internal Phase 2: Creation", e);
            return phaseService.createResponse("ERROR", "Internal Phase 2 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes creation for specific correspondences
     */
    public ImportResponseDto executeCreationForSpecific(List<String> correspondenceGuids) {
        logger.info("Starting internal creation for {} specific correspondences", correspondenceGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        // Process each correspondence in its own transaction for immediate status updates
        for (int i = 0; i < correspondenceGuids.size(); i++) {
            String correspondenceGuid = correspondenceGuids.get(i);
            try {
                logger.info("Processing internal correspondence: {} ({}/{})", 
                           correspondenceGuid, i + 1, correspondenceGuids.size());
                
                // Process in separate transaction for immediate status update
                boolean success = processCorrespondenceCreationInNewTransaction(correspondenceGuid);
                
                if (success) {
                    successfulImports++;
                    logger.info("Successfully completed internal creation for correspondence: {}", correspondenceGuid);
                } else {
                    failedImports++;
                    logger.warn("Failed to complete internal creation for correspondence: {}", correspondenceGuid);
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
                String errorMsg = "Error processing internal correspondence " + correspondenceGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = phaseService.determineFinalStatus(successfulImports, failedImports);
        String message = String.format("Specific internal creation completed. Created: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, correspondenceGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Processes correspondence creation in a new transaction for immediate status updates
     */
    @Transactional(readOnly = false, timeout = 120)
    public boolean processCorrespondenceCreationInNewTransaction(String correspondenceGuid) {
        try {
            logger.debug("Starting new transaction for internal correspondence: {}", correspondenceGuid);
            
            Optional<InternalCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (!migrationOpt.isPresent()) {
                logger.error("Internal migration record not found for correspondence: {}", correspondenceGuid);
                return false;
            }
            
            InternalCorrespondenceMigration migration = migrationOpt.get();
            
            // Mark as in progress
            migration.setCreationStatus("IN_PROGRESS");
            migration.setLastModifiedDate(LocalDateTime.now());
            migrationRepository.save(migration);
            
            // Process the creation
            boolean result = processCorrespondenceCreation(migration);
            
            // Update final status
            if (result) {
                migration.setCreationStatus("COMPLETED");
                migration.setCreationStep("COMPLETED");
                
                // Check if there are assignment transactions for this correspondence
                boolean hasAssignments = checkIfCorrespondenceHasAssignments(correspondenceGuid);
                if (hasAssignments) {
                    migration.setCurrentPhase("ASSIGNMENT");
                    migration.setNextPhase("APPROVAL");
                } else {
                    // Skip assignment phase and go directly to approval
                    migration.setCurrentPhase("APPROVAL");
                    migration.setNextPhase("BUSINESS_LOG");
                    migration.setAssignmentStatus("SKIPPED");
                    logger.info("No assignments found for internal correspondence {}, skipping to approval phase", correspondenceGuid);
                }
                migration.setPhaseStatus("PENDING");
                migration.setRetryCount(0); // Reset retry count on success
            } else {
                migration.setCreationStatus("ERROR");
                migration.setCreationError("Creation process failed");
                migration.setRetryCount(migration.getRetryCount() + 1);
                migration.setLastErrorAt(LocalDateTime.now());
            }
            
            migrationRepository.save(migration);
            
            logger.info("Completed internal creation transaction for correspondence: {} with result: {}", 
                       correspondenceGuid, result);
            return result;
            
        } catch (Exception e) {
            logger.error("Error in internal creation transaction for correspondence: {}", correspondenceGuid, e);
            
            // Update error status in separate try-catch to ensure it gets saved
            try {
                Optional<InternalCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
                if (migrationOpt.isPresent()) {
                    InternalCorrespondenceMigration migration = migrationOpt.get();
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
            logger.info("Starting internal creation process for correspondence: {}", correspondenceGuid);
            
            Optional<InternalCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            
            if (!migrationOpt.isPresent()) {
                logger.error("Internal migration record not found for correspondence: {}", correspondenceGuid);
                return false;
            }
            
            InternalCorrespondenceMigration migration = migrationOpt.get();
            boolean result = processCorrespondenceCreation(migration);
            
            // Final status update
            if (result) {
                migration.setCreationStatus("COMPLETED");
                migration.setCreationStep("COMPLETED");
                
                // Check if there are assignment transactions for this correspondence
                boolean hasAssignments = checkIfCorrespondenceHasAssignments(correspondenceGuid);
                if (hasAssignments) {
                    migration.setCurrentPhase("ASSIGNMENT");
                    migration.setNextPhase("APPROVAL");
                } else {
                    // Skip assignment phase and go directly to approval
                    migration.setCurrentPhase("APPROVAL");
                    migration.setNextPhase("BUSINESS_LOG");
                    migration.setAssignmentStatus("SKIPPED");
                    logger.info("No assignments found for internal correspondence {}, skipping to approval phase", correspondenceGuid);
                }
                migration.setPhaseStatus("PENDING");
            } else {
                migration.setCreationStatus("ERROR");
                migration.setRetryCount(migration.getRetryCount() + 1);
                migration.setLastErrorAt(LocalDateTime.now());
            }
            
            migrationRepository.save(migration);
            
            logger.info("Completed internal creation process for correspondence: {} with result: {}", 
                       correspondenceGuid, result);
            return result;
        } catch (Exception e) {
            logger.error("Error in internal creation process for correspondence: {}", correspondenceGuid, e);
            updateCreationErrorForCorrespondence(correspondenceGuid, e.getMessage());
            return false;
        }
    }
    
    /**
     * Processes the complete creation workflow for a single internal correspondence
     * Implements step-based processing for fault tolerance and resume capability
     */
    private boolean processCorrespondenceCreation(InternalCorrespondenceMigration migration) {
        String correspondenceGuid = migration.getCorrespondenceGuid();
        logger.info("Processing internal creation for correspondence: {}", correspondenceGuid);
        
        try {
            // Step 1: Get correspondence details
            Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(correspondenceGuid);
            if (!correspondenceOpt.isPresent()) {
                logger.error("Internal correspondence not found: {}", correspondenceGuid);
                return false;
            }
            Correspondence correspondence = correspondenceOpt.get();
            
            CorrespondenceAttachment primaryAttachment = null;
            List<CorrespondenceAttachment> attachments = null;
            String documentId = null;
            
            // Step-based processing: Resume from current step
            if ("GET_DETAILS".equals(migration.getCreationStep())) {
                logger.info("Step 1: Getting details and attachments for internal correspondence: {}", correspondenceGuid);
                updateCreationStep(migration, "GET_ATTACHMENTS");
                migrationRepository.save(migration); // Save step progress
                
                attachments = attachmentRepository.findByDocGuid(correspondenceGuid);
                primaryAttachment = AttachmentUtils.findPrimaryAttachment(attachments);

                String batchId = null;

                // Step 2: Upload main attachment if exists
                if (primaryAttachment != null && AttachmentUtils.isValidForUpload(primaryAttachment)) {
                    logger.info("Step 2: Uploading main attachment for internal correspondence: {}", correspondenceGuid);
                    updateCreationStep(migration, "UPLOAD_MAIN_ATTACHMENT");
                    migrationRepository.save(migration); // Save step progress
                    
                    batchId = destinationService.createBatch();
                    if (batchId != null) {
                        migration.setBatchId(batchId);

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
                            logger.error("Step 2 failed: Failed to upload primary attachment for internal correspondence: {}", correspondenceGuid);
                            return false;
                        }
                    } else {
                        logger.error("Step 2 failed: Failed to create batch for primary attachment upload: {}", correspondenceGuid);
                        return false;
                    }
                }

                // Step 3: Create internal correspondence
                logger.info("Step 3: Creating internal correspondence in destination: {}", correspondenceGuid);
                updateCreationStep(migration, "CREATE_CORRESPONDENCE");
                migrationRepository.save(migration); // Save step progress
                
                documentId = createInternalCorrespondenceInDestination(correspondence, batchId);
                if (documentId == null) {
                    logger.error("Step 3 failed: Failed to create internal correspondence in destination system: {}", correspondenceGuid);
                    return false;
                }

                migration.setCreatedDocumentId(documentId);
                migrationRepository.save(migration); // Save progress
                updateCreationStep(migration, "UPLOAD_OTHER_ATTACHMENTS");
                migrationRepository.save(migration); // Save step progress
            }
            
            // Resume processing: Get document ID if not available
            if (documentId == null) {
                documentId = migration.getCreatedDocumentId();
                if (documentId == null) {
                    logger.error("No document ID available for internal correspondence: {}", correspondenceGuid);
                    return false;
                }
            }
            
            // Step 4: Upload other attachments
            if ("UPLOAD_OTHER_ATTACHMENTS".equals(migration.getCreationStep())) {
                logger.info("Step 4: Uploading other attachments for internal correspondence: {}", correspondenceGuid);
                if (attachments == null) {
                    attachments = attachmentRepository.findByDocGuid(correspondenceGuid);
                    primaryAttachment = AttachmentUtils.findPrimaryAttachment(attachments);
                }
                
                boolean otherAttachmentsSuccess = uploadOtherAttachments(attachments, primaryAttachment, documentId);
                if (!otherAttachmentsSuccess) {
                    logger.error("Step 4 failed: Failed to upload other attachments for internal correspondence: {}", correspondenceGuid);
                    return false;
                }

                updateCreationStep(migration, "CREATE_PHYSICAL_ATTACHMENT");
                migrationRepository.save(migration); // Save progress
            }

            // Step 5: Create physical attachment
            if ("CREATE_PHYSICAL_ATTACHMENT".equals(migration.getCreationStep())) {
                logger.info("Step 5: Creating physical attachment for internal correspondence: {}", correspondenceGuid);
                boolean physicalAttachmentSuccess = createPhysicalAttachment(correspondence, documentId);
                if (!physicalAttachmentSuccess) {
                    logger.error("Step 5 failed: Failed to create physical attachment for internal correspondence: {}", correspondenceGuid);
                    return false;
                }
                
                updateCreationStep(migration, "COMPLETED");
                migrationRepository.save(migration); // Save final progress
                logger.info("Successfully completed internal creation for correspondence: {}", correspondenceGuid);
                return true;
            }
            
            logger.warn("Internal creation process reached end without completion for correspondence: {}", correspondenceGuid);
            return false;
        } catch (Exception e) {
            logger.error("Error in internal creation process for correspondence: {}", correspondenceGuid, e);
            
            // Update error status immediately
            try {
                migration.setCreationStatus("ERROR");
                migration.setCreationError("Step failed: " + e.getMessage());
                migration.setRetryCount(migration.getRetryCount() + 1);
                migration.setLastErrorAt(LocalDateTime.now());
                migrationRepository.save(migration);
            } catch (Exception saveError) {
                logger.error("Error saving error status for correspondence: {}", correspondenceGuid, saveError);
            }
            
            return false;
        }
    }
    
    /**
     * Updates the creation step for tracking progress
     */
    private void updateCreationStep(InternalCorrespondenceMigration migration, String step) {
        try {
            migration.setCreationStep(step);
            migration.setLastModifiedDate(LocalDateTime.now());
            logger.debug("Updated internal creation step to {} for correspondence: {}", step, migration.getCorrespondenceGuid());
        } catch (Exception e) {
            logger.warn("Error updating internal creation step to {}: {}", step, e.getMessage());
        }
    }
    
    /**
     * Creates internal correspondence in destination system
     */
    private String createInternalCorrespondenceInDestination(Correspondence correspondence, String batchId) {
        try {
            String asUser = correspondence.getCreationUserName() != null ? 
                          correspondence.getCreationUserName() : "itba-emp1";
            
            return destinationService.createInternalCorrespondence(
                correspondence.getGuid(),
                asUser,
                correspondence,
                batchId
            );
            
        } catch (Exception e) {
            logger.error("Error creating internal correspondence in destination: {}", correspondence.getGuid(), e);
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
                    logger.warn("Failed to create physical attachment for internal correspondence: {}", correspondence.getGuid());
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
     * Updates creation error for a migration
     */
    private void updateCreationError(InternalCorrespondenceMigration migration, String errorMsg) {
        try {
            migration.setCreationStatus("ERROR");
            migration.setCreationError(errorMsg);
            migration.setRetryCount(migration.getRetryCount() + 1);
            migration.setLastErrorAt(LocalDateTime.now());
            migrationRepository.save(migration);
        } catch (Exception e) {
            logger.error("Error updating internal creation error status: {}", e.getMessage());
        }
    }
    
    /**
     * Updates creation error for a specific correspondence GUID
     */
    private void updateCreationErrorForCorrespondence(String correspondenceGuid, String errorMsg) {
        try {
            Optional<InternalCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            if (migrationOpt.isPresent()) {
                updateCreationError(migrationOpt.get(), errorMsg);
            }
        } catch (Exception e) {
            logger.error("Error updating internal creation error status for correspondence: {}", e.getMessage());
        }
    }
    
    /**
     * Gets creation migrations for UI display
     */
    @Transactional(readOnly = true, timeout = 60)
    public List<InternalCorrespondenceMigration> getCreationMigrations() {
        try {
            List<InternalCorrespondenceMigration> migrations = migrationRepository.findAll();
            logger.info("Retrieved {} internal creation migrations", migrations.size());
            return migrations;
        } catch (Exception e) {
            logger.error("Error getting internal creation migrations", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Gets creation migrations with correspondence details for UI display
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getCreationMigrationsWithDetails() {
        try {
            List<InternalCorrespondenceMigration> migrations = migrationRepository.findAll();
            List<Map<String, Object>> migrationsWithDetails = new ArrayList<>();
            
            for (InternalCorrespondenceMigration migration : migrations) {
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
            
            logger.info("Retrieved {} internal creation migrations with details", migrations.size());
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting internal creation migrations with details", e);
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
            List<InternalCorrespondenceMigration> migrations = migrationRepository.findAll();
            
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
            for (InternalCorrespondenceMigration migration : migrations) {
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
            
            logger.info("Generated internal creation statistics: total={}, completed={}, pending={}, error={}", 
                       total, completed, pending, error);
            
            return statistics;
            
        } catch (Exception e) {
            logger.error("Error getting internal creation statistics", e);
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
     * Checks if a correspondence has assignment transactions
     */
    @Autowired
    private com.importservice.repository.CorrespondenceTransactionRepository transactionRepository;
    
    private boolean checkIfCorrespondenceHasAssignments(String correspondenceGuid) {
        try {
            List<com.importservice.entity.CorrespondenceTransaction> assignments = 
                transactionRepository.findByDocGuidAndActionId(correspondenceGuid, 12);
            
            boolean hasAssignments = !assignments.isEmpty();
            logger.info("Correspondence {} has {} assignment transactions", correspondenceGuid, assignments.size());
            return hasAssignments;
        } catch (Exception e) {
            logger.error("Error checking assignments for correspondence: {}", correspondenceGuid, e);
            return false; // Default to no assignments if error occurs
        }
    }
}