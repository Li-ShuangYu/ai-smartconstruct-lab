package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI引擎任务状态查询响应DTO
 *
 * 用于轮询AI引擎获取处理状态（备用方案，当回调失败时使用）
 *
 * @author SmartConstruct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiJobStatus {
    /** 任务ID (UUID格式) */
    private String jobId;
    /** 处理状态: 1=处理中, 2=成功, 3=失败 */
    private Integer status;
    /** 预估剩余时间（秒） */
    private Integer estimatedRemainingSeconds;
    /** 错误原因（status=3时有值） */
    private String errorReason;
}
