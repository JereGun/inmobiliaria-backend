package dev.jgunsett.inmobiliaria.application.dto.property;
import java.util.List;
import java.util.Set;

import dev.jgunsett.inmobiliaria.domain.enums.Amenity;
import dev.jgunsett.inmobiliaria.domain.enums.OperationType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyUpdateRequest {
	
    private String name;

    private Set<OperationType> operationTypes;
    
    private Double salePrice;
    private Double rentPrice;

    private String street;
    private String numeration;
    private String floor;
    private String department;
    private String zipCode;
    private String city;
    private String province;
    private String country;

    private Set<Amenity> amenities;
    private Integer bathrooms;
    private Integer bedrooms;
    private Boolean furnished;
    private Integer constructionYear;
    private Double totalArea;
    private Double coveredArea;

    private String description;
	
    private List<Long> imageIdsToDelete;
    private Long coverImageId; // Trae el ID de la imagen la cual es la portada
}
