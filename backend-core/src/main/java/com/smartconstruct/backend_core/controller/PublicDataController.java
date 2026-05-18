package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.entity.BizAdminClass;
import com.smartconstruct.backend_core.entity.BizDepartment;
import com.smartconstruct.backend_core.entity.BizMajor;
import com.smartconstruct.backend_core.entity.WfNodeDef;
import com.smartconstruct.backend_core.service.IAdminClassService;
import com.smartconstruct.backend_core.service.IDepartmentService;
import com.smartconstruct.backend_core.service.IMajorService;
import com.smartconstruct.backend_core.service.INodeDefService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicDataController {

    private final IDepartmentService departmentService;
    private final IMajorService majorService;
    private final IAdminClassService adminClassService;
    private final INodeDefService nodeDefService;

    public PublicDataController(IDepartmentService departmentService, IMajorService majorService, IAdminClassService adminClassService, INodeDefService nodeDefService) {
        this.departmentService = departmentService;
        this.majorService = majorService;
        this.adminClassService = adminClassService;
        this.nodeDefService = nodeDefService;
    }

    @GetMapping("/departments")
    public ApiResult<List<BizDepartment>> getDepartments() {
        return ApiResult.ok(departmentService.list());
    }

    @GetMapping("/majors")
    public ApiResult<List<BizMajor>> getMajors(@RequestParam Long deptId) {
        List<BizMajor> list = majorService.list(new LambdaQueryWrapper<BizMajor>().eq(BizMajor::getDeptId, deptId));
        return ApiResult.ok(list);
    }

    @GetMapping("/classes")
    public ApiResult<List<BizAdminClass>> getClasses(@RequestParam Long majorId) {
        List<BizAdminClass> list = adminClassService.list(new LambdaQueryWrapper<BizAdminClass>().eq(BizAdminClass::getMajorId, majorId));
        return ApiResult.ok(list);
    }

    @GetMapping("/active-nodes")
    public ApiResult<List<WfNodeDef>> getActiveNodes() {
        LambdaQueryWrapper<WfNodeDef> qw = new LambdaQueryWrapper<>();
        qw.eq(WfNodeDef::getIsActive, 1);
        qw.orderByAsc(WfNodeDef::getNodeType);
        return ApiResult.ok(nodeDefService.list(qw));
    }

    /** 初始化编排节点定义（可独立调用） */
    @PostMapping("/seed-nodes")
    public ApiResult<String> seedNodes() {
        if (nodeDefService.count() > 0) {
            return ApiResult.ok("节点数据已存在，跳过");
        }
        LocalDateTime now = LocalDateTime.now();
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
        return ApiResult.ok("已初始化 " + nodes.length + " 个编排节点");
    }
}