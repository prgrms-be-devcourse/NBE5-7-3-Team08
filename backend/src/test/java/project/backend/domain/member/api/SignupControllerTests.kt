package project.backend.domain.member.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import project.backend.auth.token.jwt.JwtProvider
import project.backend.domain.member.app.MemberService
import project.backend.domain.member.dto.MemberResponse
import project.backend.domain.member.dto.SignUpRequest
import project.backend.domain.member.entity.ProviderType
import project.backend.global.security.config.TestSecurityConfig

@WebMvcTest(
    controllers = [SignupController::class]
)
@Import(TestSecurityConfig::class)
class SignupControllerTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var memberService: MemberService

    @Autowired
    lateinit var om: ObjectMapper

    @MockitoBean
    lateinit var jwtProvider: JwtProvider

    @Test
    @WithMockUser
    fun `폼 회원가입 요청 및 성공시 CREATED(201), MemberResponse 반환 테스트`() {
        // given
        val request = SignUpRequest(
            username = "member1",
            nickname = "nickname1",
            email = "member1@gmail.com",
            password = "1234"
        )

        val expectedResponse = MemberResponse(
            id = 1L,
            username = request.username,
            nickname = request.nickname,
            email = request.email,
            profileImg = "defaultProfileImage.png",
            provider = ProviderType.LOCAL
        )

        `when`(memberService.saveMember(request)).thenReturn(expectedResponse)

        // when, then
        mockMvc.post("/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = om.writeValueAsString(request)
        }
            .andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.id") { value(expectedResponse.id) }
                jsonPath("$.username") { value(expectedResponse.username) }
                jsonPath("$.nickname") { value(expectedResponse.nickname) }
                jsonPath("$.email") { value(expectedResponse.email) }
                jsonPath("$.profileImg") { value(expectedResponse.profileImg) }
                jsonPath("$.provider") { value(expectedResponse.provider.name) }
            }
            .andDo { print() }
    }
}