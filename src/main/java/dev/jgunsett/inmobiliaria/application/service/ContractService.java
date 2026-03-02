package dev.jgunsett.inmobiliaria.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.contract.ContractAdjustmentCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.contract.ContractAdjustmentResponse;
import dev.jgunsett.inmobiliaria.application.dto.contract.ContractCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.contract.ContractResponse;
import dev.jgunsett.inmobiliaria.application.dto.contract.ContractUpdateRequest;
import dev.jgunsett.inmobiliaria.application.mapper.ContractAdjustmentMapper;
import dev.jgunsett.inmobiliaria.application.mapper.ContractMapper;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.entity.ContractAdjustment;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Property;
import dev.jgunsett.inmobiliaria.domain.enums.ContractStatus;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.ContractAdjustmentRepository;
import dev.jgunsett.inmobiliaria.repository.ContractRepository;
import dev.jgunsett.inmobiliaria.repository.CustomerRepository;
import dev.jgunsett.inmobiliaria.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;


/**
 * Servicio de dominio encargado de la gestión de contratos.
 *
 * <p>Responsabilidades principales:</p>
 * <ul>
 *   <li>Creación, modificación y eliminación de contratos</li>
 *   <li>Validación de reglas de negocio del contrato</li>
 *   <li>Gestión de ajustes contractuales (ContractAdjustment)</li>
 *   <li>Cálculo del monto de alquiler vigente para una fecha determinada</li>
 * </ul>
 *
 * <p>Este servicio es la única fuente de verdad para la lógica asociada a contratos.
 * Otros servicios (Invoice, Settlement) deben consultar aquí cualquier cálculo
 * relacionado con importes contractuales.</p>
 */
@Service
@RequiredArgsConstructor
public class ContractService {
	
	private final ContractRepository contractRepository;
	private final PropertyRepository propertyRepository;
	private final CustomerRepository customerRepository;
	private final ContractAdjustmentRepository contractAdjustmentRepository;
	
	
	// Creacion de contrato
	/**
	 * Crea un nuevo contrato validando las reglas de negocio básicas.
	 *
	 * <p>Reglas aplicadas:</p>
	 * <ul>
	 *   <li>Una propiedad no puede tener más de un contrato activo</li>
	 *   <li>La fecha de fin debe ser posterior a la de inicio</li>
	 *   <li>El propietario y el inquilino no pueden ser la misma persona</li>
	 * </ul>
	 *
	 * @param req datos necesarios para crear el contrato
	 * @return contrato creado
	 * @throws ResourceNotFoundException si la propiedad, el dueño o el inquilino no existen
	 * @throws IllegalStateException si alguna regla de negocio no se cumple
	 */
	@Transactional
	public ContractResponse create(ContractCreateRequest req) {
		
		Property property = propertyRepository.findById(req.getPropertyId())
				.orElseThrow(() -> new ResourceNotFoundException("La propiedad no funciona"));
		
		Customer owner = property.getOwner();
		
		Customer tenant = customerRepository.findById(req.getTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("El inquilino no funciona"));
		
		if (contractRepository.existsByPropertyIdAndStatus(req.getPropertyId(), ContractStatus.ACTIVE)) {
			throw new RuntimeException("La propiedad con ID " + req.getPropertyId() + " ya posee un contrato activo");
		}
		
		if (req.getEndDate().isBefore(req.getStartDate())) {
		    throw new IllegalStateException("La fecha de fin no puede ser anterior a la de inicio");
		}

		if (owner.getId().equals(tenant.getId())) {
		    throw new IllegalStateException("El dueño no puede ser el inquilino");
		}
		
		if (req.getFirstAdjustmentDate().isBefore(req.getStartDate())) {
		    throw new BusinessException(
		        "La fecha del primer ajuste no puede ser anterior al inicio del contrato"
		    );
		}

		Contract contract = Contract.builder()
				.property(property)
				.owner(owner)
				.tenant(tenant)
				.startDate(req.getStartDate())
				.endDate(req.getEndDate())
				.baseRentalAmount(req.getBaseRentalAmount())
				.firstAdjustmentDate(req.getFirstAdjustmentDate())
				.currency(req.getCurrency())
				.billingFrequency(req.getBillingFrequency())
				.contractType(req.getContractType())
				.lateFeePercentage(req.getLateFeePercentage())
				.build();
		
		return ContractMapper.toResponse(contractRepository.save(contract));
	}
	
