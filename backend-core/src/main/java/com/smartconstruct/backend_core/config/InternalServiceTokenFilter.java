package com.smartconstruct.backend_core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 内部服务Token验证过滤器
 *
 * 仅对 /api/internal/** 路径生效，验证请求头中的 X-Service-Token
 * 是否与配置的 ai-engine.service-token 一致。
 * 用于保护内部服务间通信接口（如AI引擎回调），替代JWT认证。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Component
public class InternalServiceTokenFilter extends OncePerRequestFilter {

    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";
    private static final String INTERNAL_PATH_PATTERN = "/api/internal/**";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper;

    @Value("${ai-engine.service-token}")
    private String serviceToken;

    public InternalServiceTokenFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Only apply to /api/internal/** paths
        if (!pathMatcher.match(INTERNAL_PATH_PATTERN, requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String errorCode, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new HashMap<>();
        body.put("code", status);
        body.put("message", message);
        body.put("errorCode", errorCode);
        body.put("data", null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
