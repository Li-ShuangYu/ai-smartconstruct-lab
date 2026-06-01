package com.smartconstruct.backend_core.controller;

import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.DemoSeedResult;
import com.smartconstruct.backend_core.dto.RegisterRequest;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.service.*;
import com.smartconstruct.backend_core.service.impl.DemoDataSeederService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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
    private final INodeDefService nodeDefService;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final DemoDataSeederService demoDataSeederService;

    public InitController(
            IDepartmentService departmentService,
            IMajorService majorService,
            IAdminClassService adminClassService,
            SysUserService sysUserService,
            IStudentService studentService,
            ITeacherService teacherService,
            INodeDefService nodeDefService,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate,
            DemoDataSeederService demoDataSeederService) {
        this.departmentService = departmentService;
        this.majorService = majorService;
        this.adminClassService = adminClassService;
        this.sysUserService = sysUserService;
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.nodeDefService = nodeDefService;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.demoDataSeederService = demoDataSeederService;
    }

    @PostMapping("/init")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> initSystemData() {
        migrateSchema();
        if (departmentService.count() > 0) {
            return ApiResult.ok("系统数据已初始化，无需重复操作");
        }

        LocalDateTime now = LocalDateTime.now();

        BizDepartment dept = new BizDepartment();
        dept.setDeptName("信息工程学院");
        dept.setCreatedAt(now);
        departmentService.save(dept);

        BizMajor major = new BizMajor();
        major.setDeptId(dept.getId());
        major.setMajorName("软件工程");
        major.setCreatedAt(now);
        majorService.save(major);

        BizAdminClass adminClass = new BizAdminClass();
        adminClass.setMajorId(major.getId());
        adminClass.setClassName("26软件工程1班");
        adminClass.setEnrollmentYear(2026);
        adminClass.setCreatedAt(now);
        adminClassService.save(adminClass);

        SysUser admin = new SysUser();
        admin.setUsername("user1");
        admin.setPasswordHash(passwordEncoder.encode("123456"));
        admin.setRoleType(1);
        admin.setStatus(0);
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        sysUserService.save(admin);

        RegisterRequest teacherReq = new RegisterRequest();
        teacherReq.setUsername("teacher");
        teacherReq.setPassword("123456");
        teacherReq.setRoleType(2);
        teacherReq.setRealName("李欣欣");
        teacherReq.setDeptId(dept.getId());
        sysUserService.registerUser(teacherReq);

        RegisterRequest studentReq = new RegisterRequest();
        studentReq.setUsername("student");
        studentReq.setPassword("123456");
        studentReq.setRoleType(3);
        studentReq.setRealName("张三");
        studentReq.setDeptId(dept.getId());
        studentReq.setMajorId(major.getId());
        studentReq.setClassId(adminClass.getId());
        sysUserService.registerUser(studentReq);

        seedNodeDefs(now);

        return ApiResult.ok("系统数据初始化完成：院系/专业/班级/管理员/教师/学生/编排节点");
    }

    @PostMapping("/demo/seed")
    public ApiResult<DemoSeedResult> seedDemoData() {
        try {
            DemoSeedResult result = demoDataSeederService.seedAll();
            return ApiResult.ok(result);
        } catch (RuntimeException e) {
            log.error("演示数据播种失败: {}", e.getMessage(), e);
            return ApiResult.error(400, e.getMessage());
        }
    }

    private void seedNodeDefs(LocalDateTime now) {
        String[][] nodes = {
            {"START", "开始实训"},
            {"END", "结束实训"},
            {"RESOURCE_READ", "资源阅读"},
            {"VIDEO_LEARN", "视频观看"},
            {"MINDMAP_PREVIEW", "导图预习"},
            {"SEMESTER_SURVEY", "学情调查"},
            {"MINDMAP_DRAW", "导图绘制"},
            {"AI_PRACTICE", "AI 对练"},
            {"THEORY_CLASS", "理论实训"},
            {"TASK_DISTRIBUTE", "任务下发"},
            {"REQ_UPLOAD", "需求上传"},
            {"PLAN_UPLOAD", "方案上传"},
            {"HOMEWORK", "课后作业"},
            {"CODING_CLASS", "编码实训"},
            {"STUDENT_PEER_REVIEW", "学生互评"},
            {"TEACHER_COMMENT", "教师点评"}
        };
        for (String[] n : nodes) {
            WfNodeDef def = new WfNodeDef();
            def.setNodeType(n[0]);
            def.setNodeName(n[1]);
            def.setIsActive(1);
            def.setCreatedAt(now);
            def.setUpdatedAt(now);
            nodeDefService.save(def);
        }
    }

    private void migrateSchema() {
        // Drop FK constraints
        String[] fkSqls = {
            "ALTER TABLE biz_student DROP FOREIGN KEY IF EXISTS fk_student_user",
            "ALTER TABLE biz_student DROP FOREIGN KEY IF EXISTS fk_student_dept",
            "ALTER TABLE biz_student DROP FOREIGN KEY IF EXISTS fk_student_major",
            "ALTER TABLE biz_student DROP FOREIGN KEY IF EXISTS fk_student_class",
            "ALTER TABLE biz_teacher DROP FOREIGN KEY IF EXISTS fk_teacher_user",
            "ALTER TABLE biz_teacher DROP FOREIGN KEY IF EXISTS fk_teacher_dept",
            "ALTER TABLE biz_course DROP FOREIGN KEY IF EXISTS fk_course_creator",
            "ALTER TABLE biz_student_course DROP FOREIGN KEY IF EXISTS fk_sc_student",
            "ALTER TABLE biz_student_course DROP FOREIGN KEY IF EXISTS fk_sc_course",
            "ALTER TABLE biz_question_bank DROP FOREIGN KEY IF EXISTS fk_qb_teacher",
            "ALTER TABLE biz_question DROP FOREIGN KEY IF EXISTS fk_q_bank",
            "ALTER TABLE biz_knowledge_point DROP FOREIGN KEY IF EXISTS fk_kp_course",
            "ALTER TABLE sys_resource DROP FOREIGN KEY IF EXISTS fk_resource_uploader",
            "ALTER TABLE biz_training_task DROP FOREIGN KEY IF EXISTS fk_tt_template",
            "ALTER TABLE biz_training_task DROP FOREIGN KEY IF EXISTS fk_tt_teacher",
            "ALTER TABLE biz_training_participation DROP FOREIGN KEY IF EXISTS fk_tp_task",
            "ALTER TABLE biz_training_participation DROP FOREIGN KEY IF EXISTS fk_tp_student",
            "ALTER TABLE biz_training_paper DROP FOREIGN KEY IF EXISTS fk_tp_task",
            "ALTER TABLE biz_training_paper DROP FOREIGN KEY IF EXISTS fk_tp_node",
            "ALTER TABLE biz_training_paper_question DROP FOREIGN KEY IF EXISTS fk_tpq_paper",
            "ALTER TABLE biz_training_paper_question DROP FOREIGN KEY IF EXISTS fk_tpq_question",
            "ALTER TABLE biz_training_upload DROP FOREIGN KEY IF EXISTS fk_tu_participation",
            "ALTER TABLE biz_training_upload DROP FOREIGN KEY IF EXISTS fk_tu_node",
            "ALTER TABLE biz_training_upload DROP FOREIGN KEY IF EXISTS fk_tu_resource",
            "ALTER TABLE biz_training_group DROP FOREIGN KEY IF EXISTS fk_tg_task",
            "ALTER TABLE biz_group_member DROP FOREIGN KEY IF EXISTS fk_gm_group",
            "ALTER TABLE biz_group_member DROP FOREIGN KEY IF EXISTS fk_gm_student",
            "ALTER TABLE wf_node_instance DROP FOREIGN KEY IF EXISTS fk_ni_task",
            "ALTER TABLE wf_node_instance DROP FOREIGN KEY IF EXISTS fk_ni_def",
            "ALTER TABLE wf_global_activity_state DROP FOREIGN KEY IF EXISTS fk_gas_task",
            "ALTER TABLE wf_global_activity_state DROP FOREIGN KEY IF EXISTS fk_gas_node",
            "ALTER TABLE wf_student_activity_state DROP FOREIGN KEY IF EXISTS fk_sas_participation",
            "ALTER TABLE wf_student_activity_state DROP FOREIGN KEY IF EXISTS fk_sas_node",
            "ALTER TABLE biz_mindmap_record DROP FOREIGN KEY IF EXISTS fk_mr_participation",
            "ALTER TABLE biz_mindmap_record DROP FOREIGN KEY IF EXISTS fk_mr_node",
            "ALTER TABLE biz_mindmap_eval_detail DROP FOREIGN KEY IF EXISTS fk_med_participation",
            "ALTER TABLE biz_mindmap_eval_detail DROP FOREIGN KEY IF EXISTS fk_med_kp",
            "ALTER TABLE biz_student_paper DROP FOREIGN KEY IF EXISTS fk_sp_paper",
            "ALTER TABLE biz_student_paper DROP FOREIGN KEY IF EXISTS fk_sp_participation",
            "ALTER TABLE biz_student_paper DROP FOREIGN KEY IF EXISTS fk_sp_student",
            "ALTER TABLE biz_student_answer_detail DROP FOREIGN KEY IF EXISTS fk_sad_student_paper",
            "ALTER TABLE biz_student_answer_detail DROP FOREIGN KEY IF EXISTS fk_sad_paper_question",
            "ALTER TABLE biz_ai_session DROP FOREIGN KEY IF EXISTS fk_ais_student",
            "ALTER TABLE biz_ai_session DROP FOREIGN KEY IF EXISTS IF EXISTS fk_ais_task",
            "ALTER TABLE biz_ai_session DROP FOREIGN KEY IF EXISTS fk_ais_node",
            "ALTER TABLE biz_ai_message DROP FOREIGN KEY IF EXISTS fk_aim_session",
            "ALTER TABLE wf_student_node_progress DROP FOREIGN KEY IF EXISTS fk_snp_participation",
            "ALTER TABLE wf_student_node_progress DROP FOREIGN KEY IF EXISTS fk_snp_node",
            "ALTER TABLE wf_teacher_node_operation DROP FOREIGN KEY IF EXISTS fk_tno_task",
            "ALTER TABLE wf_teacher_node_operation DROP FOREIGN KEY IF EXISTS fk_tno_node",
            "ALTER TABLE wf_teacher_node_operation DROP FOREIGN KEY IF EXISTS fk_tno_teacher",
            "ALTER TABLE biz_peer_review_assignment DROP FOREIGN KEY IF EXISTS fk_pra_node",
            "ALTER TABLE biz_peer_review_assignment DROP FOREIGN KEY IF EXISTS fk_pra_reviewer",
            "ALTER TABLE biz_peer_review_assignment DROP FOREIGN KEY IF EXISTS fk_pra_reviewee",
            "ALTER TABLE fact_eval_result DROP FOREIGN KEY IF EXISTS fk_fer_task",
            "ALTER TABLE fact_eval_result DROP FOREIGN KEY IF EXISTS fk_fer_indicator",
            "ALTER TABLE biz_student_achievement DROP FOREIGN KEY IF EXISTS fk_sa_student",
            "ALTER TABLE biz_student_achievement DROP FOREIGN KEY IF EXISTS fk_sa_achievement",
            "ALTER TABLE biz_student_achievement DROP FOREIGN KEY IF EXISTS fk_sa_task",
            "ALTER TABLE sys_ticket DROP FOREIGN KEY IF EXISTS fk_ticket_creator",
            "ALTER TABLE sys_feedback DROP FOREIGN KEY IF EXISTS fk_feedback_user",
            "ALTER TABLE sys_operation_log DROP FOREIGN KEY IF EXISTS fk_operation_log_user"
        };
        for (String sql : fkSqls) {
            executeIgnoreError(sql);
        }

        String[] dropCols = {
            "ALTER TABLE biz_teacher DROP INDEX IF EXISTS uk_employee_no",
            "ALTER TABLE biz_teacher DROP COLUMN IF EXISTS employee_no"
        };
        for (String sql : dropCols) {
            executeIgnoreError(sql);
        }

        String[] addCols = {
            "ALTER TABLE biz_department ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "ALTER TABLE biz_major ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "ALTER TABLE biz_admin_class ADD COLUMN enrollment_year INT NOT NULL DEFAULT 2026",
            "ALTER TABLE biz_admin_class ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "ALTER TABLE biz_student ADD COLUMN student_no VARCHAR(32) DEFAULT NULL",
            "ALTER TABLE biz_student ADD COLUMN real_name VARCHAR(64) NOT NULL DEFAULT ''",
            "ALTER TABLE biz_student ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "ALTER TABLE biz_student ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "ALTER TABLE biz_teacher ADD COLUMN real_name VARCHAR(64) NOT NULL DEFAULT ''",
            "ALTER TABLE biz_teacher ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "ALTER TABLE biz_teacher ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "ALTER TABLE wf_node_def ADD COLUMN node_type VARCHAR(32) NOT NULL DEFAULT ''",
            "ALTER TABLE wf_node_def ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "ALTER TABLE wf_node_def ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "ALTER TABLE wf_global_activity_state ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
            "ALTER TABLE wf_global_activity_state ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"
        };
        for (String sql : addCols) {
            executeIgnoreError(sql);
        }
        // Make teacher_id nullable
        executeIgnoreError("ALTER TABLE biz_course MODIFY COLUMN teacher_id BIGINT DEFAULT NULL");
        // Fill null creator_id in wf_training_template — set to teacher user
        executeIgnoreError("UPDATE wf_training_template SET creator_id = (SELECT user_id FROM biz_teacher LIMIT 1) WHERE creator_id IS NULL");
    }

    private void executeIgnoreError(String sql) {
        try {
            jdbcTemplate.execute(sql);
            log.info("Schema OK: {}", sql.substring(0, Math.min(sql.length(), 100)));
        } catch (Exception e) {
            log.debug("Schema skip: {}", e.getMessage());
        }
    }
}