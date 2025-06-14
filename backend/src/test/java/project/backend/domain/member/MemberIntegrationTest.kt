package project.backend.domain.member

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import project.backend.domain.member.dao.MemberRepository
import project.backend.domain.member.dto.SignUpRequest
import project.backend.domain.member.entity.ProviderType
import project.backend.global.security.config.TestSecurityConfig

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestSecurityConfig::class) // 테스트 전용 SecurityConfig (permitAll 설정)
class MemberIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Test
    @WithMockUser // 인증된 사용자로 요청 (Security 필터 통과)
    fun `폼 회원가입 요청시 CREATED(201) 반환 및 DB 저장 확인`() {
        // given
        val request = SignUpRequest(
            username = "member1",
            nickname = "nickname1",
            email = "member1@gmail.com",
            password = "1234"
        )

        // when
        mockMvc.post("/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isCreated() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.username") { value(request.username) }
                jsonPath("$.nickname") { value(request.nickname) }
                jsonPath("$.email") { value(request.email) }
                jsonPath("$.profileImg") { value("default-profile.png") }
                jsonPath("$.provider") { value(ProviderType.LOCAL.name) }
            }

        // then - DB 저장 값 검증
        val member = memberRepository.findByEmail(request.email!!)
        assertThat(member).isNotNull
        assertThat(member!!.username).isEqualTo(request.username)
        assertThat(member.nickname).isEqualTo(request.nickname)
        assertThat(member.email).isEqualTo(request.email)
        assertThat(member.provider).isEqualTo(ProviderType.LOCAL)
        assertThat(member.profileImage).isEqualTo("default-profile.png")
    }
}