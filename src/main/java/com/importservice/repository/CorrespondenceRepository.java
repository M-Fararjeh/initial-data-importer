package com.importservice.repository;

import com.importservice.entity.Correspondence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface CorrespondenceRepository extends JpaRepository<Correspondence, String> {
    Page<Correspondence> findAll(Pageable pageable);
}