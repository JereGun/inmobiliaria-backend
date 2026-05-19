package dev.jgunsett.inmobiliaria.application.dto.company;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyUpdateRequest {

	@NotBlank
	private String name;

	private String taxId;
	private String fiscalCondition;
	private String street;
	private String number;
	private String city;
	private String province;
	private String country;

	@Email
	@NotBlank
	private String email;

	private String phone;
	private String website;
	private String logoUrl;
}
