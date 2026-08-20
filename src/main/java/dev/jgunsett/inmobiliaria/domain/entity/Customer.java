package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.DocumentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
	    name = "customer",
	    indexes = {
	        @Index(name = "idx_customer_document", columnList = "document_type, document_number"),
	        @Index(name = "idx_customer_email", columnList = "email")
	    }
	)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank
	@Column(nullable = false)
	private String name;
	
	private String middleName;
	
	@NotBlank
	@Column(nullable = false)
	private String surname;
	
	private String secondSurname;
	
	@Enumerated(EnumType.STRING)
	@NotNull
	private DocumentType documentType;
	
	@NotBlank
	@Column(unique = true)
	private String documentNumber;
	
	private String cuit;
	
	private LocalDate birthdate;
	
	@Email
	@NotBlank
	private String email;
	
	private String phone;

	@Column(name = "whatsapp_phone", length = 30)
	private String whatsappPhone;

	@Column(name = "whatsapp_enabled", nullable = false)
	@Builder.Default
	private Boolean whatsappEnabled = false;

	@Column(name = "whatsapp_invoice_enabled", nullable = false)
	@Builder.Default
	private Boolean whatsappInvoiceEnabled = false;

	@Column(name = "whatsapp_payment_enabled", nullable = false)
	@Builder.Default
	private Boolean whatsappPaymentEnabled = false;

	@Column(name = "whatsapp_reminder_enabled", nullable = false)
	@Builder.Default
	private Boolean whatsappReminderEnabled = false;

	private LocalDateTime whatsappOptedInAt;
	private LocalDateTime whatsappOptedOutAt;

	@Column(name = "whatsapp_opt_in_source", length = 50)
	private String whatsappOptInSource;
	
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
	
	public String getFullName() {
		return name + " " + surname;
	}
	
}
