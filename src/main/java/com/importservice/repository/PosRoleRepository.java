package com.importservice.repository;

import com.importservice.entity.PosRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosRoleRepository extends JpaRepository<PosRole, String> {
}