	// Modificacion de contrato
	/**
	 * Modifica un contrato existente.
	 *
	 * <p>Solo se permiten modificaciones sobre contratos que no estén
	 * finalizados, suspendidos o terminados.</p>
	 *
	 * <p>El método actualiza únicamente los campos informados en el request.</p>
	 *
	 * @param contractId identificador del contrato
	 * @param req datos a modificar
	 * @return contrato actualizado
	 */
	@Transactional
	public ContractResponse update(Long contractId, ContractUpdateRequest req) {
		Contract contract = contractRepository.findById(contractId)
				.orElseThrow(() -> new ResourceNotFoundException("El contrato no existe o no esta en funcionamiento"));
		
		// No se puede modificar un contrato ya cerrado
		if (contract.getStatus() == ContractStatus.FINISHED ||
				contract.getStatus() == ContractStatus.SUSPENDED ||
				contract.getStatus() == ContractStatus.TERMINATED) {
			throw new ResourceNotFoundException("No se puede modificar un contrato finalizado o cancelado");
		}
		
		// Solo se actualiza lo que venga informado
		if (req.getEndDate() != null) {
			if (req.getEndDate().isBefore(contract.getStartDate())) {
				throw new ResourceNotFoundException("La fecha de fin no puede ser anterior a la fecha de inicio");
			}
			
			contract.setEndDate(req.getEndDate());
		}
		
	    if (req.getBaseRentalAmount() != null) {

	        if (req.getBaseRentalAmount().compareTo(BigDecimal.ZERO) <= 0) {
	            throw new ResourceNotFoundException("El importe del alquiler debe ser mayor a cero");
	        }

	        contract.setBaseRentalAmount(req.getBaseRentalAmount());
	    }

	    if (req.getLateFeePercentage() != null) {

	        if (req.getLateFeePercentage().compareTo(BigDecimal.ZERO) < 0) {
	            throw new ResourceNotFoundException("El cargo por pago atrasado no puede ser negativo");
	        }

	        contract.setLateFeePercentage(req.getLateFeePercentage());
	    }
	    
	    return ContractMapper.toResponse(contractRepository.save(contract));
	}
	
	// Listar todos los contratos
	@Transactional(readOnly = true)
	public Page<ContractResponse> findAll(int page, int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		
		Page<Contract> contractsPage = contractRepository.findAll(pageable);
		
		return contractsPage.map(ContractMapper::toResponse);
	}
	
