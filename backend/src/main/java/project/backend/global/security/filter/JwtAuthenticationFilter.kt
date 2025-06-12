package project.backend.global.security.filter

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import project.backend.auth.app.CookieUtils
import project.backend.auth.token.jwt.JwtProvider
import project.backend.auth.token.jwt.TokenStatus
import project.backend.global.exception.errorcode.TokenErrorCode
import project.backend.global.exception.ex.CustomJwtException

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    private val log = KotlinLogging.logger {}

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestURI = request.requestURI
        if (requestURI.startsWith("/github/") || requestURI in WHITE_LIST) {
            filterChain.doFilter(request, response) // JWT 검사 건너뜀
            return
        }

        log.info { "JWT 필터 도달 = ${request.requestURI}" }

        val accessToken = CookieUtils.getCookie(request, "accessToken")
            ?.value ?: throw CustomJwtException(TokenErrorCode.NOT_FOUND_TOKEN)

        when (jwtProvider.validateAccessToken(accessToken)) {
            TokenStatus.VALID -> {
                log.info { "[JWT] 유효한 토큰" }
                val authentication = jwtProvider.getAuthentication(accessToken)
                SecurityContextHolder.getContext().authentication = authentication
            }

            else -> {
                SecurityContextHolder.clearContext()
            }
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        private val WHITE_LIST = listOf(
            "/signup",
            "/login",
            "/token/refresh"
        )
    }
}
