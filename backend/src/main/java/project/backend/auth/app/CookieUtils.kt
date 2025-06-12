package project.backend.auth.app

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

object CookieUtils {

    fun saveCookie(response: HttpServletResponse, accessToken: String) {
        val cookie = Cookie("accessToken", accessToken).apply {
            path = "/"
            isHttpOnly = true
            maxAge = 60 * 60 * 24 * 7
        }
        response.addCookie(cookie)
    }

    fun deleteCookie(response: HttpServletResponse) {
        val cookie = Cookie("accessToken", null).apply {
            path = "/"
            isHttpOnly = true
            maxAge = 0
        }
        response.addCookie(cookie)
    }

    fun getCookie(request: HttpServletRequest, name: String): Cookie? {
        return request.cookies
            ?.find { it.name == name }
    }
}