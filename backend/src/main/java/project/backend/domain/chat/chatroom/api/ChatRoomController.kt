package project.backend.domain.chat.chatroom.api

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import project.backend.auth.dto.MemberDetails
import project.backend.domain.chat.chatroom.app.ChatRoomService
import project.backend.domain.chat.chatroom.dto.*

@RestController
@RequestMapping("/chat-rooms")
class ChatRoomController(
    private val chatRoomService: ChatRoomService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createChatRoom(
        @Valid @RequestBody request: ChatRoomRequest,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ChatRoomSimpleResponse {
        return chatRoomService.createChatRoom(request, memberDetails.id)
    }

    @PostMapping("/join")
    fun joinChatRoom(
        @RequestBody request: InviteJoinRequest,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): InviteJoinResponse {
        return chatRoomService.joinChatRoom(request.inviteCode, memberDetails.id)
    }

    @GetMapping("/recent")
    fun getRecentRoomInviteCode(
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): RecentChatRoomResponse {
        val inviteCode = chatRoomService.getRecentRoomInviteCode(memberDetails.id)
        return RecentChatRoomResponse(inviteCode)
    }

    @GetMapping
    fun getChatRooms(
        @AuthenticationPrincipal memberDetails: MemberDetails,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<RoomInfoResponse> {
        return chatRoomService.findChatRoomsByMemberId(memberDetails.id, pageable)
    }

    @GetMapping("/{roomId}/participants")
    fun getParticipants(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): MutableList<Any>? {
        return chatRoomService.getParticipants(memberDetails.id, roomId)
    }

    @GetMapping("/mine/{memberId}")
    fun findMyAllChatRooms(
        @PathVariable memberId: Long,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<MyChatRoomResponse> {
        return chatRoomService.findAllRoomsByOwnerId(memberId, pageable)
    }

    @DeleteMapping("/{roomId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun leaveChatRoom(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ) {
        chatRoomService.leaveChatRoom(roomId, memberDetails.id)
    }

    @GetMapping("/{inviteCode}")
    fun entryChatRoom(
        @PathVariable inviteCode: String,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): EntryRoomResponse {
        return chatRoomService.getEntryInfo(inviteCode, memberDetails.id)
    }

    @GetMapping("/info/{inviteCode}")
    fun getChatRoomDetails(
        @PathVariable inviteCode: String,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): RoomInfoResponse {
        return chatRoomService.getRoomInfo(inviteCode, memberDetails.id)
    }

    @DeleteMapping("/{roomId}")
    fun deleteChatRoom(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ) {
        chatRoomService.deleteChatRoom(roomId, memberDetails.id)
    }
}
