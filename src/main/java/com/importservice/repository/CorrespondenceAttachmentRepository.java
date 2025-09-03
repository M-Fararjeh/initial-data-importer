package com.importservice.repository;

import com.importservice.entity.CorrespondenceAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CorrespondenceAttachmentRepository extends JpaRepository<CorrespondenceAttachment, String> {
    @Transactional(readOnly = true)
    List<CorrespondenceAttachment> findByDocGuid(String docGuid);
}