package project.backend.global.security.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.backend.global.security.app.AuthService;
import project.backend.global.security.jwt.JwtProvider;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@GetMapping("/refresh")
	public void validateToken(HttpServletRequest request, HttpServletResponse response) {
		authService.validateAuthentication(request, response);
	}

}
