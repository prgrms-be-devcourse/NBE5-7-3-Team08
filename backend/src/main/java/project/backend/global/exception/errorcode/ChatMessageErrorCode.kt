package project.backend.global.exception.errorcode

import org.springframework.http.HttpStatus

enum class ChatMessageErrorCode(
    override val code: String,
    override val message: String,
    override val status: HttpStatus
) : ErrorCode {
    INVALID_KEYWORD_LENGTH(
        "CME-001", "검색어는 최소 2자 이상이어야 합니다.",
        HttpStatus.BAD_REQUEST
    ),
    INVALID_ROUTE("CME-002", "유효하지 않은 경로입니다.", HttpStatus.BAD_REQUEST),
    MESSAGE_NOT_FOUND("CME-003", "메세지를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);
}
