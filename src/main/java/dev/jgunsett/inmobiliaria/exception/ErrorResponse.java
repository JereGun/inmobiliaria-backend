package dev.jgunsett.inmobiliaria.exception;

import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
	
	private int status;
	private String message;
	private LocalDateTime timestamp;

}
