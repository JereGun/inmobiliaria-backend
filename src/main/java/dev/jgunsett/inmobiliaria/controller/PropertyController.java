package dev.jgunsett.inmobiliaria.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.jgunsett.inmobiliaria.application.dto.property.PropertyCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertyImageResponse;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertyResponse;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertySearchResponse;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertyUpdateRequest;
import dev.jgunsett.inmobiliaria.application.service.PropertyImageService;
import dev.jgunsett.inmobiliaria.application.service.PropertyService;
import dev.jgunsett.inmobiliaria.domain.enums.OperationType;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

	private final PropertyService propertyService;
	private final PropertyImageService propertyImageService;
	
	@GetMapping
	public Page<PropertyResponse> getAll(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "10") int size) {
		return propertyService.findAll(page, size);
	}
	
	@GetMapping("/{id}")
	public PropertyResponse getById(@PathVariable Long id) {
		return propertyService.findById(id);
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PropertyResponse create(
	        @ModelAttribute PropertyCreateRequest request,
	        @RequestParam(value = "images", required = false) List<MultipartFile> images,
	        @RequestParam(value = "coverIndex", required = false) Integer coverIndex) {

	    return propertyService.create(request, images, coverIndex);
	}
	
	@PutMapping("/{id}")
	public PropertyResponse update(
	        @PathVariable Long id,
	        @RequestBody PropertyUpdateRequest request) {

	    return propertyService.update(id, request);
	}

	@PostMapping("/{id}/images")
	@ResponseStatus(HttpStatus.CREATED)
	public PropertyImageResponse uploadImage(
	        @PathVariable Long id,
	        @RequestParam MultipartFile file,
	        @RequestParam(defaultValue = "false") boolean cover) {

	    return propertyImageService.addImage(id, file, cover);
	}
	
	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		propertyService.delete(id);
	}
	
	@GetMapping("/operation")
	public Page<PropertyResponse> getByOperation(
			@RequestParam OperationType operation,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return propertyService.findRentalStatus(operation, page, size);
	}
	
    @GetMapping("/search")
    public Page<PropertySearchResponse> search(
            @RequestParam String query,
            Pageable pageable) {

        return propertyService.search(query, pageable);
    }
}
