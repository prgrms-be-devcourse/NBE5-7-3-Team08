package project.backend.domain.chat.github

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import project.backend.domain.imagefile.ImageFileRepository
import project.backend.domain.member.dao.MemberRepository
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType

@Component
class GitHubBotInitializer(
    private val imageFileRepository: ImageFileRepository,
    private val memberRepository: MemberRepository,
) {

    @Value("\${file.images.profile.github}")
    private lateinit var githubProfile: String

    @Value("\${github.username}")
    private lateinit var githubUsername: String

    @PostConstruct
    fun init() {
        val gitHubBot = Member(
            username = githubUsername,
            nickname = githubUsername,
            provider = ProviderType.LOCAL,
            profileImage = githubProfile
        )

        memberRepository.save(gitHubBot)
        memberRepository.flush()
    }
}
