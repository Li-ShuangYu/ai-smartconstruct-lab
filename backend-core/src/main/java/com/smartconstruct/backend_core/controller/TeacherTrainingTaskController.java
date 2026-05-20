package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.service.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.smartconstruct.backend_core.util.Java8Compat;

/**
 * 教师实训任务控制器
 * 
 * 负责处理教师端实训任务下发相关的API请求，主要功能：
 * - 创建实训任务并下发给学生
 * - 根据下发范围（班级/课程）自动获取目标学生列表
 * - 批量创建学生参与记录
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/teacher/training-tasks")
public class TeacherTrainingTaskController {

    /** 实训任务服务 - 管理实训任务信息 */
    private final ITrainingTaskService trainingTaskService;
    
    /** 实训参与服务 - 管理学生参与记录 */
    private final ITrainingParticipationService participationService;
    
    /** 学生服务 - 查询学生信息 */
    private final IStudentService studentService;
    
    /** 学生选课服务 - 查询学生选课信息 */
    private final IStudentCourseService studentCourseService;
    
    /** 实训模板服务 - 查询模板信息 */
    private final ITrainingTemplateService templateService;

    /**
     * 构造函数 - 注入依赖服务
     * 
     * @param trainingTaskService 实训任务服务
     * @param participationService 实训参与服务
     * @param studentService 学生服务
     * @param studentCourseService 学生选课服务
     * @param templateService 实训模板服务
     */
    public TeacherTrainingTaskController(ITrainingTaskService trainingTaskService,
                                          ITrainingParticipationService participationService,
                                          IStudentService studentService,
                                          IStudentCourseService studentCourseService,
                                          ITrainingTemplateService templateService) {
        this.trainingTaskService = trainingTaskService;
        this.participationService = participationService;
        this.studentService = studentService;
        this.studentCourseService = studentCourseService;
        this.templateService = templateService;
    }

    /**
     * 获取当前登录教师ID
     * 
     * 从Spring Security上下文获取当前认证教师的ID
     * 
     * @return 当前教师ID
     */
    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    /**
     * 下发实训任务
     * 
     * 创建实训任务并批量创建学生参与记录，流程如下：
     * 1. 参数校验：检查模板ID、任务名称、下发范围等
     * 2. 模板验证：检查模板是否存在且AI处理完成
     * 3. 获取目标学生：根据下发范围（班级/课程）查询学生列表
     * 4. 创建任务：保存实训任务记录
     * 5. 批量创建参与记录：为每个学生创建参与记录
     * 
     * @param body 请求体，包含 templateId, taskName, dispatchScope, dispatchTargetId
     * @return 任务ID和学生数量
     */
    @OperationLog(action = "下发实训任务")
    @PostMapping
    public ApiResult<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Long teacherId = getCurrentUserId();
        Long templateId = body.get("templateId") != null ? Long.valueOf(body.get("templateId").toString()) : null;
        String taskName = (String) body.get("taskName");
        Integer dispatchScope = body.get("dispatchScope") != null ? Integer.valueOf(body.get("dispatchScope").toString()) : null;
        Long targetId = body.get("dispatchTargetId") != null ? Long.valueOf(body.get("dispatchTargetId").toString()) : null;

        if (templateId == null || taskName == null || Java8Compat.isBlank(taskName) || dispatchScope == null || targetId == null) {
            return ApiResult.error("参数不完整：需要 templateId, taskName, dispatchScope, dispatchTargetId");
        }

        WfTrainingTemplate template = templateService.getById(templateId);
        if (template == null || (template.getAiStatus() != null && template.getAiStatus() != 2)) {
            return ApiResult.error("模板不存在或尚未就绪");
        }

        // 根据下发范围获取学生列表
        List<Long> studentIds;
        if (dispatchScope == 1) {
            studentIds = studentService.list(new LambdaQueryWrapper<BizStudent>().eq(BizStudent::getClassId, targetId))
                    .stream().map(BizStudent::getUserId).collect(Collectors.toList());
        } else if (dispatchScope == 2) {
            studentIds = studentCourseService.list(new LambdaQueryWrapper<BizStudentCourse>().eq(BizStudentCourse::getCourseId, targetId))
                    .stream().map(BizStudentCourse::getStudentId).collect(Collectors.toList());
        } else {
            return ApiResult.error("无效的下发范围类型");
        }

        if (studentIds.isEmpty()) {
            return ApiResult.error("目标范围内没有学生");
        }

        BizTrainingTask task = new BizTrainingTask();
        task.setTemplateId(templateId);
        task.setTeacherId(teacherId);
        task.setTaskName(taskName);
        task.setStatus(0);
        task.setDispatchScope(dispatchScope);
        task.setDispatchTargetId(targetId);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        trainingTaskService.save(task);

        List<BizTrainingParticipation> participations = new ArrayList<>();
        for (Long sid : studentIds) {
            BizTrainingParticipation p = new BizTrainingParticipation();
            p.setTaskId(task.getId());
            p.setStudentId(sid);
            p.setStatus(0);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            participations.add(p);
        }
        participationService.saveBatch(participations);

        return ApiResult.ok(Java8Compat.mapOf("taskId", task.getId(), "studentCount", participations.size()));
    }
}
