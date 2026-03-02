package dev.jgunsett.inmobiliaria.application.dto.customer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.DocumentType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponse {

    private Long id;

    private String name;
    private String middleName;
    private String surname;
    private String secondSurname;

    private String fullName;

    private DocumentType documentType;
    private String documentNumber;
    private String cuit;

    private LocalDate birthdate;

    private String email;
    private String phone;

    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;
}
