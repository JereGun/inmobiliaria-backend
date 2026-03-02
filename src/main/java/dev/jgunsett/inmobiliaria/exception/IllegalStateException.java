package dev.jgunsett.inmobiliaria.exception;

import org.springframework.http.HttpStatus;

public class IllegalStateException extends ApiException {
	
	public IllegalStateException (String message) {
		super(message);
	}
	
	@Override
	public HttpStatus getStatus() {
		return HttpStatus.CONFLICT; //409
	}

}
