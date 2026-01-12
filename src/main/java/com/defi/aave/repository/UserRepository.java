package com.defi.aave.repository;

import com.defi.aave.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository
 * Data access layer for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by wallet address
     */
    Optional<User> findByWalletAddress(String walletAddress);
    
    /**
     * Find active users
     */
    List<User> findByIsActiveTrue();
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Custom query: Find users with username containing keyword
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);
}
