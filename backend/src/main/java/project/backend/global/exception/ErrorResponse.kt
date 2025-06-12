package project.backend.global.exception

import lombok.Builder
import lombok.Getter
import org.springframework.validation.FieldError
import project.backend.global.exception.errorcode.ErrorCode

@Builder
@Getter
data class ErrorResponse(
    private val code: String,
    private val message: String?
) {
    companion object {
        @JvmStatic //fixme 자바에서 사용하려면 필요, 추후 없애자
        fun toResponse(errorCode: ErrorCode): ErrorResponse =
            ErrorResponse(errorCode.code, errorCode.message)


        @JvmStatic
        fun toResponse(error: FieldError): ErrorResponse =
            ErrorResponse("VALIDATION_FAILED", error.defaultMessage)
    }
}
