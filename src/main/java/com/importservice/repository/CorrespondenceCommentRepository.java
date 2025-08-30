package com.importservice.repository;

import com.importservice.entity.CorrespondenceComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceCommentRepository extends JpaRepository<CorrespondenceComment, String> {
    List<CorrespondenceComment> findByDocGuid(String docGuid);
}