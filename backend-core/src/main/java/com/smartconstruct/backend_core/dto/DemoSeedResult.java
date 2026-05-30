package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 演示数据播种结果 DTO
 *
 * 包含播种操作的统计信息，用于 POST /api/public/demo/seed 端点的响应。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemoSeedResult {
    /** 创建的学生数量 */
    private int studentsCreated;
    /** 创建的实训任务ID */
    private Long taskId;
    /** 创建的节点实例数量 */
    private int nodeInstanceCount;
    /** 创建的参与记录数量 */
    private int participationCount;
    /** 创建的节点进度记录数量 */
    private int nodeProgressCount;
    /** 模板ID */
    private Long templateId;
    /** 班级ID */
    private Long classId;
    /** 播种结果消息 */
    private String message;
}
