package project.backend.auth.token.dao

import org.springframework.data.repository.CrudRepository
import project.backend.auth.token.entity.TokenRedis
import java.util.Optional

interface TokenRedisRepository : CrudRepository<TokenRedis, Long> {
    fun findByAccessToken(accessToken: String): Optional<TokenRedis>
}