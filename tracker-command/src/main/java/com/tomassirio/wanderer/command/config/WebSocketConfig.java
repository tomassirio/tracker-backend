package com.tomassirio.wanderer.command.config;

import com.tomassirio.wanderer.command.websocket.TripWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final TripWebSocketHandler tripWebSocketHandler;

    @Value("${app.cors.allowed-origins:http://localhost:51538}")
    private String allowedOriginsString;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] allowedOrigins = allowedOriginsString.split(",");
        registry.addHandler(tripWebSocketHandler, "/ws").setAllowedOrigins(allowedOrigins);
    }
}
