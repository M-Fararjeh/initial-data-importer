package com.importservice.repository;

import com.importservice.entity.Importance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportanceRepository extends JpaRepository<Importance, Integer> {
}