package project.backend.global.security.handler.form

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import project.backend.auth.app.CookieUtils
import project.backend.auth.dto.MemberDetails
import project.backend.auth.token.dao.TokenRedisRepository
import project.backend.auth.token.entity.TokenRedis
import project.backend.auth.token.jwt.JwtProvider

@Component
class FormSuccessHandler(
    private val jwtProvider: JwtProvider,
    private val tokenRedisRepository: TokenRedisRepository
) : AuthenticationSuccessHandler {

    private val log = KotlinLogging.logger {}

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val memberDetails = authentication.principal as MemberDetails
        val token = jwtProvider.generateTokenPair(memberDetails)

        CookieUtils.saveCookie(response, token.accessToken)

        tokenRedisRepository.save(
            TokenRedis(
                memberDetails.id, token.accessToken, token.refreshToken,
                null
            )
        )

        response.apply {
            status = HttpServletResponse.SC_OK
            contentType = "application/json"
            characterEncoding = "UTF-8"
            writer.write("""{"message":"로그인 성공"}""")
        }

        log.info { "로그인 성공: ${authentication.name}" }
    }
}
