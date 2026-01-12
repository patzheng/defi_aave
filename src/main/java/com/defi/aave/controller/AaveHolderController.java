package com.defi.aave.controller;

import com.defi.aave.dto.ApiResponse;
import com.defi.aave.dto.HolderDto;
import com.defi.aave.dto.PageDto;
import com.defi.aave.dto.SyncResultDto;
import com.defi.aave.service.AaveHolderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * AAVE持有者控制器
 * 提供REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/aave/holders")
public class AaveHolderController {
    
    @Autowired
    private AaveHolderService aaveHolderService;
    
    /**
     * 触发数据同步
     * POST /api/aave/holders/sync
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<SyncResultDto>> syncHolders() {
        log.info("Received request to sync AAVE holders");
        
        try {
            SyncResultDto result = aaveHolderService.syncHolders();
            
            return ResponseEntity.ok(ApiResponse.<SyncResultDto>builder()
                    .code(200)
                    .message("Sync completed successfully")
                    .data(result)
                    .build());
        } catch (Exception e) {
            log.error("Error during sync: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<SyncResultDto>builder()
                            .code(500)
                            .message("Sync failed: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }
    
    /**
     * 查询持有者列表（分页）
     * GET /api/aave/holders
     * 
     * @param page 页码（从0开始），默认0
     * @param size 每页数量，默认20
     * @param sortBy 排序字段（holdingAmount, profitLossPercentage），默认holdingAmount
     * @param order 排序方向（asc, desc），默认desc
     * @param minHolding 最小持仓量过滤，默认3000
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageDto<HolderDto>>> getHolders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) BigDecimal minHolding) {
        
        log.info("Querying holders - page: {}, size: {}, sortBy: {}, order: {}, minHolding: {}", 
                page, size, sortBy, order, minHolding);
        
        try {
            PageDto<HolderDto> result = aaveHolderService.getHolders(page, size, sortBy, order, minHolding);
            
            return ResponseEntity.ok(ApiResponse.<PageDto<HolderDto>>builder()
                    .code(200)
                    .message("Query successful")
                    .data(result)
                    .build());
        } catch (Exception e) {
            log.error("Error querying holders: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<PageDto<HolderDto>>builder()
                            .code(500)
                            .message("Query failed: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }
    
    /**
     * 查询单个持有者详情
     * GET /api/aave/holders/{address}
     * 
     * @param address 钱包地址
     */
    @GetMapping("/{address}")
    public ResponseEntity<ApiResponse<HolderDto>> getHolderByAddress(@PathVariable String address) {
        log.info("Querying holder details for address: {}", address);
        
        try {
            HolderDto holder = aaveHolderService.getHolderByAddress(address);
            
            if (holder == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<HolderDto>builder()
                                .code(404)
                                .message("Holder not found for address: " + address)
                                .data(null)
                                .build());
            }
            
            return ResponseEntity.ok(ApiResponse.<HolderDto>builder()
                    .code(200)
                    .message("Query successful")
                    .data(holder)
                    .build());
        } catch (Exception e) {
            log.error("Error querying holder {}: {}", address, e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<HolderDto>builder()
                            .code(500)
                            .message("Query failed: " + e.getMessage())
                            .data(null)
                            .build());
        }
    }
}
