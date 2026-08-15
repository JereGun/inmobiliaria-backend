package dev.jgunsett.inmobiliaria.controller;

import dev.jgunsett.inmobiliaria.application.dto.public_.ContactRequest;
import dev.jgunsett.inmobiliaria.application.service.CompanyService;
import dev.jgunsett.inmobiliaria.application.service.EmailSenderService;
import dev.jgunsett.inmobiliaria.application.dto.company.CompanyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/contact")
@RequiredArgsConstructor
public class PublicContactController {

    private final EmailSenderService emailSenderService;
    private final CompanyService companyService;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendContact(@Valid @RequestBody ContactRequest request) {
        CompanyResponse company = companyService.get();

        if (company == null || company.getEmail() == null) {
            return;
        }

        String subject = request.getPropertyId() != null
                ? "Consulta sobre propiedad #" + request.getPropertyId() + " - " + request.getName()
                : "Consulta general - " + request.getName();

        String body = buildEmailBody(request);

        emailSenderService.sendNotificationEmail(company.getEmail(), subject, body);
    }

    private String buildEmailBody(ContactRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nueva consulta recibida desde el sitio web.\n\n");
        sb.append("Nombre: ").append(req.getName()).append("\n");
        sb.append("Email: ").append(req.getEmail()).append("\n");
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            sb.append("Teléfono: ").append(req.getPhone()).append("\n");
        }
        if (req.getPropertyId() != null) {
            sb.append("Propiedad ID: ").append(req.getPropertyId()).append("\n");
        }
        sb.append("\nMensaje:\n").append(req.getMessage());
        return sb.toString();
    }
}
