package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 教师业务实体类
 * 
 * 存储教师的扩展信息，与 sys_user 表通过 userId 关联。
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("biz_teacher")
public class BizTeacher {
    /** 用户ID（关联 sys_user.id） */
    private Long userId;
    
    /** 工号 */
    private String employeeNo;
    
    /** 真实姓名 */
    private String realName;
    
    /** 院系ID（关联 biz_department.id） */
    private Long deptId;
}
