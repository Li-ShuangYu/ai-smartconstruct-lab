package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标准知识点实体
 *
 * 对应 biz_knowledge_point 表，存储课程相关的知识点层级结构。
 * 支持通过 parentId 构建父子层级关系。
 *
 * @author SmartConstruct
 */
@Data
@TableName("biz_knowledge_point")
public class BizKnowledgePoint {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 关联课程ID */
    private Long courseId;

    /** 知识点名称 */
    private String kpName;

    /** 父知识点ID（null表示根节点） */
    private Long parentId;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
