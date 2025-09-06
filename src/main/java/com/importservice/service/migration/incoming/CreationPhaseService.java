package com.importservice.service.migration.incoming;

import java.util.Arrays;
import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.CorrespondenceAttachment;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceAttachmentRepository;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
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
     * Phase 2: Creation - Creates correspondences in destination system
     */
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
                    }
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error processing correspondence " + migration.getCorrespondenceGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    phaseService.updatePhaseStatus(migration.getCorrespondenceGuid(), "CREATION", "ERROR", errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            return createResponse(successfulImports, failedImports, migrations.size(), errors, "Phase 2 completed");
            
        } catch (Exception e) {
            logger.error("Error in Phase 2: Creation", e);
            return phaseService.createResponse("ERROR", "Phase 2 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes creation for specific correspondences
     */
    public ImportResponseDto executeCreationForSpecific(List<String> correspondenceGuids) {
        logger.info("Starting creation for {} specific correspondences", correspondenceGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (int i = 0; i < correspondenceGuids.size(); i++) {
            String correspondenceGuid = correspondenceGuids.get(i);
            try {
                logger.info("Processing correspondence: {} ({}/{})", 
                           correspondenceGuid, i + 1, correspondenceGuids.size());
                
                boolean success = processCorrespondenceCreationInNewTransaction(correspondenceGuid);
                
                if (success) {
                    successfulImports++;
                    logger.info("Successfully completed creation for correspondence: {}", correspondenceGuid);
                } else {
                    failedImports++;
                    logger.warn("Failed to complete creation for correspondence: {}", correspondenceGuid);
                }
                
                if (i < correspondenceGuids.size() - 1) {
                    Thread.sleep(200); // 200ms delay between correspondences
                }
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing correspondence " + correspondenceGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        return createResponse(successfulImports, failedImports, correspondenceGuids.size(), errors, "Specific creation completed");
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
            migrationRepository.save(migration);
            
            // Process the creation
            boolean result = processCorrespondenceCreation(migration);
            
            // Update final status
            updateMigrationStatus(migration, result);
            
            logger.info("Completed creation transaction for correspondence: {} with result: {}", 
                       correspondenceGuid, result);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error in creation transaction for correspondence: {}", correspondenceGuid, e);
            updateErrorStatus(correspondenceGuid, "Transaction failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Processes the complete creation workflow for a single correspondence
     */
    private boolean processCorrespondenceCreation(IncomingCorrespondenceMigration migration) {
        String correspondenceGuid = migration.getCorrespondenceGuid();
        logger.info("Processing creation for correspondence: {}", correspondenceGuid);
        
        try {
            // Get correspondence details
            Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(correspondenceGuid);
            if (!correspondenceOpt.isPresent()) {
                logger.error("Correspondence not found: {}", correspondenceGuid);
                return false;
            }
            Correspondence correspondence = correspondenceOpt.get();
            
            // Execute creation steps
            return executeCreationSteps(migration, correspondence);
            
        } catch (Exception e) {
            logger.error("Error in creation process for correspondence: {}", correspondenceGuid, e);
            updateMigrationError(migration, "Step failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Executes all creation steps in sequence
     */
    private boolean executeCreationSteps(IncomingCorrespondenceMigration migration, Correspondence correspondence) {
        String correspondenceGuid = migration.getCorrespondenceGuid();
        
        // Step 1-2: Get details and attachments
        if ("GET_DETAILS".equals(migration.getCreationStep()) || "GET_ATTACHMENTS".equals(migration.getCreationStep())) {
            List<CorrespondenceAttachment> attachments = attachmentRepository.findByDocGuid(correspondenceGuid);
            CorrespondenceAttachment primaryAttachment = AttachmentUtils.findPrimaryAttachment(attachments);
            
            // Step 3: Upload main attachment and create correspondence
            String batchId = uploadMainAttachment(migration, primaryAttachment);
            String documentId = createCorrespondenceInDestination(correspondence, batchId);
            
            if (documentId == null) {
                logger.error("Failed to create correspondence in destination system: {}", correspondenceGuid);
                return false;
            }
            
            migration.setCreatedDocumentId(documentId);
            updateCreationStep(migration, "UPLOAD_OTHER_ATTACHMENTS");
            
            // Step 4-5: Upload other attachments and create physical attachment
            if (!uploadOtherAttachments(attachments, primaryAttachment, documentId) ||
                !createPhysicalAttachment(correspondence, documentId)) {
                return false;
            }
            
            updateCreationStep(migration, "SET_READY_TO_REGISTER");
        }
        
        // Steps 6-10: Registration workflow
        return executeRegistrationWorkflow(migration, correspondence);
    }
    
    /**
     * Executes the registration workflow steps
     */
    private boolean executeRegistrationWorkflow(IncomingCorrespondenceMigration migration, Correspondence correspondence) {
        String correspondenceGuid = migration.getCorrespondenceGuid();
        String documentId = migration.getCreatedDocumentId();
        String asUser = correspondence.getCreationUserName() != null ? 
                       correspondence.getCreationUserName() : "itba-emp1";
        
        // Step 7: Set ready to register
        if ("SET_READY_TO_REGISTER".equals(migration.getCreationStep())) {
            if (!destinationService.setIncomingReadyToRegister(documentId, asUser)) {
                logger.error("Failed to set ready to register for correspondence: {}", correspondenceGuid);
                return false;
            }
            updateCreationStep(migration, "REGISTER_WITH_REFERENCE");
        }
        
        // Step 8: Register with reference
        if ("REGISTER_WITH_REFERENCE".equals(migration.getCreationStep())) {
            if (!registerCorrespondenceWithReference(correspondence, documentId)) {
                logger.error("Failed to register correspondence with reference: {}", correspondenceGuid);
                return false;
            }
            updateCreationStep(migration, "START_WORK");
            
            try {
                Thread.sleep(1000); // Delay for destination system processing
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Step 9: Start work
        if ("START_WORK".equals(migration.getCreationStep())) {
            if (!destinationService.startIncomingCorrespondenceWork(documentId, asUser)) {
                logger.error("Failed to start work for correspondence: {}", correspondenceGuid);
                return false;
            }
            updateCreationStep(migration, "SET_OWNER");
        }
        
        // Step 10: Set owner
        if ("SET_OWNER".equals(migration.getCreationStep())) {
            if (!destinationService.setCorrespondenceOwner(documentId, asUser)) {
                logger.error("Failed to set owner for correspondence: {}", correspondenceGuid);
                return false;
            }
            updateCreationStep(migration, "COMPLETED");
            logger.info("Successfully completed creation for correspondence: {}", correspondenceGuid);
            return true;
        }
        
        return false;
    }
    
    /**
     * Uploads main attachment if exists
     */
    private String uploadMainAttachment(IncomingCorrespondenceMigration migration, CorrespondenceAttachment primaryAttachment) {
        if (primaryAttachment != null && AttachmentUtils.isValidForUpload(primaryAttachment)) {
            updateCreationStep(migration, "UPLOAD_MAIN_ATTACHMENT");
            
            String batchId = destinationService.createBatch();
            if (batchId != null) {
                migration.setBatchId(batchId);
                migrationRepository.save(migration);
                
                String fileData = AttachmentUtils.getFileDataForUpload(
                    primaryAttachment.getFileData(),
                    primaryAttachment.getName(),
                    true
                );
                
                destinationService.uploadBase64FileToBatch(
                    batchId, "0", fileData,
                    AttachmentUtils.getFileNameForUpload(primaryAttachment.getName(), true)
                );
            }
            return batchId;
        }
        return null;
    }
    
    /**
     * Creates correspondence in destination system
     */
    private String createCorrespondenceInDestination(Correspondence correspondence, String batchId) {
        try {
            String asUser = correspondence.getCreationUserName() != null ? 
                          correspondence.getCreationUserName() : "itba-emp1";
            
            // Map correspondence data
            String subject = getSubjectForCorrespondence(correspondence);
            String category = CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid());
            String priority = CorrespondenceUtils.mapPriority(correspondence.getPriorityId());
            String secrecyLevel = CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId());
            String fromAgency = AgencyMappingUtils.mapAgencyGuidToCode(correspondence.getComingFromGuid());
            String action = CorrespondenceUtils.mapAction(correspondence.getLastDecisionGuid());
            
            // Convert dates
            String gDueDate = formatDate(correspondence.getDueDate());
            String hDueDate = convertToHijri(correspondence.getDueDate());
            String gDocumentDate = formatDate(correspondence.getIncomingDate() != null ? 
                                            correspondence.getIncomingDate() : 
                                            correspondence.getCorrespondenceCreationDate());
            String hDocumentDate = convertToHijri(correspondence.getIncomingDate() != null ? 
                                                correspondence.getIncomingDate() : 
                                                correspondence.getCorrespondenceCreationDate());
            
            Boolean requireReply = CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus());
            
            return destinationService.createIncomingCorrespondence(
                correspondence.getGuid(), asUser, gDocumentDate, subject,
                correspondence.getExternalReferenceNumber() != null ? correspondence.getExternalReferenceNumber() : "",
                correspondence.getNotes() != null ? CorrespondenceUtils.cleanHtmlTags(correspondence.getNotes()) : "",
                correspondence.getReferenceNo() != null ? correspondence.getReferenceNo() : "",
                category, secrecyLevel, priority, gDueDate, hDueDate, requireReply, fromAgency,
                gDocumentDate, hDocumentDate, gDocumentDate, hDocumentDate, "COF", batchId, action
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
                    continue;
                }
                
                String batchId = destinationService.createBatch();
                if (batchId == null) return false;
                
                String fileData = AttachmentUtils.getFileDataForUpload(
                    attachment.getFileData(), attachment.getName(), false);
                
                if (!destinationService.uploadBase64FileToBatch(batchId, "0", fileData, 
                    AttachmentUtils.getFileNameForUpload(attachment.getName(), false)) ||
                    !destinationService.createAttachment(attachment, batchId, documentId)) {
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
                
                return destinationService.createPhysicalAttachment(
                    documentId,
                    correspondence.getCreationUserName(),
                    correspondence.getManualAttachmentsCount()
                );
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
            
            Map<String, Object> incCorrespondenceContext = buildCorrespondenceContext(correspondence);
            String action = CorrespondenceUtils.mapAction(correspondence.getLastDecisionGuid());
            String toDepartment = DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getFromDepartmentGuid());
            if (toDepartment == null) {
                toDepartment = "COF";
            }
            
            return destinationService.registerWithReference(
                documentId, asUser, incCorrespondenceContext, action, toDepartment);
            
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
        
        String finalSubject = getSubjectForCorrespondence(correspondence);
        
        context.put("corr:subject", finalSubject);
        context.put("corr:externalCorrespondenceNumber", 
                   correspondence.getExternalReferenceNumber() != null ? correspondence.getExternalReferenceNumber() : "");
        context.put("corr:remarks", correspondence.getNotes() != null ? correspondence.getNotes() : "");
        context.put("corr:referenceNumber", correspondence.getReferenceNo() != null ? correspondence.getReferenceNo() : "");
        context.put("corr:category", CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid()));
        context.put("corr:secrecyLevel", CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId()));
        context.put("corr:priority", CorrespondenceUtils.mapPriority(correspondence.getPriorityId()));
        
        // Convert dates
        String gDueDate = formatDate(correspondence.getDueDate());
        String hDueDate = convertToHijri(correspondence.getDueDate());
        String gDocumentDate = formatDate(correspondence.getIncomingDate() != null ? 
                                        correspondence.getIncomingDate() : 
                                        correspondence.getCorrespondenceCreationDate());
        String hDocumentDate = convertToHijri(correspondence.getIncomingDate() != null ? 
                                            correspondence.getIncomingDate() : 
                                            correspondence.getCorrespondenceCreationDate());
        
        context.put("corr:gDueDate", gDueDate);
        context.put("corr:hDueDate", hDueDate);
        context.put("corr:requireReply", CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus()));
        context.put("corr:fromAgency", AgencyMappingUtils.mapAgencyGuidToCode(correspondence.getComingFromGuid()));
        context.put("corr:gDocumentDate", gDocumentDate);
        context.put("corr:hDocumentDate", hDocumentDate);
        context.put("corr:gDate", gDocumentDate);
        context.put("corr:hDate", hDocumentDate);
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
                Map<String, Object> migrationData = createMigrationDataMap(migration);
                migrationsWithDetails.add(migrationData);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", migrationsWithDetails);
            result.put("totalElements", migrations.size());
            
            logger.info("Retrieved {} creation migrations with details", migrations.size());
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting creation migrations with details", e);
            return createErrorResult(e.getMessage());
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
            statistics.put("stepStatistics", calculateStepStatistics(migrations));
            
            logger.info("Generated creation statistics: total={}, completed={}, pending={}, error={}", 
                       total, completed, pending, error);
            
            return statistics;
            
        } catch (Exception e) {
            logger.error("Error getting creation statistics", e);
            return createErrorStatistics(e.getMessage());
        }
    }
    
    // Helper methods
    
    private void updateCreationStep(IncomingCorrespondenceMigration migration, String step) {
        try {
            migration.setCreationStep(step);
            migration.setLastModifiedDate(LocalDateTime.now());
            migrationRepository.save(migration);
            logger.debug("Updated creation step to {} for correspondence: {}", step, migration.getCorrespondenceGuid());
        } catch (Exception e) {
            logger.warn("Error updating creation step to {}: {}", step, e.getMessage());
        }
    }
    
    private void updateMigrationStatus(IncomingCorrespondenceMigration migration, boolean success) {
        if (success) {
            migration.setCreationStatus("COMPLETED");
            migration.setCreationStep("COMPLETED");
            migration.setCurrentPhase("ASSIGNMENT");
            migration.setNextPhase("BUSINESS_LOG");
            migration.setPhaseStatus("PENDING");
            migration.setRetryCount(0);
        } else {
            migration.setCreationStatus("ERROR");
            migration.setCreationError("Creation process failed");
            migration.setRetryCount(migration.getRetryCount() + 1);
            migration.setLastErrorAt(LocalDateTime.now());
        }
        migrationRepository.save(migration);
    }
    
    private void updateMigrationError(IncomingCorrespondenceMigration migration, String errorMsg) {
        try {
            migration.setCreationStatus("ERROR");
            migration.setCreationError(errorMsg);
            migration.setRetryCount(migration.getRetryCount() + 1);
            migration.setLastErrorAt(LocalDateTime.now());
            migrationRepository.save(migration);
        } catch (Exception e) {
            logger.error("Error updating migration error status: {}", e.getMessage());
        }
    }
    
    private void updateErrorStatus(String correspondenceGuid, String errorMsg) {
        try {
            Optional<IncomingCorrespondenceMigration> migrationOpt = 
                migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
            if (migrationOpt.isPresent()) {
                updateMigrationError(migrationOpt.get(), errorMsg);
            }
        } catch (Exception e) {
            logger.error("Error updating error status for correspondence: {}", correspondenceGuid, e);
        }
    }
    
    private String getSubjectForCorrespondence(Correspondence correspondence) {
        String originalSubject = correspondence.getSubject();
        String subject = subjectGenerator.generateSubject(originalSubject);
        
        if (subjectGenerator.isRandomSubjectEnabled()) {
            String category = CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid());
            subject = subjectGenerator.generateSubjectWithCategory(category);
            logger.info("Generated random subject for correspondence {}: {}", correspondence.getGuid(), subject);
        }
        
        return subject;
    }
    
    private String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toString() + "Z" : LocalDateTime.now().toString() + "Z";
    }
    
    private String convertToHijri(LocalDateTime dateTime) {
        return dateTime != null ? HijriDateUtils.convertToHijri(dateTime) : HijriDateUtils.getCurrentHijriDate();
    }
    
    private ImportResponseDto createResponse(int successful, int failed, int total, List<String> errors, String messagePrefix) {
        String status = phaseService.determineFinalStatus(successful, failed);
        String message = String.format("%s. Created: %d, Failed: %d", messagePrefix, successful, failed);
        return phaseService.createResponse(status, message, total, successful, failed, errors);
    }
    
    private Map<String, Object> createMigrationDataMap(IncomingCorrespondenceMigration migration) {
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
        
        return migrationData;
    }
    
    private List<Map<String, Object>> calculateStepStatistics(List<IncomingCorrespondenceMigration> migrations) {
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
        
        return stepStatistics;
    }
    
    private Map<String, Object> createErrorResult(String errorMsg) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("content", new ArrayList<>());
        errorResult.put("totalElements", 0);
        errorResult.put("error", errorMsg);
        return errorResult;
    }
    
    private Map<String, Object> createErrorStatistics(String errorMsg) {
        Map<String, Object> errorStats = new HashMap<>();
        errorStats.put("total", 0L);
        errorStats.put("completed", 0L);
        errorStats.put("pending", 0L);
        errorStats.put("error", 0L);
        errorStats.put("stepStatistics", new ArrayList<>());
        errorStats.put("error", errorMsg);
        return errorStats;
    }
}