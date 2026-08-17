package dev.jgunsett.inmobiliaria.controller;

import dev.jgunsett.inmobiliaria.application.dto.company.CompanyResponse;
import dev.jgunsett.inmobiliaria.application.dto.company.PublicCompanyResponse;
import dev.jgunsett.inmobiliaria.application.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/company")
@RequiredArgsConstructor
public class PublicCompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<PublicCompanyResponse> getCompany() {
        CompanyResponse company = companyService.get();
        return company == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(PublicCompanyResponse.from(company));
    }
}
