package dev.jgunsett.inmobiliaria.application.dto.invoice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import dev.jgunsett.inmobiliaria.domain.enums.InvoiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class InvoiceUpdateRequest {

    @NotNull
    private InvoiceType type;

    @NotNull
    private LocalDateTime date;

    private LocalDate dueDate;

    @Valid
    @NotEmpty
    private List<InvoiceLineRequest> lines;
}
