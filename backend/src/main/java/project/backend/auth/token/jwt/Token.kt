package project.backend.auth.token.jwt

data class Token(
	val accessToken: String,
	val refreshToken: String
)