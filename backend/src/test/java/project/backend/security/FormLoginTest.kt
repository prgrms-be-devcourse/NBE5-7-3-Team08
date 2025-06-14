package project.backend.security

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import project.backend.auth.app.LoginService
import project.backend.auth.dto.MemberDetails
import project.backend.auth.token.jwt.JwtProvider
import project.backend.auth.token.jwt.Token
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType

@SpringBootTest
@AutoConfigureMockMvc
class FormLoginTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @MockitoBean
    lateinit var jwtProvider: JwtProvider

    @MockitoBean
    lateinit var loginService: LoginService

    @Test
    fun `form 로그인 성공 시 accessToken 쿠키와 성공 메세지를 반환한다`() {
//    given
        val accessToken = "access-token"
        val refreshToken = "refresh-token"
        val expectedToken = Token(accessToken, refreshToken)

        val username = "jeeun5482"
        val password = "1234"
        val loginUser = createUser(username, password)

        whenever(loginService.loadUserByUsername(loginUser.usernameValue)).thenReturn(
            loginUser
        )
        whenever(jwtProvider.generateTokenPair(any())).thenReturn(expectedToken)

//    when & then
        val expectedMsg = "로그인 성공"
        mockMvc.perform(
            post("/login")
                .param("username", "jeeun5482")
                .param("password", "1234")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.message").value(expectedMsg))
            .andExpect(cookie().value("accessToken", accessToken))
    }

    @Test
    fun `form 로그인 실패 시 401 상태코드와 실패 메세지를 반환한다`() {
        //given
        val username = "jeeun5482"
        val correctPassword = "1234"
        val expectedUser = createUser(username, correctPassword)

        val wrongPassword = "wrong"

        whenever(loginService.loadUserByUsername(username)).thenReturn(expectedUser)

        //when & then
        mockMvc.perform(
            post("/login")
                .param("username", username)
                .param("password", wrongPassword)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
            .andExpect(status().isUnauthorized) //401
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.message").value("ID 또는 비밀번호가 일치하지 않습니다. 다시 확인해 주십시오."))
            .andExpect(jsonPath("$.code").value("LE-001"))
    }

    private fun createUser(username: String, password: String): MemberDetails {
        val member = Member(
            id = 1L,
            username = username,
            password = passwordEncoder.encode(password),
            nickname = "ziening",
            provider = ProviderType.LOCAL,
            profileImage = "image"
        )
        return MemberDetails(member)
    }

}