package dev.jgunsett.inmobiliaria.application.dto.property;

import java.util.HashSet;
import java.util.Set;

import dev.jgunsett.inmobiliaria.domain.enums.Amenity;
import dev.jgunsett.inmobiliaria.domain.enums.OperationType;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyCreateRequest {
	
	@NotBlank
	private String name;
	
	@NotNull
	private Long ownerId;
	
	@NotNull
	private PropertyType propertyType;
	
	private Set<OperationType> operationTypes;
	
	//Valores de la Propiedad
	private Double salePrice;
	private Double rentPrice;
	
	//Direccion
	private String street;
	private String numeration;
	private String floor;
	private String department;
	private String zipCode;
	private String city;
	private String province;
	private String country;
	
	//Caracteristicas
	private Set<Amenity> amenities = new HashSet<>();
	private Integer bathrooms;
	private Integer bedrooms;
	private Boolean furnished;
	private Integer constructionYear;
	private Double totalArea;
	private Double coveredArea;
	
	private String description;

}
