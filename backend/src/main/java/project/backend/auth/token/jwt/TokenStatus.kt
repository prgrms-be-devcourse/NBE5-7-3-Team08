package project.backend.auth.token.jwt

enum class TokenStatus {
    VALID,
    EXPIRED,
    INVALID_SIGNATURE,
    MALFORMED,
    UNKNOWN_ERROR
}
