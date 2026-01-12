package com.defi.aave.repository;

import com.defi.aave.entity.AaveHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * AAVE持有者数据访问接口
 */
@Repository
public interface AaveHolderRepository extends JpaRepository<AaveHolder, Long> {
    
    /**
     * 根据钱包地址查找持有者
     */
    Optional<AaveHolder> findByWalletAddress(String walletAddress);
    
    /**
     * 判断钱包地址是否已存在
     */
    boolean existsByWalletAddress(String walletAddress);
    
    /**
     * 查询持仓量大于指定值的持有者（分页）
     */
    Page<AaveHolder> findByHoldingAmountGreaterThanEqual(BigDecimal minHolding, Pageable pageable);
    
    /**
     * 查询持仓量大于指定值的持有者数量
     */
    long countByHoldingAmountGreaterThanEqual(BigDecimal minHolding);
    
    /**
     * 自定义查询：按持仓量排序并分页
     */
    @Query("SELECT a FROM AaveHolder a WHERE a.holdingAmount >= :minHolding ORDER BY a.holdingAmount DESC")
    Page<AaveHolder> findHoldersByMinHoldingOrderByAmount(@Param("minHolding") BigDecimal minHolding, Pageable pageable);
    
    /**
     * 自定义查询：按收益率排序并分页
     */
    @Query("SELECT a FROM AaveHolder a WHERE a.holdingAmount >= :minHolding AND a.profitLossPercentage IS NOT NULL ORDER BY a.profitLossPercentage DESC")
    Page<AaveHolder> findHoldersByMinHoldingOrderByProfitPercentage(@Param("minHolding") BigDecimal minHolding, Pageable pageable);
}
