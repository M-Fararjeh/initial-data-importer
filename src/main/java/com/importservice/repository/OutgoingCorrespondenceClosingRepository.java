package com.importservice.repository;

import com.importservice.entity.OutgoingCorrespondenceClosing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OutgoingCorrespondenceClosingRepository extends JpaRepository<OutgoingCorrespondenceClosing, String> {
    List<OutgoingCorrespondenceClosing> findByDocGuid(String docGuid);
    List<OutgoingCorrespondenceClosing> findByDocGuidOrderByClosingDateDesc(String docGuid);
}