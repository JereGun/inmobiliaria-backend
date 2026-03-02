package dev.jgunsett.inmobiliaria.domain.entity;

import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
		name = "invoice",
	    indexes = {
	            @Index(name = "idx_invoice_code", columnList = "code"),
	            @Index(name = "idx_invoice_status", columnList = "status"),
	            @Index(name = "idx_invoice_type", columnList = "type"),
	            @Index(name = "idx_invoice_customer", columnList = "customer_id")
	        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract; // opcional (venta / factura manual)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @OneToMany(
        mappedBy = "invoice",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<InvoiceLine> lines = new ArrayList<>();

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

    public void recalculateTotal() {
        this.total = lines.stream()
                .map(InvoiceLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
