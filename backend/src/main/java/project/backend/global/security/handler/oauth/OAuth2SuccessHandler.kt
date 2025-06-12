package project.backend.global.security.handler.oauth

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import project.backend.auth.app.CookieUtils
import project.backend.auth.app.OAuthSignUpService
import project.backend.auth.dto.MemberDetails
import project.backend.auth.dto.OAuthMemberDto
import project.backend.auth.token.dao.TokenRedisRepository
import project.backend.auth.token.entity.TokenRedis
import project.backend.auth.token.jwt.JwtProvider

@Component
class OAuth2SuccessHandler(
    @Value("\${jwt.redirection.base}")
    private val baseUrl: String,
    private val jwtProvider: JwtProvider,
    private val oAuthSignUpService: OAuthSignUpService,
    private val tokenRedisRepository: TokenRedisRepository
) : SimpleUrlAuthenticationSuccessHandler() {

    private val log = KotlinLogging.logger {}

    @Transactional
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as OAuth2User

        log.info { "oAuth2User = $oAuth2User" }

        val userDto = OAuthMemberDto(
            oAuth2User.attributes["email"] as String,
            oAuth2User.attributes["name"] as String,
            oAuth2User.attributes["login"] as String
        )

        // 기존에 없는 email이면 회원가입
        val member = oAuthSignUpService.oAuthSignUp(userDto)
        val token = jwtProvider.generateTokenPair(MemberDetails(member))

        //쿠키 생성 및 저장
        CookieUtils.saveCookie(response, token.accessToken)

        // 깃허브 엑세스 토큰
        val githubAccess = oAuth2User.attributes["githubAccess"] as String?

        tokenRedisRepository.save(
            TokenRedis(
                member.id, token.accessToken, token.refreshToken,
                githubAccess
            )
        )

        log.info { "OAuth 로그인 성공: $member.username" }

        val redirectUrl = UriComponentsBuilder.fromUriString(baseUrl)
            .build().toUriString()

        log.info { "OAuth 로그인 후 리다이렉트 URL = $redirectUrl" }
        response.status = HttpServletResponse.SC_OK
        response.contentType = "application/json"
        response.sendRedirect(redirectUrl)
    }
}
