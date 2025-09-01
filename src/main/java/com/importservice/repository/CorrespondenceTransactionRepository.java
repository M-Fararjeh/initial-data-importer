package com.importservice.repository;

import com.importservice.entity.CorrespondenceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceTransactionRepository extends JpaRepository<CorrespondenceTransaction, String> {
    List<CorrespondenceTransaction> findByDocGuid(String docGuid);
    
    @Query("SELECT ct FROM CorrespondenceTransaction ct WHERE ct.actionId = :actionId")
    List<CorrespondenceTransaction> findByActionId(@Param("actionId") Integer actionId);
    
    @Query("SELECT ct FROM CorrespondenceTransaction ct WHERE ct.actionId = :actionId AND ct.migrateStatus IN :statuses")
    List<CorrespondenceTransaction> findByActionIdAndMigrateStatusIn(@Param("actionId") Integer actionId, @Param("statuses") List<String> statuses);
    
    /**
     * Optimized query for assignment migrations with pagination
     * Uses native query for better performance with large datasets
     */
    @Query(value = "SELECT " +
                   "ct.guid as transactionGuid, " +
                   "ct.doc_guid as correspondenceGuid, " +
                   "ct.from_user_name as fromUserName, " +
                   "ct.to_user_name as toUserName, " +
                   "ct.action_date as actionDate, " +
                   "ct.decision_guid as decisionGuid, " +
                   "ct.notes as notes, " +
                   "ct.migrate_status as migrateStatus, " +
                   "ct.retry_count as retryCount, " +
                   "ct.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "icm.created_document_id as createdDocumentId " +
                   "FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN incoming_correspondence_migrations icm ON ct.doc_guid = icm.correspondence_guid " +
                   "WHERE ct.action_id = 12 " +
                   "ORDER BY ct.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findAssignmentMigrationsWithPagination(Pageable pageable);
    
    /**
     * Optimized query for assignment migrations with search and pagination
     */
    @Query(value = "SELECT " +
                   "ct.guid as transactionGuid, " +
                   "ct.doc_guid as correspondenceGuid, " +
                   "ct.from_user_name as fromUserName, " +
                   "ct.to_user_name as toUserName, " +
                   "ct.action_date as actionDate, " +
                   "ct.decision_guid as decisionGuid, " +
                   "ct.notes as notes, " +
                   "ct.migrate_status as migrateStatus, " +
                   "ct.retry_count as retryCount, " +
                   "ct.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "icm.created_document_id as createdDocumentId " +
                   "FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN incoming_correspondence_migrations icm ON ct.doc_guid = icm.correspondence_guid " +
                   "WHERE ct.action_id = 12 " +
                   "AND (:status IS NULL OR ct.migrate_status = :status) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(ct.guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(ct.doc_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(icm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.from_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.to_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.notes, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "ORDER BY ct.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findAssignmentMigrationsWithSearchAndPagination(
        @Param("status") String status,
        @Param("search") String search,
        Pageable pageable);
    
    /**
     * Count assignments with search filters for statistics
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN incoming_correspondence_migrations icm ON ct.doc_guid = icm.correspondence_guid " +
                   "WHERE ct.action_id = 12 " +
                   "AND (:status IS NULL OR ct.migrate_status = :status) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(ct.guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(ct.doc_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(icm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.from_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.to_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.notes, '')) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    Long countAssignmentMigrationsWithSearch(
        @Param("status") String status,
        @Param("search") String search);
    
    /**
     * Count assignments by migrate status for statistics
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_transactions WHERE action_id = 12 AND migrate_status = :status", 
           nativeQuery = true)
    Long countAssignmentsByMigrateStatus(@Param("status") String status);
    
    /**
     * Get assignments that need processing (PENDING or FAILED with retry count < 3)
     */
    @Query(value = "SELECT ct.* FROM correspondence_transactions ct " +
                   "LEFT JOIN incoming_correspondence_migrations icm ON ct.doc_guid = icm.correspondence_guid " +
                   "WHERE ct.action_id = 12 " +
                   "AND (ct.migrate_status = 'PENDING' OR (ct.migrate_status = 'FAILED' AND ct.retry_count < 3)) " +
                   "AND icm.created_document_id IS NOT NULL " +
                   "ORDER BY ct.retry_count ASC, ct.last_modified_date ASC",
           nativeQuery = true)
    List<CorrespondenceTransaction> findAssignmentsNeedingProcessing();
    
    /**
     * Optimized query to get assignments by status with minimal data
     */
    @Query("SELECT ct FROM CorrespondenceTransaction ct WHERE ct.actionId = 12 AND ct.migrateStatus IN :statuses ORDER BY ct.lastModifiedDate DESC")
    List<CorrespondenceTransaction> findAssignmentsByMigrateStatusIn(@Param("statuses") List<String> statuses);
    
    /**
     * Get assignment statistics efficiently
     */
    @Query(value = "SELECT " +
                   "SUM(CASE WHEN migrate_status = 'PENDING' THEN 1 ELSE 0 END) as pending, " +
                   "SUM(CASE WHEN migrate_status = 'SUCCESS' THEN 1 ELSE 0 END) as success, " +
                   "SUM(CASE WHEN migrate_status = 'FAILED' THEN 1 ELSE 0 END) as failed, " +
                   "COUNT(*) as total " +
                   "FROM correspondence_transactions " +
                   "WHERE action_id = 12",
           nativeQuery = true)
    Object[] getAssignmentStatistics();
}