package project.backend.auth.token.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.*
import io.lettuce.core.RedisException
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import project.backend.auth.app.CookieUtils
import project.backend.auth.dto.MemberDetails
import project.backend.auth.token.dao.TokenRedisRepository
import project.backend.domain.member.dao.MemberRepository
import project.backend.global.exception.errorcode.TokenErrorCode
import project.backend.global.exception.ex.CustomJwtException
import java.util.Date

@Component
class JwtProvider(
    val tokenRedisRepository: TokenRedisRepository,
    val memberRepository: MemberRepository,
    @Value("\${jwt.info.secret}")
    val secretKey: String,
    @Value("\${jwt.info.issuer}")
    val issuer: String
) {

    companion object {
        const val TOKEN_VALIDATION_SECOND = 10L
        const val REFRESH_TOKEN_VALIDATION_SECOND = 7 * 24 * 60 * 60L
    }

    private fun getSignatureAlgorithm(): Algorithm = Algorithm.HMAC256(secretKey)

    fun generateTokenPair(memberDetails: MemberDetails): Token {
        val payload = mapOf("username" to memberDetails.username)
        return Token(
            accessToken = generateAccessToken(payload),
            refreshToken = generateRefreshToken(payload)
        )
    }

    private fun generateAccessToken(payload: Map<String, String>): String =
        doGenerateToken(TOKEN_VALIDATION_SECOND, payload)

    private fun generateRefreshToken(payload: Map<String, String>): String =
        doGenerateToken(REFRESH_TOKEN_VALIDATION_SECOND, payload)

    fun regenerateAccessToken(refreshToken: String): String {
        val decodedJWT = getJwtVerifier(REFRESH_TOKEN_VALIDATION_SECOND).verify(refreshToken)
        val username = decodedJWT.getClaim("username").asString()
        return generateAccessToken(mapOf("username" to username))
    }

    fun getJwtVerifier(expiresSeconds: Long): JWTVerifier =
        JWT.require(getSignatureAlgorithm())
            .withIssuer(issuer)
            .acceptExpiresAt(expiresSeconds)
            .build()

    fun validateAccessToken(token: String): TokenStatus = try {
        getJwtVerifier(TOKEN_VALIDATION_SECOND).verify(token)
        TokenStatus.VALID
    } catch (e: TokenExpiredException) {
        TokenStatus.EXPIRED
    } catch (e: SignatureVerificationException) {
        TokenStatus.INVALID_SIGNATURE
    } catch (e: JWTDecodeException) {
        TokenStatus.MALFORMED
    } catch (e: JWTVerificationException) {
        TokenStatus.UNKNOWN_ERROR
    } catch (e: Exception) {
        TokenStatus.UNKNOWN_ERROR
    }

    private fun doGenerateToken(expiration: Long, payload: Map<String, String>): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + expiration * 1000))
            .withPayload(payload)
            .withIssuer(issuer)
            .sign(getSignatureAlgorithm())
    }

    private fun getUsernameFromToken(token: String): String =
        getJwtVerifier(TOKEN_VALIDATION_SECOND)
            .verify(token)
            .getClaim("username")
            .asString()

    fun getAuthentication(token: String): Authentication {
        val username = getUsernameFromToken(token)
        val member = memberRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("존재 하지 않는 유저입니다.")
        val memberDetails = MemberDetails(member)
        return UsernamePasswordAuthenticationToken(memberDetails, token, memberDetails.authorities)
    }

    @Transactional
    fun replaceAccessToken(response: HttpServletResponse, token: String) {
        try {
            val tokenRedis = tokenRedisRepository.findByAccessToken(token)
                .orElseThrow { CustomJwtException(TokenErrorCode.NOT_FOUND_TOKEN) }

            val refreshToken = tokenRedis.refreshToken

            getJwtVerifier(REFRESH_TOKEN_VALIDATION_SECOND).verify(refreshToken)

            val newAccessToken = regenerateAccessToken(refreshToken)
            CookieUtils.saveCookie(response, newAccessToken)

            tokenRedis.updateAccessToken(newAccessToken)
            tokenRedisRepository.save(tokenRedis)

        } catch (e: TokenExpiredException) {
            throw CustomJwtException(TokenErrorCode.EXPIRED_TOKEN)
        } catch (e: JwtException) {
            throw CustomJwtException(TokenErrorCode.INVALID_TOKEN)
        } catch (e: RedisException) {
            throw CustomJwtException(TokenErrorCode.UNKNOWN_ERROR)
        }
    }
}