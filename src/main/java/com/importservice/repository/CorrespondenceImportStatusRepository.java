package com.importservice.repository;

import com.importservice.entity.CorrespondenceImportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorrespondenceImportStatusRepository extends JpaRepository<CorrespondenceImportStatus, Long> {
    
    Optional<CorrespondenceImportStatus> findByCorrespondenceGuid(String correspondenceGuid);
    
    List<CorrespondenceImportStatus> findByOverallStatus(String overallStatus);
    
    @Query("SELECT c FROM CorrespondenceImportStatus c WHERE c.overallStatus = 'FAILED' AND c.retryCount < c.maxRetries ORDER BY c.lastErrorAt ASC")
    List<CorrespondenceImportStatus> findRetryableImports();
    
    @Query("SELECT COUNT(c) FROM CorrespondenceImportStatus c WHERE c.overallStatus = :status")
    Long countByOverallStatus(@Param("status") String status);
    
    @Query("SELECT c FROM CorrespondenceImportStatus c WHERE c.overallStatus IN ('PENDING', 'IN_PROGRESS') ORDER BY c.creationDate ASC")
    List<CorrespondenceImportStatus> findPendingImports();
    
    @Query("SELECT c FROM CorrespondenceImportStatus c WHERE c.overallStatus = 'FAILED' ORDER BY c.lastErrorAt DESC")
    List<CorrespondenceImportStatus> findFailedImports();
    
    // Statistics queries
    @Query(value = "SELECT " +
                   "SUM(CASE WHEN overall_status = 'PENDING' THEN 1 ELSE 0 END) as pending, " +
                   "SUM(CASE WHEN overall_status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgress, " +
                   "SUM(CASE WHEN overall_status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
                   "SUM(CASE WHEN overall_status = 'FAILED' THEN 1 ELSE 0 END) as failed, " +
                   "COUNT(*) as total " +
                   "FROM correspondence_import_status",
           nativeQuery = true)
    Object[] getImportStatistics();
    
    // Entity-specific statistics
    @Query(value = "SELECT " +
                   "SUM(CASE WHEN attachments_status = 'SUCCESS' THEN 1 ELSE 0 END) as attachmentsSuccess, " +
                   "SUM(CASE WHEN comments_status = 'SUCCESS' THEN 1 ELSE 0 END) as commentsSuccess, " +
                   "SUM(CASE WHEN copy_tos_status = 'SUCCESS' THEN 1 ELSE 0 END) as copyTosSuccess, " +
                   "SUM(CASE WHEN current_departments_status = 'SUCCESS' THEN 1 ELSE 0 END) as currentDepartmentsSuccess, " +
                   "SUM(CASE WHEN current_positions_status = 'SUCCESS' THEN 1 ELSE 0 END) as currentPositionsSuccess, " +
                   "SUM(CASE WHEN current_users_status = 'SUCCESS' THEN 1 ELSE 0 END) as currentUsersSuccess, " +
                   "SUM(CASE WHEN custom_fields_status = 'SUCCESS' THEN 1 ELSE 0 END) as customFieldsSuccess, " +
                   "SUM(CASE WHEN links_status = 'SUCCESS' THEN 1 ELSE 0 END) as linksSuccess, " +
                   "SUM(CASE WHEN send_tos_status = 'SUCCESS' THEN 1 ELSE 0 END) as sendTosSuccess, " +
                   "SUM(CASE WHEN transactions_status = 'SUCCESS' THEN 1 ELSE 0 END) as transactionsSuccess " +
                   "FROM correspondence_import_status",
           nativeQuery = true)
    Object[] getEntityStatistics();
}