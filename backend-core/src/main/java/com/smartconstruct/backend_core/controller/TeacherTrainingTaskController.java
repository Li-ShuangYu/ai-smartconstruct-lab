package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.service.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher/training-tasks")
public class TeacherTrainingTaskController {

    private final ITrainingTaskService trainingTaskService;
    private final ITrainingParticipationService participationService;
    private final IStudentService studentService;
    private final IStudentCourseService studentCourseService;
    private final ITrainingTemplateService templateService;
    private final ICourseService courseService;
    private final IAdminClassService adminClassService;
    private final IGlobalActivityStateService globalStateService;

    public TeacherTrainingTaskController(
            ITrainingTaskService trainingTaskService,
            ITrainingParticipationService participationService,
            IStudentService studentService,
            IStudentCourseService studentCourseService,
            ITrainingTemplateService templateService,
            ICourseService courseService,
            IAdminClassService adminClassService,
            IGlobalActivityStateService globalStateService) {
        this.trainingTaskService = trainingTaskService;
        this.participationService = participationService;
        this.studentService = studentService;
        this.studentCourseService = studentCourseService;
        this.templateService = templateService;
        this.courseService = courseService;
        this.adminClassService = adminClassService;
        this.globalStateService = globalStateService;
    }

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    private String getDispatchTargetName(BizTrainingTask task) {
        if (task.getDispatchScope() == 1) {
            BizAdminClass c = adminClassService.getById(task.getDispatchTargetId());
            return c != null ? c.getClassName() : "-";
        } else if (task.getDispatchScope() == 2) {
            BizCourse c = courseService.getById(task.getDispatchTargetId());
            return c != null ? c.getCourseName() : "-";
        }
        return "-";
    }

    @GetMapping("/classes")
    public ApiResult<List<BizAdminClass>> getClasses() {
        return ApiResult.ok(adminClassService.list());
    }

    @GetMapping("/courses")
    public ApiResult<List<BizCourse>> getCourses() {
        Long teacherId = getCurrentUserId();
        return ApiResult.ok(courseService.list(new LambdaQueryWrapper<BizCourse>().eq(BizCourse::getTeacherId, teacherId)));
    }

    @GetMapping
    public ApiResult<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        Long teacherId = getCurrentUserId();
        LambdaQueryWrapper<BizTrainingTask> qw = new LambdaQueryWrapper<>();
        qw.eq(BizTrainingTask::getTeacherId, teacherId);
        if (status != null) qw.eq(BizTrainingTask::getStatus, status);
        qw.orderByDesc(BizTrainingTask::getCreatedAt);
        Page<BizTrainingTask> p = trainingTaskService.page(new Page<>(page, pageSize), qw);

        Set<Long> templateIds = p.getRecords().stream()
                .filter(t -> t.getTemplateId() != null)
                .map(BizTrainingTask::getTemplateId).collect(Collectors.toSet());
        Map<Long, WfTrainingTemplate> tplMap = new HashMap<>();
        if (!templateIds.isEmpty()) {
            tplMap = templateService.listByIds(templateIds).stream()
                    .collect(Collectors.toMap(WfTrainingTemplate::getId, t -> t));
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (BizTrainingTask t : p.getRecords()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", String.valueOf(t.getId()));
            m.put("taskName", t.getTaskName());
            WfTrainingTemplate tpl = tplMap.get(t.getTemplateId());
            m.put("templateName", tpl != null ? tpl.getTemplateName() : "-");
            m.put("dispatchScope", t.getDispatchScope());
            m.put("dispatchTargetName", getDispatchTargetName(t));
            m.put("isInClass", t.getIsInClass());
            m.put("startTime", t.getStartTime());
            m.put("endTime", t.getEndTime());
            m.put("status", t.getStatus());
            m.put("createdAt", t.getCreatedAt());
            records.add(m);
        }
        return ApiResult.ok(new PageResult<>(records, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @OperationLog(action = "下发实训任务")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Long teacherId = getCurrentUserId();
        String templateIdStr = (String) body.get("templateId");
        String taskName = (String) body.get("taskName");
        Integer dispatchScope = body.get("dispatchScope") != null ? Integer.valueOf(body.get("dispatchScope").toString()) : null;
        String targetIdStr = (String) body.get("dispatchTargetId");
        Integer isInClass = body.get("isInClass") != null ? Integer.valueOf(body.get("isInClass").toString()) : 0;
        String startTimeStr = (String) body.get("startTime");
        String endTimeStr = (String) body.get("endTime");

        if (templateIdStr == null || taskName == null || taskName.trim().isEmpty() || dispatchScope == null || targetIdStr == null || startTimeStr == null || endTimeStr == null) {
            return ApiResult.error("参数不完整");
        }

        Long templateId = Long.parseLong(templateIdStr);
        Long targetId = Long.parseLong(targetIdStr);
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr.substring(0, 19));
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr.substring(0, 19));
        if (!endTime.isAfter(startTime)) return ApiResult.error("结束时间必须晚于开始时间");

        WfTrainingTemplate template = templateService.getById(templateId);
        if (template == null || template.getAiStatus() == null || template.getAiStatus() != 2)
            return ApiResult.error("模板不存在或尚未就绪");

        List<Long> studentIds;
        if (dispatchScope == 1) {
            studentIds = studentService.list(new LambdaQueryWrapper<BizStudent>().eq(BizStudent::getClassId, targetId))
                    .stream().map(BizStudent::getUserId).collect(Collectors.toList());
        } else if (dispatchScope == 2) {
            studentIds = studentCourseService.list(new LambdaQueryWrapper<BizStudentCourse>().eq(BizStudentCourse::getCourseId, targetId))
                    .stream().map(BizStudentCourse::getStudentId).collect(Collectors.toList());
        } else return ApiResult.error("无效的下发范围类型");

        if (studentIds.isEmpty()) return ApiResult.error("目标范围内无学生");

        BizTrainingTask task = new BizTrainingTask();
        task.setTemplateId(templateId);
        task.setTeacherId(teacherId);
        task.setTaskName(taskName);
        task.setStartTime(startTime);
        task.setEndTime(endTime);
        task.setIsInClass(isInClass);
        task.setStatus(0);
        task.setDispatchScope(dispatchScope);
        task.setDispatchTargetId(targetId);
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        trainingTaskService.save(task);

        List<BizTrainingParticipation> participations = studentIds.stream().map(sid -> {
            BizTrainingParticipation p = new BizTrainingParticipation();
            p.setTaskId(task.getId());
            p.setStudentId(sid);
            p.setStatus(0);
            p.setCreatedAt(now);
            p.setUpdatedAt(now);
            return p;
        }).collect(Collectors.toList());
        participationService.saveBatch(participations);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", String.valueOf(task.getId()));
        result.put("studentCount", participations.size());
        return ApiResult.ok(result);
    }

