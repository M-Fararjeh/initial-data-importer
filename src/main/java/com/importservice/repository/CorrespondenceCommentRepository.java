package com.importservice.repository;

import com.importservice.entity.CorrespondenceComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceCommentRepository extends JpaRepository<CorrespondenceComment, String> {
    List<CorrespondenceComment> findByDocGuid(String docGuid);
    
    /**
     * Optimized query for comment migrations with pagination
    * Uses native query for better performance with large datasets
    * Filters for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query(value = "SELECT " +
                   "cc.comment_guid as commentGuid, " +
                   "cc.doc_guid as correspondenceGuid, " +
                   "cc.comment_creation_date as commentCreationDate, " +
                   "cc.comment as comment, " +
                   "cc.comment_type as commentType, " +
                   "cc.creation_user_guid as creationUserGuid, " +
                   "cc.role_guid as roleGuid, " +
                   "cc.attachment_caption as attachmentCaption, " +
                   "cc.migrate_status as migrateStatus, " +
                   "cc.retry_count as retryCount, " +
                   "cc.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "icm.created_document_id as createdDocumentId " +
                   "FROM correspondence_comments cc " +
                   "LEFT JOIN correspondences c ON cc.doc_guid = c.guid " +
                   "LEFT JOIN incoming_correspondence_migrations icm ON cc.doc_guid = icm.correspondence_guid " +
                   "WHERE c.correspondence_type_id = 2 " +
                   "ORDER BY cc.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findCommentMigrationsWithPagination(Pageable pageable);
    
    /**
     * Optimized query for comment migrations with search and pagination
    * Filters for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query(value = "SELECT " +
                   "cc.comment_guid as commentGuid, " +
                   "cc.doc_guid as correspondenceGuid, " +
                   "cc.comment_creation_date as commentCreationDate, " +
                   "cc.comment as comment, " +
                   "cc.comment_type as commentType, " +
                   "cc.creation_user_guid as creationUserGuid, " +
                   "cc.role_guid as roleGuid, " +
                   "cc.attachment_caption as attachmentCaption, " +
                   "cc.migrate_status as migrateStatus, " +
                   "cc.retry_count as retryCount, " +
                   "cc.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "icm.created_document_id as createdDocumentId " +
                   "FROM correspondence_comments cc " +
                   "LEFT JOIN correspondences c ON cc.doc_guid = c.guid " +
                   "LEFT JOIN incoming_correspondence_migrations icm ON cc.doc_guid = icm.correspondence_guid " +
                   "WHERE c.correspondence_type_id = 2 " +
                   "AND (:status IS NULL OR cc.migrate_status = :status) " +
                   "AND (:commentType IS NULL OR cc.comment_type = :commentType) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(cc.comment_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(cc.doc_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(icm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(cc.comment, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(cc.creation_user_guid, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(cc.comment_type, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "ORDER BY cc.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findCommentMigrationsWithSearchAndPagination(
        @Param("status") String status,
        @Param("commentType") String commentType,
        @Param("search") String search,
        Pageable pageable);
    
    /**
     * Count comments with search filters for statistics
    * Filters for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_comments cc " +
                   "LEFT JOIN correspondences c ON cc.doc_guid = c.guid " +
                   "LEFT JOIN incoming_correspondence_migrations icm ON cc.doc_guid = icm.correspondence_guid " +
                   "WHERE c.correspondence_type_id = 2 " +
                   "AND (:status IS NULL OR cc.migrate_status = :status) " +
                   "AND (:commentType IS NULL OR cc.comment_type = :commentType) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(cc.comment_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(cc.doc_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(icm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(cc.comment, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(cc.creation_user_guid, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(cc.comment_type, '')) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    Long countCommentMigrationsWithSearch(
        @Param("status") String status,
        @Param("commentType") String commentType,
        @Param("search") String search);
    
    /**
     * Count comments by migrate status for statistics
    * Filters for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_comments cc " +
                   "LEFT JOIN correspondences c ON cc.doc_guid = c.guid " +
                   "WHERE c.correspondence_type_id = 2 AND cc.migrate_status = :status", 
           nativeQuery = true)
    Long countCommentsByMigrateStatus(@Param("status") String status);
    
    /**
     * Get comments that need processing (PENDING or FAILED with retry count < 3)
    * Filters for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query(value = "SELECT cc.* FROM correspondence_comments cc " +
                   "LEFT JOIN correspondences c ON cc.doc_guid = c.guid " +
                   "LEFT JOIN incoming_correspondence_migrations icm ON cc.doc_guid = icm.correspondence_guid " +
                   "WHERE c.correspondence_type_id = 2 " +
                   "AND (cc.migrate_status = 'PENDING' OR (cc.migrate_status = 'FAILED' AND cc.retry_count < 3)) " +
                   "AND icm.created_document_id IS NOT NULL " +
                   "ORDER BY cc.retry_count ASC, cc.last_modified_date ASC",
           nativeQuery = true)
    List<CorrespondenceComment> findCommentsNeedingProcessing();
    
    /**
     * Get comment statistics efficiently
    * Filters for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query(value = "SELECT " +
                   "SUM(CASE WHEN cc.migrate_status = 'PENDING' THEN 1 ELSE 0 END) as pending, " +
                   "SUM(CASE WHEN cc.migrate_status = 'SUCCESS' THEN 1 ELSE 0 END) as success, " +
                   "SUM(CASE WHEN cc.migrate_status = 'FAILED' THEN 1 ELSE 0 END) as failed, " +
                   "SUM(CASE WHEN migrate_status = 'FAILED' THEN 1 ELSE 0 END) as failed, " +
                   "COUNT(*) as total " +
                   "FROM correspondence_comments cc " +
                   "INNER JOIN correspondences c ON cc.doc_guid = c.guid " +
                   "WHERE c.correspondence_type_id = 2",
           nativeQuery = true)
    Object[] getCommentStatistics();
    
    /**
     * Get comments by migrate status for processing
    * Filters for INCOMING correspondences (correspondence_type_id = 2)
     */
    @Query("SELECT cc FROM CorrespondenceComment cc " +
           "JOIN Correspondence c ON cc.docGuid = c.guid " +
           "WHERE c.correspondenceTypeId = 2 AND cc.migrateStatus IN :statuses " +
           "ORDER BY cc.lastModifiedDate DESC")
    List<CorrespondenceComment> findCommentsByMigrateStatusIn(@Param("statuses") List<String> statuses);
    
    /**
     * Optimized query for OUTGOING comment migrations with pagination
     * Filters for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT " +
                   "cc.comment_guid as commentGuid, " +
                   "cc.doc_guid as correspondenceGuid, " +
                   "cc.comment_creation_date as commentCreationDate, " +
                   "cc.comment as comment, " +
                   "cc.comment_type as commentType, " +
                   "cc.creation_user_guid as creationUserGuid, " +
                   "cc.role_guid as roleGuid, " +
                   "cc.attachment_caption as attachmentCaption, " +
                   "cc.migrate_status as migrateStatus, " +
                   "cc.retry_count as retryCount, " +
                   "cc.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "ocm.created_document_id as createdDocumentId " +
                   "FROM correspondence_comments cc " +
                   "LEFT JOIN correspondences c ON cc.doc_guid = c.guid " +
                   "LEFT JOIN outgoing_correspondence_migrations ocm ON cc.doc_guid = ocm.correspondence_guid " +
                   "WHERE c.correspondence_type_id = 1 " +
                   "ORDER BY cc.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findOutgoingCommentMigrationsWithPagination(Pageable pageable);
    
    /**
     * Optimized query for OUTGOING comment migrations with search and pagination
     * Filters for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT " +
                   "cc.comment_guid as commentGuid, " +
                   "cc.doc_guid as correspondenceGuid, " +
                   "cc.comment_creation_date as commentCreationDate, " +
                   "cc.comment as comment, " +
                   "cc.comment_type as commentType, " +
                   "cc.creation_user_guid as creationUserGuid, " +
                   "cc.role_guid as roleGuid, " +
                   "cc.attachment_caption as attachmentCaption, " +
                   "cc.migrate_status as migrateStatus, " +
                   "cc.retry_count as retryCount, " +
                   "cc.last_modified_date as lastModifiedDate, " +
                   "c.subject as correspondenceSubject, " +
                   "c.reference_no as correspondenceReferenceNo, " +
                   "ocm.created_document_id as createdDocumentId " +
                   "FROM correspondence_comments cc " +
                   "LEFT JOIN correspondences c ON cc.doc_guid = c.guid " +
                   "LEFT JOIN outgoing_correspondence_migrations ocm ON cc.doc_guid = ocm.correspondence_guid " +
                   "WHERE c.correspondence_type_id = 1 " +
                   "AND (:status IS NULL OR cc.migrate_status = :status) " +
                   "AND (:commentType IS NULL OR cc.comment_type = :commentType) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(cc.comment_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(cc.doc_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ocm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(cc.comment, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(cc.creation_user_guid, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(cc.comment_type, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "ORDER BY cc.last_modified_date DESC",
           nativeQuery = true)
    Page<Object[]> findOutgoingCommentMigrationsWithSearchAndPagination(
        @Param("status") String status,
        @Param("commentType") String commentType,
        @Param("search") String search,
        Pageable pageable);
    
    /**
     * Count OUTGOING comments with search filters for statistics
     * Filters for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_comments cc " +
                   "LEFT JOIN correspondences c ON cc.doc_guid = c.guid " +
                   "LEFT JOIN outgoing_correspondence_migrations ocm ON cc.doc_guid = ocm.correspondence_guid " +
                   "WHERE c.correspondence_type_id = 1 " +
                   "AND (:status IS NULL OR cc.migrate_status = :status) " +
                   "AND (:commentType IS NULL OR cc.comment_type = :commentType) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(cc.comment_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(cc.doc_guid) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(ocm.created_document_id, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.subject, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(c.reference_no, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(cc.comment, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(cc.creation_user_guid, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(COALESCE(cc.comment_type, '')) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    Long countOutgoingCommentMigrationsWithSearch(
        @Param("status") String status,
        @Param("commentType") String commentType,
        @Param("search") String search);
    
    /**
     * Get OUTGOING comments that need processing (PENDING or FAILED with retry count < 3)
     * Filters for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT cc.* FROM correspondence_comments cc " +
                   "LEFT JOIN correspondences c ON cc.doc_guid = c.guid " +
                   "LEFT JOIN outgoing_correspondence_migrations ocm ON cc.doc_guid = ocm.correspondence_guid " +
                   "WHERE c.correspondence_type_id = 1 " +
                   "AND (cc.migrate_status = 'PENDING' OR (cc.migrate_status = 'FAILED' AND cc.retry_count < 3)) " +
                   "AND ocm.created_document_id IS NOT NULL " +
                   "ORDER BY cc.retry_count ASC, cc.last_modified_date ASC",
           nativeQuery = true)
    List<CorrespondenceComment> findOutgoingCommentsNeedingProcessing();
    
    /**
     * Count OUTGOING comments by migrate status for statistics
     * Filters for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT COUNT(*) FROM correspondence_comments cc " +
                   "LEFT JOIN correspondences c ON cc.doc_guid = c.guid " +
                   "WHERE c.correspondence_type_id = 1 AND cc.migrate_status = :status", 
           nativeQuery = true)
    Long countOutgoingCommentsByMigrateStatus(@Param("status") String status);
    
    /**
     * Get OUTGOING comment statistics efficiently
     * Filters for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query(value = "SELECT " +
                   "SUM(CASE WHEN cc.migrate_status = 'PENDING' THEN 1 ELSE 0 END) as pending, " +
                   "SUM(CASE WHEN cc.migrate_status = 'SUCCESS' THEN 1 ELSE 0 END) as success, " +
                   "SUM(CASE WHEN cc.migrate_status = 'FAILED' THEN 1 ELSE 0 END) as failed, " +
                   "COUNT(*) as total " +
                   "FROM correspondence_comments cc " +
                   "INNER JOIN correspondences c ON cc.doc_guid = c.guid " +
                   "WHERE c.correspondence_type_id = 1",
           nativeQuery = true)
    Object[] getOutgoingCommentStatistics();
    
    /**
     * Get OUTGOING comments by migrate status for processing
     * Filters for OUTGOING correspondences (correspondence_type_id = 1)
     */
    @Query("SELECT cc FROM CorrespondenceComment cc " +
           "JOIN Correspondence c ON cc.docGuid = c.guid " +
           "WHERE c.correspondenceTypeId = 1 AND cc.migrateStatus IN :statuses " +
           "ORDER BY cc.lastModifiedDate DESC")
    List<CorrespondenceComment> findOutgoingCommentsByMigrateStatusIn(@Param("statuses") List<String> statuses);
}