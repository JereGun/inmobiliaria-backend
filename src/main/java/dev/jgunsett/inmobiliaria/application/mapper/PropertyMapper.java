package dev.jgunsett.inmobiliaria.application.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.jgunsett.inmobiliaria.application.dto.property.PropertyCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertyImageResponse;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertyResponse;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertyUpdateRequest;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Property;
import dev.jgunsett.inmobiliaria.domain.enums.Amenity;
import dev.jgunsett.inmobiliaria.domain.enums.OperationType;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyType;

public class PropertyMapper {
	
	public static Property toEntity(PropertyCreateRequest dto, Customer owner) {
		
		return Property.builder()
				.name(dto.getName())
				.owner(owner)
				.propertyType(dto.getPropertyType())
				.operationTypes(dto.getOperationTypes())
				.salePrice(dto.getSalePrice())
				.rentPrice(dto.getRentPrice())
				.street(dto.getStreet())
				.numeration(dto.getNumeration())
				.floor(dto.getFloor())
				.department(dto.getDepartment())
				.zipCode(dto.getZipCode())
				.city(dto.getCity())
				.province(dto.getProvince())
				.country(dto.getCountry())
				.amenities(dto.getAmenities())
				.bathrooms(dto.getBathrooms())
				.bedrooms(dto.getBedrooms())
				.furnished(dto.getFurnished())
				.constructionYear(dto.getConstructionYear())
				.totalArea(dto.getTotalArea())
				.coveredArea(dto.getCoveredArea())
				.description(dto.getDescription())
				.build();
	}
	
	public static PropertyResponse toResponse(Property p) {

	    Long ownerId = null;
	    String ownerFullName = null;
	    if (p.getOwner() != null) {
	        ownerId = p.getOwner().getId();
	        ownerFullName = p.getOwner().getFullName();
	    }

	    PropertyType propertyType = null;
	    if (p.getPropertyType() != null) {
	        propertyType = p.getPropertyType();
	    }

	    Set<OperationType> operationTypes = new HashSet<>();
	    if (p.getOperationTypes() != null) {
	        operationTypes = p.getOperationTypes();
	    }

	    Set<Amenity> amenities = new HashSet<>();
	    if (p.getAmenities() != null) {
	        amenities = p.getAmenities();
	    }

	    String fullAddress = null;
	    if (p.getFullAddress() != null) {
	        fullAddress = p.getFullAddress();
	    }

	    // imagenes
	    List<PropertyImageResponse> images = new ArrayList<>();

	    if (p.getImages() != null) {
	        images = p.getImages().stream()
	                .map(img -> PropertyImageResponse.builder()
	                        .id(img.getId())
	                        .url(img.getUrl())
	                        .cover(img.isCover())
	                        .build())
	                .toList();
	    }

	    return PropertyResponse.builder()
	            .id(p.getId())
	            .name(p.getName())
	            .ownerId(ownerId)
	            .ownerFullName(ownerFullName)
	            .propertyType(propertyType)
	            .operationTypes(operationTypes)
	            .status(p.getStatus())
	            .salePrice(p.getSalePrice())
	            .rentPrice(p.getRentPrice())
	            .fullAddress(fullAddress)
	            .amenities(amenities)
	            .bathrooms(p.getBathrooms())
	            .bedrooms(p.getBedrooms())
	            .furnished(p.getFurnished())
	            .constructionYear(p.getConstructionYear())
	            .totalArea(p.getTotalArea())
	            .coveredArea(p.getCoveredArea())
	            .description(p.getDescription())
	            .creationDate(p.getCreationDate())
	            .modificationDate(p.getModificationDate())
	            .images(images)
	            .build();
	}
	
	public static void updateEntity(Property property, PropertyUpdateRequest dto) {
		
		if (dto.getName() != null) {
			property.setName(dto.getName());
		}
		
		if (dto.getOperationTypes() != null) {
			property.getOperationTypes().clear();
			property.getOperationTypes().addAll(dto.getOperationTypes());
		}
		
		if (dto.getSalePrice() != null) {
			property.setSalePrice(dto.getSalePrice());
		}
		
		if (dto.getRentPrice() != null) {
			property.setRentPrice(dto.getRentPrice());
		}
		
		if (dto.getStreet() != null) {
			property.setStreet(dto.getStreet());
		}
		
		if (dto.getNumeration() != null) {
			property.setNumeration(dto.getNumeration());
		}
		
		if (dto.getFloor() != null) {
			property.setFloor(dto.getFloor());
		}
		
		if (dto.getDepartment() != null) {
			property.setDepartment(dto.getDepartment());
		}
		
		if (dto.getZipCode() != null) {
			property.setZipCode(dto.getZipCode());
		}
		
		if (dto.getCity() != null) {
			property.setCity(dto.getCity());
		}
		
		if (dto.getProvince() != null) {
			property.setProvince(dto.getProvince());
		}
		
		if (dto.getCountry() != null) {
			property.setCountry(dto.getCountry());
		}
		
		if (dto.getAmenities() != null) {
			property.getAmenities().clear();
			property.getAmenities().addAll(dto.getAmenities());
		}
		
		if (dto.getBathrooms() != null) {
			property.setBathrooms(dto.getBathrooms());
		}
		
		if (dto.getBedrooms() != null) {
			property.setBedrooms(dto.getBedrooms());
		}
		
		if (dto.getFurnished() != null) {
			property.setFurnished(dto.getFurnished());
		}
		
		if (dto.getConstructionYear() != null) {
			property.setConstructionYear(dto.getConstructionYear());
		}
		
		if (dto.getTotalArea() != null) {
			property.setTotalArea(dto.getTotalArea());
		}
		
		if (dto.getCoveredArea() != null) {
			property.setCoveredArea(dto.getCoveredArea());
		}
		
		if (dto.getDescription() != null) {
			property.setDescription(dto.getDescription());
		}
	}

}
