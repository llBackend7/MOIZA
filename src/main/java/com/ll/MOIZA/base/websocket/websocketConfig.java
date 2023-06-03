package com.ll.MOIZA.base.websocket;

import com.ll.MOIZA.boundedContext.member.entity.Member;
import com.ll.MOIZA.boundedContext.member.service.MemberService;
import com.ll.MOIZA.boundedContext.room.entity.Room;
import com.ll.MOIZA.boundedContext.room.service.EnterRoomService;
import com.ll.MOIZA.boundedContext.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class websocketConfig implements WebSocketMessageBrokerConfigurer {
    private final MemberService memberService;
    private final RoomService roomService;
    private final EnterRoomService enterRoomService;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // TODO origin 경로 수정필요
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/send");         //모든 메시지 매핑 경로 앞에 /send/이 붙음
        registry.enableSimpleBroker("/room");     //메시지 브로커 구독 경로 앞에 /room/이 붙음
    }

//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(new ChannelInterceptor() {
//            @Override
//            public Message<?> preSend(Message<?> message, MessageChannel channel) {
//                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//                if (accessor.getCommand().equals(StompCommand.CONNECT)) {
//                    String destination = accessor.getDestination(); //TODO 여기가 null이 나오는 거 고치기
//                    if (destination.startsWith("/room/")) {
//                        String roomId = destination.substring(destination.lastIndexOf("/") + 1);
//                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//                        Room room = roomService.getRoom(Long.parseLong(roomId));
//                        Member member = memberService.findByName(authentication.getName());
//
//                        if (!enterRoomService.isRoomMember(room, member)) {
//                            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "권한이 없습니다.");
//                        }
//                    }
//                }
//                return message;
//            }
//        });
//    }
}
