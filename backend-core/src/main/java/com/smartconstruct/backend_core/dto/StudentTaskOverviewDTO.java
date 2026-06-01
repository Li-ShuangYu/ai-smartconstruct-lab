package com.smartconstruct.backend_core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("task_id")
    private String taskId;
    /** 实训任务名称 */
    @JsonProperty("task_name")
    private String taskName;
    /** 模板名称 */
    @JsonProperty("template_name")
    private String templateName;
    /** 任务开始时间 */
    @JsonProperty("start_time")
    private LocalDateTime startTime;
    /** 任务结束时间 */
    @JsonProperty("end_time")
    private LocalDateTime endTime;
    /** 是否已过期 */
    @JsonProperty("is_expired")
    private Boolean isExpired;
    /** 参与记录ID */
    @JsonProperty("participation_id")
    private String participationId;
    /** 参与状态：0=未开始，1=进行中，2=已完成 */
    @JsonProperty("participation_status")
    private Integer participationStatus;
    /** 当前所在阶段ID */
    @JsonProperty("current_phase_id")
    private String currentPhaseId;
    /** 当前所在节点实例ID */
    @JsonProperty("current_node_instance_id")
    private String currentNodeInstanceId;
    /** 阶段进度列表 */
    private List<PhaseProgressDTO> phases;
}
