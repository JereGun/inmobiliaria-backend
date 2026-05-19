package dev.jgunsett.inmobiliaria.application.dto.company;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyResponse {

	private Long id;
	private String name;
	private String taxId;
	private String fiscalCondition;
	private String street;
	private String number;
	private String city;
	private String province;
	private String country;
	private String email;
	private String phone;
	private String website;
	private String logoUrl;
	private LocalDateTime creationDate;
	private LocalDateTime modificationDate;
}
