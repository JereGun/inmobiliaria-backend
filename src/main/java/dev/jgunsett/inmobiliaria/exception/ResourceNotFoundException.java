package dev.jgunsett.inmobiliaria.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException{
	
	public ResourceNotFoundException(String message) {
		super(message);
	}
	
	@Override
	public HttpStatus getStatus() {
		return HttpStatus.NOT_FOUND; //404
	}
}
