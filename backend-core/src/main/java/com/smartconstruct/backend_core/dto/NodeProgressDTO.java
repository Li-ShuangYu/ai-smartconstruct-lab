package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 节点进度 DTO
 *
 * 表示学生在某个节点实例上的进度信息，包含节点基本信息和学生进度状态。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeProgressDTO {
    /** 节点实例ID */
    private String nodeInstanceId;
    /** 节点定义ID */
    private String nodeId;
    /** 节点类型 */
    private String nodeType;
    /** 节点名称 */
    private String nodeName;
    /** 所属阶段ID */
    private String phaseId;
    /** 排序序号 */
    private Integer sortNum;
    /** 是否为必修节点 */
    private Boolean isRequired;
    /** 节点进度状态：0-未开始 1-进行中 2-已完成 3-已跳过 */
    private Integer status;
    /** 进入节点时间 */
    private LocalDateTime enteredAt;
    /** 离开节点时间 */
    private LocalDateTime exitedAt;
    /** 总停留时长(秒) */
    private Integer stayDurationSeconds;
}
