package com.importservice.service.migration;

import java.math.BigInteger;
import com.importservice.repository.CorrespondenceCommentRepository;
import com.importservice.repository.CorrespondenceTransactionRepository;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for migration statistics and reporting
 */
@Service
public class MigrationStatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(MigrationStatisticsService.class);
    
    @Autowired
    private IncomingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private CorrespondenceTransactionRepository transactionRepository;
    
    @Autowired
    private CorrespondenceCommentRepository commentRepository;
    
    /**
     * Gets comprehensive migration statistics
     */
    @Transactional(readOnly = true, timeout = 60)
    public Map<String, Object> getMigrationStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // Phase-specific counts - use safe defaults
            Long prepareDataCount = 0L;
            Long creationCount = 0L;
            Long assignmentCount = 0L;
            Long businessLogCount = 0L;
            Long commentCount = 0L;
            Long closingCount = 0L;
            
            try {
                prepareDataCount = migrationRepository.countByCurrentPhase("PREPARE_DATA");
                creationCount = migrationRepository.countByCurrentPhase("CREATION");
                assignmentCount = migrationRepository.countByCurrentPhase("ASSIGNMENT");
                businessLogCount = migrationRepository.countByCurrentPhase("BUSINESS_LOG");
                commentCount = migrationRepository.countByCurrentPhase("COMMENT");
                closingCount = migrationRepository.countByCurrentPhase("CLOSING");
            } catch (Exception e) {
                logger.warn("Error getting phase counts, using defaults: {}", e.getMessage());
            }
            
            statistics.put("prepareData", prepareDataCount != null ? prepareDataCount : 0L);
            statistics.put("creation", creationCount != null ? creationCount : 0L);
            statistics.put("assignment", assignmentCount != null ? assignmentCount : 0L);
            statistics.put("businessLog", businessLogCount != null ? businessLogCount : 0L);
            statistics.put("comment", commentCount != null ? commentCount : 0L);
            statistics.put("closing", closingCount != null ? closingCount : 0L);
            
            // Overall status counts - use safe defaults
            Long completedCount = 0L;
            Long failedCount = 0L;
            Long inProgressCount = 0L;
            
            try {
                completedCount = migrationRepository.countByOverallStatus("COMPLETED");
                failedCount = migrationRepository.countByOverallStatus("FAILED");
                inProgressCount = migrationRepository.countByOverallStatus("IN_PROGRESS");
            } catch (Exception e) {
                logger.warn("Error getting overall status counts, using defaults: {}", e.getMessage());
            }
            
            statistics.put("completed", completedCount != null ? completedCount : 0L);
            statistics.put("failed", failedCount != null ? failedCount : 0L);
            statistics.put("inProgress", inProgressCount != null ? inProgressCount : 0L);
            
            // Assignment statistics - handle potential ambiguity
            try {
                Object[] assignmentStats = transactionRepository.getAssignmentStatistics();
                if (assignmentStats != null && assignmentStats.length >= 4) {
                    Map<String, Object> assignmentStatistics = new HashMap<>();
                    assignmentStatistics.put("pending", assignmentStats[0] != null ? ((Number) assignmentStats[0]).longValue() : 0L);
                    assignmentStatistics.put("success", assignmentStats[1] != null ? ((Number) assignmentStats[1]).longValue() : 0L);
                    assignmentStatistics.put("failed", assignmentStats[2] != null ? ((Number) assignmentStats[2]).longValue() : 0L);
                    assignmentStatistics.put("total", assignmentStats[3] != null ? ((Number) assignmentStats[3]).longValue() : 0L);
                    statistics.put("assignmentDetails", assignmentStatistics);
                }
            } catch (Exception e) {
                logger.warn("Error getting assignment statistics: {}", e.getMessage());
            }
            
            // Business log statistics - handle potential ambiguity
            try {
                Object[] businessLogStats = transactionRepository.getBusinessLogStatistics();
                if (businessLogStats != null && businessLogStats.length >= 4) {
                    Map<String, Object> businessLogStatistics = new HashMap<>();
                    businessLogStatistics.put("pending", businessLogStats[0] != null ? ((Number) businessLogStats[0]).longValue() : 0L);
                    businessLogStatistics.put("success", businessLogStats[1] != null ? ((Number) businessLogStats[1]).longValue() : 0L);
                    businessLogStatistics.put("failed", businessLogStats[2] != null ? ((Number) businessLogStats[2]).longValue() : 0L);
                    businessLogStatistics.put("total", businessLogStats[3] != null ? ((Number) businessLogStats[3]).longValue() : 0L);
                    statistics.put("businessLogDetails", businessLogStatistics);
                }
            } catch (Exception e) {
                logger.warn("Error getting business log statistics: {}", e.getMessage());
            }
            
            // Comment statistics - handle potential ambiguity
            try {
                Object[] commentStats = commentRepository.getCommentStatistics();
                if (commentStats != null && commentStats.length >= 4) {
                    Map<String, Object> commentStatistics = new HashMap<>();
                    commentStatistics.put("pending", commentStats[0] != null ? ((Number) commentStats[0]).longValue() : 0L);
                    commentStatistics.put("success", commentStats[1] != null ? ((Number) commentStats[1]).longValue() : 0L);
                    commentStatistics.put("failed", commentStats[2] != null ? ((Number) commentStats[2]).longValue() : 0L);
                    commentStatistics.put("total", commentStats[3] != null ? ((Number) commentStats[3]).longValue() : 0L);
                    statistics.put("commentDetails", commentStatistics);
                }
            } catch (Exception e) {
                logger.warn("Error getting comment statistics: {}", e.getMessage());
            }
            
            // Closing statistics - use safe defaults
            Long needToCloseCount = 0L;
            Long closingCompletedCount = 0L;
            Long closingFailedCount = 0L;
            
            try {
                needToCloseCount = migrationRepository.countByIsNeedToClose(true);
                closingCompletedCount = migrationRepository.countByClosingStatus("COMPLETED");
                closingFailedCount = migrationRepository.countByClosingStatus("FAILED");
            } catch (Exception e) {
                logger.warn("Error getting closing statistics, using defaults: {}", e.getMessage());
            }
            
            statistics.put("needToCloseCount", needToCloseCount != null ? needToCloseCount : 0L);
            statistics.put("closingCompleted", closingCompletedCount != null ? closingCompletedCount : 0L);
            statistics.put("closingFailed", closingFailedCount != null ? closingFailedCount : 0L);
            
            logger.debug("Generated migration statistics: {}", statistics);
            return statistics;
            
        } catch (Exception e) {
            logger.error("Error getting migration statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", "Failed to get statistics: " + e.getMessage());
            
            // Add default values for all expected fields
            errorStats.put("prepareData", 0L);
            errorStats.put("creation", 0L);
            errorStats.put("assignment", 0L);
            errorStats.put("businessLog", 0L);
            errorStats.put("comment", 0L);
            errorStats.put("closing", 0L);
            errorStats.put("completed", 0L);
            errorStats.put("failed", 0L);
            errorStats.put("inProgress", 0L);
            errorStats.put("needToCloseCount", 0L);
            errorStats.put("closingCompleted", 0L);
            errorStats.put("closingFailed", 0L);
            
            return errorStats;
        }
    }
    
    /**
     * Gets retry failed migrations statistics
     */
    @Transactional(readOnly = true, timeout = 30)
    public Map<String, Object> getRetryStatistics() {
        try {
            Map<String, Object> retryStats = new HashMap<>();
            
            // Get retryable migrations count
            Long retryableCount = (long) migrationRepository.findRetryableMigrations().size();
            retryStats.put("retryableCount", retryableCount);
            
            // Get failed assignments count
            Long failedAssignments = transactionRepository.countAssignmentsByMigrateStatus("FAILED");
            retryStats.put("failedAssignments", failedAssignments);
            
            // Get failed business logs count
            Long failedBusinessLogs = transactionRepository.countBusinessLogsByMigrateStatus("FAILED");
            retryStats.put("failedBusinessLogs", failedBusinessLogs);
            
            // Get failed comments count
            Long failedComments = commentRepository.countCommentsByMigrateStatus("FAILED");
            retryStats.put("failedComments", failedComments);
            
            return retryStats;
            
        } catch (Exception e) {
            logger.error("Error getting retry statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", "Failed to get retry statistics: " + e.getMessage());
            return errorStats;
        }
    }
}