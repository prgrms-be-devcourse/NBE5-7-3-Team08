package project.backend.domain.chat.chatmessage.api

import ChatMessageResponse
import org.springframework.data.domain.Page
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import project.backend.auth.dto.MemberDetails
import project.backend.domain.chat.chatmessage.app.ChatMessageService
import project.backend.domain.chat.chatmessage.dto.*
import project.backend.domain.imagefile.ImageFileService
import java.security.Principal

@RestController
class ChatMessageController(
    private val chatMessageService: ChatMessageService,
    private val imageFileService: ImageFileService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @MessageMapping("/send-message/{roomId}")
    fun sendMessage(
        @DestinationVariable roomId: Long,
        @Payload request: ChatMessageRequest,
        principal: Principal
    ): ChatMessageResponse {
        val response = chatMessageService.save(roomId, request, principal.name)
        messagingTemplate.convertAndSend("/topic/chat/$roomId", response)
        return response
    }

    @MessageMapping("/edit-message/{roomId}")
    fun editMessage(
        @DestinationVariable roomId: Long,
        @Payload request: ChatMessageEditRequest,
        principal: Principal
    ): ChatMessageResponse {
        val response = chatMessageService.editMessage(roomId, request, principal.name)
        messagingTemplate.convertAndSend("/topic/chat/$roomId", response)
        return response
    }

    @GetMapping("/chat/search/{roomId}")
    fun searchMessages(
        @AuthenticationPrincipal memberDetails: MemberDetails,
        @PathVariable("roomId") roomId: Long,
        @RequestParam("keyword") keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<ChatMessageSearchResponse> {
        val request = ChatMessageSearchRequest.of(keyword, page, size)
        return chatMessageService.searchMessages(memberDetails.id, roomId, request)
    }

    @GetMapping("/{roomId}/messages")
    fun getMessages(
        @AuthenticationPrincipal memberDetails: MemberDetails,
        @PathVariable roomId: Long,
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "30") size: Int
    ): ChatScrollResponse {
        return chatMessageService.getMessagesByRoomId(memberDetails.id, roomId, cursor, size)
    }

    @MessageMapping("/delete-message/{roomId}")
    fun deleteMessage(
        @DestinationVariable roomId: Long,
        @Payload messageId: Long,
        principal: Principal
    ): ChatMessageResponse {
        val response = chatMessageService.deleteMessage(roomId, messageId, principal.name)
        messagingTemplate.convertAndSend("/topic/chat/$roomId", response)
        return response
    }

    @PostMapping("/send-image")
    fun uploadImage(@RequestParam image: MultipartFile): Long? {
        val imageFile = imageFileService.saveChatImage(image)
        return imageFile.imageId
    }
}

