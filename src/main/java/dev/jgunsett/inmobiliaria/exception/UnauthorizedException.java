package dev.jgunsett.inmobiliaria.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {

	public UnauthorizedException(String message) {
		super(message);
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.UNAUTHORIZED;
	}
}
