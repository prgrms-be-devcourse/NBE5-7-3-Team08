package project.backend.security

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import project.backend.auth.app.OAuthSignUpService
import project.backend.auth.dto.CustomOAuth2User
import project.backend.auth.token.dao.TokenRedisRepository
import project.backend.auth.token.jwt.JwtProvider
import project.backend.auth.token.jwt.Token
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType
import project.backend.global.security.handler.oauth.OAuth2FailureHandler
import project.backend.global.security.handler.oauth.OAuth2SuccessHandler

@SpringBootTest
@AutoConfigureMockMvc
class Oauth2LoginTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var oAuthSignUpService: OAuthSignUpService

    @MockitoBean
    lateinit var tokenRedisRepository: TokenRedisRepository

    @MockitoBean
    lateinit var jwtProvider: JwtProvider

    @Value("\${jwt.redirection.base}")
    lateinit var baseUrl: String

    @Test
    fun `OAuth2 로그인 성공 시 baseUrl로 리다이렉트한다`() {
        //given
        val token = Token("access-token", "refresh-token")

        val oAuth2User = createOAuth2User()
        val auth = createAuthToken(oAuth2User)
        val member = createMember()

        val handler = OAuth2SuccessHandler(
            baseUrl,
            jwtProvider,
            oAuthSignUpService,
            tokenRedisRepository
        )

        whenever(oAuthSignUpService.oAuthSignUp(any())).thenReturn(member)
        whenever(jwtProvider.generateTokenPair(any())).thenReturn(token)

        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        //when
        handler.onAuthenticationSuccess(request, response, auth)

        //then
        assertThat(response.status).isEqualTo(HttpServletResponse.SC_FOUND) // 302
        assertThat(response.redirectedUrl).isEqualTo(baseUrl)
    }

    @Test
    fun `OAuth2 로그인 실패 시 401 상태코드와 에러 메세지를 응답한다`() {
        //given
        val handler = OAuth2FailureHandler()
        val request = MockHttpServletRequest().apply {
            requestURI = "/mock/url"
            queryString = "code=invalid"
        }
        val response = MockHttpServletResponse()
        val exception = OAuth2AuthenticationException("invalid_token")
        val expectedErrorMsg = "OAuth 로그인 실패"

        //when
        handler.onAuthenticationFailure(request, response, exception)

        //then
        assertThat(response.status).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED)
        assertThat(response.errorMessage).isEqualTo(expectedErrorMsg)
    }


    private fun createMember(): Member {
        return Member(
            id = 1L,
            username = "jeeun5482",
            nickname = "ziening",
            provider = ProviderType.GITHUB,
            profileImage = "default_profile.png"
        )
    }

    private fun createOAuth2User(): OAuth2User {
        val attributes = mapOf(
            "email" to "jeeun03@gmail.com",
            "name" to "지은",
            "login" to "ziening",
            "githubAccess" to "gho_mockToken"
        )

        val oAuth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "login"
        )

        return CustomOAuth2User(oAuth2User, "gho_mockToken")
    }

    private fun createAuthToken(oAuth2User: OAuth2User): OAuth2AuthenticationToken {
        return OAuth2AuthenticationToken(
            oAuth2User,
            oAuth2User.authorities,
            "github"
        )
    }

}

