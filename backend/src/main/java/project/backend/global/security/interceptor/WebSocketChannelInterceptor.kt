package project.backend.global.security.interceptor

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class WebSocketChannelInterceptor : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {

        // 웹소켓을 통해 들어온 메시지를 STOMP 헤더 기반으로 래핑
        val accessor = MessageHeaderAccessor
            .getAccessor(message, StompHeaderAccessor::class.java)

        if (accessor.command == StompCommand.CONNECT) {
            // handshakeInterceptor에서 넣어준 auth 객체를 꺼내기
            val auth = accessor.sessionAttributes["auth"] as Authentication?

            if (auth != null) {
                accessor.user = auth //websocket principal로 주입
            }
        }

        // 메타정보(헤더) 반영된 새 메시지로 return
        return MessageBuilder.createMessage(message.payload, accessor.messageHeaders)
    }
}
