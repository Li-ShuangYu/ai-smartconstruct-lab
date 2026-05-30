package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生实训任务总览 DTO
 *
 * 包含实训任务基本信息、学生参与状态、阶段进度列表和当前位置信息。
 * 用于学生端实训详情页面的数据展示。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentTaskOverviewDTO {
    /** 实训任务ID */
    private String taskId;
    /** 实训任务名称 */
    private String taskName;
    /** 模板名称 */
    private String templateName;
    /** 任务开始时间 */
    private LocalDateTime startTime;
    /** 任务结束时间 */
    private LocalDateTime endTime;
    /** 是否已过期 */
    private Boolean isExpired;
    /** 参与记录ID */
    private String participationId;
    /** 参与状态：0=未开始，1=进行中，2=已完成 */
    private Integer participationStatus;
    /** 当前所在阶段ID */
    private String currentPhaseId;
    /** 当前所在节点实例ID */
    private String currentNodeInstanceId;
    /** 阶段进度列表 */
    private List<PhaseProgressDTO> phases;
}
