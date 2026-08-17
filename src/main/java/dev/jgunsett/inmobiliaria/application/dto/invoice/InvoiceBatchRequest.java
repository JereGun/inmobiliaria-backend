package dev.jgunsett.inmobiliaria.application.dto.invoice;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InvoiceBatchRequest {

    @NotEmpty
    @Size(max = 500)
    private List<Long> invoiceIds;
}
