package dev.jgunsett.inmobiliaria.application.dto.invoice;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceBatchResponse {

    private int requested;
    private int succeeded;
    private int failed;
    private List<InvoiceBatchItemResponse> results;
}
