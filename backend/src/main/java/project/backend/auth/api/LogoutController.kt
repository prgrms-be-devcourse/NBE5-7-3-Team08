package project.backend.auth.api

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import project.backend.auth.app.CookieUtils
import project.backend.auth.dto.MemberDetails
import project.backend.auth.token.dao.TokenRedisRepository

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/logout")
class LogoutController(
    private val tokenRedisRepository: TokenRedisRepository
) {

    @PostMapping
    fun logout(
        @AuthenticationPrincipal memberDetails: MemberDetails,
        response: HttpServletResponse
    ) {
        SecurityContextHolder.clearContext()
        CookieUtils.deleteCookie(response)
        tokenRedisRepository.deleteById(memberDetails.id)
        log.info { "[로그아웃] ${memberDetails.nickname}" }
        response.status = HttpServletResponse.SC_NO_CONTENT
    }
}