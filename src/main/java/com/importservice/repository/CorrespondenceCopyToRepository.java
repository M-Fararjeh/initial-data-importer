package com.importservice.repository;

import com.importservice.entity.CorrespondenceCopyTo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceCopyToRepository extends JpaRepository<CorrespondenceCopyTo, String> {
    List<CorrespondenceCopyTo> findByDocGuid(String docGuid);
}