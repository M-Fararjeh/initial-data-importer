package com.importservice.repository;

import com.importservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * Find user by email (case-insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);
    
    /**
     * Get department GUID by user email using native query
     */
    @Query(value = "SELECT d.guid " +
                   "FROM users u " +
                   "JOIN user_positions up ON u.guid = up.user_guid " +
                   "JOIN positions p ON up.pos_guid = p.guid " +
                   "JOIN departments d ON p.department_guid = d.guid " +
                   "WHERE LOWER(u.email) = LOWER(:email) " +
                   "LIMIT 1", 
           nativeQuery = true)
    String findDepartmentGuidByUserEmail(@Param("email") String email);
    
    /**
     * Get department GUID by user GUID using native query
     */
    @Query(value = "SELECT d.guid " +
                   "FROM users u " +
                   "JOIN user_positions up ON u.guid = up.user_guid " +
                   "JOIN positions p ON up.pos_guid = p.guid " +
                   "JOIN departments d ON p.department_guid = d.guid " +
                   "WHERE u.guid = :userGuid " +
                   "LIMIT 1", 
           nativeQuery = true)
    String findDepartmentGuidByUserGuid(@Param("userGuid") String userGuid);
}