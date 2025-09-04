package com.importservice.repository;

import com.importservice.entity.InternalCorrespondenceMigration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternalCorrespondenceMigrationRepository extends JpaRepository<InternalCorrespondenceMigration, Long> {
    
    Optional<InternalCorrespondenceMigration> findByCorrespondenceGuid(String correspondenceGuid);
    
    List<InternalCorrespondenceMigration> findByCurrentPhase(String currentPhase);
    
    List<InternalCorrespondenceMigration> findByOverallStatus(String overallStatus);
    
    List<InternalCorrespondenceMigration> findByPhaseStatus(String phaseStatus);
    
    @Query("SELECT m FROM InternalCorrespondenceMigration m WHERE m.overallStatus = 'IN_PROGRESS' ORDER BY m.creationDate ASC")
    List<InternalCorrespondenceMigration> findInProgressMigrations();
    
    @Query("SELECT m FROM InternalCorrespondenceMigration m WHERE m.phaseStatus = 'ERROR' AND m.retryCount < m.maxRetries ORDER BY m.lastErrorAt ASC")
    List<InternalCorrespondenceMigration> findRetryableMigrations();
    
    @Query("SELECT COUNT(m) FROM InternalCorrespondenceMigration m WHERE m.currentPhase = :phase")
    Long countByCurrentPhase(@Param("phase") String phase);
    
    @Query("SELECT COUNT(m) FROM InternalCorrespondenceMigration m WHERE m.overallStatus = :status")
    Long countByOverallStatus(@Param("status") String status);
    
    List<InternalCorrespondenceMigration> findByIsNeedToCloseAndCreatedDocumentIdIsNotNull(Boolean isNeedToClose);
    
    @Query("SELECT COUNT(m) FROM InternalCorrespondenceMigration m WHERE m.isNeedToClose = :isNeedToClose")
    Long countByIsNeedToClose(@Param("isNeedToClose") Boolean isNeedToClose);
    
    @Query("SELECT COUNT(m) FROM InternalCorrespondenceMigration m WHERE m.closingStatus = :status")
    Long countByClosingStatus(@Param("status") String status);
    
    /**
     * Optimized query for internal approval migrations with pagination
     */
    @Query(value = "SELECT " +
                   "icm.id as id, " +
                   "icm.correspondence_guid as correspondenceGuid, " +
                   "icm.created_document_id as createdDocumentId, " +
                   "icm.approval_status as approvalStatus, " +
                   "icm.approval_step as approvalStep, " +
                   "icm.approval_error as approvalError, " +
                   "icm.retry_count as retryCount, " +
                   "icm.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "c.creation_user_name as creationUserName " +
                   "FROM internal_correspondence_migrations icm " +
                   "LEFT JOIN correspondences c ON icm.correspondence_guid = c.guid " +
                   "WHERE icm.assignment_status = 'COMPLETED' AND icm.approval_status = 'PENDING' " +
                   "ORDER BY icm.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findInternalApprovalMigrationsWithPagination(Pageable pageable);
    
    /**
     * Optimized query for internal approval migrations with search and pagination
     */
    @Query(value = "SELECT " +
                   "icm.id as id, " +
                   "icm.correspondence_guid as correspondenceGuid, " +
                   "icm.created_document_id as createdDocumentId, " +
                   "icm.approval_status as approvalStatus, " +
                   "icm.approval_step as approvalStep, " +
                   "icm.approval_error as approvalError, " +
                   "icm.retry_count as retryCount, " +
                   "icm.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "c.creation_user_name as creationUserName " +
                   "FROM internal_correspondence_migrations icm " +
                   "LEFT JOIN correspondences c ON icm.correspondence_guid = c.guid " +
                   "WHERE icm.assignment_status = 'COMPLETED' AND icm.approval_status = 'PENDING' " +
                   "AND (:status = 'all' OR icm.approval_status = :status) " +
                   "AND (:step = 'all' OR icm.approval_step = :step) " +
                   "AND (:search = '' OR :search IS NULL OR " +
                   "     icm.correspondence_guid LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(icm.created_document_id, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.subject, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.reference_no, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.creation_user_name, '') LIKE CONCAT('%', :search, '%')) " +
                   "ORDER BY icm.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findInternalApprovalMigrationsWithSearchAndPagination(
        @Param("status") String status,
        @Param("step") String step,
        @Param("search") String search,
        Pageable pageable);
    
    /**
     * Optimized query for closing migrations with pagination
     */
    @Query(value = "SELECT " +
                   "icm.id as id, " +
                   "icm.correspondence_guid as correspondenceGuid, " +
                   "icm.is_need_to_close as isNeedToClose, " +
                   "icm.closing_status as closingStatus, " +
                   "icm.closing_error as closingError, " +
                   "icm.created_document_id as createdDocumentId, " +
                   "icm.retry_count as retryCount, " +
                   "icm.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "c.correspondence_last_modified_date as correspondenceLastModifiedDate, " +
                   "c.creation_user_name as creationUserName " +
                   "FROM internal_correspondence_migrations icm " +
                   "LEFT JOIN correspondences c ON icm.correspondence_guid = c.guid " +
                   "ORDER BY icm.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findClosingMigrationsWithPagination(Pageable pageable);
    
    /**
     * Optimized query for closing migrations with search and pagination
     */
    @Query(value = "SELECT " +
                   "icm.id as id, " +
                   "icm.correspondence_guid as correspondenceGuid, " +
                   "icm.is_need_to_close as isNeedToClose, " +
                   "icm.closing_status as closingStatus, " +
                   "icm.closing_error as closingError, " +
                   "icm.created_document_id as createdDocumentId, " +
                   "icm.retry_count as retryCount, " +
                   "icm.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "c.correspondence_last_modified_date as correspondenceLastModifiedDate, " +
                   "c.creation_user_name as creationUserName " +
                   "FROM internal_correspondence_migrations icm " +
                   "LEFT JOIN correspondences c ON icm.correspondence_guid = c.guid " +
                   "WHERE (:status IS NULL OR icm.closing_status = :status) " +
                   "AND (:needToClose IS NULL OR icm.is_need_to_close = :needToClose) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(icm.correspondence_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(icm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.creation_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "ORDER BY icm.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findClosingMigrationsWithSearchAndPagination(
        @Param("status") String status,
        @Param("needToClose") Boolean needToClose,
        @Param("search") String search,
        Pageable pageable);
    
    /**
     * Optimized query for internal assignment migrations with pagination
     * Gets actual assignment transactions for internal correspondences where creation is completed
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
                   "icm.created_document_id as createdDocumentId, " +
                   "c.creation_user_name as creationUserName " +
                   "FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN internal_correspondence_migrations icm ON ct.doc_guid = icm.correspondence_guid " +
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 3 " +
                   "AND icm.creation_status = 'COMPLETED' AND icm.assignment_status = 'PENDING' " +
                   "ORDER BY ct.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findInternalAssignmentMigrationsWithPagination(Pageable pageable);
    
    /**
     * Optimized query for internal assignment migrations with search and pagination
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
                   "icm.created_document_id as createdDocumentId, " +
                   "c.creation_user_name as creationUserName " +
                   "FROM correspondence_transactions ct " +
                   "LEFT JOIN correspondences c ON ct.doc_guid = c.guid " +
                   "LEFT JOIN internal_correspondence_migrations icm ON ct.doc_guid = icm.correspondence_guid " +
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 3 " +
                   "AND icm.creation_status = 'COMPLETED' AND icm.assignment_status = 'PENDING' " +
                   "AND (:status = 'all' OR ct.migrate_status = :status) " +
                   "AND (:search = '' OR :search IS NULL OR " +
                   "     ct.guid LIKE CONCAT('%', :search, '%') OR " +
                   "     ct.doc_guid LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(icm.created_document_id, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.subject, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.reference_no, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(ct.from_user_name, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(ct.to_user_name, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(ct.notes, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.creation_user_name, '') LIKE CONCAT('%', :search, '%')) " +
                   "ORDER BY ct.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findInternalAssignmentMigrationsWithSearchAndPagination(
        @Param("status") String status,
        @Param("search") String search,
        Pageable pageable);
    
    /**
     * Count internal assignment migrations with search filters
     */
    @Query(value = "SELECT COUNT(*) FROM internal_correspondence_migrations icm " +
                   "LEFT JOIN correspondences c ON icm.correspondence_guid = c.guid " +
                   "LEFT JOIN correspondence_transactions ct ON icm.correspondence_guid = ct.doc_guid " +
                   "WHERE ct.action_id = 12 AND c.correspondence_type_id = 3 " +
                   "AND icm.creation_status = 'COMPLETED' AND icm.assignment_status = 'PENDING' " +
                   "AND (:status = 'all' OR ct.migrate_status = :status) " +
                   "AND (:search = '' OR :search IS NULL OR " +
                   "     ct.guid LIKE CONCAT('%', :search, '%') OR " +
                   "     icm.correspondence_guid LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(icm.created_document_id, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.subject, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.reference_no, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(ct.from_user_name, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(ct.to_user_name, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(ct.notes, '') LIKE CONCAT('%', :search, '%') OR " +
                   "     IFNULL(c.creation_user_name, '') LIKE CONCAT('%', :search, '%'))",
           nativeQuery = true)
    Long countInternalAssignmentMigrationsWithSearch(
        @Param("status") String status,
        @Param("search") String search);
}