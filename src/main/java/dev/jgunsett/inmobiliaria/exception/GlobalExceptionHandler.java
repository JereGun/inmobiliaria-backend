package dev.jgunsett.inmobiliaria.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;
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

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(
						error -> error.getField(),
						error -> error.getDefaultMessage() == null ? "Valor inválido" : error.getDefaultMessage(),
						(existing, ignored) -> existing,
						LinkedHashMap::new));

		return errorResponse(
				HttpStatus.BAD_REQUEST,
				fieldErrors.isEmpty() ? "Revisá los datos ingresados" : String.join("; ", fieldErrors.values()),
				fieldErrors);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
		String message = ex.getConstraintViolations().stream()
				.map(violation -> violation.getMessage())
				.distinct()
				.collect(Collectors.joining("; "));

		return errorResponse(HttpStatus.BAD_REQUEST,
				message.isBlank() ? "Revisá los datos ingresados" : message, null);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleMalformedRequest(HttpMessageNotReadableException ex) {
		return errorResponse(HttpStatus.BAD_REQUEST,
				"La solicitud contiene datos inválidos o incompletos", null);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
		log.warn("Violación de integridad de datos al procesar la solicitud", ex);
		return errorResponse(HttpStatus.CONFLICT,
				"No se pudo guardar la información porque entra en conflicto con datos existentes", null);
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

	private ResponseEntity<ErrorResponse> errorResponse(
			HttpStatus status,
			String message,
			Map<String, String> fieldErrors) {
		ErrorResponse error = new ErrorResponse(status.value(), message, LocalDateTime.now(), fieldErrors);
		return ResponseEntity.status(status).body(error);
	}

}
