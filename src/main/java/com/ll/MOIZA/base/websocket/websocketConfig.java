package com.ll.MOIZA.base.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class websocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // origin 경로 수정필요
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:8080")
                .setAllowedOrigins("https://moiza.online")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        Principal principal = request.getPrincipal();
                        if (principal != null) {
                            // WebSocket 세션에 인증 정보를 넣어둡니다.
                            attributes.put("PRINCIPAL", principal);
                        }
                        return principal;
                    }
                })
                .withSockJS()
                .setInterceptors(new HttpSessionHandshakeInterceptor());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/send");         //모든 메시지 매핑 경로 앞에 /send/이 붙음
        registry.enableSimpleBroker("/room");     //메시지 브로커 구독 경로 앞에 /room/이 붙음
    }



    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                Principal principal = (Principal) accessor.getSessionAttributes().get("PRINCIPAL");

                if (accessor.getCommand().equals(StompCommand.CONNECT)) {
                    if (principal != null) {
                        accessor.setUser(principal);
                    }
                } else if (accessor.getCommand().equals(StompCommand.SEND)) {
                    String destination = accessor.getDestination();
                    String roomId = destination.substring(destination.lastIndexOf("/") + 1);

                    if (principal != null && principal instanceof Authentication) {
                        Authentication auth = (Authentication) principal;
                        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROOM#" + roomId + "_MEMBER"))) {
                            throw new AccessDeniedException("권한 없음.");
                        }
                    }
                }

                return message;
            }
        });
    }
}