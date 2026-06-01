package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单实体
 *
 * 对应数据库表 sys_ticket
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("sys_ticket")
public class SysTicket {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 创建用户ID */
    private Long creatorId;
    /** 工单标题 */
    private String title;
    /** 工单内容 */
    private String content;
    /** 工单状态：0=待处理，1=处理中，2=已解决，3=已关闭 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
