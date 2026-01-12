package com.defi.aave.controller;

import com.defi.aave.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides application health and status endpoints
 */
@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {
    
    /**
     * Basic health check endpoint
     * GET /api/health
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("application", "DeFi Aave Application");
        health.put("version", "1.0.0");
        
        log.debug("Health check requested");
        return ApiResponse.success(health);
    }
    
    /**
     * Welcome endpoint
     * GET /api/health/welcome
     */
    @GetMapping("/welcome")
    public ApiResponse<String> welcome() {
        return ApiResponse.success("Welcome to DeFi Aave Application!");
    }
}
