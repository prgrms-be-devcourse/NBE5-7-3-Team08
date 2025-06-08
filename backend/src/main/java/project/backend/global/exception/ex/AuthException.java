package project.backend.global.exception.ex;

import lombok.Getter;
import project.backend.global.exception.errorcode.AuthErrorCode;

@Getter
public class AuthException extends BaseException {

	private final Long roomId;
	private final String inviteCode;

	public AuthException(AuthErrorCode errorCode) {
		super(errorCode);
		this.roomId = null;
		this.inviteCode = null;
	}

}

