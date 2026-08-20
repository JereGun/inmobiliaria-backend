package dev.jgunsett.inmobiliaria.domain.entity;

import java.util.HashSet;
import java.util.Set;

import dev.jgunsett.inmobiliaria.domain.enums.Permission;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_group", uniqueConstraints = @UniqueConstraint(name = "uk_user_group_name", columnNames = "name"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 255)
	private String description;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_group_permission", joinColumns = @JoinColumn(name = "group_id"))
	@Column(name = "permission", nullable = false, length = 50)
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private Set<Permission> permissions = new HashSet<>();

	@ManyToMany(mappedBy = "groups")
	@Builder.Default
	private Set<User> users = new HashSet<>();
}
