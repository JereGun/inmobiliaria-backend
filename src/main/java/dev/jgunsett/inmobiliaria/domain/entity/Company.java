package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Data
@Table(name = "company")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Company {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank
	private String name;
	
	//Direccion
	private String street;
	private String number;
	private String city;
	private String province;
	private String country;
	
	@Email
	@NotBlank
	private String email;
	private String phone;
	private String website;
	private String logoUrl;
	
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
}
