package com.importservice.repository;

import com.importservice.entity.Correspondence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceRepository extends JpaRepository<Correspondence, String> {
    
    /**
     * Find correspondences by type, deleted status, and draft status
     */
    @Query("SELECT c FROM Correspondence c WHERE c.correspondenceTypeId = :typeId AND c.isDeleted = :isDeleted AND c.isDraft = :isDraft")
    List<Correspondence> findByCorrespondenceTypeIdAndIsDeletedAndIsDraft(
        @Param("typeId") Integer correspondenceTypeId, 
        @Param("isDeleted") Boolean isDeleted, 
        @Param("isDraft") Boolean isDraft);
}