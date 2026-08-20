package dev.jgunsett.inmobiliaria.exception;

import java.time.LocalDateTime;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
	
	private int status;
	private String message;
	private LocalDateTime timestamp;
	private Map<String, String> fieldErrors;

	public ErrorResponse(int status, String message, LocalDateTime timestamp) {
		this(status, message, timestamp, null);
	}

}
