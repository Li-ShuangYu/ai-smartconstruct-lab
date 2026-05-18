package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.RegisterRequest;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 系统初始化控制器
 *
 * 仅在首次启动时调用，初始化院系、建表、初始化院系、专业、班级和初始用户及初始种子数据。
 * 幂等设计：如数据已存在则直接跳过，不会重复插入。
 */
@RestController
@RequestMapping("/api/public")
public class InitController {

    private static final Logger log = LoggerFactory.getLogger(InitController.class);

    private final IDepartmentService departmentService;
    private final IMajorService majorService;
    private final IAdminClassService adminClassService;
    private final SysUserService sysUserService;
    private final IStudentService studentService;
    private final ITeacherService teacherService;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public InitController(
            IDepartmentService departmentService,
            IMajorService majorService,
            IAdminClassService adminClassService,
            SysUserService sysUserService,
            IStudentService studentService,
            ITeacherService teacherService,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        this.departmentService = departmentService;
        this.majorService = majorService;
        this.adminClassService = adminClassService;
        this.sysUserService = sysUserService;
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/init")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> initSystemData() {
        // 1. 检查是否已初始化（通过部门数据判断）
        if (departmentService.count() > 0) {
            return ApiResult.ok("系统数据已初始化，无需重复操作");
        }

        // 2. 执行建表/加列迁移（兼容旧的 Hibernate ddl-auto=update 未管理的表）
        migrateSchema();

        LocalDateTime now = LocalDateTime.now();

        // ========== 3. 初始化院系 ==========
        BizDepartment dept = new BizDepartment();
        dept.setDeptName("信息工程学院");
        dept.setCreatedAt(now);
        departmentService.save(dept);

        // ========== 4. 初始化专业 ==========
        BizMajor major = new BizMajor();
        major.setDeptId(dept.getId());
        major.setMajorName("软件工程");
        major.setCreatedAt(now);
        majorService.save(major);

        // ========== 5. 初始化班级 ==========
        BizAdminClass adminClass = new BizAdminClass();
        adminClass.setMajorId(major.getId());
        adminClass.setClassName("26软件工程1班");
        adminClass.setEnrollmentYear(2026);
        adminClass.setCreatedAt(now);
        adminClassService.save(adminClass);

        // ========== 6. 初始化管理员用户 ==========
        SysUser admin = new SysUser();
        admin.setUsername("user1");
        admin.setPasswordHash(passwordEncoder.encode("123456"));
        admin.setRoleType(1);
        admin.setStatus(0);
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        sysUserService.save(admin);

        // ========== 7. 初始化教师 ==========
        RegisterRequest teacherReq = new RegisterRequest();
        teacherReq.setUsername("teacher");
        teacherReq.setPassword("123456");
        teacherReq.setRoleType(2);
        teacherReq.setEmployeeNo("T001");
        teacherReq.setRealName("李欣欣");
        teacherReq.setDeptId(dept.getId());
        sysUserService.registerUser(teacherReq);

        // ========== 8. 初始化学生 ==========
        RegisterRequest studentReq = new RegisterRequest();
        studentReq.setUsername("student");
        studentReq.setPassword("123456");
        studentReq.setRoleType(3);
        studentReq.setStudentNo("S001");
        studentReq.setRealName("张三");
        studentReq.setDeptId(dept.getId());
        studentReq.setMajorId(major.getId());
        studentReq.setClassId(adminClass.getId());
        sysUserService.registerUser(studentReq);

        return ApiResult.ok("系统数据初始化完成：院系/专业/班级/管理员/教师/学生");
    }

    /**
     * 迁移数据库 schema，为 MyBatis-Plus 实体补充 Hibernate 未管理的列
     */
    private void migrateSchema() {
        // biz_department: add created_at if missing
        safeAlter("ALTER TABLE biz_department ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        // biz_major: add created_at if missing
        safeAlter("ALTER TABLE biz_major ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        // biz_admin_class: add enrollment_year if missing
        safeAlter("ALTER TABLE biz_admin_class ADD COLUMN enrollment_year INT NOT NULL DEFAULT 2026");
        safeAlter("ALTER TABLE biz_admin_class ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        // biz_student: add columns if missing
        safeAlter("ALTER TABLE biz_student ADD COLUMN student_no VARCHAR(32) NOT NULL DEFAULT ''");
        safeAlter("ALTER TABLE biz_student ADD COLUMN real_name VARCHAR(64) NOT NULL DEFAULT ''");
        safeAlter("ALTER TABLE biz_student ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        safeAlter("ALTER TABLE biz_student ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        safeAlter("ALTER TABLE biz_student ADD UNIQUE INDEX uk_student_no (student_no)");
        // biz_teacher: add columns if missing
        safeAlter("ALTER TABLE biz_teacher ADD COLUMN employee_no VARCHAR(32) NOT NULL DEFAULT ''");
        safeAlter("ALTER TABLE biz_teacher ADD COLUMN real_name VARCHAR(64) NOT NULL DEFAULT ''");
        safeAlter("ALTER TABLE biz_teacher ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        safeAlter("ALTER TABLE biz_teacher ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        safeAlter("ALTER TABLE biz_teacher ADD UNIQUE INDEX uk_employee_no (employee_no)");
        // wf_node_def: add node_type if missing
        safeAlter("ALTER TABLE wf_node_def ADD COLUMN node_type VARCHAR(32) NOT NULL DEFAULT ''");
        safeAlter("ALTER TABLE wf_node_def ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        safeAlter("ALTER TABLE wf_node_def ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        safeAlter("ALTER TABLE wf_node_def ADD UNIQUE INDEX uk_node_type (node_type)");
        // biz_training_task: add group_config_json if missing
        safeAlter("ALTER TABLE biz_training_task ADD COLUMN group_config_json JSON DEFAULT NULL");
        // biz_training_participation: add total_score, satisfaction_detail_json
        safeAlter("ALTER TABLE biz_training_participation ADD COLUMN total_score DECIMAL(5,2) DEFAULT NULL");
        safeAlter("ALTER TABLE biz_training_participation ADD COLUMN satisfaction_detail_json JSON DEFAULT NULL");
        // biz_mindmap_record: add node_instance_id / map_topology_json
        safeAlter("ALTER TABLE biz_mindmap_record ADD COLUMN node_instance_id BIGINT DEFAULT NULL");
        safeAlter("ALTER TABLE biz_mindmap_record ADD COLUMN map_topology_json JSON DEFAULT NULL");
        // wf_global_activity_state: add created_at if missing
        safeAlter("ALTER TABLE wf_global_activity_state ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        safeAlter("ALTER TABLE wf_global_activity_state ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        // wf_student_activity_state: add created_at if missing
        safeAlter("ALTER TABLE wf_student_activity_state ADD COLUMN current_node_instance_id BIGINT DEFAULT NULL");
        safeAlter("ALTER TABLE wf_student_activity_state ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
    }

    private void safeAlter(String sql) {
        try {
            jdbcTemplate.execute(sql);
            log.info("Schema migration OK: {}", sql.substring(0, Math.min(sql.length(), 80)));
        } catch (Exception e) {
            // Column/index already exists — this is expected on re-run
            log.debug("Schema migration skipped (likely already exists): {}", e.getMessage());
        }
    }
}