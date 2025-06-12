package project.backend.auth.token.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed

@RedisHash(value = "token", timeToLive = 604800)
data class TokenRedis(
    @Id
    val id: Long,

    @Indexed
    var accessToken: String,

    val refreshToken: String,

    val githubAccess: String
) {
    fun updateAccessToken(accessToken: String) {
        this.accessToken = accessToken
    }
}