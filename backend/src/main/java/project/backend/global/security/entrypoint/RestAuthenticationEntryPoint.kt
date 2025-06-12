package project.backend.global.security.entrypoint

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import project.backend.global.exception.ex.CustomJwtException

@Component
class RestAuthenticationEntryPoint : AuthenticationEntryPoint {

    private val log = KotlinLogging.logger {}

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        log.warn { "인증되지 않은 사용자 접근: ${request.requestURI} - ${authException.message}" }

        val (status, message) = when (authException) {
            is CustomJwtException -> authException.errorCode.let {
                it.status.value() to it.message
            }

            else -> HttpServletResponse.SC_UNAUTHORIZED to "로그인이 필요한 서비스입니다."
        }

        response.status = status
        response.contentType = "application/json; charset=utf-8"
        response.writer
            .write("""{"message":"$message"}""")
    }
}