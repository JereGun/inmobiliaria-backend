package dev.jgunsett.inmobiliaria.application.dto.company;

public record PublicCompanyResponse(
        String name,
        String street,
        String number,
        String city,
        String province,
        String country,
        String email,
        String phone,
        String website,
        String logoUrl) {

    public static PublicCompanyResponse from(CompanyResponse company) {
        return new PublicCompanyResponse(
                company.getName(),
                company.getStreet(),
                company.getNumber(),
                company.getCity(),
                company.getProvince(),
                company.getCountry(),
                company.getEmail(),
                company.getPhone(),
                company.getWebsite(),
                company.getLogoUrl());
    }
}
