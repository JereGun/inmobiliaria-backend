package dev.jgunsett.inmobiliaria.application.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import dev.jgunsett.inmobiliaria.application.dto.property.PropertyCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertyResponse;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertySearchResponse;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertyUpdateRequest;
import dev.jgunsett.inmobiliaria.application.mapper.PropertyMapper;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Property;
import dev.jgunsett.inmobiliaria.domain.entity.PropertyImage;
import dev.jgunsett.inmobiliaria.domain.enums.ContractStatus;
import dev.jgunsett.inmobiliaria.domain.enums.OperationType;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.IllegalStateException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.ContractRepository;
import dev.jgunsett.inmobiliaria.repository.CustomerRepository;
import dev.jgunsett.inmobiliaria.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyService {
	
	private final PropertyRepository propertyRepository;
	
	private final CustomerRepository customerRepository;
	
	private final ContractRepository contractRepository;
	
	// Creacion de Propiedad
	@Transactional
	public PropertyResponse create(PropertyCreateRequest req,
	                               List<MultipartFile> images,
	                               Integer coverIndex) {

	    Customer owner = customerRepository.findById(req.getOwnerId())
	            .orElseThrow(() -> new ResourceNotFoundException(
	                    "El dueño con el ID: " + req.getOwnerId() + " no existe"));

	    Property property = Property.builder()
	            .name(req.getName())
	            .owner(owner)
	            .propertyType(req.getPropertyType())
	            .operationTypes(req.getOperationTypes())
	            .salePrice(req.getSalePrice())
	            .rentPrice(req.getRentPrice())
	            .street(req.getStreet())
	            .numeration(req.getNumeration())
	            .floor(req.getFloor())
	            .department(req.getDepartment())
	            .zipCode(req.getZipCode())
	            .city(req.getCity())
	            .province(req.getProvince())
	            .country(req.getCountry())
	            .amenities(req.getAmenities())
	            .bathrooms(req.getBathrooms())
	            .bedrooms(req.getBedrooms())
	            .furnished(req.getFurnished())
	            .constructionYear(req.getConstructionYear())
	            .totalArea(req.getTotalArea())
	            .coveredArea(req.getCoveredArea())
	            .description(req.getDescription())
	            .build();

	    propertyRepository.save(property);

	    // imagenes
	    if (images != null && !images.isEmpty()) {

	        // validar coverIndex si viene
	        if (coverIndex != null &&
	                (coverIndex < 0 || coverIndex >= images.size())) {
	            throw new BusinessException("Índice de cover inválido");
	        }

	        saveImages(property, images, coverIndex);

	        // garantizar que haya exactamente una cover
	        ensureSingleCover(property);
	    }

	    return PropertyMapper.toResponse(property);
	}
	
	// modificacion de propiedad
	@Transactional
	public PropertyResponse update(Long id, PropertyUpdateRequest dto) {

	    Property property = propertyRepository.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException(
	                    "No existe propiedad con ID: " + id));

	    validateNumericFields(dto);
	    validateAreas(property, dto);

	    if (dto.getOperationTypes() != null &&
	            !dto.getOperationTypes().equals(property.getOperationTypes()) &&
	            contractRepository.existsByPropertyIdAndStatus(id, ContractStatus.ACTIVE)) {

	        throw new BusinessException(
	                "El tipo de operacion no se puede modificar si la propiedad posee un contracto Activo");
	    }

	    PropertyMapper.updateEntity(property, dto);

	    validateOperationTypes(property, dto);

	    // eliminar imagenes
	    if (dto.getImageIdsToDelete() != null &&
	            !dto.getImageIdsToDelete().isEmpty()) {

	        List<PropertyImage> imagesToRemove = property.getImages().stream()
	                .filter(img -> dto.getImageIdsToDelete().contains(img.getId()))
	                .toList();

	        for (PropertyImage image : imagesToRemove) {
	            deleteImageFile(image.getUrl());      // borrar archivo físico
	            property.getImages().remove(image);   // elimina de DB (orphanRemoval)
	        }
	    }

	    // reasignar cover (si viene)
	    if (dto.getCoverImageId() != null) {

	        boolean found = false;

	        for (PropertyImage img : property.getImages()) {
	            if (img.getId() != null &&
	                    img.getId().equals(dto.getCoverImageId())) {

	                img.setCover(true);
	                found = true;
	            } else {
	                img.setCover(false);
	            }
	        }

	        if (!found) {
	            throw new BusinessException("La imagen seleccionada como cover no pertenece a la propiedad");
	        }
	    }

	    // garantizar una sola cover
	    ensureSingleCover(property);

	    return PropertyMapper.toResponse(propertyRepository.save(property));
	}
	
	// Listar todas las propiedades
	@Transactional(readOnly = true)
	public Page<PropertyResponse> findAll(int page, int size) {
		
	    Pageable pageable = PageRequest.of(page, size);

	    Page<Property> propertiesPage = propertyRepository.findAll(pageable);

	    return propertiesPage.map(property -> {
	        // Fuerza la inicialización DENTRO de la transaccion
	        property.getOperationTypes().size();
	        property.getAmenities().size();
	        property.getImages().size();
	        return PropertyMapper.toResponse(property);
	    });
	}
	
	// Buscar propiedad por ID
	@Transactional(readOnly = true)
	public PropertyResponse findById(Long propertyId) {

	    Property property = propertyRepository.findById(propertyId)
	        .orElseThrow(() -> new ResourceNotFoundException("No existe propiedad con el ID " + propertyId));

	    property.getOperationTypes().size();
	    property.getAmenities().size();
	    property.getImages().size();

	    return PropertyMapper.toResponse(property);
	}
	
	// Busqueda por tipo de Operacion (Alquiler, Venta, etc.)
	@Transactional(readOnly = true)
	public Page<PropertyResponse> findRentalStatus(OperationType operation, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		
		Page<Property> propertiesPage = propertyRepository.findByOperationTypes(operation, pageable);
		
		return propertiesPage.map(PropertyMapper::toResponse);
	}
	
	// Eliminar una propiedad
	@Transactional
	public void delete(Long propertyId) {
		Property property = propertyRepository.findById(propertyId)
				.orElseThrow(() -> new ResourceNotFoundException("No se encontro propiedad con el ID " + propertyId));
		
		if (contractRepository.existsByPropertyIdAndStatus(propertyId, ContractStatus.ACTIVE)) {
			throw new IllegalStateException("No se puede eliminar una propiedad con un contrato Activo");
		}
		
		propertyRepository.delete(property);
	}
	
	// Helpers
	private void validateAreas(Property property, PropertyUpdateRequest dto) {

	    Double total = dto.getTotalArea() != null
	            ? dto.getTotalArea()
	            : property.getTotalArea();

	    Double covered = dto.getCoveredArea() != null
	            ? dto.getCoveredArea()
	            : property.getCoveredArea();

	    if (total != null && covered != null && covered > total) {
	        throw new BusinessException(
	            "La superficie cubierta no puede ser mayor a la superficie total"
	        );
	    }
	}
	
	private void validateOperationTypes(Property property, PropertyUpdateRequest dto) {

	    Set<OperationType> ops = dto.getOperationTypes() != null
	            ? dto.getOperationTypes()
	            : property.getOperationTypes();

	    Double salePrice = dto.getSalePrice() != null
	            ? dto.getSalePrice()
	            : property.getSalePrice();

	    Double rentPrice = dto.getRentPrice() != null
	            ? dto.getRentPrice()
	            : property.getRentPrice();

	    if (ops.contains(OperationType.SALE) && salePrice == null) {
	        throw new BusinessException("La propiedad en venta debe tener precio de venta");
	    }

	    if (ops.contains(OperationType.RENT) && rentPrice == null) {
	        throw new BusinessException("La propiedad en alquiler debe tener precio de alquiler");
	    }

	    if (!ops.contains(OperationType.SALE)) {
	        property.setSalePrice(null);
	    }

	    if (!ops.contains(OperationType.RENT)) {
	        property.setRentPrice(null);
	    }
	}
	
	private void validateNumericFields(PropertyUpdateRequest dto) {

	    if (dto.getBathrooms() != null && dto.getBathrooms() < 0)
	        throw new BusinessException("La cantidad de baños no puede ser negativa");

	    if (dto.getBedrooms() != null && dto.getBedrooms() < 0)
	        throw new BusinessException("La cantidad de dormitorios no puede ser negativa");

	    if (dto.getSalePrice() != null && dto.getSalePrice() <= 0)
	        throw new BusinessException("El precio de venta debe ser mayor a cero");

	    if (dto.getRentPrice() != null && dto.getRentPrice() <= 0)
	        throw new BusinessException("El precio de alquiler debe ser mayor a cero");
	}
	
	private void saveImages(Property property, List<MultipartFile> files, Integer coverIndex) {

		for (int i = 0; i < files.size(); i++) {
			
			MultipartFile file = files.get(i);
			
			String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
			
			try {
				Path uploadPath = Paths.get("uploads/");
				Files.createDirectories(uploadPath);
				
				Path filePath = uploadPath.resolve(fileName);
				Files.copy(file.getInputStream(), filePath);
			
			} catch (IOException e) {
				throw new BusinessException("Error al guardar la imagen");
			}
			
			PropertyImage image = new PropertyImage();
			image.setUrl("/uploads/" + fileName);
			image.setProperty(property);
			
			// logica cover
			if (coverIndex != null && coverIndex == i) {
				image.setCover(true);
			} else {
				image.setCover(false);
			}
			
			property.getImages().add(image);
		}
	}
	
	private void deleteImageFile(String url) {

	    try {
	        String fileName = url.replace("/uploads/", "");
	        Path path = Paths.get("uploads/").resolve(fileName);
	        Files.deleteIfExists(path);

	    } catch (IOException e) {
	        throw new BusinessException("Error al eliminar archivo de imagen");
	    }
	}
	
	private void ensureSingleCover(Property property) {

	    List<PropertyImage> images = property.getImages();

	    if (images.isEmpty()) {
	        return;
	    }

	    long coverCount = images.stream()
	            .filter(PropertyImage::isCover)
	            .count();

	    // Si no hay cover → asigna la primera
	    if (coverCount == 0) {
	        images.get(0).setCover(true);
	        return;
	    }

	    // Si hay más de una → deja solo la primera
	    if (coverCount > 1) {
	        boolean firstFound = false;

	        for (PropertyImage img : images) {
	            if (img.isCover()) {
	                if (!firstFound) {
	                    firstFound = true;
	                } else {
	                    img.setCover(false);
	                }
	            }
	        }
	    }
	}
	
	@Transactional(readOnly = true)
	public Page<PropertySearchResponse> search(String query, Pageable pageable) {
	    if (query == null || query.isBlank()) {
	        throw new BusinessException("Query de búsqueda vacía");
	    }

	    return propertyRepository.search(query, pageable);
	}
}
