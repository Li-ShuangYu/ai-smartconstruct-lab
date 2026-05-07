package com.smartconstruct.backend_core.websocket;

import com.smartconstruct.backend_core.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class TrainingHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TrainingHandshakeInterceptor.class);
    private final JwtUtil jwtUtil;

    public TrainingHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String query = request.getURI().getQuery();
        if (query == null) return false;

        String token = null, taskIdStr = null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2) {
                if ("token".equals(kv[0])) token = kv[1];
                if ("taskId".equals(kv[0])) taskIdStr = kv[1];
            }
        }

        if (token == null || taskIdStr == null) return false;

        try {
            String username = jwtUtil.parseToken(token);
            if (username == null) return false;
            attributes.put("username", username);
            attributes.put("taskId", Long.parseLong(taskIdStr));
            return true;
        } catch (Exception e) {
            log.warn("WebSocket 握手鉴权失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {}
}
