package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Representa la liquidación económica de un contrato para un propietario
 * en un período determinado.
 *
 * <p>
 * Un Settlement consolida el resultado financiero final de un contrato,
 * agrupando el total efectivamente cobrado al inquilino y aplicando
 * las deducciones correspondientes (comisión inmobiliaria, impuestos, etc.),
 * para determinar el monto neto a pagar al propietario.
 * </p>
 *
 * <h3>Rol en el dominio</h3>
 * <ul>
 *   <li>No representa una factura ni un pago.</li>
 *   <li>Es un registro interno de cierre contable.</li>
 *   <li>Funciona como snapshot histórico: una vez creado no debe recalcularse.</li>
 * </ul>
 *
 * <h3>Origen de los datos</h3>
 * <ul>
 *   <li>Se genera a partir de las facturas (Invoice) y pagos (Pay)
 *       asociados a un contrato.</li>
 *   <li>El período suele ser mensual (ej: 2026-01), pero no está acoplado
 *       a un formato específico.</li>
 * </ul>
 *
 * <h3>Uso principal</h3>
 * <ul>
 *   <li>Liquidación y pago al propietario.</li>
 *   <li>Reportes financieros.</li>
 *   <li>Auditoría y trazabilidad histórica.</li>
 * </ul>
 *
 * <p>
 * Importante: esta entidad no debe usarse para cálculos dinámicos.
 * Toda la lógica de cálculo vive en el Service correspondiente.
 * </p>
 */
@Entity
@Data
@Table(name = "settlement")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Settlement {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank
	private Long ownerId;
	
	@NotBlank
	private Long contractId;
	
	/** Período liquidado (ej: 2026-01) */
	private String period;
	
	/** Total cobrado al inquilino en el período */
	private double totalCharged;
	
	/** Comisión retenida por la inmobiliaria */
	private double commission;
	
	/** Impuestos aplicados sobre la liquidación */
	private double tax;
	
	/** Monto final a pagar al propietario */
	private double netPay;
	
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
