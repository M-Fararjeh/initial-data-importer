package com.importservice.repository;

import com.importservice.entity.CorrespondenceSendTo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceSendToRepository extends JpaRepository<CorrespondenceSendTo, String> {
    List<CorrespondenceSendTo> findByDocGuid(String docGuid);
}