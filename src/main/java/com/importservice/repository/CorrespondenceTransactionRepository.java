package com.importservice.repository;

import com.importservice.entity.CorrespondenceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
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
}