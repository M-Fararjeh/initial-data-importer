package com.importservice.repository;

import com.importservice.entity.OutgoingCorrespondenceComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OutgoingCorrespondenceCommentRepository extends JpaRepository<OutgoingCorrespondenceComment, String> {
    List<OutgoingCorrespondenceComment> findByDocGuid(String docGuid);
    List<OutgoingCorrespondenceComment> findByDocGuidOrderByCommentCreationDateDesc(String docGuid);
}