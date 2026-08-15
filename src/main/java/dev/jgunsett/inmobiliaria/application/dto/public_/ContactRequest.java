package dev.jgunsett.inmobiliaria.application.dto.public_;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    private String phone;

    @NotBlank(message = "El mensaje es obligatorio")
    private String message;

    private Long propertyId;
}
