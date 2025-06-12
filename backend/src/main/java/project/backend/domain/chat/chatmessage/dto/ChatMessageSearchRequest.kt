package project.backend.domain.chat.chatmessage.dto

data class ChatMessageSearchRequest(
    val keyword: String,
    val page: Int = 0,
    val pageSize: Int = 10
) {
    companion object {
        fun of(keyword: String, page: Int, pageSize: Int): ChatMessageSearchRequest {
            return ChatMessageSearchRequest(
                keyword = keyword,
                page = page,
                pageSize = pageSize
            )
        }
    }
}
