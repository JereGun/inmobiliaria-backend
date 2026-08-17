package dev.jgunsett.inmobiliaria.application.dto.collection;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverdueCollectionResponse {
    private List<OverdueInvoiceResponse> content;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
    private BigDecimal totalOutstanding;
    private long oldestDaysOverdue;
}
