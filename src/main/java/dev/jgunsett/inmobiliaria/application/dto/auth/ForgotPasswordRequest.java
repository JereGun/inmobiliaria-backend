package dev.jgunsett.inmobiliaria.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @Email(message = "Email inválido")
    @NotBlank(message = "El email es requerido")
    private String email;
}
