package com.importservice.repository;

import com.importservice.entity.IncomingCorrespondenceMigration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomingCorrespondenceMigrationRepository extends JpaRepository<IncomingCorrespondenceMigration, Long> {
    
    Optional<IncomingCorrespondenceMigration> findByCorrespondenceGuid(String correspondenceGuid);
    
    List<IncomingCorrespondenceMigration> findByCurrentPhase(String currentPhase);
    
    List<IncomingCorrespondenceMigration> findByOverallStatus(String overallStatus);
    
    List<IncomingCorrespondenceMigration> findByPhaseStatus(String phaseStatus);
    
    @Query("SELECT m FROM IncomingCorrespondenceMigration m WHERE m.overallStatus = 'IN_PROGRESS' ORDER BY m.creationDate ASC")
    List<IncomingCorrespondenceMigration> findInProgressMigrations();
    
    @Query("SELECT m FROM IncomingCorrespondenceMigration m WHERE m.phaseStatus = 'ERROR' AND m.retryCount < m.maxRetries ORDER BY m.lastErrorAt ASC")
    List<IncomingCorrespondenceMigration> findRetryableMigrations();
    
    @Query("SELECT COUNT(m) FROM IncomingCorrespondenceMigration m WHERE m.currentPhase = :phase")
    Long countByCurrentPhase(@Param("phase") String phase);
    
    @Query("SELECT COUNT(m) FROM IncomingCorrespondenceMigration m WHERE m.overallStatus = :status")
    Long countByOverallStatus(@Param("status") String status);
    
    List<IncomingCorrespondenceMigration> findByIsNeedToCloseAndCreatedDocumentIdIsNotNull(Boolean isNeedToClose);
    
    @Query("SELECT COUNT(m) FROM IncomingCorrespondenceMigration m WHERE m.isNeedToClose = :isNeedToClose")
    Long countByIsNeedToClose(@Param("isNeedToClose") Boolean isNeedToClose);
    
    @Query("SELECT COUNT(m) FROM IncomingCorrespondenceMigration m WHERE m.closingStatus = :status")
    Long countByClosingStatus(@Param("status") String status);
    
    /**
     * Optimized query for closing migrations with pagination
     * Uses native query for better performance with large datasets
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
                   "FROM incoming_correspondence_migrations icm " +
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
                   "FROM incoming_correspondence_migrations icm " +
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
}