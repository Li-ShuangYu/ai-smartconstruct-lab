package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.service.*;
import com.smartconstruct.backend_core.websocket.TrainingWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/teacher/training")
public class TeacherTrainingController {

    private static final Logger log = LoggerFactory.getLogger(TeacherTrainingController.class);
    private static final ObjectMapper om = new ObjectMapper();

    private final ITrainingTaskService trainingTaskService;
    private final IGlobalActivityStateService globalStateService;
    private final TrainingWebSocketHandler wsHandler;

    public TeacherTrainingController(ITrainingTaskService trainingTaskService,
                                      IGlobalActivityStateService globalStateService,
                                      TrainingWebSocketHandler wsHandler) {
        this.trainingTaskService = trainingTaskService;
        this.globalStateService = globalStateService;
        this.wsHandler = wsHandler;
    }

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    /**
     * 教师推进课中实训节点（写入全局状态 + WebSocket 广播）
     */
    @OperationLog(action = "教师推进课中实训节点")
    @PostMapping("/{taskId}/next-node")
    public ApiResult<Map<String, Object>> nextNode(@PathVariable Long taskId,
                                                    @RequestBody Map<String, Object> body) {
        Long teacherId = getCurrentUserId();

        // 1. 权限校验
        BizTrainingTask task = trainingTaskService.getById(taskId);
        if (task == null) return ApiResult.error("实训任务不存在");
        if (!teacherId.equals(task.getTeacherId())) return ApiResult.error("无权操作该实训任务");

        String nextNodeId = body.get("nextNodeId") != null ? body.get("nextNodeId").toString() : null;
        if (nextNodeId == null || nextNodeId.isBlank()) return ApiResult.error("缺少 nextNodeId");

        // 2. 写入/更新 MySQL wf_global_activity_state
        WfGlobalActivityState state = globalStateService.getOne(
                new LambdaQueryWrapper<WfGlobalActivityState>().eq(WfGlobalActivityState::getTaskId, taskId));
        if (state == null) {
            state = new WfGlobalActivityState();
            state.setTaskId(taskId);
            state.setCurrentNodeId(nextNodeId);
            state.setUpdatedAt(LocalDateTime.now());
            globalStateService.save(state);
        } else {
            state.setCurrentNodeId(nextNodeId);
            state.setUpdatedAt(LocalDateTime.now());
            globalStateService.updateById(state);
        }

        // 3. 构建广播消息
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "STAGE_CHANGED");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", taskId);
        payload.put("nextNodeId", nextNodeId);
        msg.put("payload", payload);

        try {
            String json = om.writeValueAsString(msg);
            // 4. 广播给该 taskId 房间内的所有 WebSocket 连接
            wsHandler.broadcastToTask(taskId, json);
            log.info("教师推进节点并广播: taskId={}, nextNodeId={}", taskId, nextNodeId);
        } catch (Exception e) {
            log.error("广播失败 taskId={}", taskId, e);
            return ApiResult.error("广播失败: " + e.getMessage());
        }

        return ApiResult.ok(Map.of("taskId", taskId, "currentNodeId", nextNodeId));
    }
}
