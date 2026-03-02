package dev.jgunsett.inmobiliaria.application.dto.customer;

import java.time.LocalDate;

import dev.jgunsett.inmobiliaria.domain.enums.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerCreateRequest {

    @NotBlank
    private String name;

    private String middleName;

    @NotBlank
    private String surname;

    private String secondSurname;

    @NotNull
    private DocumentType documentType;

    @NotBlank
    private String documentNumber;

    private String cuit;

    private LocalDate birthdate;

    @Email
    @NotBlank
    private String email;

    private String phone;
}
