package com.importservice.repository;

import com.importservice.entity.OutgoingCorrespondenceBusinessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OutgoingCorrespondenceBusinessLogRepository extends JpaRepository<OutgoingCorrespondenceBusinessLog, String> {
    List<OutgoingCorrespondenceBusinessLog> findByDocGuid(String docGuid);
    List<OutgoingCorrespondenceBusinessLog> findByDocGuidOrderByLogDateDesc(String docGuid);
}