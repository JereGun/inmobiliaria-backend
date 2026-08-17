package dev.jgunsett.inmobiliaria.application.dto.invoice;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceReminderRunResponse {
    private LocalDate date;
    private int sent;
}
