package project.backend.global.security.handler.oauth

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component

@Component
class OAuth2FailureHandler : SimpleUrlAuthenticationFailureHandler() {

    private val log = KotlinLogging.logger {}

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        log.error(exception) { "OAuth 로그인 실패: ${exception.message}" } //fixme exception이 null일 때 고려

        with(request) {
            log.info { "요청 URI: $requestURI" }
            log.info { "요청 전체 URL: $requestURL" }
            log.info { "Query String: $queryString" }

            parameterNames.asSequence().forEach { name ->
                log.info { "요청 파라미터: $name = ${request.getParameter(name)}" }
            }
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth 로그인 실패")
    }
}
