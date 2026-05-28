package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.mapper.WfNodeInstanceMapper;
import com.smartconstruct.backend_core.mapper.WfStudentNodeProgressMapper;
import com.smartconstruct.backend_core.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher/training-tasks")
public class TeacherTrainingTaskController {

    private static final Logger log = LoggerFactory.getLogger(TeacherTrainingTaskController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ITrainingTaskService trainingTaskService;
    private final ITrainingParticipationService participationService;
    private final IStudentService studentService;
    private final IStudentCourseService studentCourseService;
    private final ITrainingTemplateService templateService;
    private final ICourseService courseService;
    private final IAdminClassService adminClassService;
    private final IGlobalActivityStateService globalStateService;
    private final WfNodeInstanceMapper nodeInstanceMapper;
    private final WfStudentNodeProgressMapper studentNodeProgressMapper;

    public TeacherTrainingTaskController(
            ITrainingTaskService trainingTaskService,
            ITrainingParticipationService participationService,
            IStudentService studentService,
            IStudentCourseService studentCourseService,
            ITrainingTemplateService templateService,
            ICourseService courseService,
            IAdminClassService adminClassService,
            IGlobalActivityStateService globalStateService,
            WfNodeInstanceMapper nodeInstanceMapper,
            WfStudentNodeProgressMapper studentNodeProgressMapper) {
        this.trainingTaskService = trainingTaskService;
        this.participationService = participationService;
        this.studentService = studentService;
        this.studentCourseService = studentCourseService;
        this.templateService = templateService;
        this.courseService = courseService;
        this.adminClassService = adminClassService;
        this.globalStateService = globalStateService;
        this.nodeInstanceMapper = nodeInstanceMapper;
        this.studentNodeProgressMapper = studentNodeProgressMapper;
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

        // Step 3: Student distribution based on dispatch_scope
        List<Long> studentIds;
        if (dispatchScope == 1) {
            studentIds = studentService.list(new LambdaQueryWrapper<BizStudent>().eq(BizStudent::getClassId, targetId))
                    .stream().map(BizStudent::getUserId).collect(Collectors.toList());
        } else if (dispatchScope == 2) {
            studentIds = studentCourseService.list(new LambdaQueryWrapper<BizStudentCourse>().eq(BizStudentCourse::getCourseId, targetId))
                    .stream().map(BizStudentCourse::getStudentId).collect(Collectors.toList());
        } else return ApiResult.error("无效的下发范围类型");

        if (studentIds.isEmpty()) return ApiResult.error("目标范围内无学生");

        // Step 1: Create biz_training_task
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

        // Step 2: Create wf_node_instance records from standard_payload_json
        List<WfNodeInstance> nodeInstances = createNodeInstances(task.getId(), template, now);

        // Step 3: Create biz_training_participation records
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

        // Step 4: Create initial wf_student_node_progress for each student × each node
        createInitialProgress(participations, nodeInstances, now);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", String.valueOf(task.getId()));
        result.put("studentCount", participations.size());
        result.put("nodeCount", nodeInstances.size());
        return ApiResult.ok(result);
    }

    /**
     * Step 2: Create wf_node_instance records for each node in each phase.
     * Reads phases from template's phases_json and nodes from standard_payload_json.
     */
    private List<WfNodeInstance> createNodeInstances(Long taskId, WfTrainingTemplate template, LocalDateTime now) {
        List<WfNodeInstance> instances = new ArrayList<>();

        // Parse phases_json for phase definitions
        List<JsonNode> phases = parsePhasesJson(template.getPhasesJson());

        // Parse standard_payload_json for node configs per phase
        JsonNode payloadRoot = parseJsonObject(template.getStandardPayloadJson());

        // If standard_payload_json has a "phases" array with nodes, use it
        JsonNode payloadPhases = payloadRoot != null ? payloadRoot.get("phases") : null;

        if (payloadPhases != null && payloadPhases.isArray()) {
            // standard_payload_json contains phased node data
            int globalSort = 0;
            for (JsonNode payloadPhase : payloadPhases) {
                String phaseId = payloadPhase.has("phase_id") ? payloadPhase.get("phase_id").asText() : null;
                JsonNode nodes = payloadPhase.get("nodes");
                if (nodes == null || !nodes.isArray()) continue;

                for (JsonNode node : nodes) {
                    WfNodeInstance instance = buildNodeInstance(taskId, phaseId, node, globalSort++, now);
                    nodeInstanceMapper.insert(instance);
                    instances.add(instance);
                }
            }
        } else if (!phases.isEmpty()) {
            // Fallback: use raw_canvas_json structure with phases
            JsonNode canvasRoot = parseJsonObject(template.getRawCanvasJson());
            JsonNode canvasPhases = canvasRoot != null ? canvasRoot.get("phases") : null;
            if (canvasPhases != null && canvasPhases.isArray()) {
                int globalSort = 0;
                for (JsonNode canvasPhase : canvasPhases) {
                    String phaseId = canvasPhase.has("phase_id") ? canvasPhase.get("phase_id").asText() : null;
                    JsonNode nodes = canvasPhase.get("nodes");
                    if (nodes == null || !nodes.isArray()) continue;

                    for (JsonNode node : nodes) {
                        WfNodeInstance instance = buildNodeInstance(taskId, phaseId, node, globalSort++, now);
                        nodeInstanceMapper.insert(instance);
                        instances.add(instance);
                    }
                }
            }
        }

        return instances;
    }

    /**
     * Build a single WfNodeInstance from a JSON node definition.
     */
    private WfNodeInstance buildNodeInstance(Long taskId, String phaseId, JsonNode node, int sortNum, LocalDateTime now) {
        WfNodeInstance instance = new WfNodeInstance();
        instance.setTaskId(taskId);
        instance.setPhaseId(phaseId);
        instance.setNodeType(node.has("node_type") ? node.get("node_type").asText() : null);
        instance.setNodeName(node.has("node_name") ? node.get("node_name").asText() :
                (node.has("nodeName") ? node.get("nodeName").asText() : null));
        instance.setSortNum(node.has("sort_num") ? node.get("sort_num").asInt(sortNum) : sortNum);
        instance.setStatus(0);
        instance.setCreatedAt(now);
        instance.setUpdatedAt(now);

        // Use config from standard_payload_json (AI-enriched) or raw config
        JsonNode config = node.get("config");
        if (config == null) config = node.get("config_json");
        if (config != null) {
            try {
                instance.setConfigJson(objectMapper.treeToValue(config, Object.class));
            } catch (Exception e) {
                log.warn("Failed to parse node config for node_type={}", instance.getNodeType(), e);
            }
        }

        // Set nodeDefId if available
        if (node.has("node_def_id")) {
            try {
                instance.setNodeDefId(node.get("node_def_id").asLong());
            } catch (Exception ignored) {}
        }

        return instance;
    }

    /**
     * Step 4: Create initial wf_student_node_progress (status=0) for each student × each node.
     */
    private void createInitialProgress(List<BizTrainingParticipation> participations,
                                       List<WfNodeInstance> nodeInstances, LocalDateTime now) {
        if (participations.isEmpty() || nodeInstances.isEmpty()) return;

        List<WfStudentNodeProgress> progressRecords = new ArrayList<>();
        for (BizTrainingParticipation participation : participations) {
            for (WfNodeInstance nodeInstance : nodeInstances) {
                WfStudentNodeProgress progress = new WfStudentNodeProgress();
                progress.setParticipationId(participation.getId());
                progress.setNodeInstanceId(nodeInstance.getId());
                progress.setStatus(0); // 未开始
                progress.setIsForcedComplete(0);
                progress.setCreatedAt(now);
                progress.setUpdatedAt(now);
                progressRecords.add(progress);
            }
        }

        // Batch insert in chunks of 500 to avoid oversized SQL
        int batchSize = 500;
        for (int i = 0; i < progressRecords.size(); i += batchSize) {
            int end = Math.min(i + batchSize, progressRecords.size());
            List<WfStudentNodeProgress> batch = progressRecords.subList(i, end);
            for (WfStudentNodeProgress p : batch) {
                studentNodeProgressMapper.insert(p);
            }
        }
        log.info("Created {} initial progress records for task", progressRecords.size());
    }

    /**
     * Parse phases_json (Object or String) into a sorted list of JsonNode.
     */
    private List<JsonNode> parsePhasesJson(Object phasesJson) {
        if (phasesJson == null) return new ArrayList<>();
        try {
            JsonNode arrayNode;
            if (phasesJson instanceof String) {
                arrayNode = objectMapper.readTree((String) phasesJson);
            } else {
                String jsonStr = objectMapper.writeValueAsString(phasesJson);
                arrayNode = objectMapper.readTree(jsonStr);
            }
            if (!arrayNode.isArray()) return new ArrayList<>();

            List<JsonNode> phases = new ArrayList<>();
            for (JsonNode node : arrayNode) {
                phases.add(node);
            }
            phases.sort(Comparator.comparingInt(n ->
                    n.has("sort_num") ? n.get("sort_num").asInt(Integer.MAX_VALUE) : Integer.MAX_VALUE));
            return phases;
        } catch (Exception e) {
            log.error("Failed to parse phases_json: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Parse a JSON object from Object (could be String, Map, etc.)
     */
    private JsonNode parseJsonObject(Object json) {
        if (json == null) return null;
        try {
            if (json instanceof String) {
                return objectMapper.readTree((String) json);
            } else {
                String jsonStr = objectMapper.writeValueAsString(json);
                return objectMapper.readTree(jsonStr);
            }
        } catch (Exception e) {
            log.error("Failed to parse JSON object: {}", e.getMessage());
            return null;
        }
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