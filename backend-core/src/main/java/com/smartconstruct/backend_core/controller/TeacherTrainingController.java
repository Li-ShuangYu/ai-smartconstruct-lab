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

import com.smartconstruct.backend_core.util.Java8Compat;

/**
 * 教师实训控制器
 * 
 * 负责处理教师端课堂实训相关的API请求，主要功能：
 * - 推进课中实训节点（同步全局状态 + WebSocket广播）
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/teacher/training")
public class TeacherTrainingController {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(TeacherTrainingController.class);
    
    /** JSON 对象映射器 */
    private static final ObjectMapper om = new ObjectMapper();

    /** 实训任务服务 - 管理实训任务信息 */
    private final ITrainingTaskService trainingTaskService;
    
    /** 全局活动状态服务 - 管理课堂实训的全局节点状态 */
    private final IGlobalActivityStateService globalStateService;
    
    /** WebSocket 处理器 - 用于向学生端广播节点变更消息 */
    private final TrainingWebSocketHandler wsHandler;

    /**
     * 构造函数 - 注入依赖服务
     * 
     * @param trainingTaskService 实训任务服务
     * @param globalStateService 全局活动状态服务
     * @param wsHandler WebSocket处理器
     */
    public TeacherTrainingController(ITrainingTaskService trainingTaskService,
                                      IGlobalActivityStateService globalStateService,
                                      TrainingWebSocketHandler wsHandler) {
        this.trainingTaskService = trainingTaskService;
        this.globalStateService = globalStateService;
        this.wsHandler = wsHandler;
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
     * 教师推进课中实训节点
     * 
     * 将课堂实训推进到下一个节点，包含以下步骤：
     * 1. 权限校验：验证教师是否有权操作该实训任务
     * 2. 状态更新：写入/更新全局活动状态表 (wf_global_activity_state)
     * 3. WebSocket广播：将节点变更消息广播给该任务房间内的所有学生
     * 
     * @param taskId 实训任务ID
     * @param body 请求体，包含 nextNodeId（下一节点ID）
     * @return 任务ID和当前节点ID
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
        if (nextNodeId == null || Java8Compat.isBlank(nextNodeId)) return ApiResult.error("缺少 nextNodeId");

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

        return ApiResult.ok(Java8Compat.mapOf("taskId", taskId, "currentNodeId", nextNodeId));
    }
}
