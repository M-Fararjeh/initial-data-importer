package com.importservice.repository;

import com.importservice.entity.FormType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormTypeRepository extends JpaRepository<FormType, Integer> {
}