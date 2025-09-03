package com.importservice.repository;

import com.importservice.entity.OutgoingCorrespondenceMigration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutgoingCorrespondenceMigrationRepository extends JpaRepository<OutgoingCorrespondenceMigration, Long> {
    
    Optional<OutgoingCorrespondenceMigration> findByCorrespondenceGuid(String correspondenceGuid);
    
    List<OutgoingCorrespondenceMigration> findByCurrentPhase(String currentPhase);
    
    List<OutgoingCorrespondenceMigration> findByOverallStatus(String overallStatus);
    
    List<OutgoingCorrespondenceMigration> findByPhaseStatus(String phaseStatus);
    
    @Query("SELECT m FROM OutgoingCorrespondenceMigration m WHERE m.overallStatus = 'IN_PROGRESS' ORDER BY m.creationDate ASC")
    List<OutgoingCorrespondenceMigration> findInProgressMigrations();
    
    @Query("SELECT m FROM OutgoingCorrespondenceMigration m WHERE m.phaseStatus = 'ERROR' AND m.retryCount < m.maxRetries ORDER BY m.lastErrorAt ASC")
    List<OutgoingCorrespondenceMigration> findRetryableMigrations();
    
    @Query("SELECT COUNT(m) FROM OutgoingCorrespondenceMigration m WHERE m.currentPhase = :phase")
    Long countByCurrentPhase(@Param("phase") String phase);
    
    @Query("SELECT COUNT(m) FROM OutgoingCorrespondenceMigration m WHERE m.overallStatus = :status")
    Long countByOverallStatus(@Param("status") String status);
    
    List<OutgoingCorrespondenceMigration> findByIsNeedToCloseAndCreatedDocumentIdIsNotNull(Boolean isNeedToClose);
    
    @Query("SELECT COUNT(m) FROM OutgoingCorrespondenceMigration m WHERE m.isNeedToClose = :isNeedToClose")
    Long countByIsNeedToClose(@Param("isNeedToClose") Boolean isNeedToClose);
    
    @Query("SELECT COUNT(m) FROM OutgoingCorrespondenceMigration m WHERE m.closingStatus = :status")
    Long countByClosingStatus(@Param("status") String status);
    
    /**
     * Optimized query for closing migrations with pagination
     */
    @Query(value = "SELECT " +
                   "ocm.id as id, " +
                   "ocm.correspondence_guid as correspondenceGuid, " +
                   "ocm.is_need_to_close as isNeedToClose, " +
                   "ocm.closing_status as closingStatus, " +
                   "ocm.closing_error as closingError, " +
                   "ocm.created_document_id as createdDocumentId, " +
                   "ocm.retry_count as retryCount, " +
                   "ocm.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "c.correspondence_last_modified_date as correspondenceLastModifiedDate, " +
                   "c.creation_user_name as creationUserName " +
                   "FROM outgoing_correspondence_migrations ocm " +
                   "LEFT JOIN correspondences c ON ocm.correspondence_guid = c.guid " +
                   "ORDER BY ocm.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findClosingMigrationsWithPagination(Pageable pageable);
    
    /**
     * Optimized query for closing migrations with search and pagination
     */
    @Query(value = "SELECT " +
                   "ocm.id as id, " +
                   "ocm.correspondence_guid as correspondenceGuid, " +
                   "ocm.is_need_to_close as isNeedToClose, " +
                   "ocm.closing_status as closingStatus, " +
                   "ocm.closing_error as closingError, " +
                   "ocm.created_document_id as createdDocumentId, " +
                   "ocm.retry_count as retryCount, " +
                   "ocm.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "c.correspondence_last_modified_date as correspondenceLastModifiedDate, " +
                   "c.creation_user_name as creationUserName " +
                   "FROM outgoing_correspondence_migrations ocm " +
                   "LEFT JOIN correspondences c ON ocm.correspondence_guid = c.guid " +
                   "WHERE (:status IS NULL OR ocm.closing_status = :status) " +
                   "AND (:needToClose IS NULL OR ocm.is_need_to_close = :needToClose) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(ocm.correspondence_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ocm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.creation_user_name, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "ORDER BY ocm.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findClosingMigrationsWithSearchAndPagination(
        @Param("status") String status,
        @Param("needToClose") Boolean needToClose,
        @Param("search") String search,
        Pageable pageable);
}