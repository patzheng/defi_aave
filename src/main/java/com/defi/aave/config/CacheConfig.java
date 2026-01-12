package com.defi.aave.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 */
@Configuration
public class CacheConfig {
    
    @Autowired
    private AaveProperties aaveProperties;
    
    /**
     * 价格缓存配置
     * 用于缓存AAVE当前价格，减少API调用
     */
    @Bean
    public Cache<String, BigDecimal> priceCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(aaveProperties.getCache().getPriceTtl(), TimeUnit.SECONDS)
                .maximumSize(100)
                .build();
    }
    
    /**
     * RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
