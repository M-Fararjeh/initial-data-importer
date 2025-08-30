package com.importservice.repository;

import com.importservice.entity.CorrespondenceCurrentPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceCurrentPositionRepository extends JpaRepository<CorrespondenceCurrentPosition, String> {
    List<CorrespondenceCurrentPosition> findByDocGuid(String docGuid);
}