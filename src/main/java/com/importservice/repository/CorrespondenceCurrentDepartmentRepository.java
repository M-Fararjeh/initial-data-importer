package com.importservice.repository;

import com.importservice.entity.CorrespondenceCurrentDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceCurrentDepartmentRepository extends JpaRepository<CorrespondenceCurrentDepartment, Long> {
    List<CorrespondenceCurrentDepartment> findByDocGuid(String docGuid);
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CorrespondenceCurrentDepartment c WHERE c.docGuid = :docGuid AND c.depGuid = :depGuid")
    boolean existsByDocGuidAndDepGuid(@Param("docGuid") String docGuid, @Param("depGuid") String depGuid);
}