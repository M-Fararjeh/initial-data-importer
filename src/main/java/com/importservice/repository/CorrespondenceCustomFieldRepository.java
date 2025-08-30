package com.importservice.repository;

import com.importservice.entity.CorrespondenceCustomField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceCustomFieldRepository extends JpaRepository<CorrespondenceCustomField, String> {
    List<CorrespondenceCustomField> findByDocGuid(String docGuid);
}