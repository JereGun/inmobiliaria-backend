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
public class InvoiceCreateRequest {

    @NotNull
    private Long customerId;

    private Long contractId; // opcional (venta / manual)

    @NotNull
    private InvoiceType type;

    @NotNull
    private LocalDateTime date;

    /** Fecha de vencimiento. Si se omite, se utiliza la fecha de emisión. */
    private LocalDate dueDate;

    @Valid
    @NotEmpty
    private List<InvoiceLineRequest> lines;
}
