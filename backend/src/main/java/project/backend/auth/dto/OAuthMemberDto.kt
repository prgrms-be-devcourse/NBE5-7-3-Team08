package project.backend.auth.dto

data class OAuthMemberDto(
	val email: String,
	val nickname: String,
	val login: String
)