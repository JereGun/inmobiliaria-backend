package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.SettingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "system_setting")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemSetting {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(name = "setting_key", nullable = false, unique = true)
	private String key;

	@NotBlank
	@Column(nullable = false)
	private String value;

	@Enumerated(EnumType.STRING)
	@NotNull
	@Column(nullable = false)
	private SettingType type;

	private String description;

	@Builder.Default
	@Column(nullable = false)
	private Boolean editable = true;

	private LocalDateTime creationDate;

	private LocalDateTime modificationDate;

	@PrePersist
	public void onCreate() {
		this.creationDate = LocalDateTime.now();
		this.modificationDate = LocalDateTime.now();
		if (this.editable == null) {
			this.editable = true;
		}
	}

	@PreUpdate
	public void onUpdate() {
		this.modificationDate = LocalDateTime.now();
	}
}
