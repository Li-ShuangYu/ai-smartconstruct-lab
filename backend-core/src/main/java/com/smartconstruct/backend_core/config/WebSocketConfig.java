package com.smartconstruct.backend_core.config;

import com.smartconstruct.backend_core.websocket.TrainingWebSocketHandler;
import com.smartconstruct.backend_core.websocket.TrainingHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置类
 *
 * 配置 WebSocket 端点和处理器，支持实训实时通信。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /** WebSocket 处理器 - 处理实训消息 */
    private final TrainingWebSocketHandler handler;

    /** 握手拦截器 - 验证连接身份 */
    private final TrainingHandshakeInterceptor interceptor;

    /**
     * 构造函数 - 注入 WebSocket 组件
     *
     * @param handler WebSocket 处理器
     * @param interceptor 握手拦截器
     */
    public WebSocketConfig(TrainingWebSocketHandler handler, TrainingHandshakeInterceptor interceptor) {
        this.handler = handler;
        this.interceptor = interceptor;
    }

    /**
     * 注册 WebSocket 处理器
     *
     * 配置 /ws/training 端点，添加身份验证拦截器，允许跨域。
     *
     * @param registry WebSocket 处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/training")
                .addInterceptors(interceptor)
                .setAllowedOrigins("*");
    }
}
