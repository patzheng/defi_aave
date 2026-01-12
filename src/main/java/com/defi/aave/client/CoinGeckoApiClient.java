package com.defi.aave.client;

import com.defi.aave.config.AaveProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * CoinGecko API客户端
 * 封装价格查询相关的API调用
 */
@Slf4j
@Component
public class CoinGeckoApiClient {
    
    @Autowired
    private AaveProperties aaveProperties;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private Cache<String, BigDecimal> priceCache;
    
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final String PRICE_CACHE_KEY = "aave_current_price";
    
    /**
     * 获取AAVE当前价格（USD）
     * 使用缓存减少API调用
     */
    public BigDecimal getCurrentPrice() {
        // 先从缓存获取
        BigDecimal cachedPrice = priceCache.getIfPresent(PRICE_CACHE_KEY);
        if (cachedPrice != null) {
            log.debug("Using cached AAVE price: {}", cachedPrice);
            return cachedPrice;
        }
        
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(aaveProperties.getCoingecko().getApiUrl())
                    .path("/simple/price")
                    .queryParam("ids", aaveProperties.getCoingecko().getTokenId())
                    .queryParam("vs_currencies", "usd")
                    .toUriString();
            
            log.debug("Fetching current AAVE price from CoinGecko");
            SimplePrice response = executeWithRetry(url, SimplePrice.class);
            
            if (response != null && response.getAave() != null) {
                BigDecimal price = response.getAave().getUsd();
                // 存入缓存
                priceCache.put(PRICE_CACHE_KEY, price);
                log.info("Current AAVE price: ${}", price);
                return price;
            }
            
            log.error("Failed to get current AAVE price from CoinGecko");
            return null;
        } catch (Exception e) {
            log.error("Error getting current price: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取历史价格（指定时间点的价格）
     * @param timestamp Unix时间戳（秒）
     */
    public BigDecimal getHistoricalPrice(long timestamp) {
        try {
            // CoinGecko的历史价格API需要日期格式：dd-mm-yyyy
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timestamp), 
                    ZoneId.systemDefault()
            );
            String date = String.format("%02d-%02d-%d", 
                    dateTime.getDayOfMonth(), 
                    dateTime.getMonthValue(), 
                    dateTime.getYear());
            
            String url = UriComponentsBuilder
                    .fromHttpUrl(aaveProperties.getCoingecko().getApiUrl())
                    .path("/coins/" + aaveProperties.getCoingecko().getTokenId() + "/history")
                    .queryParam("date", date)
                    .queryParam("localization", "false")
                    .toUriString();
            
            log.debug("Fetching historical AAVE price for date: {}", date);
            HistoricalPrice response = executeWithRetry(url, HistoricalPrice.class);
            
            if (response != null && response.getMarketData() != null 
                    && response.getMarketData().getCurrentPrice() != null) {
                BigDecimal price = response.getMarketData().getCurrentPrice().get("usd");
                log.debug("Historical AAVE price for {}: ${}", date, price);
                return price;
            }
            
            log.warn("No historical price data found for date: {}", date);
            return null;
        } catch (Exception e) {
            log.error("Error getting historical price for timestamp {}: {}", timestamp, e.getMessage());
            return null;
        }
    }
    
    /**
     * 执行API请求并重试
     */
    private <T> T executeWithRetry(String url, Class<T> responseType) {
        Exception lastException = null;
        
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                T response = restTemplate.getForObject(url, responseType);
                return response;
            } catch (Exception e) {
                lastException = e;
                log.warn("API call failed (attempt {}/{}): {}", i + 1, MAX_RETRIES, e.getMessage());
                
                if (i < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        throw new RuntimeException("API call failed after " + MAX_RETRIES + " retries", lastException);
    }
    
    // Response DTOs
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SimplePrice {
        private AavePrice aave;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AavePrice {
        private BigDecimal usd;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoricalPrice {
        @JsonProperty("market_data")
        private MarketData marketData;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MarketData {
        @JsonProperty("current_price")
        private Map<String, BigDecimal> currentPrice;
    }
}
