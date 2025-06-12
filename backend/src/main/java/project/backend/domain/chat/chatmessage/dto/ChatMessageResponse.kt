import project.backend.domain.chat.chatmessage.entity.MessageStatus
import project.backend.domain.chat.chatmessage.entity.MessageType
import java.time.LocalDateTime

data class ChatMessageResponse(
    val content: String? = null,
    val senderName: String,
    val sendAt: LocalDateTime,
    val type: MessageType,
    val language: String? = null,
    val profileImageUrl: String? = null,
    val chatImageUrl: String? = null,
    val senderId: Long,
    val messageId: Long,
    val status: MessageStatus
)