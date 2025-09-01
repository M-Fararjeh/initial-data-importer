package com.importservice.service.migration;

import java.math.BigInteger;
import com.importservice.repository.CorrespondenceCommentRepository;
import com.importservice.repository.CorrespondenceTransactionRepository;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Map<String, Object> getMigrationStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // Phase-specific counts
            statistics.put("prepareData", migrationRepository.countByCurrentPhase("PREPARE_DATA"));
            statistics.put("creation", migrationRepository.countByCurrentPhase("CREATION"));
            statistics.put("assignment", migrationRepository.countByCurrentPhase("ASSIGNMENT"));
            statistics.put("businessLog", migrationRepository.countByCurrentPhase("BUSINESS_LOG"));
            statistics.put("comment", migrationRepository.countByCurrentPhase("COMMENT"));
            statistics.put("closing", migrationRepository.countByCurrentPhase("CLOSING"));
            
            // Overall status counts
            statistics.put("completed", migrationRepository.countByOverallStatus("COMPLETED"));
            statistics.put("failed", migrationRepository.countByOverallStatus("FAILED"));
            statistics.put("inProgress", migrationRepository.countByOverallStatus("IN_PROGRESS"));
            
            // Assignment statistics
            Object[] assignmentStats = transactionRepository.getAssignmentStatistics();
            if (assignmentStats != null && assignmentStats.length >= 4) {
                Map<String, Object> assignmentStatistics = new HashMap<>();
                assignmentStatistics.put("pending", assignmentStats[0] != null ? ((BigInteger) assignmentStats[0]).longValue() : 0L);
                assignmentStatistics.put("success", assignmentStats[1] != null ? ((BigInteger) assignmentStats[1]).longValue() : 0L);
                assignmentStatistics.put("failed", assignmentStats[2] != null ? ((BigInteger) assignmentStats[2]).longValue() : 0L);
                assignmentStatistics.put("total", assignmentStats[3] != null ? ((BigInteger) assignmentStats[3]).longValue() : 0L);
                statistics.put("assignmentDetails", assignmentStatistics);
            }
            
            // Business log statistics
            Object[] businessLogStats = transactionRepository.getBusinessLogStatistics();
            if (businessLogStats != null && businessLogStats.length >= 4) {
                Map<String, Object> businessLogStatistics = new HashMap<>();
                businessLogStatistics.put("pending", businessLogStats[0] != null ? ((BigInteger) businessLogStats[0]).longValue() : 0L);
                businessLogStatistics.put("success", businessLogStats[1] != null ? ((BigInteger) businessLogStats[1]).longValue() : 0L);
                businessLogStatistics.put("failed", businessLogStats[2] != null ? ((BigInteger) businessLogStats[2]).longValue() : 0L);
                businessLogStatistics.put("total", businessLogStats[3] != null ? ((BigInteger) businessLogStats[3]).longValue() : 0L);
                statistics.put("businessLogDetails", businessLogStatistics);
            }
            
            // Comment statistics
            Object[] commentStats = commentRepository.getCommentStatistics();
            if (commentStats != null && commentStats.length >= 4) {
                Map<String, Object> commentStatistics = new HashMap<>();
                commentStatistics.put("pending", commentStats[0] != null ? ((BigInteger) commentStats[0]).longValue() : 0L);
                commentStatistics.put("success", commentStats[1] != null ? ((BigInteger) commentStats[1]).longValue() : 0L);
                commentStatistics.put("failed", commentStats[2] != null ? ((BigInteger) commentStats[2]).longValue() : 0L);
                commentStatistics.put("total", commentStats[3] != null ? ((BigInteger) commentStats[3]).longValue() : 0L);
                statistics.put("commentDetails", commentStatistics);
            }
            
            // Closing statistics
            statistics.put("needToCloseCount", migrationRepository.countByIsNeedToClose(true));
            statistics.put("closingCompleted", migrationRepository.countByClosingStatus("COMPLETED"));
            statistics.put("closingFailed", migrationRepository.countByClosingStatus("FAILED"));
            
            logger.debug("Generated migration statistics: {}", statistics);
            return statistics;
            
        } catch (Exception e) {
            logger.error("Error getting migration statistics", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", "Failed to get statistics: " + e.getMessage());
            return errorStats;
        }
    }
    
    /**
     * Gets retry failed migrations statistics
     */
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