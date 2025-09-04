package com.importservice.repository;

import com.importservice.entity.OutgoingCorrespondence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface OutgoingCorrespondenceRepository extends JpaRepository<OutgoingCorrespondence, String> {
    Page<OutgoingCorrespondence> findAll(Pageable pageable);
}