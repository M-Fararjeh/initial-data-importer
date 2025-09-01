package com.importservice.service;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.CorrespondenceAttachment;
import com.importservice.entity.CorrespondenceTransaction;
import com.importservice.entity.CorrespondenceComment;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.CorrespondenceAttachmentRepository;
import com.importservice.repository.CorrespondenceTransactionRepository;
import com.importservice.repository.CorrespondenceCommentRepository;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
import com.importservice.util.AgencyMappingUtils;
import com.importservice.util.AttachmentUtils;
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

import java.time.LocalDateTime;
import java.util.*;

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
    private CorrespondenceTransactionRepository transactionRepository;
    
    @Autowired
    private CorrespondenceCommentRepository commentRepository;
    
    @Autowired
    private DestinationSystemService destinationSystemService;
    
    @Autowired
    private DepartmentUtils departmentUtils;
    
    // Cache for user department mappings to improve performance
    private final Map<String, String> userDepartmentCache = new HashMap<>();
    
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
            // Get incoming correspondences (CorrespondenceTypeId = 2)
            // Filter out deleted, draft, and cancelled correspondences
            List<Correspondence> incomingCorrespondences = correspondenceRepository
                .findByCorrespondenceTypeIdAndIsDeletedAndIsDraft(2, false, false);
            
            logger.info("Found {} incoming correspondences to prepare", incomingCorrespondences.size());
            
            for (Correspondence correspondence : incomingCorrespondences) {
                try {
                    // Check if migration record already exists
                    Optional<IncomingCorrespondenceMigration> existing = 
                        migrationRepository.findByCorrespondenceGuid(correspondence.getGuid());
                    
                    if (existing.isPresent()) {
                        logger.debug("Migration record already exists for correspondence: {}", correspondence.getGuid());
                        successfulImports++;
                        continue;
                    }
                    
                    // Determine if correspondence needs to be closed
                    boolean needToClose = determineIfNeedToClose(correspondence);
                    
                    // Create migration tracking record
                    IncomingCorrespondenceMigration migration = new IncomingCorrespondenceMigration(
                        correspondence.getGuid(), needToClose);
                    
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
            String message = String.format("Phase 1 completed. Prepared: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return new ImportResponseDto(status, message, incomingCorrespondences.size(), 
                                       successfulImports, failedImports, errors);
                                       
        } catch (Exception e) {
            logger.error("Failed to execute Phase 1: Prepare Data", e);
            return new ImportResponseDto("ERROR", "Failed to prepare data: " + e.getMessage(), 
                0, 0, 0, Arrays.asList("Failed to prepare data: " + e.getMessage()));
        }
    }
    
    /**
     * Phase 2: Creation
     * Creates correspondences in destination system with attachments
     */
    @Transactional
    public ImportResponseDto executeCreationPhase() {
        logger.info("Starting Phase 2: Creation");
        
        List<IncomingCorrespondenceMigration> pendingMigrations = 
            migrationRepository.findByCurrentPhase("CREATION");
        
        return executeCreationForSpecific(
            pendingMigrations.stream()
                .map(IncomingCorrespondenceMigration::getCorrespondenceGuid)
                .collect(java.util.stream.Collectors.toList())
        );
    }
    
    /**
     * Execute creation for specific correspondence GUIDs
     */
    @Transactional
    public ImportResponseDto executeCreationForSpecific(List<String> correspondenceGuids) {
        logger.info("Executing creation for {} specific correspondences", correspondenceGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String correspondenceGuid : correspondenceGuids) {
            try {
                Optional<IncomingCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(correspondenceGuid);
                
                if (!migrationOpt.isPresent()) {
                    failedImports++;
                    errors.add("Migration record not found for correspondence: " + correspondenceGuid);
                    continue;
                }
                
                IncomingCorrespondenceMigration migration = migrationOpt.get();
                
                // Execute creation steps
                boolean success = executeCreationSteps(migration);
                
                if (success) {
                    successfulImports++;
                    migration.markPhaseCompleted("CREATION");
                } else {
                    failedImports++;
                    migration.markPhaseError("CREATION", "Creation steps failed");
                }
                
                migrationRepository.save(migration);
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error creating correspondence " + correspondenceGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
        String message = String.format("Creation completed. Success: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return new ImportResponseDto(status, message, correspondenceGuids.size(), 
                                   successfulImports, failedImports, errors);
    }
    
    /**
     * Phase 3: Assignment - Optimized for large datasets
     */
    @Transactional
    public ImportResponseDto executeAssignmentPhase() {
        logger.info("Starting Phase 3: Assignment (Optimized)");
        
        // Get assignments that need processing using optimized query
        List<CorrespondenceTransaction> assignments = transactionRepository.findAssignmentsNeedingProcessing();
        
        logger.info("Found {} assignments to process", assignments.size());
        
        return executeAssignmentForSpecific(
            assignments.stream()
                .map(CorrespondenceTransaction::getGuid)
                .collect(java.util.stream.Collectors.toList())
        );
    }
    
    /**
     * Execute assignment for specific transaction GUIDs - Optimized
     */
    @Transactional
    public ImportResponseDto executeAssignmentForSpecific(List<String> transactionGuids) {
        logger.info("Executing assignment for {} specific transactions", transactionGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        // Load user department cache for performance
        loadUserDepartmentCache();
        
        for (String transactionGuid : transactionGuids) {
            try {
                Optional<CorrespondenceTransaction> transactionOpt = 
                    transactionRepository.findById(transactionGuid);
                
                if (!transactionOpt.isPresent()) {
                    failedImports++;
                    errors.add("Transaction not found: " + transactionGuid);
                    continue;
                }
                
                CorrespondenceTransaction transaction = transactionOpt.get();
                
                // Get the created document ID from migration record
                Optional<IncomingCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(transaction.getDocGuid());
                
                if (!migrationOpt.isPresent() || migrationOpt.get().getCreatedDocumentId() == null) {
                    failedImports++;
                    errors.add("No created document ID found for transaction: " + transactionGuid);
                    continue;
                }
                
                String documentId = migrationOpt.get().getCreatedDocumentId();
                
                // Get department code using cached lookup
                String departmentCode = getCachedDepartmentCode(transaction.getToUserName());
                
                // Create assignment in destination system
                boolean success = destinationSystemService.createAssignment(
                    transactionGuid,
                    transaction.getFromUserName(),
                    documentId,
                    transaction.getActionDate(),
                    transaction.getToUserName(),
                    departmentCode,
                    transaction.getDecisionGuid()
                );
                
                if (success) {
                    transaction.setMigrateStatus("SUCCESS");
                    successfulImports++;
                    logger.debug("Successfully created assignment: {}", transactionGuid);
                } else {
                    transaction.setMigrateStatus("FAILED");
                    transaction.setRetryCount(transaction.getRetryCount() + 1);
                    failedImports++;
                    errors.add("Failed to create assignment in destination system: " + transactionGuid);
                }
                
                transactionRepository.save(transaction);
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing assignment " + transactionGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
        String message = String.format("Assignment completed. Success: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return new ImportResponseDto(status, message, transactionGuids.size(), 
                                   successfulImports, failedImports, errors);
    }
    
    /**
     * Get assignment migrations with search and pagination - Optimized for large datasets
     */
    public Map<String, Object> getAssignmentMigrations(int page, int size, String status, String search) {
        logger.info("Getting assignment migrations - page: {}, size: {}, status: {}, search: '{}'", 
                   page, size, status, search);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // Normalize parameters for database query
            String normalizedStatus = (status == null || "all".equals(status)) ? null : status;
            String normalizedSearch = (search == null || search.trim().isEmpty()) ? null : search.trim();
            
            Page<Object[]> assignmentPage = transactionRepository.findAssignmentMigrationsWithSearchAndPagination(
                normalizedStatus, normalizedSearch, pageable);
            
            List<Map<String, Object>> assignments = new ArrayList<>();
            
            // Load user department cache for performance
            loadUserDepartmentCache();
            
            for (Object[] row : assignmentPage.getContent()) {
                Map<String, Object> assignment = new HashMap<>();
                assignment.put("transactionGuid", row[0]);
                assignment.put("correspondenceGuid", row[1]);
                assignment.put("fromUserName", row[2]);
                assignment.put("toUserName", row[3]);
                assignment.put("actionDate", row[4]);
                assignment.put("decisionGuid", row[5]);
                assignment.put("notes", row[6]);
                assignment.put("migrateStatus", row[7]);
                assignment.put("retryCount", row[8]);
                assignment.put("lastModifiedDate", row[9]);
                assignment.put("correspondenceSubject", row[10]);
                assignment.put("correspondenceReferenceNo", row[11]);
                assignment.put("createdDocumentId", row[12]);
                
                // Add department code using cached lookup
                String departmentCode = getCachedDepartmentCode((String) row[3]); // toUserName
                assignment.put("departmentCode", departmentCode);
                
                assignments.add(assignment);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", assignments);
            result.put("totalElements", assignmentPage.getTotalElements());
            result.put("totalPages", assignmentPage.getTotalPages());
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("hasNext", assignmentPage.hasNext());
            result.put("hasPrevious", assignmentPage.hasPrevious());
            
            // Create applied filters map (Java 8 compatible)
            Map<String, Object> appliedFilters = new HashMap<>();
            appliedFilters.put("status", normalizedStatus);
            appliedFilters.put("search", normalizedSearch);
            result.put("appliedFilters", appliedFilters);
            
            logger.info("Retrieved {} assignments for page {} with filters (status: {}, search: '{}')", 
                       assignments.size(), page, normalizedStatus, normalizedSearch);
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
     * Get creation migrations for detailed view
     */
    public List<IncomingCorrespondenceMigration> getCreationMigrations() {
        logger.info("Getting creation migrations");
        return migrationRepository.findByCurrentPhase("CREATION");
    }
    
    /**
     * Phase 4: Business Log (Placeholder)
     */
    @Transactional
    public ImportResponseDto executeBusinessLogPhase() {
        logger.info("Starting Phase 4: Business Log");
        
        // Get business logs that need processing using optimized query
        List<CorrespondenceTransaction> businessLogs = transactionRepository.findBusinessLogsNeedingProcessing();
        
        logger.info("Found {} business logs to process", businessLogs.size());
        
        return executeBusinessLogForSpecific(
            businessLogs.stream()
                .map(CorrespondenceTransaction::getGuid)
                .collect(java.util.stream.Collectors.toList())
        );
    }
    
    /**
     * Execute business log for specific transaction GUIDs
     */
    @Transactional
    public ImportResponseDto executeBusinessLogForSpecific(List<String> transactionGuids) {
        logger.info("Executing business log for {} specific transactions", transactionGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String transactionGuid : transactionGuids) {
            try {
                Optional<CorrespondenceTransaction> transactionOpt = 
                    transactionRepository.findById(transactionGuid);
                
                if (!transactionOpt.isPresent()) {
                    failedImports++;
                    errors.add("Transaction not found: " + transactionGuid);
                    continue;
                }
                
                CorrespondenceTransaction transaction = transactionOpt.get();
                
                // Get the created document ID from migration record
                Optional<IncomingCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(transaction.getDocGuid());
                
                if (!migrationOpt.isPresent() || migrationOpt.get().getCreatedDocumentId() == null) {
                    failedImports++;
                    errors.add("No created document ID found for transaction: " + transactionGuid);
                    continue;
                }
                
                String documentId = migrationOpt.get().getCreatedDocumentId();
                
                // Create business log in destination system
                boolean success = destinationSystemService.createBusinessLog(
                    transactionGuid,
                    documentId,
                    transaction.getActionDate(),
                    transaction.getActionEnglishName(),
                    transaction.getNotes(),
                    transaction.getFromUserName()
                );
                
                if (success) {
                    transaction.setMigrateStatus("SUCCESS");
                    successfulImports++;
                    logger.debug("Successfully created business log: {}", transactionGuid);
                } else {
                    transaction.setMigrateStatus("FAILED");
                    transaction.setRetryCount(transaction.getRetryCount() + 1);
                    failedImports++;
                    errors.add("Failed to create business log in destination system: " + transactionGuid);
                }
                
                transactionRepository.save(transaction);
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing business log " + transactionGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
        String message = String.format("Business log completed. Success: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return new ImportResponseDto(status, message, transactionGuids.size(), 
                                   successfulImports, failedImports, errors);
    }
    
    /**
     * Get business log migrations with search and pagination - Optimized for large datasets
     */
    public Map<String, Object> getBusinessLogMigrations(int page, int size, String status, String search) {
        logger.info("Getting business log migrations - page: {}, size: {}, status: {}, search: '{}'", 
                   page, size, status, search);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // Normalize parameters for database query
            String normalizedStatus = (status == null || "all".equals(status)) ? null : status;
            String normalizedSearch = (search == null || search.trim().isEmpty()) ? null : search.trim();
            
            Page<Object[]> businessLogPage = transactionRepository.findBusinessLogMigrationsWithSearchAndPagination(
                normalizedStatus, normalizedSearch, pageable);
            
            List<Map<String, Object>> businessLogs = new ArrayList<>();
            
            for (Object[] row : businessLogPage.getContent()) {
                Map<String, Object> businessLog = new HashMap<>();
                businessLog.put("transactionGuid", row[0]);
                businessLog.put("correspondenceGuid", row[1]);
                businessLog.put("actionId", row[2]);
                businessLog.put("actionEnglishName", row[3]);
                businessLog.put("actionLocalName", row[4]);
                businessLog.put("actionDate", row[5]);
                businessLog.put("fromUserName", row[6]);
                businessLog.put("notes", row[7]);
                businessLog.put("migrateStatus", row[8]);
                businessLog.put("retryCount", row[9]);
                businessLog.put("lastModifiedDate", row[10]);
                businessLog.put("correspondenceSubject", row[11]);
                businessLog.put("correspondenceReferenceNo", row[12]);
                businessLog.put("createdDocumentId", row[13]);
                
                businessLogs.add(businessLog);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", businessLogs);
            result.put("totalElements", businessLogPage.getTotalElements());
            result.put("totalPages", businessLogPage.getTotalPages());
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("hasNext", businessLogPage.hasNext());
            result.put("hasPrevious", businessLogPage.hasPrevious());
            
            // Create applied filters map (Java 8 compatible)
            Map<String, Object> appliedFilters = new HashMap<>();
            appliedFilters.put("status", normalizedStatus);
            appliedFilters.put("search", normalizedSearch);
            result.put("appliedFilters", appliedFilters);
            
            logger.info("Retrieved {} business logs for page {} with filters (status: {}, search: '{}')", 
                       businessLogs.size(), page, normalizedStatus, normalizedSearch);
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
     * Phase 5: Comment (Placeholder)
     */
    @Transactional
    public ImportResponseDto executeCommentPhase() {
        logger.info("Starting Phase 5: Comment");
        
        // Get comments that need processing using optimized query
        List<CorrespondenceComment> comments = commentRepository.findCommentsNeedingProcessing();
        
        logger.info("Found {} comments to process", comments.size());
        
        return executeCommentForSpecific(
            comments.stream()
                .map(CorrespondenceComment::getCommentGuid)
                .collect(java.util.stream.Collectors.toList())
        );
    }
    
    /**
     * Execute comment for specific comment GUIDs
     */
    @Transactional
    public ImportResponseDto executeCommentForSpecific(List<String> commentGuids) {
        logger.info("Executing comment for {} specific comments", commentGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String commentGuid : commentGuids) {
            try {
                Optional<CorrespondenceComment> commentOpt = 
                    commentRepository.findById(commentGuid);
                
                if (!commentOpt.isPresent()) {
                    failedImports++;
                    errors.add("Comment not found: " + commentGuid);
                    continue;
                }
                
                CorrespondenceComment comment = commentOpt.get();
                
                // Get the created document ID from migration record
                Optional<IncomingCorrespondenceMigration> migrationOpt = 
                    migrationRepository.findByCorrespondenceGuid(comment.getDocGuid());
                
                if (!migrationOpt.isPresent() || migrationOpt.get().getCreatedDocumentId() == null) {
                    failedImports++;
                    errors.add("No created document ID found for comment: " + commentGuid);
                    continue;
                }
                
                String documentId = migrationOpt.get().getCreatedDocumentId();
                
                // Create comment in destination system
                boolean success = destinationSystemService.createComment(
                    commentGuid,
                    documentId,
                    comment.getCommentCreationDate(),
                    comment.getComment(),
                    comment.getCreationUserGuid()
                );
                
                if (success) {
                    comment.setMigrateStatus("SUCCESS");
                    successfulImports++;
                    logger.debug("Successfully created comment: {}", commentGuid);
                } else {
                    comment.setMigrateStatus("FAILED");
                    comment.setRetryCount(comment.getRetryCount() + 1);
                    failedImports++;
                    errors.add("Failed to create comment in destination system: " + commentGuid);
                }
                
                commentRepository.save(comment);
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing comment " + commentGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
        String message = String.format("Comment completed. Success: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return new ImportResponseDto(status, message, commentGuids.size(), 
                                   successfulImports, failedImports, errors);
    }
    
    /**
     * Get comment migrations with search and pagination - Optimized for large datasets
     */
    public Map<String, Object> getCommentMigrations(int page, int size, String status, String commentType, String search) {
        logger.info("Getting comment migrations - page: {}, size: {}, status: {}, commentType: {}, search: '{}'", 
                   page, size, status, commentType, search);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // Normalize parameters for database query
            String normalizedStatus = (status == null || "all".equals(status)) ? null : status;
            String normalizedCommentType = (commentType == null || "all".equals(commentType)) ? null : commentType;
            String normalizedSearch = (search == null || search.trim().isEmpty()) ? null : search.trim();
            
            Page<Object[]> commentPage = commentRepository.findCommentMigrationsWithSearchAndPagination(
                normalizedStatus, normalizedCommentType, normalizedSearch, pageable);
            
            List<Map<String, Object>> comments = new ArrayList<>();
            
            for (Object[] row : commentPage.getContent()) {
                Map<String, Object> comment = new HashMap<>();
                comment.put("commentGuid", row[0]);
                comment.put("correspondenceGuid", row[1]);
                comment.put("commentCreationDate", row[2]);
                comment.put("comment", row[3]);
                comment.put("commentType", row[4]);
                comment.put("creationUserGuid", row[5]);
                comment.put("roleGuid", row[6]);
                comment.put("attachmentCaption", row[7]);
                comment.put("migrateStatus", row[8]);
                comment.put("retryCount", row[9]);
                comment.put("lastModifiedDate", row[10]);
                comment.put("correspondenceSubject", row[11]);
                comment.put("correspondenceReferenceNo", row[12]);
                comment.put("createdDocumentId", row[13]);
                
                comments.add(comment);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", comments);
            result.put("totalElements", commentPage.getTotalElements());
            result.put("totalPages", commentPage.getTotalPages());
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("hasNext", commentPage.hasNext());
            result.put("hasPrevious", commentPage.hasPrevious());
            
            // Create applied filters map (Java 8 compatible)
            Map<String, Object> appliedFilters = new HashMap<>();
            appliedFilters.put("status", normalizedStatus);
            appliedFilters.put("commentType", normalizedCommentType);
            appliedFilters.put("search", normalizedSearch);
            result.put("appliedFilters", appliedFilters);
            
            logger.info("Retrieved {} comments for page {} with filters (status: {}, commentType: {}, search: '{}')", 
                       comments.size(), page, normalizedStatus, normalizedCommentType, normalizedSearch);
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
     * Phase 6: Closing (Placeholder)
     */
    @Transactional
    public ImportResponseDto executeClosingPhase() {
        logger.info("Starting Phase 6: Closing");
        
        // Placeholder implementation
        return new ImportResponseDto("SUCCESS", "Closing phase not yet implemented", 
            0, 0, 0, new ArrayList<>());
    }
    
    /**
     * Retry failed migrations
     */
    @Transactional
    public ImportResponseDto retryFailedMigrations() {
        logger.info("Retrying failed migrations");
        
        List<IncomingCorrespondenceMigration> retryableMigrations = 
            migrationRepository.findRetryableMigrations();
        
        logger.info("Found {} migrations to retry", retryableMigrations.size());
        
        // Group by current phase and execute appropriate retry logic
        Map<String, List<IncomingCorrespondenceMigration>> groupedByPhase = new HashMap<>();
        for (IncomingCorrespondenceMigration migration : retryableMigrations) {
            groupedByPhase.computeIfAbsent(migration.getCurrentPhase(), k -> new ArrayList<>()).add(migration);
        }
        
        int totalRetried = 0;
        List<String> errors = new ArrayList<>();
        
        for (Map.Entry<String, List<IncomingCorrespondenceMigration>> entry : groupedByPhase.entrySet()) {
            String phase = entry.getKey();
            List<String> guids = entry.getValue().stream()
                .map(IncomingCorrespondenceMigration::getCorrespondenceGuid)
                .collect(java.util.stream.Collectors.toList());
            
            try {
                ImportResponseDto result;
                switch (phase) {
                    case "CREATION":
                        result = executeCreationForSpecific(guids);
                        break;
                    case "ASSIGNMENT":
                        // Get transaction GUIDs for assignment retry
                        List<String> transactionGuids = transactionRepository
                            .findAssignmentsByMigrateStatusIn(Arrays.asList("FAILED"))
                            .stream()
                            .map(CorrespondenceTransaction::getGuid)
                            .collect(java.util.stream.Collectors.toList());
                        result = executeAssignmentForSpecific(transactionGuids);
                        break;
                    default:
                        result = new ImportResponseDto("SUCCESS", "Phase " + phase + " retry not implemented", 
                            0, 0, 0, new ArrayList<>());
                        break;
                }
                
                totalRetried += result.getSuccessfulImports();
                if (result.getErrors() != null) {
                    errors.addAll(result.getErrors());
                }
                
            } catch (Exception e) {
                String errorMsg = "Error retrying phase " + phase + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        return new ImportResponseDto("SUCCESS", 
            "Retry completed. Retried: " + totalRetried + " migrations", 
            retryableMigrations.size(), totalRetried, 0, errors);
    }
    
    /**
     * Get migration statistics
     */
    public Map<String, Object> getMigrationStatistics() {
        logger.info("Getting migration statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get phase counts
            stats.put("prepareData", migrationRepository.countByCurrentPhase("PREPARE_DATA"));
            stats.put("creation", migrationRepository.countByCurrentPhase("CREATION"));
            stats.put("assignment", migrationRepository.countByCurrentPhase("ASSIGNMENT"));
            stats.put("businessLog", migrationRepository.countByCurrentPhase("BUSINESS_LOG"));
            stats.put("comment", migrationRepository.countByCurrentPhase("COMMENT"));
            stats.put("closing", migrationRepository.countByCurrentPhase("CLOSING"));
            
            // Get overall status counts
            stats.put("completed", migrationRepository.countByOverallStatus("COMPLETED"));
            stats.put("failed", migrationRepository.countByOverallStatus("FAILED"));
            stats.put("inProgress", migrationRepository.countByOverallStatus("IN_PROGRESS"));
            
            // Get assignment-specific statistics using optimized query
            Object[] assignmentStats = transactionRepository.getAssignmentStatistics();
            if (assignmentStats != null && assignmentStats.length >= 4) {
                Map<String, Object> assignmentMap = new HashMap<>();
                assignmentMap.put("pending", assignmentStats[0]);
                assignmentMap.put("success", assignmentStats[1]);
                assignmentMap.put("failed", assignmentStats[2]);
                assignmentMap.put("total", assignmentStats[3]);
                stats.put("assignmentDetails", assignmentMap);
            }
            
        } catch (Exception e) {
            logger.error("Error getting migration statistics", e);
            // Return default values on error
            stats.put("prepareData", 0L);
            stats.put("creation", 0L);
            stats.put("assignment", 0L);
            stats.put("businessLog", 0L);
            stats.put("comment", 0L);
            stats.put("closing", 0L);
            stats.put("completed", 0L);
            stats.put("failed", 0L);
            stats.put("inProgress", 0L);
        }
        
        return stats;
    }
    
    // Helper methods
    
    private boolean determineIfNeedToClose(Correspondence correspondence) {
        // Logic to determine if correspondence needs to be closed
        // This could be based on status, dates, or other business rules
        return correspondence.getIsFinal() != null && correspondence.getIsFinal();
    }
    
    private boolean executeCreationSteps(IncomingCorrespondenceMigration migration) {
        String correspondenceGuid = migration.getCorrespondenceGuid();
        logger.info("Executing creation steps for correspondence: {}", correspondenceGuid);
        
        try {
            // Step 1: Get Details
            migration.setCreationStep("GET_DETAILS");
            migrationRepository.save(migration);
            
            Optional<Correspondence> correspondenceOpt = correspondenceRepository.findById(correspondenceGuid);
            if (!correspondenceOpt.isPresent()) {
                logger.error("Correspondence not found: {}", correspondenceGuid);
                return false;
            }
            
            Correspondence correspondence = correspondenceOpt.get();
            
            // Step 2: Get Attachments
            migration.setCreationStep("GET_ATTACHMENTS");
            migrationRepository.save(migration);
            
            List<CorrespondenceAttachment> attachments = attachmentRepository.findByDocGuid(correspondenceGuid);
            CorrespondenceAttachment primaryAttachment = AttachmentUtils.findPrimaryAttachment(attachments);
            
            String batchId = null;
            
            // Step 3: Upload Main Attachment (if exists)
            if (primaryAttachment != null && AttachmentUtils.isValidForUpload(primaryAttachment)) {
                migration.setCreationStep("UPLOAD_MAIN_ATTACHMENT");
                migrationRepository.save(migration);
                
                batchId = destinationSystemService.createBatch();
                if (batchId != null) {
                    migration.setBatchId(batchId);
                    
                    String fileName = AttachmentUtils.getFileNameForUpload(primaryAttachment.getName(), true);
                    String fileData = AttachmentUtils.getFileDataForUpload(
                        primaryAttachment.getFileData(), fileName, true);
                    
                    boolean uploadSuccess = destinationSystemService.uploadBase64FileToBatch(
                        batchId, "0", fileData, fileName);
                    
                    if (!uploadSuccess) {
                        logger.warn("Failed to upload primary attachment for correspondence: {}", correspondenceGuid);
                    }
                }
            }
            
            // Step 4: Create Correspondence
            migration.setCreationStep("CREATE_CORRESPONDENCE");
            migrationRepository.save(migration);
            
            String documentId = createCorrespondenceInDestination(correspondence, batchId);
            if (documentId == null) {
                logger.error("Failed to create correspondence in destination system: {}", correspondenceGuid);
                return false;
            }
            
            migration.setCreatedDocumentId(documentId);
            
            // Step 5: Upload Other Attachments
            migration.setCreationStep("UPLOAD_OTHER_ATTACHMENTS");
            migrationRepository.save(migration);
            
            List<CorrespondenceAttachment> otherAttachments = AttachmentUtils.getNonPrimaryAttachments(attachments, primaryAttachment);
            for (CorrespondenceAttachment attachment : otherAttachments) {
                if (AttachmentUtils.isValidForUpload(attachment)) {
                    String attachmentBatchId = destinationSystemService.createBatch();
                    if (attachmentBatchId != null) {
                        String fileName = AttachmentUtils.getFileNameForUpload(attachment.getName(), false);
                        String fileData = AttachmentUtils.getFileDataForUpload(
                            attachment.getFileData(), fileName, false);
                        
                        boolean uploadSuccess = destinationSystemService.uploadBase64FileToBatch(
                            attachmentBatchId, "0", fileData, fileName);
                        
                        if (uploadSuccess) {
                            destinationSystemService.createAttachment(attachment, attachmentBatchId, documentId);
                        }
                    }
                }
            }
            
            // Additional steps for complete correspondence setup
            executeAdditionalCreationSteps(migration, documentId, correspondence);
            
            migration.setCreationStep("COMPLETED");
            migrationRepository.save(migration);
            
            logger.info("Successfully completed creation for correspondence: {}", correspondenceGuid);
            return true;
            
        } catch (Exception e) {
            logger.error("Error in creation steps for correspondence: {}", correspondenceGuid, e);
            migration.setCreationError(e.getMessage());
            migrationRepository.save(migration);
            return false;
        }
    }
    
    private String createCorrespondenceInDestination(Correspondence correspondence, String batchId) {
        try {
            // Map correspondence data to destination format
            String subject = correspondence.getSubject() != null ? correspondence.getSubject() : "";
            String externalRef = correspondence.getExternalReferenceNumber();
            String notes = correspondence.getNotes();
            String referenceNo = correspondence.getReferenceNo();
            String category = CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid());
            String secrecyLevel = CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId());
            String priority = CorrespondenceUtils.mapPriority(correspondence.getPriorityId());
            Boolean requireReply = CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus());
            String fromAgency = AgencyMappingUtils.mapAgencyGuidToCode(correspondence.getComingFromGuid());
            
            // Convert dates to Hijri
            String gDate = HijriDateUtils.formatToIsoString(correspondence.getIncomingDate());
            String hDate = HijriDateUtils.convertToHijri(correspondence.getIncomingDate());
            String gDueDate = HijriDateUtils.formatToIsoString(correspondence.getDueDate());
            String hDueDate = HijriDateUtils.convertToHijri(correspondence.getDueDate());
            String gDocumentDate = HijriDateUtils.formatToIsoString(correspondence.getCorrespondenceCreationDate());
            String hDocumentDate = HijriDateUtils.convertToHijri(correspondence.getCorrespondenceCreationDate());
            
            // Get target department
            String toDepartment = DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getCreationDepartmentGuid());
            if (toDepartment == null) {
                toDepartment = "CEO"; // Default fallback
            }
            
            String action = "ForAdvice"; // Default action
            String asUser = correspondence.getCreationUserName() != null ? 
                          correspondence.getCreationUserName() : "itba-emp1";
            
            return destinationSystemService.createIncomingCorrespondence(
                correspondence.getGuid(), asUser, gDate, subject, externalRef, notes,
                referenceNo, category, secrecyLevel, priority, gDueDate, hDueDate,
                requireReply, fromAgency, gDocumentDate, hDocumentDate, gDate, hDate,
                toDepartment, batchId, action
            );
            
        } catch (Exception e) {
            logger.error("Error creating correspondence in destination: {}", correspondence.getGuid(), e);
            return null;
        }
    }
    
    private void executeAdditionalCreationSteps(IncomingCorrespondenceMigration migration, 
                                              String documentId, Correspondence correspondence) {
        try {
            String asUser = correspondence.getCreationUserName() != null ? 
                          correspondence.getCreationUserName() : "itba-emp1";
            
            // Step: Create Physical Attachment
            migration.setCreationStep("CREATE_PHYSICAL_ATTACHMENT");
            migrationRepository.save(migration);
            
            String physicalAttachments = correspondence.getManualAttachmentsCount();
            destinationSystemService.createPhysicalAttachment(documentId, asUser, physicalAttachments);
            
            // Step: Set Ready to Register
            migration.setCreationStep("SET_READY_TO_REGISTER");
            migrationRepository.save(migration);
            
            destinationSystemService.setIncomingReadyToRegister(documentId, asUser);
            
            // Step: Register with Reference
            migration.setCreationStep("REGISTER_WITH_REFERENCE");
            migrationRepository.save(migration);
            
            // Build context for registration (reuse creation context)
            Map<String, Object> incCorrespondenceContext = buildCorrespondenceContext(correspondence);
            destinationSystemService.registerWithReference(documentId, asUser, incCorrespondenceContext);
            
            // Step: Start Work
            migration.setCreationStep("START_WORK");
            migrationRepository.save(migration);
            
            destinationSystemService.startIncomingCorrespondenceWork(documentId, asUser);
            
            // Step: Set Owner
            migration.setCreationStep("SET_OWNER");
            migrationRepository.save(migration);
            
            destinationSystemService.setCorrespondenceOwner(documentId, asUser);
            
        } catch (Exception e) {
            logger.error("Error in additional creation steps", e);
            throw e;
        }
    }
    
    private Map<String, Object> buildCorrespondenceContext(Correspondence correspondence) {
        Map<String, Object> context = new HashMap<>();
        
        context.put("corr:subject", correspondence.getSubject() != null ? correspondence.getSubject() : "");
        context.put("corr:externalCorrespondenceNumber", correspondence.getExternalReferenceNumber() != null ? correspondence.getExternalReferenceNumber() : "");
        context.put("corr:remarks", correspondence.getNotes() != null ? correspondence.getNotes() : "");
        context.put("corr:referenceNumber", correspondence.getReferenceNo() != null ? correspondence.getReferenceNo() : "");
        context.put("corr:category", CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid()));
        context.put("corr:secrecyLevel", CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId()));
        context.put("corr:priority", CorrespondenceUtils.mapPriority(correspondence.getPriorityId()));
        context.put("corr:requireReply", CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus()));
        context.put("corr:fromAgency", AgencyMappingUtils.mapAgencyGuidToCode(correspondence.getComingFromGuid()));
        context.put("corr:toAgency", "ITBA");
        context.put("corr:delivery", "unknown");
        
        // Add dates
        context.put("corr:gDate", HijriDateUtils.formatToIsoString(correspondence.getIncomingDate()));
        context.put("corr:hDate", HijriDateUtils.convertToHijri(correspondence.getIncomingDate()));
        context.put("corr:gDueDate", HijriDateUtils.formatToIsoString(correspondence.getDueDate()));
        context.put("corr:hDueDate", HijriDateUtils.convertToHijri(correspondence.getDueDate()));
        context.put("corr:gDocumentDate", HijriDateUtils.formatToIsoString(correspondence.getCorrespondenceCreationDate()));
        context.put("corr:hDocumentDate", HijriDateUtils.convertToHijri(correspondence.getCorrespondenceCreationDate()));
        
        return context;
    }
    
    /**
     * Load user department cache for performance optimization
     */
    private void loadUserDepartmentCache() {
        if (userDepartmentCache.isEmpty()) {
            logger.info("Loading user department cache for performance optimization");
            
            try {
                // Load users.json and cache department mappings
                com.fasterxml.jackson.core.type.TypeReference<List<com.importservice.dto.UserImportDto>> typeRef = 
                    new com.fasterxml.jackson.core.type.TypeReference<List<com.importservice.dto.UserImportDto>>() {};
                
                org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource("users.json");
                java.io.InputStream inputStream = resource.getInputStream();
                
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                List<com.importservice.dto.UserImportDto> users = objectMapper.readValue(inputStream, typeRef);
                
                for (com.importservice.dto.UserImportDto user : users) {
                    if (user.getEmail() != null && user.getDepartmentCode() != null) {
                        // Cache both email and username mappings
                        userDepartmentCache.put(user.getEmail(), user.getDepartmentCode());
                        userDepartmentCache.put(user.getUsernameFromEmail(), user.getDepartmentCode());
                    }
                }
                
                logger.info("Loaded {} user department mappings into cache", userDepartmentCache.size());
                
            } catch (Exception e) {
                logger.error("Error loading user department cache", e);
            }
        }
    }
    
    /**
     * Get department code using cached lookup for performance
     */
    private String getCachedDepartmentCode(String userIdentifier) {
        if (userIdentifier == null || userIdentifier.trim().isEmpty()) {
            return "CEO"; // Default fallback
        }
        
        // Try cache lookup first
        String departmentCode = userDepartmentCache.get(userIdentifier.trim());
        
        if (departmentCode != null) {
            return departmentCode;
        }
        
        // Fallback to database lookup (slower)
        try {
            String departmentGuid = departmentUtils.getDepartmentGuidByUserEmail(userIdentifier);
            if (departmentGuid != null) {
                departmentCode = DepartmentUtils.getDepartmentCodeByOldGuid(departmentGuid);
                if (departmentCode != null) {
                    // Cache the result for future use
                    userDepartmentCache.put(userIdentifier, departmentCode);
                    return departmentCode;
                }
            }
        } catch (Exception e) {
            logger.debug("Error in database lookup for user: {}", userIdentifier, e);
        }
        
        return "CEO"; // Default fallback
    }
}