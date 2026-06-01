package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.dto.AiChatRequest;
import com.smartconstruct.backend_core.dto.AiChatResponse;
import com.smartconstruct.backend_core.dto.AiSessionVO;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.entity.BizAiMessage;
import com.smartconstruct.backend_core.entity.BizAiSession;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.service.IAiMessageService;
import com.smartconstruct.backend_core.service.IAiSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生 AI 对话控制器
 *
 * 提供 AI 悬浮窗对话功能：
 * - POST /api/student/ai/chat       — 发送消息，获取 AI 回复
 * - GET  /api/student/ai/sessions   — 获取历史会话列表
 * - GET  /api/student/ai/sessions/{sessionId} — 获取会话详情（含消息）
 * - DELETE /api/student/ai/sessions/{sessionId} — 软删除会话
 *
 * @author SmartConstruct
 */
@RestController
@RequestMapping("/api/student/ai")
public class StudentAiChatController {

    private static final Logger log = LoggerFactory.getLogger(StudentAiChatController.class);

    private final IAiSessionService aiSessionService;
    private final IAiMessageService aiMessageService;

    @Value("${ai-engine.base-url:http://localhost:8000}")
    private String aiEngineBaseUrl;

    @Value("${ai-engine.service-token:}")
    private String serviceToken;

    private WebClient webClient;