	// Buscar contrato por ID
	@Transactional(readOnly = true)
	public ContractResponse findById(Long contractId) {
		Contract contract = contractRepository.findById(contractId)
				.orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));
		
		return ContractMapper.toResponse(contract);
	}
	
	// Listar los contratos filtrados por el estado
	@Transactional(readOnly = true)
	public Page<ContractResponse> findByStatus(ContractStatus status, int page, int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		
		Page<Contract> contractsPage = contractRepository.findByStatus(status, pageable);

	    return contractsPage.map(ContractMapper::toResponse);
	}
	
	// Eliminar contrato
	/**
	 * Elimina un contrato.
	 *
	 * <p>No se permite eliminar contratos activos.</p>
	 *
	 * @param contractId identificador del contrato
	 */
	@Transactional
	public void delete(Long contractId) {

	    Contract contract = contractRepository.findById(contractId)
	            .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

	    if (contract.getStatus() == ContractStatus.ACTIVE) {
	        throw new IllegalStateException("No se puede eliminar un contrato activo");
	    }

	    contractRepository.delete(contract);
	}
	
	// MODIFICACION DE ESTADOS:
	// Activar Contrato
	@Transactional
	public ContractResponse activate(Long contractId) {
		Contract contract = contractRepository.findById(contractId)
				.orElseThrow(() -> new ResourceNotFoundException("El contrato con el ID " + contractId + " no se encuentra o no existe"));
		
		if (contract.getStatus() != ContractStatus.DRAFT) {
			throw new BusinessException("Solo se puede activar un contrato en estado DRAFT");
		}
		
		contract.setStatus(ContractStatus.ACTIVE);
		
		return ContractMapper.toResponse(contract);
	}
	
	// Suspender Contrato
	@Transactional
	public ContractResponse suspend(Long contractId) {

	    Contract contract = contractRepository.findById(contractId)
	    		.orElseThrow(() -> new ResourceNotFoundException("El contrato con el ID " + contractId + " no se encuentra o no existe"));

	    if (contract.getStatus() != ContractStatus.ACTIVE) {
	        throw new BusinessException("Solo se puede suspender un contrato activo");
	    }

	    contract.setStatus(ContractStatus.SUSPENDED);

	    return ContractMapper.toResponse(contract);
	}

	// Reanudar Contrato
	@Transactional
	public ContractResponse resume(Long contractId) {

	    Contract contract = contractRepository.findById(contractId)
	    		.orElseThrow(() -> new ResourceNotFoundException("El contrato con el ID " + contractId + " no se encuentra o no existe"));

	    if (contract.getStatus() != ContractStatus.SUSPENDED) {
	        throw new BusinessException("Solo se puede reanudar un contrato suspendido");
	    }

	    contract.setStatus(ContractStatus.ACTIVE);

	    return ContractMapper.toResponse(contract);
	}
	
	//Finalizar Contrato
	@Transactional
	public ContractResponse finish(Long contractId) {

	    Contract contract = contractRepository.findById(contractId)
	    		.orElseThrow(() -> new ResourceNotFoundException("El contrato con el ID " + contractId + " no se encuentra o no existe"));

	    if (contract.getStatus() != ContractStatus.ACTIVE) {
	        throw new BusinessException("Solo se puede finalizar un contrato activo");
	    }

	    contract.setStatus(ContractStatus.FINISHED);

	    return ContractMapper.toResponse(contract);
	}
	
	// Rescindir Contrato
	@Transactional
	public ContractResponse terminate(Long contractId) {

	    Contract contract = contractRepository.findById(contractId)
	    		.orElseThrow(() -> new ResourceNotFoundException("El contrato con el ID " + contractId + " no se encuentra o no existe"));

	    if (contract.getStatus() == ContractStatus.FINISHED) {
	        throw new BusinessException("No se puede rescindir un contrato finalizado");
	    }

	    if (contract.getStatus() == ContractStatus.TERMINATED) {
	        throw new BusinessException("El contrato ya está rescindido");
	    }

	    contract.setStatus(ContractStatus.TERMINATED);

	    return ContractMapper.toResponse(contract);
	}
	
	// AJUSTES DE CONTRATO:
	// Agregar ajuste a un contrato
	/**
	 * Agrega un ajuste a un contrato activo.
	 *
	 * <p>Los ajustes no modifican el monto base del contrato.
	 * Se aplican únicamente a cálculos futuros (facturación).</p>
	 *
	 * <p>Reglas:</p>
	 * <ul>
	 *   <li>Solo contratos activos pueden ser ajustados</li>
	 *   <li>La fecha efectiva no puede ser anterior al inicio del contrato</li>
	 * </ul>
	 *
	 * @param contractId contrato a ajustar
	 * @param req datos del ajuste
	 * @return ajuste creado
	 */
	@Transactional
	public ContractAdjustmentResponse addAdjustment(Long contractId, ContractAdjustmentCreateRequest req) {
	    Contract contract = contractRepository.findById(contractId)
	            .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

	    if (contract.getStatus() != ContractStatus.ACTIVE) {
	        throw new BusinessException("Solo se pueden ajustar contratos activos");
	    }

	    if (!isValidAdjustmentDate(contract, req.getEffectiveDate())) {
	        throw new BusinessException(
	            "La fecha del ajuste no corresponde a una ventana válida según el contrato"
	        );
	    }
	    
	    if (contractAdjustmentRepository.existsByContractIdAndEffectiveDate(contractId, req.getEffectiveDate())) {
	    	
	        throw new BusinessException("Ya existe un ajuste registrado para la fecha indicada");
	    }

	    ContractAdjustment adjustment = ContractAdjustment.builder()
	            .contract(contract)
	            .effectiveDate(req.getEffectiveDate())
	            .adjustmentType(req.getAdjustmentType())
	            .value(req.getValue())
	            .active(true)
	            .build();

	    contractAdjustmentRepository.save(adjustment);

	    return ContractAdjustmentMapper.toResponse(adjustment);
	}

	
	// Listar ajustes en un contrato
	@Transactional(readOnly = true)
	public List<ContractAdjustmentResponse> getAdjustments(Long contractId) {

	    if (!contractRepository.existsById(contractId)) {
	        throw new ResourceNotFoundException("Contrato no encontrado");
	    }

	    return contractAdjustmentRepository.findByContractId(contractId)
	            .stream()
	            .map(ContractAdjustmentMapper::toResponse)
	            .toList();
	}

	/**
	 * Calcula el importe de alquiler vigente para un contrato en una fecha determinada.
	 *
	 * <p>El cálculo se realiza a partir del monto base del contrato y el último
	 * ajuste activo cuya fecha efectiva sea menor o igual a la fecha consultada.</p>
	 *
	 * <p>Este método:</p>
	 * <ul>
	 *   <li>No modifica el contrato</li>
	 *   <li>No recalcula facturas existentes</li>
	 *   <li>Es determinístico para una fecha dada</li>
	 * </ul>
	 *
	 * @param contractId identificador del contrato
	 * @param date fecha para la cual se desea calcular el importe
	 * @return importe final de alquiler
	 */
    @Transactional(readOnly = true)
    public BigDecimal calculateRentalAmount(Long contractId, LocalDate date) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        BigDecimal amount = contract.getBaseRentalAmount();

        List<ContractAdjustment> adjustments =
                contractAdjustmentRepository
                        .findActiveAdjustmentsUpToDate(contractId, date);

        if (!adjustments.isEmpty()) {
            amount = applyAdjustment(amount, adjustments.get(0));
        }

        return amount;
    }

    /**
     * Aplica un ajuste sobre un importe base.
     *
     * <p>Método interno de dominio. No debe ser expuesto fuera del servicio.</p>
     */
    private BigDecimal applyAdjustment(BigDecimal baseAmount, ContractAdjustment adjustment) {

        return switch (adjustment.getAdjustmentType()) {

            case FIXED_AMOUNT ->
                    baseAmount.add(adjustment.getValue());

            case PERCENTAGE -> {
                BigDecimal percentage =
                        adjustment.getValue().divide(BigDecimal.valueOf(100));
                yield baseAmount.add(baseAmount.multiply(percentage));
            }

            default ->
                    throw new IllegalStateException(
                            "Tipo de ajuste no soportado: " +
                            adjustment.getAdjustmentType()
                    );
        };
    }
    
    private boolean isValidAdjustmentDate(Contract contract, LocalDate effectiveDate) {

        if (effectiveDate.isBefore(contract.getFirstAdjustmentDate())) {
            return false;
        }

        int frequencyMonths = contract.getAdjustmentFrequency().getMonths();

        long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(
                contract.getFirstAdjustmentDate(),
                effectiveDate
        );

        return monthsBetween % frequencyMonths == 0;
    }
}
