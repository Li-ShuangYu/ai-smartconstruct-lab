package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 学生业务实体类
 * 
 * 存储学生的扩展信息，与 sys_user 表通过 userId 关联。
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("biz_student")
public class BizStudent {
    /** 用户ID（关联 sys_user.id） */
    private Long userId;
    
    /** 学号 */
    private String realName;
    
    /** 真实姓名 */
    private String studentNo;
    
    /** 院系ID（关联 biz_department.id） */
    private Long deptId;
    
    /** 专业ID（关联 biz_major.id） */
    private Long majorId;
    
    /** 班级ID（关联 biz_admin_class.id） */
    private Long classId;
}
