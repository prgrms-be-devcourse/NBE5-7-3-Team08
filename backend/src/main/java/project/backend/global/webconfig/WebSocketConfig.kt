package project.backend.global.webconfig

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration
import org.springframework.web.socket.handler.WebSocketHandlerDecorator
import project.backend.global.security.interceptor.WebSocketChannelInterceptor
import project.backend.global.security.interceptor.WebSocketHandShakeInterceptor

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val handShakeInterceptor: WebSocketHandShakeInterceptor,
    private val channelInterceptor: WebSocketChannelInterceptor
) : WebSocketMessageBrokerConfigurer {

    private val log = KotlinLogging.logger {}

    //클라이언트가 연결할 웹소켓 엔드포인트 지정
    //해당 주소로 접속 시 웹소켓 핸드셰이크 커넥션 생성
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("http://localhost:3000")
            .addInterceptors(handShakeInterceptor)
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/chat") //클라이언트 -> 서버, 클라이언트에서 SEND 요청을 처리
        //서버 -> 클라이언트, 해당 경로를 SUBSCRIBE하는 클라이언트에게 메세지를 전달
        registry.enableSimpleBroker("/topic")
            .setHeartbeatValue(longArrayOf(10000, 20000))
            .setTaskScheduler(customWebSocketTaskScheduler())
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(channelInterceptor)
    }

    @Bean
    fun customWebSocketTaskScheduler(): ThreadPoolTaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        //        scheduler.setPoolSize(1); // 너무 높지 않게 설정
//        scheduler.setThreadNamePrefix("wss-heartbeat-thread-");
        scheduler.initialize()
        return scheduler
    }

    // 로그용
    override fun configureWebSocketTransport(registration: WebSocketTransportRegistration) {
        registration.addDecoratorFactory { handler ->
            object : WebSocketHandlerDecorator(handler) {
                override fun afterConnectionEstablished(session: WebSocketSession) {
                    val principalName = session.principal?.name ?: "anonymous"
                    log.info { "✅ WebSocket connected - sessionId=${session.id}, principal=${principalName}" }

                    super.afterConnectionEstablished(session)
                }

                override fun afterConnectionClosed(
                    session: WebSocketSession,
                    closeStatus: CloseStatus
                ) {
                    log.warn { "🔌 WebSocket closed - sessionId=${session.id}, reason=${closeStatus}" }
                    super.afterConnectionClosed(session, closeStatus)
                }
            }
        }
    }
}
