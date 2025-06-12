package project.backend.global.security.interceptor

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import project.backend.auth.app.CookieUtils
import project.backend.auth.token.jwt.JwtProvider

@Component
class WebSocketHandShakeInterceptor(
    private val jwtProvider: JwtProvider
) : HandshakeInterceptor {

    private val log = KotlinLogging.logger {}

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        if (request is ServletServerHttpRequest) {
            val httpServletRequest: HttpServletRequest = request.servletRequest
            val accessTokenCookie = CookieUtils.getCookie(
                httpServletRequest,
                "accessToken"
            )

            if (accessTokenCookie != null) {
                val token = accessTokenCookie.value //return type: Cookie?
                val authentication = jwtProvider.getAuthentication(token)

                // 이후 ChannelInterceptor에서 꺼내쓰기 위해 attributes에 저장
                attributes["auth"] = authentication
            } else {
                log.error { "웹소켓 핸드쉐이크 요청 시 쿠키에 accessToken이 포함되지 않음" }
                return false //handshake 허용x
            }
        }
        return true // handshake 허용
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception
    ) {
        SecurityContextHolder.clearContext() //cleanup
    }
}
