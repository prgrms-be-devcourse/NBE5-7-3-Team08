package project.backend.global.exception.ex;

import org.springframework.security.core.AuthenticationException;
import project.backend.global.exception.errorcode.TokenErrorCode;

public class JwtException extends AuthenticationException {

	private final TokenErrorCode errorCode;

	public JwtException(TokenErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public JwtException(TokenErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}
}
