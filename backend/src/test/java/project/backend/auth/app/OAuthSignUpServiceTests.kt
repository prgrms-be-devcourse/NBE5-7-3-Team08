package project.backend.auth.app

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import project.backend.auth.dto.OAuthMemberDto
import project.backend.domain.member.dao.MemberRepository
import project.backend.domain.member.entity.ProviderType
import project.backend.domain.member.mapper.MemberMapper
import project.backend.domain.imagefile.ImageFileService

class OAuthSignUpServiceTests : BehaviorSpec({

    Given("OAuth 로그인 - 새로운 유저일 때") {
        val memberRepository = mockk<MemberRepository>(relaxed = true)
        val imageFileService = mockk<ImageFileService>()
        val oAuthSignUpService = OAuthSignUpService(
            memberRepository,
            imageFileService,
            defaultProfileImg = "defaultProfileImage.png"
        )

        val request = OAuthMemberDto(
            login = "memberGitHub",
            email = "memberGitHub@gmail.com",
            nickname = "githubNickname"
        )

        every { memberRepository.findByUsername(request.login) } returns null
        every { memberRepository.save(any()) } answers {
            val saved = firstArg<project.backend.domain.member.entity.Member>()
            saved.id = 1L
            saved
        }

        When("oAuthSignUp 호출 시") {
            val result = oAuthSignUpService.oAuthSignUp(request)

            Then("새로운 Member가 저장된다.") {
                result.username shouldBe request.login
                result.email shouldBe request.email
                result.nickname shouldBe request.nickname
                result.provider shouldBe ProviderType.GITHUB
                result.profileImage shouldBe "defaultProfileImage.png"

                verify(exactly = 1) { memberRepository.save(any()) }
            }
        }
    }

    Given("OAuth 로그인 - 기존 유저가 있을 때") {
        val memberRepository = mockk<MemberRepository>(relaxed = true)
        val imageFileService = mockk<ImageFileService>()
        val oAuthSignUpService = OAuthSignUpService(
            memberRepository,
            imageFileService,
            defaultProfileImg = "defaultProfileImage.png"
        )

        val request = OAuthMemberDto(
            login = "existingGithubUser",
            email = "existingUser@gmail.com",
            nickname = "existingNickname"
        )

        val existingMember = MemberMapper.toEntity(request, "defaultProfileImage.png").apply {
            id = 1L
        }

        every { memberRepository.findByUsername(request.login) } returns existingMember
        every { memberRepository.save(any()) } throws AssertionError("save should not be called!")

        When("oAuthSignUp 호출 시") {
            val result = oAuthSignUpService.oAuthSignUp(request)

            Then("기존 Member가 반환된다.") {
                result shouldBe existingMember
            }
        }

        verify(exactly = 0) { memberRepository.save(any()) }
    }
})