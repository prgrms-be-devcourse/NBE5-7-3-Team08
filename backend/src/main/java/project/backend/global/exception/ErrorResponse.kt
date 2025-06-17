package project.backend.global.exception

import org.springframework.validation.FieldError
import project.backend.global.exception.errorcode.ErrorCode

data class ErrorResponse(
    val code: String,
    val message: String?
) {
    companion object {
        fun toResponse(errorCode: ErrorCode): ErrorResponse =
            ErrorResponse(errorCode.code, errorCode.message)

        fun toResponse(error: FieldError): ErrorResponse =
            ErrorResponse("VALIDATION_FAILED", error.defaultMessage)
    }
}
