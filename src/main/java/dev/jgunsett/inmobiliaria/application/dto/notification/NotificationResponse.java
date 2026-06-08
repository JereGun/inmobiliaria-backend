package dev.jgunsett.inmobiliaria.application.dto.notification;

import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.NotificationType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long contractId;
    private Long invoiceId;
    private Long propertyId;
    private String propertyName;
    private NotificationType type;
    private String title;
    private String message;
    private LocalDate dueDate;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime creationDate;
}
