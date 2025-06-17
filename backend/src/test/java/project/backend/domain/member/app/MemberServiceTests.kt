package project.backend.domain.member.app

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.* // clearAllMocks(), mockk() 를 위해 필요
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import project.backend.domain.imagefile.ImageFileService
import project.backend.domain.member.dao.MemberRepository
import project.backend.domain.member.dto.SignUpRequest
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType
import project.backend.global.exception.errorcode.MemberErrorCode
import project.backend.global.exception.ex.MemberException

class MemberServiceTests : BehaviorSpec({

    Given("Form 회원가입 테스트 진행") {
        val memberRepository: MemberRepository = mockk(relaxed = true)
        val imageFileService: ImageFileService = mockk()
        val passwordEncoder: PasswordEncoder = mockk()
        val eventPublisher: ApplicationEventPublisher = mockk()

        val memberService = MemberService(
            memberRepository,
            imageFileService,
            passwordEncoder,
            eventPublisher,
            defaultProfileImg = "defaultProfileImage.png"
        )

        val request = SignUpRequest(
            username = "member1",
            nickname = "member1Nickname",
            email = "member1@gmail.com",
            password = "1234",
        )

        val encryptedPassword = "encryptedPassword"

        every { memberRepository.findByUsername(request.username) } returns null
        every { memberRepository.findByEmail(request.email!!) } returns null
        every { passwordEncoder.encode(request.password) } returns encryptedPassword

        every { memberRepository.save(any()) } answers {
            val saved = firstArg<Member>()
            saved.id = 1L
            saved
        }

        When("save를 호출 시") {
            val response = memberService.saveMember(request)

            Then("정상적으로 회원값이 반환된다.") {
                response.username shouldBe request.username
                response.nickname shouldBe request.nickname
                response.email shouldBe request.email
                response.profileImg shouldBe "defaultProfileImage.png"
                response.provider shouldBe ProviderType.LOCAL

                // 이 테스트에서 save가 1번 호출되었음을 검증.
                verify(exactly = 1) { memberRepository.save(any()) }
                verify { passwordEncoder.encode(request.password) }
            }
        }
    }

    Given("Form 회원가입 - 회원가입 시도 email이 존재하는 경우") {
        // 두 번째 Given 블록에서도 독립적으로 Mock 객체와 SUT를 선언하고 초기화합니다.
        val memberRepository: MemberRepository = mockk(relaxed = true)
        val imageFileService: ImageFileService = mockk()
        val passwordEncoder: PasswordEncoder = mockk()
        val eventPublisher: ApplicationEventPublisher = mockk()

        val memberService = MemberService(
            memberRepository,
            imageFileService,
            passwordEncoder,
            eventPublisher,
            defaultProfileImg = "default-profile.png"
        )

        val request = SignUpRequest(
            username = "newUser",
            nickname = "nickname",
            email = "existingEmail@gmail.com",
            password = "1234"
        )

        val existingMember = Member(
            username = "existingUser",
            password = "encryptedPassword",
            nickname = "nickname",
            email = request.email!!,
            provider = ProviderType.LOCAL,
            profileImage = "default-profile.png"
        ).apply { id = 1L }

        every { memberRepository.findByUsername(request.username) } returns null
        every { memberRepository.findByEmail(request.email!!) } returns existingMember

        When("saveMember 호출 시") {
            Then("MemberException(EMAIL_ALREADY_EXISTS) 예외가 발생한다.") {
                val exception = runCatching { memberService.saveMember(request) }.exceptionOrNull()

                exception.shouldBeInstanceOf<MemberException>()
                exception.message shouldBe MemberErrorCode.EMAIL_ALREADY_EXISTS.message

                // 이 테스트에서 save가 0번 호출되었음을 검증.
                verify(exactly = 0) { memberRepository.save(any()) }
            }
        }
    }
})