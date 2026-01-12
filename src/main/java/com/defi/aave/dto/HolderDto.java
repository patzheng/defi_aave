package com.defi.aave.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AAVE持有者DTO
 * 用于API响应返回持有者信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolderDto {
    
    /**
     * 钱包地址
     */
    private String walletAddress;
    
    /**
     * 持有数量
     */
    private BigDecimal holdingAmount;
    
    /**
     * 首次购买时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime firstPurchaseTime;
    
    /**
     * 首次购买价格（USD）
     */
    private BigDecimal firstPurchasePrice;
    
    /**
     * 首次购买数量
     */
    private BigDecimal firstPurchaseAmount;
    
    /**
     * 当前价格（USD）
     */
    private BigDecimal currentPrice;
    
    /**
     * 购买成本（USD）
     */
    private BigDecimal costBasis;
    
    /**
     * 当前持仓价值（USD）
     */
    private BigDecimal currentValue;
    
    /**
     * 收益金额（USD）
     */
    private BigDecimal profitLoss;
    
    /**
     * 收益率（%）
     */
    private BigDecimal profitLossPercentage;
    
    /**
     * 数据来源
     */
    private String dataSource;
    
    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;
}
