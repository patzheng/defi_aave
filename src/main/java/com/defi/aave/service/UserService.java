package com.defi.aave.service;

import com.defi.aave.entity.User;
import com.defi.aave.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * User Service
 * Business logic layer for User operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Create a new user
     */
    @Transactional
    public User createUser(User user) {
        log.info("Creating user: {}", user.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }
    
    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id);
    }
    
    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username);
    }
    
    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }
    
    /**
     * Get all users with pagination
     */
    public Page<User> getAllUsers(Pageable pageable) {
        log.debug("Fetching users with pagination: {}", pageable);
        return userRepository.findAll(pageable);
    }
    
    /**
     * Get active users
     */
    public List<User> getActiveUsers() {
        log.debug("Fetching active users");
        return userRepository.findByIsActiveTrue();
    }
    
    /**
     * Search users by keyword
     */
    public List<User> searchUsers(String keyword) {
        log.debug("Searching users with keyword: {}", keyword);
        return userRepository.searchUsers(keyword);
    }
    
    /**
     * Update user
     */
    @Transactional
    public User updateUser(Long id, User userDetails) {
        log.info("Updating user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        // Update fields
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getFullName() != null) {
            user.setFullName(userDetails.getFullName());
        }
        if (userDetails.getWalletAddress() != null) {
            user.setWalletAddress(userDetails.getWalletAddress());
        }
        if (userDetails.getBalance() != null) {
            user.setBalance(userDetails.getBalance());
        }
        if (userDetails.getIsActive() != null) {
            user.setIsActive(userDetails.getIsActive());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getId());
        return updatedUser;
    }
    
    /**
     * Delete user by ID
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with ID: " + id);
        }
        
        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }
    
    /**
     * Count total users
     */
    public long countUsers() {
        return userRepository.count();
    }
}
