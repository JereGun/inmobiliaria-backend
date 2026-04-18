package dev.jgunsett.inmobiliaria.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.jgunsett.inmobiliaria.application.dto.property.PropertyImageResponse;
import dev.jgunsett.inmobiliaria.application.service.PropertyImageService;
import dev.jgunsett.inmobiliaria.domain.entity.PropertyImage;

@RestController
@RequestMapping("/api/v1/property-images")
public class PropertyImageController {

    private final PropertyImageService propertyImageService;

    public PropertyImageController(PropertyImageService propertyImageService) {
        this.propertyImageService = propertyImageService;
    }

    @PostMapping("/{propertyId}")
    public ResponseEntity<PropertyImageResponse> upload(
            @PathVariable Long propertyId,
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "false") boolean cover) {

        return ResponseEntity.ok(propertyImageService.addImage(propertyId, file, cover));
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<List<PropertyImage>> getByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(propertyImageService.getByProperty(propertyId));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(@PathVariable Long imageId) {
        propertyImageService.delete(imageId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{imageId}/cover")
    public ResponseEntity<PropertyImageResponse> setCover(@PathVariable Long imageId) {
        return ResponseEntity.ok(propertyImageService.setCover(imageId));
    }
}
