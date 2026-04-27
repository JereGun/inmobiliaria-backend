package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
		name = "app_user",
		indexes = {
				@Index(name = "idx_user_email", columnList = "email")
		}
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Email
	@NotBlank
	@Column(nullable = false, unique = true)
	private String email;
	
	@NotBlank
	@Column(nullable = false)
	private String password;
	
	@Enumerated(EnumType.STRING)
	@NotNull
	@Column(nullable = false)
	private Role role;
	
	@Builder.Default
	@Column(nullable = false)
	private Boolean active = true;
	
	@Builder.Default
	@Column(nullable = false)
	private Boolean verified = false;
	
	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;
	
	private LocalDateTime lastLogin;
	
	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		if (this.active == null) {
			this.active = true;
		}
		if (this.verified == null) {
			this.verified = false;
		}
	}
	
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

}
