package com.defi.aave.controller;

import com.defi.aave.dto.ApiResponse;
import com.defi.aave.entity.User;
import com.defi.aave.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * User Controller
 * REST API endpoints for User operations
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    /**
     * Create a new user
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody User user) {
        log.info("REST request to create user: {}", user.getUsername());
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User created successfully", createdUser));
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest(e.getMessage()));
        }
    }
    
    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        log.info("REST request to get user by ID: {}", id);
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success(user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "User not found with ID: " + id)));
    }
    
    /**
     * Get all users
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(
            @RequestParam(required = false) String keyword) {
        log.info("REST request to get all users");
        
        try {
            List<User> users;
            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userService.searchUsers(keyword);
            } else {
                users = userService.getAllUsers();
            }
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("Error fetching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get users with pagination
     * GET /api/users/page
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<User>>> getUsersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        log.info("REST request to get users with pagination: page={}, size={}", page, size);
        
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            Page<User> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("Error fetching users with pagination", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get active users
     * GET /api/users/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<User>>> getActiveUsers() {
        log.info("REST request to get active users");
        try {
            List<User> users = userService.getActiveUsers();
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("Error fetching active users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Update user
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id,
            @RequestBody User userDetails) {
        log.info("REST request to update user: {}", id);
        
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
        } catch (RuntimeException e) {
            log.error("Error updating user", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Delete user
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("REST request to delete user: {}", id);
        
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
        } catch (RuntimeException e) {
            log.error("Error deleting user", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get user count
     * GET /api/users/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getUserCount() {
        log.info("REST request to get user count");
        try {
            long count = userService.countUsers();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Error getting user count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
