package com.defi.aave.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AAVE代币持有者实体类
 * 存储AAVE持有者的钱包地址、持仓量、首次购买信息和收益分析
 */
@Entity
@Table(name = "aave_holders", indexes = {
    @Index(name = "idx_wallet_address", columnList = "wallet_address", unique = true),
    @Index(name = "idx_holding_amount", columnList = "holding_amount"),
    @Index(name = "idx_profit_loss_percentage", columnList = "profit_loss_percentage")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AaveHolder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 钱包地址（42位十六进制字符，以0x开头）
     */
    @Column(name = "wallet_address", nullable = false, unique = true, length = 42)
    private String walletAddress;
    
    /**
     * 当前持有AAVE数量
     */
    @Column(name = "holding_amount", nullable = false, precision = 30, scale = 18)
    private BigDecimal holdingAmount;
    
    /**
     * 首次购买时间
     */
    @Column(name = "first_purchase_time")
    private LocalDateTime firstPurchaseTime;
    
    /**
     * 首次购买时的AAVE价格（USD）
     */
    @Column(name = "first_purchase_price", precision = 20, scale = 8)
    private BigDecimal firstPurchasePrice;
    
    /**
     * 首次购买数量
     */
    @Column(name = "first_purchase_amount", precision = 30, scale = 18)
    private BigDecimal firstPurchaseAmount;
    
    /**
     * 查询时的AAVE当前价格（USD）
     */
    @Column(name = "current_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal currentPrice;
    
    /**
     * 购买成本（USD） = first_purchase_price × first_purchase_amount
     */
    @Column(name = "cost_basis", precision = 30, scale = 8)
    private BigDecimal costBasis;
    
    /**
     * 当前持仓价值（USD） = current_price × holding_amount
     */
    @Column(name = "current_value", nullable = false, precision = 30, scale = 8)
    private BigDecimal currentValue;
    
    /**
     * 收益金额（USD） = current_value - cost_basis
     */
    @Column(name = "profit_loss", precision = 30, scale = 8)
    private BigDecimal profitLoss;
    
    /**
     * 收益率（%） = (profit_loss / cost_basis) × 100
     */
    @Column(name = "profit_loss_percentage", precision = 10, scale = 2)
    private BigDecimal profitLossPercentage;
    
    /**
     * 数据来源
     */
    @Column(name = "data_source", length = 50, nullable = false)
    @Builder.Default
    private String dataSource = "Etherscan";
    
    /**
     * 最后更新时间
     */
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 自动更新最后修改时间
     */
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
