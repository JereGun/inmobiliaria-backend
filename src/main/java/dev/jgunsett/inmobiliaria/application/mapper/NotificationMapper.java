package dev.jgunsett.inmobiliaria.application.mapper;

import dev.jgunsett.inmobiliaria.application.dto.notification.NotificationResponse;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.entity.Notification;
import dev.jgunsett.inmobiliaria.domain.entity.Property;

public class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        Contract contract = notification.getContract();
        Property property = contract != null ? contract.getProperty() : null;

        return NotificationResponse.builder()
                .id(notification.getId())
                .contractId(contract != null ? contract.getId() : null)
                .invoiceId(notification.getInvoice() != null ? notification.getInvoice().getId() : null)
                .propertyId(property != null ? property.getId() : null)
                .propertyName(property != null ? property.getName() : null)
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .dueDate(notification.getDueDate())
                .read(notification.getRead())
                .readAt(notification.getReadAt())
                .creationDate(notification.getCreationDate())
                .build();
    }
}
