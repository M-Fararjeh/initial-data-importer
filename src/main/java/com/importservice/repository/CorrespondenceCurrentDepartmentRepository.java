package com.importservice.repository;

import com.importservice.entity.CorrespondenceCurrentDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceCurrentDepartmentRepository extends JpaRepository<CorrespondenceCurrentDepartment, Long> {
    List<CorrespondenceCurrentDepartment> findByDocGuid(String docGuid);
}