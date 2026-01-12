package com.defi.aave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 同步结果DTO
 * 用于返回数据同步操作的统计信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResultDto {
    
    /**
     * 处理的地址总数
     */
    private Integer totalProcessed;
    
    /**
     * 成功分析的数量
     */
    private Integer successCount;
    
    /**
     * 失败的数量
     */
    private Integer failedCount;
    
    /**
     * 同步开始时间
     */
    private String startTime;
    
    /**
     * 同步结束时间
     */
    private String endTime;
    
    /**
     * 耗时（秒）
     */
    private Long durationSeconds;
}
