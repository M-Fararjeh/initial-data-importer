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
    
    @Query("SELECT ct FROM CorrespondenceTransaction ct WHERE ct.docGuid = :docGuid AND ct.actionId = :actionId")
    List<CorrespondenceTransaction> findByDocGuidAndActionId(@Param("docGuid") String docGuid, @Param("actionId") Integer actionId);
    
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
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 2 " +
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
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 2 " +
                   "AND (:status IS NULL OR :status = 'all' OR ct.migrate_status = :status) " +
                   "AND (:search = '' OR :search IS NULL OR " +
                   "     ct.guid LIKE CONCAT('%', :search, '%') OR " +
                   "     ct.doc_guid LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(icm.created_document_id, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.subject, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.reference_no, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(ct.from_user_name, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(ct.to_user_name, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(ct.notes, '') LIKE CONCAT('%', :search, '%')) " +
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
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 2 " +
                   "AND (:status IS NULL OR :status = 'all' OR ct.migrate_status = :status) " +
                   "AND (:search = '' OR :search IS NULL OR " +
                   "     ct.guid LIKE CONCAT('%', :search, '%') OR " +
                   "     ct.doc_guid LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(icm.created_document_id, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.subject, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.reference_no, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(ct.from_user_name, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(ct.to_user_name, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(ct.notes, '') LIKE CONCAT('%', :search, '%')) ",
           nativeQuery = true)
    Long countAssignmentMigrationsWithSearch(
        @Param("status") String status,
        @Param("search") String search);
    
    /**
     * Count assignments by migrate status for statistics
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 2 AND ct.migrate_status = :status", 
           nativeQuery = true)
    Long countAssignmentsByMigrateStatus(@Param("status") String status);
    
    /**
     * Get assignments that need processing (PENDING or FAILED with retry count < 3)
     */
    @Query(value = "SELECT ct.* FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN incoming_correspondence_migrations icm ON ct.doc_guid = icm.correspondence_guid " +
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 2 " +
                   "AND (ct.migrate_status = 'PENDING' OR (ct.migrate_status = 'FAILED' AND ct.retry_count < 3)) " +
                   "AND icm.created_document_id IS NOT NULL " +
                   "ORDER BY ct.retry_count ASC, ct.last_modified_date ASC",
           nativeQuery = true)
    List<CorrespondenceTransaction> findAssignmentsNeedingProcessing();
    
    /**
     * Optimized query to get assignments by status with minimal data
     */
    @Query("SELECT ct FROM CorrespondenceTransaction ct " +
           "JOIN Correspondence c ON ct.docGuid = c.guid " +
           "WHERE ct.actionId = 12 AND c.correspondenceTypeId = 2 AND ct.migrateStatus IN :statuses " +
           "ORDER BY ct.lastModifiedDate DESC")
    List<CorrespondenceTransaction> findAssignmentsByMigrateStatusIn(@Param("statuses") List<String> statuses);
    
    /**
     * Get assignment statistics efficiently
     */
    @Query(value = "SELECT " +
                   "SUM(CASE WHEN ct.migrate_status = 'PENDING' THEN 1 ELSE 0 END) as pending, " +
                   "SUM(CASE WHEN ct.migrate_status = 'SUCCESS' THEN 1 ELSE 0 END) as success, " +
                   "SUM(CASE WHEN ct.migrate_status = 'FAILED' THEN 1 ELSE 0 END) as failed, " +
                   "COUNT(*) as total " +
                   "FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 2",
           nativeQuery = true)
    Object[] getAssignmentStatistics();
    
    /**
     * Optimized query for business log migrations with pagination
    * Gets all transactions except assignments (action_id != 12) for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query(value = "SELECT " +
                   "ct.guid as transactionGuid, " +
                   "ct.doc_guid as correspondenceGuid, " +
                   "ct.action_id as actionId, " +
                   "ct.action_english_name as actionEnglishName, " +
                   "ct.action_local_name as actionLocalName, " +
                   "ct.action_date as actionDate, " +
                   "ct.from_user_name as fromUserName, " +
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
                   "WHERE ct.action_id != 12 AND c.correspondence_type_id = 2 " +
                   "ORDER BY ct.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findBusinessLogMigrationsWithPagination(Pageable pageable);
    
    /**
     * Optimized query for business log migrations with search and pagination
    * Filters for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query(value = "SELECT " +
                   "ct.guid as transactionGuid, " +
                   "ct.doc_guid as correspondenceGuid, " +
                   "ct.action_id as actionId, " +
                   "ct.action_english_name as actionEnglishName, " +
                   "ct.action_local_name as actionLocalName, " +
                   "ct.action_date as actionDate, " +
                   "ct.from_user_name as fromUserName, " +
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
                   "WHERE ct.action_id != 12 AND c.correspondence_type_id = 2 " +
                   "AND (:status IS NULL OR ct.migrate_status = :status) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(ct.guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(ct.doc_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(icm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.action_english_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.from_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.notes, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "ORDER BY ct.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findBusinessLogMigrationsWithSearchAndPagination(
        @Param("status") String status,
        @Param("search") String search,
        Pageable pageable);
    
    /**
     * Count business logs with search filters for statistics
    * Filters for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN incoming_correspondence_migrations icm ON ct.doc_guid = icm.correspondence_guid " +
                   "WHERE ct.action_id != 12 AND c.correspondence_type_id = 2 " +
                   "AND (:status IS NULL OR ct.migrate_status = :status) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(ct.guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(ct.doc_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(icm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.action_english_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.from_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.notes, '')) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    Long countBusinessLogMigrationsWithSearch(
        @Param("status") String status,
        @Param("search") String search);
    
    /**
     * Count business logs by migrate status for statistics
    * Filters for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "WHERE ct.action_id != 12 AND c.correspondence_type_id = 2 AND ct.migrate_status = :status", 
           nativeQuery = true)
    Long countBusinessLogsByMigrateStatus(@Param("status") String status);
    
    /**
     * Get business logs that need processing (PENDING or FAILED with retry count < 3)
    * Filters for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query(value = "SELECT ct.* FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN incoming_correspondence_migrations icm ON ct.doc_guid = icm.correspondence_guid " +
                   "WHERE ct.action_id != 12 AND c.correspondence_type_id = 2 " +
                   "AND (ct.migrate_status = 'PENDING' OR (ct.migrate_status = 'FAILED' AND ct.retry_count < 3)) " +
                   "AND icm.created_document_id IS NOT NULL " +
                   "ORDER BY ct.retry_count ASC, ct.last_modified_date ASC",
           nativeQuery = true)
    List<CorrespondenceTransaction> findBusinessLogsNeedingProcessing();
    
    /**
     * Get business log statistics efficiently
    * Filters for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query(value = "SELECT " +
                   "SUM(CASE WHEN ct.migrate_status = 'PENDING' THEN 1 ELSE 0 END) as pending, " +
                   "SUM(CASE WHEN ct.migrate_status = 'SUCCESS' THEN 1 ELSE 0 END) as success, " +
                   "SUM(CASE WHEN ct.migrate_status = 'FAILED' THEN 1 ELSE 0 END) as failed, " +
                   "COUNT(*) as total " +
                   "FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "WHERE ct.action_id != 12 AND c.correspondence_type_id = 2",
           nativeQuery = true)
    Object[] getBusinessLogStatistics();
    
    /**
     * Optimized query for OUTGOING assignment migrations with pagination
     * Uses native query for better performance with large datasets
     * Filters for outgoing correspondences (correspondence_type_id = 1)
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
                   "ocm.created_document_id as createdDocumentId " +
                   "FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN outgoing_correspondence_migrations ocm ON ct.doc_guid = ocm.correspondence_guid " +
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 1 " +
                   "ORDER BY ct.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findOutgoingAssignmentMigrationsWithPagination(Pageable pageable);
    
    /**
     * Optimized query for OUTGOING assignment migrations with search and pagination
     * Filters for outgoing correspondences (correspondence_type_id = 1)
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
                   "ocm.created_document_id as createdDocumentId " +
                   "FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN outgoing_correspondence_migrations ocm ON ct.doc_guid = ocm.correspondence_guid " +
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 1 " +
                   "AND (:status IS NULL OR :status = 'all' OR ct.migrate_status = :status) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(COALESCE(ct.guid, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.doc_guid, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ocm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.from_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.to_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.notes, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "ORDER BY ct.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findOutgoingAssignmentMigrationsWithSearchAndPagination(
        @Param("status") String status,
        @Param("search") String search,
        Pageable pageable);
    
    /**
     * Count OUTGOING assignments with search filters for statistics
     * Filters for outgoing correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN outgoing_correspondence_migrations ocm ON ct.doc_guid = ocm.correspondence_guid " +
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 1 " +
                   "AND (:status IS NULL OR :status = 'all' OR ct.migrate_status = :status) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(COALESCE(ct.guid, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.doc_guid, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ocm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.from_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.to_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.notes, '')) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    Long countOutgoingAssignmentMigrationsWithSearch(
        @Param("status") String status,
        @Param("search") String search);
    
    /**
     * Get OUTGOING assignments that need processing (PENDING or FAILED with retry count < 3)
     * Filters for outgoing correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT ct.* FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN outgoing_correspondence_migrations ocm ON ct.doc_guid = ocm.correspondence_guid " +
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 1 " +
                   "AND (ct.migrate_status = 'PENDING' OR (ct.migrate_status = 'FAILED' AND ct.retry_count < 3)) " +
                   "AND ocm.created_document_id IS NOT NULL " +
                   "ORDER BY ct.retry_count ASC, ct.last_modified_date ASC",
           nativeQuery = true)
    List<CorrespondenceTransaction> findOutgoingAssignmentsNeedingProcessing();
    
    /**
     * Count OUTGOING assignments by migrate status for statistics
     * Filters for outgoing correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 1 AND ct.migrate_status = :status", 
           nativeQuery = true)
    Long countOutgoingAssignmentsByMigrateStatus(@Param("status") String status);
    
    /**
     * Get OUTGOING assignment statistics efficiently
     * Filters for outgoing correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT " +
                   "SUM(CASE WHEN ct.migrate_status = 'PENDING' THEN 1 ELSE 0 END) as pending, " +
                   "SUM(CASE WHEN ct.migrate_status = 'SUCCESS' THEN 1 ELSE 0 END) as success, " +
                   "SUM(CASE WHEN ct.migrate_status = 'FAILED' THEN 1 ELSE 0 END) as failed, " +
                   "COUNT(*) as total " +
                   "FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 1",
           nativeQuery = true)
    Object[] getOutgoingAssignmentStatistics();
    
    /**
     * Optimized query for OUTGOING business log migrations with pagination
     * Gets all transactions except assignments (action_id != 12) for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT " +
                   "ct.guid as transactionGuid, " +
                   "ct.doc_guid as correspondenceGuid, " +
                   "ct.action_id as actionId, " +
                   "ct.action_english_name as actionEnglishName, " +
                   "ct.action_local_name as actionLocalName, " +
                   "ct.action_date as actionDate, " +
                   "ct.from_user_name as fromUserName, " +
                   "ct.notes as notes, " +
                   "ct.migrate_status as migrateStatus, " +
                   "ct.retry_count as retryCount, " +
                   "ct.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "ocm.created_document_id as createdDocumentId " +
                   "FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN outgoing_correspondence_migrations ocm ON ct.doc_guid = ocm.correspondence_guid " +
                   "WHERE ct.action_id != 12 AND c.correspondence_type_id = 1 " +
                   "ORDER BY ct.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findOutgoingBusinessLogMigrationsWithPagination(Pageable pageable);
    
    /**
     * Optimized query for OUTGOING business log migrations with search and pagination
     * Filters for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT " +
                   "ct.guid as transactionGuid, " +
                   "ct.doc_guid as correspondenceGuid, " +
                   "ct.action_id as actionId, " +
                   "ct.action_english_name as actionEnglishName, " +
                   "ct.action_local_name as actionLocalName, " +
                   "ct.action_date as actionDate, " +
                   "ct.from_user_name as fromUserName, " +
                   "ct.notes as notes, " +
                   "ct.migrate_status as migrateStatus, " +
                   "ct.retry_count as retryCount, " +
                   "ct.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "ocm.created_document_id as createdDocumentId " +
                   "FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN outgoing_correspondence_migrations ocm ON ct.doc_guid = ocm.correspondence_guid " +
                   "WHERE ct.action_id != 12 AND c.correspondence_type_id = 1 " +
                   "AND (:status IS NULL OR ct.migrate_status = :status) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(COALESCE(ct.guid, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.doc_guid, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ocm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.action_english_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.from_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.notes, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "ORDER BY ct.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findOutgoingBusinessLogMigrationsWithSearchAndPagination(
        @Param("status") String status,
        @Param("search") String search,
        Pageable pageable);
    
    /**
     * Count OUTGOING business logs with search filters for statistics
     * Filters for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN outgoing_correspondence_migrations ocm ON ct.doc_guid = ocm.correspondence_guid " +
                   "WHERE ct.action_id != 12 AND c.correspondence_type_id = 1 " +
                   "AND (:status IS NULL OR ct.migrate_status = :status) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(COALESCE(ct.guid, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.doc_guid, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ocm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.action_english_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.from_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ct.notes, '')) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    Long countOutgoingBusinessLogMigrationsWithSearch(
        @Param("status") String status,
        @Param("search") String search);
    
    /**
     * Get OUTGOING business logs that need processing (PENDING or FAILED with retry count < 3)
     * Filters for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT ct.* FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN outgoing_correspondence_migrations ocm ON ct.doc_guid = ocm.correspondence_guid " +
                   "WHERE ct.action_id != 12 AND c.correspondence_type_id = 1 " +
                   "AND (ct.migrate_status = 'PENDING' OR (ct.migrate_status = 'FAILED' AND ct.retry_count < 3)) " +
                   "AND ocm.created_document_id IS NOT NULL " +
                   "ORDER BY ct.retry_count ASC, ct.last_modified_date ASC",
           nativeQuery = true)
    List<CorrespondenceTransaction> findOutgoingBusinessLogsNeedingProcessing();
    
    /**
     * Count OUTGOING business logs by migrate status for statistics
     * Filters for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "WHERE ct.action_id != 12 AND c.correspondence_type_id = 1 AND ct.migrate_status = :status", 
           nativeQuery = true)
    Long countOutgoingBusinessLogsByMigrateStatus(@Param("status") String status);
    
    /**
     * Get OUTGOING business log statistics efficiently
     * Filters for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT " +
                   "SUM(CASE WHEN ct.migrate_status = 'PENDING' THEN 1 ELSE 0 END) as pending, " +
                   "SUM(CASE WHEN ct.migrate_status = 'SUCCESS' THEN 1 ELSE 0 END) as success, " +
                   "SUM(CASE WHEN ct.migrate_status = 'FAILED' THEN 1 ELSE 0 END) as failed, " +
                   "COUNT(*) as total " +
                   "FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "WHERE ct.action_id != 12 AND c.correspondence_type_id = 1",
           nativeQuery = true)
    Object[] getOutgoingBusinessLogStatistics();
}