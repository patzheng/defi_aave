package com.defi.aave.service;

import com.defi.aave.client.CoinGeckoApiClient;
import com.defi.aave.client.EtherscanApiClient;
import com.defi.aave.config.AaveProperties;
import com.defi.aave.dto.HolderDto;
import com.defi.aave.dto.PageDto;
import com.defi.aave.dto.SyncResultDto;
import com.defi.aave.entity.AaveHolder;
import com.defi.aave.repository.AaveHolderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AAVE持有者服务
 * 实现核心业务逻辑
 */
@Slf4j
@Service
public class AaveHolderService {
    
    @Autowired
    private AaveHolderRepository aaveHolderRepository;
    
    @Autowired
    private EtherscanApiClient etherscanApiClient;
    
    @Autowired
    private CoinGeckoApiClient coinGeckoApiClient;
    
    @Autowired
    private AaveProperties aaveProperties;
    
    /**
     * 同步AAVE持有者数据
     */
    @Transactional
    public SyncResultDto syncHolders() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Starting AAVE holders sync at {}", startTime);
        
        int totalProcessed = 0;
        int successCount = 0;
        int failedCount = 0;
        
        try {
            // 获取当前AAVE价格
            BigDecimal currentPrice = coinGeckoApiClient.getCurrentPrice();
            if (currentPrice == null) {
                throw new RuntimeException("Failed to get current AAVE price");
            }
            
            // 获取持有者地址列表
            List<String> holderAddresses = etherscanApiClient.getTopHolders();
            log.info("Found {} potential holder addresses", holderAddresses.size());
            
            // 批量处理
            int batchSize = aaveProperties.getSync().getBatchSize();
            for (int i = 0; i < holderAddresses.size(); i += batchSize) {
                int end = Math.min(i + batchSize, holderAddresses.size());
                List<String> batch = holderAddresses.subList(i, end);
                
                for (String address : batch) {
                    totalProcessed++;
                    try {
                        boolean success = processHolder(address, currentPrice);
                        if (success) {
                            successCount++;
                        } else {
                            failedCount++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to process holder {}: {}", address, e.getMessage());
                        failedCount++;
                    }
                }
                
                // 批次间延迟，避免API限流
                if (end < holderAddresses.size()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error during sync: {}", e.getMessage(), e);
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        long durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
        
        log.info("Sync completed. Total: {}, Success: {}, Failed: {}, Duration: {}s", 
                totalProcessed, successCount, failedCount, durationSeconds);
        
        return SyncResultDto.builder()
                .totalProcessed(totalProcessed)
                .successCount(successCount)
                .failedCount(failedCount)
                .startTime(startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .endTime(endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .durationSeconds(durationSeconds)
                .build();
    }
    
    /**
     * 处理单个持有者
     */
    private boolean processHolder(String address, BigDecimal currentPrice) {
        try {
            // 获取余额
            BigDecimal balance = etherscanApiClient.getTokenBalance(address);
            
            // 检查是否满足最小持仓量
            BigDecimal minHolding = new BigDecimal(aaveProperties.getToken().getMinHolding());
            if (balance.compareTo(minHolding) < 0) {
                log.debug("Address {} holding {} is below minimum {}", address, balance, minHolding);
                return false;
            }
            
            log.info("Processing holder {} with balance {}", address, balance);
            
            // 获取首次购买信息
            EtherscanApiClient.TokenTransaction firstTx = etherscanApiClient.getFirstIncomingTransaction(address);
            
            AaveHolder holder = AaveHolder.builder()
                    .walletAddress(address)
                    .holdingAmount(balance)
                    .currentPrice(currentPrice)
                    .currentValue(balance.multiply(currentPrice))
                    .build();
            
            // 如果找到首次交易，获取历史价格并计算收益
            if (firstTx != null) {
                long timestamp = Long.parseLong(firstTx.getTimeStamp());
                LocalDateTime purchaseTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestamp), 
                        ZoneId.systemDefault()
                );
                
                BigDecimal purchaseAmount = new BigDecimal(firstTx.getValue())
                        .divide(new BigDecimal("1000000000000000000"), 18, RoundingMode.HALF_UP);
                
                BigDecimal historicalPrice = coinGeckoApiClient.getHistoricalPrice(timestamp);
                
                holder.setFirstPurchaseTime(purchaseTime);
                holder.setFirstPurchaseAmount(purchaseAmount);
                
                if (historicalPrice != null) {
                    holder.setFirstPurchasePrice(historicalPrice);
                    
                    // 计算成本和收益
                    BigDecimal costBasis = historicalPrice.multiply(purchaseAmount);
                    holder.setCostBasis(costBasis);
                    
                    if (costBasis.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal profitLoss = holder.getCurrentValue().subtract(costBasis);
                        holder.setProfitLoss(profitLoss);
                        
                        BigDecimal profitPercentage = profitLoss
                                .divide(costBasis, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(2, RoundingMode.HALF_UP);
                        holder.setProfitLossPercentage(profitPercentage);
                    }
                }
            }
            
            // 保存或更新
            AaveHolder existing = aaveHolderRepository.findByWalletAddress(address).orElse(null);
            if (existing != null) {
                holder.setId(existing.getId());
                holder.setCreatedAt(existing.getCreatedAt());
            }
            
            aaveHolderRepository.save(holder);
            log.info("Saved holder {} with profit/loss: {}", 
                    address, holder.getProfitLossPercentage());
            
            return true;
        } catch (Exception e) {
            log.error("Error processing holder {}: {}", address, e.getMessage());
            return false;
        }
    }
    
    /**
     * 查询持有者列表（分页）
     */
    public PageDto<HolderDto> getHolders(Integer page, Integer size, String sortBy, String order, BigDecimal minHolding) {
        // 参数默认值
        page = (page != null && page >= 0) ? page : 0;
        size = (size != null && size > 0) ? size : 20;
        sortBy = (sortBy != null) ? sortBy : "holdingAmount";
        order = (order != null && "asc".equalsIgnoreCase(order)) ? "asc" : "desc";
        minHolding = (minHolding != null) ? minHolding : new BigDecimal(aaveProperties.getToken().getMinHolding());
        
        // 构建排序
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, convertSortField(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // 查询
        Page<AaveHolder> holderPage = aaveHolderRepository.findByHoldingAmountGreaterThanEqual(minHolding, pageable);
        
        // 转换为DTO
        List<HolderDto> holderDtos = holderPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return PageDto.<HolderDto>builder()
                .content(holderDtos)
                .totalElements(holderPage.getTotalElements())
                .totalPages(holderPage.getTotalPages())
                .currentPage(holderPage.getNumber())
                .pageSize(holderPage.getSize())
                .first(holderPage.isFirst())
                .last(holderPage.isLast())
                .build();
    }
    
    /**
     * 根据地址查询持有者详情
     */
    public HolderDto getHolderByAddress(String address) {
        AaveHolder holder = aaveHolderRepository.findByWalletAddress(address)
                .orElse(null);
        
        if (holder == null) {
            return null;
        }
        
        return convertToDto(holder);
    }
    
    /**
     * 转换排序字段名
     */
    private String convertSortField(String sortBy) {
        if ("profitLossPercentage".equals(sortBy)) {
            return "profitLossPercentage";
        }
        return "holdingAmount";
    }
    
    /**
     * 实体转DTO
     */
    private HolderDto convertToDto(AaveHolder holder) {
        return HolderDto.builder()
                .walletAddress(holder.getWalletAddress())
                .holdingAmount(holder.getHoldingAmount())
                .firstPurchaseTime(holder.getFirstPurchaseTime())
                .firstPurchasePrice(holder.getFirstPurchasePrice())
                .firstPurchaseAmount(holder.getFirstPurchaseAmount())
                .currentPrice(holder.getCurrentPrice())
                .costBasis(holder.getCostBasis())
                .currentValue(holder.getCurrentValue())
                .profitLoss(holder.getProfitLoss())
                .profitLossPercentage(holder.getProfitLossPercentage())
                .dataSource(holder.getDataSource())
                .lastUpdated(holder.getLastUpdated())
                .build();
    }
}
