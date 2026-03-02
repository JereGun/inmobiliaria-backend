package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.jgunsett.inmobiliaria.domain.enums.Amenity;
import dev.jgunsett.inmobiliaria.domain.enums.OperationType;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyStatus;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "property")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Property {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private Customer owner;
	
	@Enumerated(EnumType.STRING)
	private PropertyType propertyType;
	
	@ElementCollection(targetClass = OperationType.class)
	@Enumerated(EnumType.STRING)
	@CollectionTable(
			name = "property_operation_type",
			joinColumns = @JoinColumn(name = "property_id")
	)
	@Column(name = "operation_type")
	private Set<OperationType> operationTypes = new HashSet<>();
	
	@Enumerated(EnumType.STRING)
	private PropertyStatus status;
	
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
	
	//Caracteristicas de la propiedad
	@ElementCollection(targetClass = Amenity.class)
	@Enumerated(EnumType.STRING)
	@CollectionTable(
			name = "property_amenity",
			joinColumns = @JoinColumn(name = "property_id")
	)
	@Column(name = "amenity")
	private Set<Amenity> amenities = new HashSet<>();
	private Integer bathrooms;
	private Integer bedrooms;
	private Boolean furnished;
	private Integer constructionYear;
	private Double totalArea;
	private Double coveredArea;

	@Lob
	private String description;
	
	@Builder.Default
	@OneToMany(mappedBy = "property",cascade = CascadeType.ALL,orphanRemoval = true)
	private List<PropertyImage> images = new ArrayList<>();
	
	//Auditoria
	private LocalDateTime creationDate;
	private LocalDateTime modificationDate;
	
	@PrePersist
	public void onCreate() {
		this.creationDate = LocalDateTime.now();
		this.modificationDate = LocalDateTime.now();
	}
	
	@PreUpdate
	public void onUpdate() {
		this.modificationDate = LocalDateTime.now();
	}
	
	public String getFullAddress() {
	    return Stream.of(
	            street,
	            numeration,
	            floor,
	            department,
	            zipCode,
	            city,
	            province,
	            country
	    )
	    .filter(Objects::nonNull)
	    .filter(s -> !s.isBlank())
	    .collect(Collectors.joining(" "));
	}
}
