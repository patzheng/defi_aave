package com.defi.aave.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AAVE相关配置属性
 */
@Configuration
@ConfigurationProperties(prefix = "aave")
@Data
public class AaveProperties {
    
    private Etherscan etherscan = new Etherscan();
    private Token token = new Token();
    private Coingecko coingecko = new Coingecko();
    private Sync sync = new Sync();
    private Cache cache = new Cache();
    
    @Data
    public static class Etherscan {
        private String apiKey;
        private String baseUrl;
    }
    
    @Data
    public static class Token {
        private String contractAddress;
        private Integer minHolding;
    }
    
    @Data
    public static class Coingecko {
        private String apiUrl;
        private String tokenId;
    }
    
    @Data
    public static class Sync {
        private Integer batchSize;
    }
    
    @Data
    public static class Cache {
        private Integer priceTtl;
    }
}
