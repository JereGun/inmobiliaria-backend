package dev.jgunsett.inmobiliaria.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.PayMedium;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@Table(name = "pay")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PayMedium medium;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, updatable = false)
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
