package com.importservice.repository;

import com.importservice.entity.CorrespondenceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceTransactionRepository extends JpaRepository<CorrespondenceTransaction, String> {
    List<CorrespondenceTransaction> findByDocGuid(String docGuid);
}