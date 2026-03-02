package dev.jgunsett.inmobiliaria.application.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import dev.jgunsett.inmobiliaria.domain.entity.Property;
import dev.jgunsett.inmobiliaria.domain.entity.PropertyImage;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.PropertyImageRepository;
import dev.jgunsett.inmobiliaria.repository.PropertyRepository;

@Service
public class PropertyImageService {

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;

    public PropertyImageService(PropertyRepository propertyRepository,
                                PropertyImageRepository propertyImageRepository) {
        this.propertyRepository = propertyRepository;
        this.propertyImageRepository = propertyImageRepository;
    }

    @Transactional
    public void addImage(Long propertyId, MultipartFile file, boolean cover) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            Path uploadPath = Paths.get("uploads/");
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

        } catch (IOException e) {
            throw new BusinessException("Error saving image file");
        }

        if (cover) {
            propertyImageRepository.findByPropertyIdAndCoverTrue(propertyId)
                    .ifPresent(existing -> {
                        existing.setCover(false);
                        propertyImageRepository.save(existing);
                    });
        }

        PropertyImage image = new PropertyImage();
        image.setUrl("/uploads/" + fileName);
        image.setCover(cover);
        image.setProperty(property);

        propertyImageRepository.save(image);
    }

    @Transactional(readOnly = true)
    public List<PropertyImage> getByProperty(Long propertyId) {
        return propertyImageRepository.findByPropertyId(propertyId);
    }

    @Transactional
    public void delete(Long imageId) {
        PropertyImage image = propertyImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        propertyImageRepository.delete(image);
    }
}