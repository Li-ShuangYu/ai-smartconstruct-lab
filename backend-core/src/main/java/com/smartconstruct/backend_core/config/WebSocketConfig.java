package com.smartconstruct.backend_core.config;

import com.smartconstruct.backend_core.websocket.TrainingWebSocketHandler;
import com.smartconstruct.backend_core.websocket.TrainingHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TrainingWebSocketHandler handler;
    private final TrainingHandshakeInterceptor interceptor;

    public WebSocketConfig(TrainingWebSocketHandler handler, TrainingHandshakeInterceptor interceptor) {
        this.handler = handler;
        this.interceptor = interceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/training")
                .addInterceptors(interceptor)
                .setAllowedOrigins("*");
    }
}
