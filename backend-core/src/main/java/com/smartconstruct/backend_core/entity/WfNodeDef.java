package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wf_node_def")
public class WfNodeDef {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String nodeCode;
    private String nodeName;
    private Integer isActive;
}
