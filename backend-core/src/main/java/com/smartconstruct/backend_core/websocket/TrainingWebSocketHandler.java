package com.smartconstruct.backend_core.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.service.SysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实训 WebSocket 处理器
 * 
 * 负责管理课堂实训的实时通信，支持：
 * - 学生/教师连接管理（按实训任务分组）
 * - 节点变更消息广播（教师推进节点时通知所有学生）
 * - 心跳回执处理
 * 
 * 连接采用双层Map结构管理：taskId -> (userId -> session)
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@Component
public class TrainingWebSocketHandler extends TextWebSocketHandler {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(TrainingWebSocketHandler.class);
    
    /** JSON 对象映射器 */
    private static final ObjectMapper om = new ObjectMapper();

    /** 用户服务 - 用于验证连接用户身份 */
    private final SysUserService sysUserService;

    /** 
     * 会话管理映射表
     * 结构：taskId -> (userId -> WebSocketSession)
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private final Map<Long, Map<Long, WebSocketSession>> taskSessions = new ConcurrentHashMap<>();

    /**
     * 构造函数 - 注入用户服务
     * 
     * @param sysUserService 用户服务
     */
    public TrainingWebSocketHandler(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 连接建立后处理
     * 
     * 验证用户身份并将会话加入对应任务房间。
     * 
     * @param session WebSocket会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 从握手拦截器获取 taskId 和 username
        Long taskId = (Long) session.getAttributes().get("taskId");
        String username = (String) session.getAttributes().get("username");
        
        // 参数校验
        if (taskId == null || username == null) {
            try { session.close(); } catch (IOException ignored) {}
            return;
        }

        // 验证用户身份
        SysUser user = sysUserService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username));
        if (user == null) {
            try { session.close(); } catch (IOException ignored) {}
            return;
        }

        // 将会话加入任务房间
        session.getAttributes().put("userId", user.getId());
        taskSessions.computeIfAbsent(taskId, k -> new ConcurrentHashMap<>()).put(user.getId(), session);
        
        log.info("WebSocket 连接建立: taskId={}, userId={}, username={}", taskId, user.getId(), username);
    }

    /**
     * 处理客户端上行消息
     * 
     * 目前仅处理心跳消息，返回 PONG 回执。
     * 
     * @param session WebSocket会话
     * @param message 接收到的消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端上行消息暂不做处理，仅做心跳回执
        try {
            session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
        } catch (IOException ignored) {}
    }

    /**
     * 连接关闭后清理
     * 
     * 从任务房间中移除断开的会话。
     * 
     * @param session WebSocket会话
     * @param status 关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long taskId = (Long) session.getAttributes().get("taskId");
        Long userId = (Long) session.getAttributes().get("userId");
        
        if (taskId != null && userId != null) {
            Map<Long, WebSocketSession> room = taskSessions.get(taskId);
            if (room != null) {
                room.remove(userId);
                // 如果房间为空，移除房间
                if (room.isEmpty()) taskSessions.remove(taskId);
            }
            log.info("WebSocket 断开: taskId={}, userId={}", taskId, userId);
        }
    }

    /**
     * 处理传输错误
     * 
     * 记录错误日志并触发连接关闭处理。
     * 
     * @param session WebSocket会话
     * @param exception 异常信息
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket 传输异常: {}", exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    /**
     * 向指定实训任务的所有连接客户端广播消息
     * 
     * 发送失败时自动移除失效 Session，不会因单个客户端断网导致整体广播中断。
     * 使用 synchronized 确保同一会话的并发安全。
     * 
     * @param taskId 实训任务ID
     * @param message 要广播的消息（JSON格式字符串）
     */
    public void broadcastToTask(Long taskId, String message) {
        Map<Long, WebSocketSession> room = taskSessions.get(taskId);
        if (room == null || room.isEmpty()) return;

        TextMessage tm = new TextMessage(message);
        
        for (Map.Entry<Long, WebSocketSession> entry : room.entrySet()) {
            WebSocketSession s = entry.getValue();
            if (s.isOpen()) {
                try {
                    synchronized (s) {
                        s.sendMessage(tm);
                    }
                } catch (IOException | IllegalStateException e) {
                    log.warn("广播发送失败，移除 session: userId={}", entry.getKey());
                    room.remove(entry.getKey());
                }
            } else {
                room.remove(entry.getKey());
            }
        }
        
        // 如果房间为空，清理房间
        if (room.isEmpty()) taskSessions.remove(taskId);
    }
}
