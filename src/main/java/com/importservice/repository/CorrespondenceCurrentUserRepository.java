package com.importservice.repository;

import com.importservice.entity.CorrespondenceCurrentUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorrespondenceCurrentUserRepository extends JpaRepository<CorrespondenceCurrentUser, String> {
    List<CorrespondenceCurrentUser> findByDocGuid(String docGuid);
}