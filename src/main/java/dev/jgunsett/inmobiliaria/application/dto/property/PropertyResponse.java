package dev.jgunsett.inmobiliaria.application.dto.property;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.jgunsett.inmobiliaria.domain.enums.Amenity;
import dev.jgunsett.inmobiliaria.domain.enums.OperationType;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyStatus;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyResponse {
	
	private Long id;
	
	private String name;
	
	private Long ownerId;
	
	private String ownerFullName;
	
	private PropertyType propertyType;
	
	private Set<OperationType> operationTypes;
	
	private PropertyStatus status;
	
	private Double salePrice;
	
	private Double rentPrice;
	
	private String fullAddress;
	
	private Set<Amenity> amenities = new HashSet<>();
	
	private Integer bathrooms;
	
	private Integer bedrooms;
	
	private Boolean furnished;
	
	private Integer constructionYear;
	
	private Double totalArea;
	
	private Double coveredArea;
	
	private String description;
	
	private List<PropertyImageResponse> images = new ArrayList<>();
	
	private LocalDateTime creationDate;
	
	private LocalDateTime modificationDate;
	
}
