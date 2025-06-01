package project.backend.global.security.app;

import static project.backend.global.security.jwt.JwtProvider.REFRESH_TOKEN_VALIDATION_SECOND;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import project.backend.global.exception.errorcode.AuthErrorCode;
import project.backend.global.exception.ex.AuthException;
import project.backend.global.redis.dao.TokenRedisRepository;
import project.backend.global.redis.entity.TokenRedis;
import project.backend.global.security.jwt.JwtProvider;
import project.backend.global.security.jwt.TokenStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtProvider jwtProvider;
	private final TokenRedisRepository tokenRedisRepository;

	public void validateAuthentication(HttpServletRequest request, HttpServletResponse response) {
		try {
			String accessTokenInCookie = getAccessTokenFromCookie(request);

			if (accessTokenInCookie == null) {

			}

			log.info("accessToken = {}", accessToken);
			log.info("refreshToken = {}", refreshToken);

			TokenStatus tokenStatus = jwtProvider.validateAccessToken(accessToken);
			log.info("tokenStatus = {}", tokenStatus);

			switch (tokenStatus) {
				case VALID:
					response.setStatus(HttpServletResponse.SC_OK);
					break;

				case EXPIRED:
					try {
						// refreshToken 재검증
						JWTVerifier jwtVerifier = jwtProvider.getJwtVerifier(
							REFRESH_TOKEN_VALIDATION_SECOND);
						jwtVerifier.verify(refreshToken);

						String newAccessToken = jwtProvider.regenerateAccessToken(refreshToken);
						CookieUtils.saveCookie(response, newAccessToken);
						tokenRedis.updateAccessToken(newAccessToken);
						tokenRedisRepository.save(tokenRedis);
						log.info("액세스 토큰 재발급 완료");
						response.setStatus(HttpServletResponse.SC_OK);
					} catch (JWTVerificationException e) {
						log.warn("리프레시 토큰이 유효하지 않습니다: {}", e.getMessage());
						response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					}
					break;

				default:
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} catch (JwtException e) {
			log.warn("토큰 인증 실패: {}", e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}


	private String getAccessTokenFromCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("accessToken".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}


}
