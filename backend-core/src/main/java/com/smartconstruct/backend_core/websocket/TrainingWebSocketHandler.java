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

@Component
public class TrainingWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(TrainingWebSocketHandler.class);
    private static final ObjectMapper om = new ObjectMapper();

    private final SysUserService sysUserService;

    /** 双层 Map：taskId -> (userId -> session) */
    private final Map<Long, Map<Long, WebSocketSession>> taskSessions = new ConcurrentHashMap<>();

    public TrainingWebSocketHandler(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long taskId = (Long) session.getAttributes().get("taskId");
        String username = (String) session.getAttributes().get("username");
        if (taskId == null || username == null) {
            try { session.close(); } catch (IOException ignored) {}
            return;
        }

        SysUser user = sysUserService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username));
        if (user == null) {
            try { session.close(); } catch (IOException ignored) {}
            return;
        }

        session.getAttributes().put("userId", user.getId());
        taskSessions.computeIfAbsent(taskId, k -> new ConcurrentHashMap<>()).put(user.getId(), session);
        log.info("WebSocket 连接建立: taskId={}, userId={}, username={}", taskId, user.getId(), username);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端上行消息暂不做处理，仅做心跳回执
        try {
            session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
        } catch (IOException ignored) {}
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long taskId = (Long) session.getAttributes().get("taskId");
        Long userId = (Long) session.getAttributes().get("userId");
        if (taskId != null && userId != null) {
            Map<Long, WebSocketSession> room = taskSessions.get(taskId);
            if (room != null) {
                room.remove(userId);
                if (room.isEmpty()) taskSessions.remove(taskId);
            }
            log.info("WebSocket 断开: taskId={}, userId={}", taskId, userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket 传输异常: {}", exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    /**
     * 向指定 taskId 房间内所有成员广播消息。
     * 发送失败时自动移除失效 Session，不会因单个客户端断网导致整体广播中断。
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
        if (room.isEmpty()) taskSessions.remove(taskId);
    }
}
