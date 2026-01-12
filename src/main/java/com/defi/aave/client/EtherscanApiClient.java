package com.defi.aave.client;

import com.defi.aave.config.AaveProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Etherscan API客户端
 * 封装与Etherscan API的交互
 */
@Slf4j
@Component
public class EtherscanApiClient {
    
    @Autowired
    private AaveProperties aaveProperties;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    /**
     * 获取指定地址的AAVE代币余额
     */
    public BigDecimal getTokenBalance(String address) {
        try {
            String url = buildUrl("account", "tokenbalance")
                    .queryParam("contractaddress", aaveProperties.getToken().getContractAddress())
                    .queryParam("address", address)
                    .queryParam("tag", "latest")
                    .toUriString();
            
            log.debug("Fetching token balance for address: {}", address);
            TokenBalanceResponse response = executeWithRetry(url, TokenBalanceResponse.class);
            
            if ("1".equals(response.getStatus()) && response.getResult() != null) {
                // AAVE has 18 decimals, convert from wei
                BigInteger balance = new BigInteger(response.getResult());
                return new BigDecimal(balance).divide(new BigDecimal("1000000000000000000"));
            }
            
            log.warn("Failed to get token balance for {}: {}", address, response.getMessage());
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error getting token balance for address {}: {}", address, e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 获取地址的代币交易历史
     */
    public List<TokenTransaction> getTokenTransactions(String address) {
        try {
            String url = buildUrl("account", "tokentx")
                    .queryParam("contractaddress", aaveProperties.getToken().getContractAddress())
                    .queryParam("address", address)
                    .queryParam("page", "1")
                    .queryParam("offset", "100")
                    .queryParam("sort", "asc")
                    .toUriString();
            
            log.debug("Fetching token transactions for address: {}", address);
            TokenTransactionResponse response = executeWithRetry(url, TokenTransactionResponse.class);
            
            if ("1".equals(response.getStatus()) && response.getResult() != null) {
                return response.getResult();
            }
            
            log.warn("No transactions found for address: {}", address);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting token transactions for address {}: {}", address, e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取首次接收AAVE的交易
     */
    public TokenTransaction getFirstIncomingTransaction(String address) {
        List<TokenTransaction> transactions = getTokenTransactions(address);
        
        return transactions.stream()
                .filter(tx -> tx.getTo().equalsIgnoreCase(address))
                .filter(tx -> new BigInteger(tx.getValue()).compareTo(BigInteger.ZERO) > 0)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取所有持有AAVE的地址（通过交易历史间接获取）
     * 注意：由于Etherscan API限制，这个方法只能获取部分地址
     * 实际应用中可能需要使用其他数据源或批量处理
     */
    public List<String> getTopHolders() {
        log.warn("Etherscan API does not directly support getting all token holders. " +
                "This method returns a limited sample from recent transactions.");
        
        try {
            // 获取最近的代币转账记录
            String url = buildUrl("account", "tokentx")
                    .queryParam("contractaddress", aaveProperties.getToken().getContractAddress())
                    .queryParam("page", "1")
                    .queryParam("offset", "1000")
                    .queryParam("sort", "desc")
                    .toUriString();
            
            TokenTransactionResponse response = executeWithRetry(url, TokenTransactionResponse.class);
            
            if ("1".equals(response.getStatus()) && response.getResult() != null) {
                // 提取所有接收地址（去重）
                return response.getResult().stream()
                        .map(TokenTransaction::getTo)
                        .distinct()
                        .collect(Collectors.toList());
            }
            
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting top holders: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * 构建API URL
     */
    private UriComponentsBuilder buildUrl(String module, String action) {
        return UriComponentsBuilder.fromHttpUrl(aaveProperties.getEtherscan().getBaseUrl())
                .queryParam("module", module)
                .queryParam("action", action)
                .queryParam("apikey", aaveProperties.getEtherscan().getApiKey());
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
    public static class TokenBalanceResponse {
        private String status;
        private String message;
        private String result;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenTransactionResponse {
        private String status;
        private String message;
        private List<TokenTransaction> result;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenTransaction {
        private String blockNumber;
        private String timeStamp;
        private String hash;
        private String from;
        private String to;
        private String value;
        private String tokenName;
        private String tokenSymbol;
        private String tokenDecimal;
        private String contractAddress;
        
        @JsonProperty("gasPrice")
        private String gasPrice;
        
        @JsonProperty("gasUsed")
        private String gasUsed;
    }
}
