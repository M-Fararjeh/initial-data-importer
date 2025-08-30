package com.importservice.repository;

import com.importservice.entity.CorrespondenceLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceLinkRepository extends JpaRepository<CorrespondenceLink, String> {
    List<CorrespondenceLink> findByDocGuid(String docGuid);
}