package dev.jgunsett.inmobiliaria.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
		
		ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(),LocalDateTime.now());
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}
	
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
		
		
		ErrorResponse error = new ErrorResponse(
				HttpStatus.CONFLICT.value(),
				ex.getMessage(),
				LocalDateTime.now()
		);
		
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
		
	}
	
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {

	    ErrorResponse error = new ErrorResponse(
	            HttpStatus.UNPROCESSABLE_ENTITY.value(),
	            ex.getMessage(),
	            LocalDateTime.now()
	    );

	    return ResponseEntity
	            .status(HttpStatus.UNPROCESSABLE_ENTITY)
	            .body(error);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {

	    ErrorResponse error = new ErrorResponse(
	            HttpStatus.UNAUTHORIZED.value(),
	            ex.getMessage(),
	            LocalDateTime.now()
	    );

	    return ResponseEntity
	            .status(HttpStatus.UNAUTHORIZED)
	            .body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
		log.error("Error no controlado al procesar la solicitud", ex);
		ErrorResponse error = new ErrorResponse(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Ocurrió un error interno al procesar la solicitud",
				LocalDateTime.now()
		);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

}
