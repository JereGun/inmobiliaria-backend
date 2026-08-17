package dev.jgunsett.inmobiliaria.application.dto.collection;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LateFeeRefreshResponse {
    private LocalDate date;
    private int updated;
}
