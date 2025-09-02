package com.importservice.service.migration;

import java.math.BigInteger;
import java.util.Arrays;
import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.CorrespondenceComment;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceCommentRepository;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
import com.importservice.service.DestinationSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for Phase 5: Comment
 * Processes comments and annotations
 */
@Service
public class CommentPhaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentPhaseService.class);
    
    @Autowired
    private CorrespondenceCommentRepository commentRepository;
    
    @Autowired
    private IncomingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private DestinationSystemService destinationService;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    /**
     * Phase 5: Comment
     * Processes comments for correspondences
     */
    @Transactional
    public ImportResponseDto executeCommentPhase() {
        logger.info("Starting Phase 5: Comment");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<CorrespondenceComment> comments = commentRepository.findCommentsNeedingProcessing();
            
            for (CorrespondenceComment comment : comments) {
                try {
                    boolean success = processComment(comment);
                    if (success) {
                        successfulImports++;
                        comment.setMigrateStatus("SUCCESS");
                    } else {
                        failedImports++;
                        comment.setMigrateStatus("FAILED");
                        comment.setRetryCount(comment.getRetryCount() + 1);
                    }
                    commentRepository.save(comment);
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error processing comment " + comment.getCommentGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    comment.setMigrateStatus("FAILED");
                    comment.setRetryCount(comment.getRetryCount() + 1);
                    commentRepository.save(comment);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Phase 5 completed. Processed: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, comments.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 5: Comment", e);
            return phaseService.createResponse("ERROR", "Phase 5 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Executes comment for specific comments
     */
    @Transactional(timeout = 180)
    public ImportResponseDto executeCommentForSpecific(List<String> commentGuids) {
        logger.info("Starting comment for {} specific comments", commentGuids.size());
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        for (String commentGuid : commentGuids) {
            try {
                boolean success = processCommentForGuid(commentGuid);
                
                if (success) {
                    successfulImports++;
                } else {
                    failedImports++;
                }
                
            } catch (Exception e) {
                failedImports++;
                String errorMsg = "Error processing comment " + commentGuid + ": " + e.getMessage();
                errors.add(errorMsg);
                logger.error(errorMsg, e);
            }
        }
        
        String status = phaseService.determineFinalStatus(successfulImports, failedImports);
        String message = String.format("Specific comment completed. Processed: %d, Failed: %d", 
                                     successfulImports, failedImports);
        
        return phaseService.createResponse(status, message, commentGuids.size(), 
                                         successfulImports, failedImports, errors);
    }
    
    /**
     * Processes comment for a single comment GUID
     */
    private boolean processCommentForGuid(String commentGuid) {
        try {
            Optional<CorrespondenceComment> commentOpt = 
                commentRepository.findById(commentGuid);
            
            if (!commentOpt.isPresent()) {
                logger.error("Comment not found: {}", commentGuid);
                return false;
            }
            
            CorrespondenceComment comment = commentOpt.get();
            boolean success = processComment(comment);
            
            if (success) {
                comment.setMigrateStatus("SUCCESS");
            } else {
                comment.setMigrateStatus("FAILED");
                comment.setRetryCount(comment.getRetryCount() + 1);
            }
            commentRepository.save(comment);
            
            return success;
        } catch (Exception e) {
            logger.error("Error processing comment for: {}", commentGuid, e);
            return false;
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
                logger.error("No created document ID found for correspondence: {}", comment.getDocGuid());
                return false;
            }
            
            String documentId = migrationOpt.get().getCreatedDocumentId();
            
            // Use actual user GUID from comment data
            String creationUser = comment.getCreationUserGuid() != null ? comment.getCreationUserGuid() : "itba-emp1";
            
            // Create comment in destination system
            return destinationService.createComment(
                comment.getCommentGuid(),
                documentId,
                comment.getCommentCreationDate(),
                comment.getComment(),
                creationUser
            );
            
        } catch (Exception e) {
            logger.error("Error processing comment: {}", comment.getCommentGuid(), e);
            return false;
        }
    }
    
    /**
     * Gets comment migrations with pagination and search
     */
    public Map<String, Object> getCommentMigrations(int page, int size, String status, String commentType, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Object[]> commentPage;
            if ((status != null && !"all".equals(status)) || 
                (commentType != null && !"all".equals(commentType)) || 
                (search != null && !search.trim().isEmpty())) {
                
                String statusParam = "all".equals(status) ? null : status;
                String commentTypeParam = "all".equals(commentType) ? null : commentType;
                String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();
                
                commentPage = commentRepository.findCommentMigrationsWithSearchAndPagination(
                    statusParam, commentTypeParam, searchParam, pageable);
            } else {
                commentPage = commentRepository.findCommentMigrationsWithPagination(pageable);
            }
            
            List<Map<String, Object>> comments = new ArrayList<>();
            for (Object[] row : commentPage.getContent()) {
                Map<String, Object> comment = new HashMap<>();
                comment.put("commentGuid", row[0]);
                comment.put("correspondenceGuid", row[1]);
                comment.put("commentCreationDate", row[2] != null ? ((Timestamp) row[2]).toLocalDateTime() : null);
                comment.put("comment", row[3]);
                comment.put("commentType", row[4]);
                comment.put("creationUserGuid", row[5]);
                comment.put("roleGuid", row[6]);
                comment.put("attachmentCaption", row[7]);
                comment.put("migrateStatus", row[8]);
                comment.put("retryCount", row[9] != null ? ((Number) row[9]).intValue() : 0);
                comment.put("lastModifiedDate", row[10] != null ? ((Timestamp) row[10]).toLocalDateTime() : null);
                comment.put("correspondenceSubject", row[11]);
                comment.put("correspondenceReferenceNo", row[12]);
                comment.put("createdDocumentId", row[13]);
                comments.add(comment);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", comments);
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
}