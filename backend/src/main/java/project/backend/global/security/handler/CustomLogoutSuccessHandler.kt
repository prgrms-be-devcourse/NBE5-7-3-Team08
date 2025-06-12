package project.backend.global.security.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.stereotype.Component
import project.backend.auth.app.CookieUtils
import project.backend.auth.dto.MemberDetails
import project.backend.auth.token.dao.TokenRedisRepository

@Component
class CustomLogoutSuccessHandler(
    private val tokenRedisRepository: TokenRedisRepository
) : LogoutSuccessHandler {

    private val log = KotlinLogging.logger {}

    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        val memberDetails = authentication?.principal as? MemberDetails

        if (memberDetails != null) {
            tokenRedisRepository.deleteById(memberDetails.id)
            log.info { "[로그아웃] ${memberDetails.nickname}의 리프레시 토큰 삭제" }
        }

        CookieUtils.deleteCookie(response)
        response.status = HttpServletResponse.SC_NO_CONTENT
    }
}