    @OperationLog(action = "开始实训")
    @PostMapping("/{id}/start")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> startTask(@PathVariable String id) {
        Long taskId = Long.parseLong(id);
        BizTrainingTask task = trainingTaskService.getById(taskId);
        if (task == null) return ApiResult.error("任务不存在");
        if (task.getStatus() != 0) return ApiResult.error("只有未开始的实训可以启动");

        task.setStatus(1);
        task.setUpdatedAt(LocalDateTime.now());
        trainingTaskService.updateById(task);

        if (task.getIsInClass() != null && task.getIsInClass() == 1) {
            WfGlobalActivityState state = globalStateService.getOne(
                    new LambdaQueryWrapper<WfGlobalActivityState>().eq(WfGlobalActivityState::getTaskId, taskId));
            if (state == null) {
                state = new WfGlobalActivityState();
                state.setTaskId(taskId);
                state.setCurrentNodeId(null);
                state.setCreatedAt(LocalDateTime.now());
                state.setUpdatedAt(LocalDateTime.now());
                globalStateService.save(state);
            }
        }
        return ApiResult.ok();
    }

    @OperationLog(action = "重新下发实训")
    @PostMapping("/{id}/re-dispatch")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Map<String, Object>> reDispatch(@PathVariable String id) {
        Long taskId = Long.parseLong(id);
        BizTrainingTask task = trainingTaskService.getById(taskId);
        if (task == null) return ApiResult.error("任务不存在");
        if (task.getStatus() != 0) return ApiResult.error("只有未开始的实训可以重新下发");

        List<Long> currentStudentIds;
        if (task.getDispatchScope() == 1) {
            currentStudentIds = studentService.list(new LambdaQueryWrapper<BizStudent>().eq(BizStudent::getClassId, task.getDispatchTargetId()))
                    .stream().map(BizStudent::getUserId).collect(Collectors.toList());
        } else if (task.getDispatchScope() == 2) {
            currentStudentIds = studentCourseService.list(new LambdaQueryWrapper<BizStudentCourse>().eq(BizStudentCourse::getCourseId, task.getDispatchTargetId()))
                    .stream().map(BizStudentCourse::getStudentId).collect(Collectors.toList());
        } else return ApiResult.error("无效的下发范围");

        Set<Long> existingIds = participationService.list(new LambdaQueryWrapper<BizTrainingParticipation>()
                .eq(BizTrainingParticipation::getTaskId, taskId))
                .stream().map(BizTrainingParticipation::getStudentId).collect(Collectors.toSet());

        List<Long> newStudentIds = currentStudentIds.stream().filter(sid -> !existingIds.contains(sid)).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("newCount", newStudentIds.size());
        if (newStudentIds.isEmpty()) return ApiResult.ok(result);

        LocalDateTime now = LocalDateTime.now();
        List<BizTrainingParticipation> newParts = newStudentIds.stream().map(sid -> {
            BizTrainingParticipation p = new BizTrainingParticipation();
            p.setTaskId(taskId);
            p.setStudentId(sid);
            p.setStatus(0);
            p.setCreatedAt(now);
            p.setUpdatedAt(now);
            return p;
        }).collect(Collectors.toList());
        participationService.saveBatch(newParts);

        return ApiResult.ok(result);
    }

    @OperationLog(action = "编辑实训任务")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Long taskId = Long.parseLong(id);
        BizTrainingTask task = trainingTaskService.getById(taskId);
        if (task == null) return ApiResult.error("任务不存在");
        if (task.getStatus() != 0) return ApiResult.error("只有未开始的实训可以编辑");

        String taskName = (String) body.get("taskName");
        if (taskName != null && !taskName.trim().isEmpty()) task.setTaskName(taskName);
        task.setUpdatedAt(LocalDateTime.now());
        trainingTaskService.updateById(task);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除实训任务")
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> delete(@PathVariable String id) {
        Long taskId = Long.parseLong(id);
        participationService.remove(new LambdaQueryWrapper<BizTrainingParticipation>().eq(BizTrainingParticipation::getTaskId, taskId));
        trainingTaskService.removeById(taskId);
        return ApiResult.ok();
    }
}