    public StudentAiChatController(IAiSessionService aiSessionService,
                                   IAiMessageService aiMessageService) {
        this.aiSessionService = aiSessionService;
        this.aiMessageService = aiMessageService;
    }

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(aiEngineBaseUrl)
                .defaultHeader("X-Service-Token", serviceToken)
                .build();
    }

    // ─── 获取当前用户 ──────────────────────────────────────────────────────────

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    // ─── 发送消息 ──────────────────────────────────────────────────────────────

    /**
     * 发送消息给 AI 助教
     *
     * 流程：
     * 1. 若 sessionId 为空，创建新会话
     * 2. 持久化用户消息
     * 3. 构建历史上下文（最近 10 条消息）
     * 4. 调用 AI 引擎 /api/chat 获取回复
     * 5. 持久化 AI 回复消息
     * 6. 返回 AI 回复内容和会话 ID
     */
    @PostMapping("/chat")
    public ApiResult<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ApiResult.error("消息内容不能为空");
        }

        Long studentId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        // 1. 获取或创建会话
        BizAiSession session = getOrCreateSession(request, studentId, now);

        // 2. 持久化用户消息
        BizAiMessage userMsg = new BizAiMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(request.getContent().trim());
        userMsg.setCreatedAt(now);
        aiMessageService.save(userMsg);

        // 3. 构建历史上下文（最近 10 条，不含刚保存的这条）
        List<Map<String, String>> history = buildHistory(session.getId(), userMsg.getId());

        // 4. 调用 AI 引擎
        Map<String, Object> aiReply = callAiEngine(request.getContent().trim(), history, session);

        String replyContent = (String) aiReply.getOrDefault("content", "抱歉，AI 助教暂时无法响应，请稍后再试。");
        Integer tokensUsed = aiReply.containsKey("tokens_used")
                ? ((Number) aiReply.get("tokens_used")).intValue()
                : null;

        // 5. 持久化 AI 回复
        BizAiMessage assistantMsg = new BizAiMessage();
        assistantMsg.setSessionId(session.getId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(replyContent);
        assistantMsg.setTokensUsed(tokensUsed);
        assistantMsg.setCreatedAt(LocalDateTime.now());
        aiMessageService.save(assistantMsg);

        // 更新会话 updated_at
        session.setUpdatedAt(LocalDateTime.now());
        aiSessionService.updateById(session);

        // 6. 返回
        AiChatResponse response = new AiChatResponse();
        response.setSessionId(session.getId());
        response.setMessageId(assistantMsg.getId());
        response.setContent(replyContent);
        response.setTokensUsed(tokensUsed);

        return ApiResult.ok(response);
    }

    // ─── 历史会话列表 ──────────────────────────────────────────────────────────

    /**
     * 获取当前学生的历史会话列表（按最后更新时间倒序，最多 20 条）
     */
    @GetMapping("/sessions")
    public ApiResult<List<AiSessionVO>> listSessions() {
        Long studentId = getCurrentUserId();

        List<BizAiSession> sessions = aiSessionService.list(
                new LambdaQueryWrapper<BizAiSession>()
                        .eq(BizAiSession::getStudentId, studentId)
                        .eq(BizAiSession::getIsDeleted, 0)
                        .orderByDesc(BizAiSession::getUpdatedAt)
                        .last("LIMIT 20"));

        List<AiSessionVO> voList = sessions.stream().map(s -> {
            AiSessionVO vo = new AiSessionVO();
            vo.setId(s.getId());
            vo.setTitle(s.getTitle());
            vo.setSessionType(s.getSessionType());
            vo.setSummary(s.getSummary());
            vo.setCreatedAt(s.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());

        return ApiResult.ok(voList);
    }

    // ─── 会话详情（含消息） ────────────────────────────────────────────────────

    /**
     * 获取指定会话的详情，包含所有消息（按时间升序）
     */
    @GetMapping("/sessions/{sessionId}")
    public ApiResult<AiSessionVO> getSession(@PathVariable Long sessionId) {
        Long studentId = getCurrentUserId();

        BizAiSession session = aiSessionService.getOne(
                new LambdaQueryWrapper<BizAiSession>()
                        .eq(BizAiSession::getId, sessionId)
                        .eq(BizAiSession::getStudentId, studentId)
                        .eq(BizAiSession::getIsDeleted, 0));

        if (session == null) {
            return ApiResult.error("会话不存在或无权访问");
        }

        List<BizAiMessage> messages = aiMessageService.list(
                new LambdaQueryWrapper<BizAiMessage>()
                        .eq(BizAiMessage::getSessionId, sessionId)
                        .orderByAsc(BizAiMessage::getCreatedAt));

        AiSessionVO vo = new AiSessionVO();
        vo.setId(session.getId());
        vo.setTitle(session.getTitle());
        vo.setSessionType(session.getSessionType());
        vo.setSummary(session.getSummary());
        vo.setCreatedAt(session.getCreatedAt());

        List<AiSessionVO.AiMessageVO> msgVOs = messages.stream().map(m -> {
            AiSessionVO.AiMessageVO msgVO = new AiSessionVO.AiMessageVO();
            msgVO.setId(m.getId());
            msgVO.setRole(m.getRole());
            msgVO.setContent(m.getContent());
            msgVO.setCreatedAt(m.getCreatedAt());
            return msgVO;
        }).collect(Collectors.toList());

        vo.setMessages(msgVOs);
        return ApiResult.ok(vo);
    }

    // ─── 软删除会话 ────────────────────────────────────────────────────────────

    /**
     * 软删除指定会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResult<Void> deleteSession(@PathVariable Long sessionId) {
        Long studentId = getCurrentUserId();

        BizAiSession session = aiSessionService.getOne(
                new LambdaQueryWrapper<BizAiSession>()
                        .eq(BizAiSession::getId, sessionId)
                        .eq(BizAiSession::getStudentId, studentId));

        if (session == null) {
            return ApiResult.error("会话不存在或无权访问");
        }

        session.setIsDeleted(1);
        session.setUpdatedAt(LocalDateTime.now());
        aiSessionService.updateById(session);

        return ApiResult.ok();
    }

    // ─── 私有辅助方法 ──────────────────────────────────────────────────────────

    /**
     * 获取或创建会话
     */
    private BizAiSession getOrCreateSession(AiChatRequest request, Long studentId, LocalDateTime now) {
        if (request.getSessionId() != null) {
            BizAiSession existing = aiSessionService.getOne(
                    new LambdaQueryWrapper<BizAiSession>()
                            .eq(BizAiSession::getId, request.getSessionId())
                            .eq(BizAiSession::getStudentId, studentId)
                            .eq(BizAiSession::getIsDeleted, 0));
            if (existing != null) {
                return existing;
            }
        }

        // 创建新会话
        BizAiSession session = new BizAiSession();
        session.setStudentId(studentId);
        session.setSessionType(request.getSessionType() != null ? request.getSessionType() : 1);
        session.setTaskId(request.getTaskId());
        session.setNodeInstanceId(request.getNodeInstanceId());
        session.setIsDeleted(0);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);

        // 用消息前 20 字作为标题
        String content = request.getContent().trim();
        session.setTitle(content.length() > 20 ? content.substring(0, 20) + "…" : content);

        aiSessionService.save(session);
        return session;
    }

    /**
     * 构建历史消息上下文（最近 10 条，排除指定消息ID）
     */
    private List<Map<String, String>> buildHistory(Long sessionId, Long excludeMessageId) {
        List<BizAiMessage> recent = aiMessageService.list(
                new LambdaQueryWrapper<BizAiMessage>()
                        .eq(BizAiMessage::getSessionId, sessionId)
                        .ne(BizAiMessage::getId, excludeMessageId)
                        .orderByDesc(BizAiMessage::getCreatedAt)
                        .last("LIMIT 10"));

        // 反转为时间升序
        Collections.reverse(recent);

        return recent.stream().map(m -> {
            Map<String, String> msg = new LinkedHashMap<>();
            msg.put("role", m.getRole());
            msg.put("content", m.getContent());
            return msg;
        }).collect(Collectors.toList());
    }

    /**
     * 调用 AI 引擎 /api/chat 接口
     *
     * 若 AI 引擎不可用，返回降级回复。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callAiEngine(String userMessage,
                                              List<Map<String, String>> history,
                                              BizAiSession session) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("message", userMessage);
            body.put("history", history);
            body.put("session_type", session.getSessionType());
            if (session.getTaskId() != null) {
                body.put("task_id", session.getTaskId());
            }

            Map<String, Object> response = webClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("content")) {
                return response;
            }

            log.warn("AI engine returned unexpected response: {}", response);
            return fallbackReply();

        } catch (Exception e) {
            log.error("Failed to call AI engine chat endpoint: {}", e.getMessage());
            return fallbackReply();
        }
    }

    /**
     * AI 引擎不可用时的降级回复
     */
    private Map<String, Object> fallbackReply() {
        Map<String, Object> result = new HashMap<>();
        result.put("content", "抱歉，AI 助教暂时无法响应，请稍后再试。如有紧急问题，请联系老师。");
        return result;
    }
}
