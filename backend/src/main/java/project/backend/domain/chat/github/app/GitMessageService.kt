package project.backend.domain.chat.github.app

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import project.backend.auth.token.dao.TokenRedisRepository
import project.backend.domain.chat.chatmessage.dao.ChatMessageRepository
import project.backend.domain.chat.chatmessage.dto.ChatMessageResponse
import project.backend.domain.chat.chatmessage.entity.ChatMessage
import project.backend.domain.chat.chatmessage.mapper.ChatMessageMapper
import project.backend.domain.chat.chatroom.dao.ChatRoomRepository
import project.backend.domain.chat.chatroom.entity.ChatRoom
import project.backend.domain.chat.github.GitHubClient
import project.backend.domain.chat.github.GitRepoUrlUtils
import project.backend.domain.chat.github.dto.GitMessageDto
import project.backend.domain.chat.github.dto.GitRepoDto
import project.backend.domain.member.app.MemberService
import project.backend.domain.member.entity.Member
import project.backend.global.exception.errorcode.ChatRoomErrorCode
import project.backend.global.exception.ex.ChatRoomException

@Service
class GitMessageService(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageMapper: ChatMessageMapper,
    private val chatMessageRepository: ChatMessageRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val memberService: MemberService,
    private val gitHubClient: GitHubClient,
    private val tokenRedisRepository: TokenRedisRepository,
) {

    @Value("\${url.ngrok}")
    private lateinit var ngrokUrl: String

    @Value("\${github.username}")
    private lateinit var githubUsername: String

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun handleEvent(roomId: Long, eventType: String, payload: Map<String, Any>) {
        val gitMessage = when (eventType) {
            "issues" -> GitMessageDto.fromIssue(payload)
            "pull_request" -> GitMessageDto.fromPullRequest(payload)
            "pull_request_review" -> GitMessageDto.fromPullRequestReview(payload)
            else -> null
        }

        if (gitMessage == null) return

        val room = chatRoomRepository.findById(roomId)
            .orElseThrow { ChatRoomException(ChatRoomErrorCode.CHATROOM_NOT_FOUND) }

        sendGitMessage(room, gitMessage.attachRoom(gitMessage, room))
    }

    private fun sendGitMessage(room: ChatRoom, gitMessage: GitMessageDto) {
        val githubBot: Member = memberService.getMemberByUsername(githubUsername)
        val message: ChatMessage = chatMessageMapper.toEntityWithGit(gitMessage, githubBot)
        chatMessageRepository.save(message)

        val response: ChatMessageResponse = chatMessageMapper.toGitResponse(message)
        messagingTemplate.convertAndSend("/topic/chat/${room.id}", response)
    }

    fun registerWebhook(repoUrl: String, roomId: Long, memberId: Long) {
        val gitRepoDto: GitRepoDto = GitRepoUrlUtils.validateAndParseUrl(repoUrl)

        log.info("owner = {}", gitRepoDto.ownerName)
        log.info("repo = {}", gitRepoDto.repoName)

        val tokenRedis = tokenRedisRepository.findById(memberId)
            .orElseThrow { RuntimeException("토큰이 존재하지 않습니다.") }

        log.info("gitHubAccessToken = {}", tokenRedis.githubAccess)

        val webhookUrl = makeWebhookUrl(roomId)

        gitHubClient.validateAdminPermission(
            tokenRedis.githubAccess,
            gitRepoDto.ownerName,
            gitRepoDto.repoName
        )

        gitHubClient.registerWebhook(
            tokenRedis.githubAccess,
            gitRepoDto.ownerName,
            gitRepoDto.repoName,
            webhookUrl
        )
    }

    private fun makeWebhookUrl(roomId: Long): String {
        return "$ngrokUrl/github/$roomId"
    }
}
