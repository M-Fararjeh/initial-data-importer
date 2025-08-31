package com.importservice.repository;

import com.importservice.entity.IncomingCorrespondenceMigration;
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
}