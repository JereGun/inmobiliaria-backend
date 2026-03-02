package dev.jgunsett.inmobiliaria.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends ApiException {

    public BusinessException(String message) {
        super(message);
    }
    
    @Override
    public HttpStatus getStatus() {
    	return HttpStatus.UNPROCESSABLE_ENTITY; // 422
    }
}