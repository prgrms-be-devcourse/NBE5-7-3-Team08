package project.backend.auth.api

import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import project.backend.auth.token.jwt.JwtProvider

@RestController
@RequestMapping("/token")
class AuthController(
    private val jwtProvider: JwtProvider
) {

    @GetMapping("/refresh")
    fun validateToken(
        @CookieValue(name = "accessToken") token: String,
        response: HttpServletResponse
    ): String {
        jwtProvider.replaceAccessToken(response, token)
        return "토큰 재발급 성공"
    }
}