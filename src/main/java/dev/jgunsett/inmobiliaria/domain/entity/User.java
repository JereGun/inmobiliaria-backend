package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

	@Column(name = "display_name", length = 150)
	private String displayName;

	@Column(length = 50)
	private String whatsapp;

	@Column(name = "avatar_url", length = 500)
	private String avatarUrl;

	@Builder.Default
	@Column(name = "avatar_position_x", nullable = false)
	private Integer avatarPositionX = 50;

	@Builder.Default
	@Column(name = "avatar_position_y", nullable = false)
	private Integer avatarPositionY = 50;
	
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

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "user_group_member",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "group_id")
	)
	@Builder.Default
	private Set<UserGroup> groups = new HashSet<>();
	
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
		if (this.avatarPositionX == null) this.avatarPositionX = 50;
		if (this.avatarPositionY == null) this.avatarPositionY = 50;
	}
	
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

}
