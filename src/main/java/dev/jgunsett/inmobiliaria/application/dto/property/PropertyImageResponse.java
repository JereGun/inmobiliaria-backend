package dev.jgunsett.inmobiliaria.application.dto.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyImageResponse {

    private Long id;
    private String url;
    private boolean cover;
}
