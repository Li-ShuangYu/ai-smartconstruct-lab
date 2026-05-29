package com.smartconstruct.backend_core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI引擎任务状态查询响应DTO
 * <p>
 * AI引擎的 /api/orchestration/status/{job_id} 返回 snake_case JSON，字段名
 * 与 Java 驼峰通过 @JsonProperty 映射：
 * <pre>
 * {
 *   "job_id": "uuid",
 *   "status": "processing|completed|failed",
 *   "error": "错误信息或null"
 * }
 * </pre>
 * status 字段在 AI 引擎端为字符串（processing/completed/failed），
 * 此处统一转换为整数（1/2/3）以保持和业务层一致。
 *
 * @author SmartConstruct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiJobStatus {
    /** 任务ID (UUID格式) */
    @JsonProperty("job_id")
    private String jobId;
    /** 处理状态: 1=处理中, 2=成功, 3=失败 */
    private Integer status;
    /** 预估剩余时间（秒）—— AI引擎响应中未使用此字段 */
    private Integer estimatedRemainingSeconds;
    /** 错误原因（status=3时有值）—— 映射自AI引擎的 "error" 字段 */
    @JsonProperty("error")
    private String errorReason;
}
