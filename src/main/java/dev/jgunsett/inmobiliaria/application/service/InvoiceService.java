package dev.jgunsett.inmobiliaria.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceBatchItemResponse;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceBatchResponse;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceDeliveryResponse;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceLateFeeRequest;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceResponse;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceUpdateRequest;
import dev.jgunsett.inmobiliaria.application.mapper.InvoiceMapper;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.InvoiceLine;
import dev.jgunsett.inmobiliaria.domain.entity.Notification;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.NotificationType;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.ContractRepository;
import dev.jgunsett.inmobiliaria.repository.CustomerRepository;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import dev.jgunsett.inmobiliaria.repository.NotificationRepository;
import dev.jgunsett.inmobiliaria.repository.PayRepository;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de dominio encargado de la gestión de facturas (Invoice).
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Creación de facturas manuales</li>
 *   <li>Gestión de líneas de factura (InvoiceLine)</li>
 *   <li>Cálculo y congelamiento del total facturado</li>
 *   <li>Manejo del ciclo de vida de la factura mediante estados</li>
 * </ul>
 *
 * <p>Este servicio trabaja con facturación manual:
 * el importe total de la factura se calcula exclusivamente
 * a partir de sus líneas.</p>
 *
 * <p>No aplica reglas de contratos ni ajustes.
 * No recalcula facturas emitidas o pagadas.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ContractRepository contractRepository;
    private final InvoiceMapper invoiceMapper;
    private final NotificationRepository notificationRepository;
    private final InvoiceDeliveryService invoiceDeliveryService;
    private final LateFeeService lateFeeService;
    private final PayRepository payRepository;

    // Crear Invoice
    /**
     * Crea una nueva factura en estado DRAFT.
     *
     * <p>La factura se crea de forma manual, a partir de las líneas
     * informadas en el request.</p>
     *
     * <p>Flujo:</p>
     * <ul>
     *   <li>Valida la existencia del cliente</li>
     *   <li>Asocia un contrato si se informa (sin aplicar reglas del mismo)</li>
     *   <li>Genera un código de factura</li>
     *   <li>Crea las líneas de factura</li>
     *   <li>Calcula el total como suma de las líneas</li>
     * </ul>
     *
     * <p>La factura queda en estado {@link InvoiceStatus#DRAFT}.</p>
     *
     * @param request datos para la creación de la factura
     * @return factura creada
     */
    public InvoiceResponse create(InvoiceCreateRequest request) {

        // 1️ Validar Customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró Cliente con el ID: " + request.getCustomerId()));

        // 2️ Validar Contract (si viene)
        Contract contract = null;
        if (request.getContractId() != null) {
            contract = contractRepository.findById(request.getContractId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró Contrato con el ID: " + request.getContractId()));
        }

        // 3️ Crear Invoice base
        Invoice invoice = invoiceMapper.toEntity(request);
        invoice.setCustomer(customer);
        invoice.setContract(contract);
        invoice.setStatus(InvoiceStatus.DRAFT);
        if (contract != null) {
            invoice.setLateFeeDailyPercentage(contract.getLateFeePercentage());
        }

        // 4️ Generar código (simple por ahora)
        invoice.setCode(generateInvoiceCode());

        // 5️ Crear líneas
        for (var lineRequest : request.getLines()) {
            InvoiceLine line = invoiceMapper.toLineEntity(lineRequest, invoice);
            invoice.getLines().add(line);
        }

        // 6️ Recalcular total
        invoice.recalculateTotal();

        // 7️ Persistir aggregate
        Invoice saved = invoiceRepository.save(invoice);

        return invoiceMapper.toResponse(saved);
    }
    
 // Update Invoice (solo DRAFT)
    /**
     * Modifica una factura existente.
     *
     * <p>Solo se permite modificar facturas en estado
     * {@link InvoiceStatus#DRAFT}.</p>
     *
     * <p>La actualización reemplaza completamente las líneas
     * de la factura y recalcula el total.</p>
     *
     * @param id identificador de la factura
     * @param request datos a actualizar
     * @return factura actualizada
     * @throws BusinessException si la factura no está en estado DRAFT
     */
    public InvoiceResponse update(Long id, InvoiceUpdateRequest request) {

    	Invoice invoice = invoiceRepository.findById(id)
    			.orElseThrow(() -> new ResourceNotFoundException("No se encontró Factura con el ID: " + id));
	
    	// 1️ Validar estado

    	if (invoice.getStatus() != InvoiceStatus.DRAFT) {
    		throw new BusinessException("Solo se pueden modificar facturas en estado DRAFT");
    	}
	
	    // 2️ Actualizar campos simples
	    invoice.setType(request.getType());
	    invoice.setDate(request.getDate());
	    if (request.getDueDate() != null) {
	     invoice.setDueDate(request.getDueDate());
	    }
	
	    // 3️ Reemplazar líneas
	    invoice.getLines().clear();
	
	    request.getLines().forEach(lineRequest -> {
	        InvoiceLine line = invoiceMapper.toLineEntity(lineRequest, invoice);
	        invoice.getLines().add(line);
	    });
	
	    // 4️ Recalcular total
	    invoice.recalculateTotal();
	
	    return invoiceMapper.toResponse(invoice);
	    
    }

    // Buscar Invoice por ID
    /**
     * Obtiene una factura por su identificador.
     *
     * @param id identificador de la factura
     * @return factura encontrada
     */
    public InvoiceResponse getById(Long id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró Factura con el ID: " + id));

        lateFeeService.updateLateFeeAutomatically(invoice, java.time.LocalDate.now());

        return invoiceMapper.toResponse(invoice);
    }

    // Listar Invoices paginadas
    /**
     * Obtiene una lista paginada de facturas.
     *
     * @param page número de página
     * @param size tamaño de página
     * @return página de facturas
     */
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAll(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return invoiceRepository.findAll(pageable)
                .map(invoiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getByStatus(InvoiceStatus status, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return invoiceRepository.findByStatus(status, pageable)
                .map(invoiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAllByDateRange(LocalDateTime from, LocalDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return invoiceRepository.findByDateBetween(from, to, pageable)
                .map(invoiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getByStatusAndDateRange(InvoiceStatus status, LocalDateTime from, LocalDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return invoiceRepository.findByStatusAndDateBetween(status, from, to, pageable)
                .map(invoiceMapper::toResponse);
    }

    // Generación de código (simple)
    private String generateInvoiceCode() {
        return "INV-" + LocalDateTime.now().toString();
    }
    
    
    //CAMBIOS DE ESTADO:
    
    /**
     * Emite una factura.
     *
     * <p>Transición de estado:</p>
     * <ul>
     *   <li>DRAFT → ISSUED</li>
     * </ul>
     *
     * <p>Reglas:</p>
     * <ul>
     *   <li>Solo facturas en estado DRAFT pueden emitirse</li>
     *   <li>La factura debe tener al menos una línea</li>
     * </ul>
     *
     * @param id identificador de la factura
     * @return factura emitida
     */
    public InvoiceResponse issue(Long id) {
    	Invoice invoice = invoiceRepository.findById(id)
    			.orElseThrow(() -> new ResourceNotFoundException("No se encontró Factura con el ID: " + id));
    	
    	if (invoice.getStatus() != InvoiceStatus.DRAFT) {
    		throw new BusinessException("Solo se puede emitir facturas en estado DRAFT");
    	}
    	
    	if (invoice.getLines().isEmpty()) {
    		throw new BusinessException("No se puede emitir factura sin lineas");
    	}
    	
    	invoice.recalculateTotal();
    	
    	invoice.setStatus(InvoiceStatus.ISSUED);

        if (invoice.getLateFeeDailyPercentage() == null && invoice.getContract() != null) {
            invoice.setLateFeeDailyPercentage(invoice.getContract().getLateFeePercentage());
        }

        lateFeeService.updateLateFeeAutomatically(invoice, java.time.LocalDate.now());

        invoiceDeliveryService.sendIfEnabled(invoice);
    	
    	return invoiceMapper.toResponse(invoice);
    }

    public InvoiceBatchResponse issueBatch(List<Long> invoiceIds) {
        List<InvoiceBatchItemResponse> results = new ArrayList<>();
        int succeeded = 0;

        for (Long invoiceId : invoiceIds) {
            try {
                InvoiceResponse issued = issue(invoiceId);
                succeeded++;
                results.add(InvoiceBatchItemResponse.builder()
                        .invoiceId(invoiceId)
                        .code(issued.getCode())
                        .success(true)
                        .message("Factura emitida correctamente")
                        .build());
            } catch (RuntimeException ex) {
                results.add(InvoiceBatchItemResponse.builder()
                        .invoiceId(invoiceId)
                        .code(findCodeSafely(invoiceId))
                        .success(false)
                        .message(ex.getMessage())
                        .build());
            }
        }

        return InvoiceBatchResponse.builder()
                .requested(invoiceIds.size())
                .succeeded(succeeded)
                .failed(invoiceIds.size() - succeeded)
                .results(results)
                .build();
    }

    public InvoiceDeliveryResponse send(Long id) {
        return invoiceDeliveryService.sendInvoice(id);
    }

    public InvoiceBatchResponse sendBatch(List<Long> invoiceIds) {
        return invoiceDeliveryService.sendBatch(invoiceIds);
    }

    public InvoiceResponse applyLateFeeManually(Long id, InvoiceLateFeeRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró Factura con el ID: " + id));

        if (invoice.getStatus() != InvoiceStatus.ISSUED
                && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID) {
            throw new BusinessException("Solo se puede aplicar mora a facturas emitidas o parcialmente pagadas");
        }
        if (invoice.getType() != dev.jgunsett.inmobiliaria.domain.enums.InvoiceType.RENT) {
            throw new BusinessException("La mora manual solo aplica a facturas de alquiler");
        }
        if (invoice.getDueDate() == null || !invoice.getDueDate().isBefore(java.time.LocalDate.now())) {
            throw new BusinessException("La factura debe estar vencida para aplicar interés por mora");
        }

        invoice.setLateFeeDailyPercentage(request.getDailyPercentage());
        lateFeeService.updateLateFee(invoice, java.time.LocalDate.now());
        return invoiceMapper.toResponse(invoice);
    }

    public InvoiceResponse revertToDraft(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró Factura con el ID: " + id));

        if (invoice.getStatus() != InvoiceStatus.ISSUED) {
            throw new BusinessException("Solo se puede volver a borrador una factura emitida sin pagos");
        }
        if (payRepository.existsByInvoiceId(id)) {
            throw new BusinessException("No se puede volver a borrador una factura que tiene pagos registrados");
        }

        invoice.setStatus(InvoiceStatus.DRAFT);
        invoiceDeliveryService.resetForReissue(invoice);
        return invoiceMapper.toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceDeliveryResponse> getDeliveries(Long id) {
        return invoiceDeliveryService.findByInvoice(id);
    }

    private String findCodeSafely(Long invoiceId) {
        try {
            return invoiceRepository.findById(invoiceId).map(Invoice::getCode).orElse(null);
        } catch (RuntimeException ignored) {
            return null;
        }
    }
    
    /**
     * Marca una factura como pagada.
     *
     * <p>Transición de estado:</p>
     * <ul>
     *   <li>ISSUED → PAID</li>
     * </ul>
     *
     * <p>Este método no valida importes ni pagos parciales.</p>
     *
     * @param id identificador de la factura
     * @return factura pagada
     */
    public InvoiceResponse pay(Long id) {
    	Invoice invoice = invoiceRepository.findById(id)
    			.orElseThrow(() -> new ResourceNotFoundException("No se encontró Factura con el ID: " + id));
    	
        if (invoice.getStatus() != InvoiceStatus.ISSUED
                && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID) {
			throw new BusinessException("Solo se pueden pagar facturas en estado ISSUED o PARTIALLY_PAID");
    	}
    	
    	invoice.setStatus(InvoiceStatus.PAID);
    	resolveOverdueRentNotifications(invoice);
    	
    	return invoiceMapper.toResponse(invoice);
    }
    
    /**
     * Cancela una factura.
     *
     * <p>Transición de estado:</p>
     * <ul>
     *   <li>DRAFT / ISSUED → CANCELED</li>
     * </ul>
     *
     * <p>No se permite cancelar facturas pagadas.</p>
     *
     * @param id identificador de la factura
     * @return factura cancelada
     */
    public InvoiceResponse cancel(Long id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró Factura con el ID: " + id));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException(
                    "No se puede cancelar una factura ya pagada");
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELED) {
            throw new BusinessException(
                    "La factura ya se encuentra cancelada");
        }

        invoice.setStatus(InvoiceStatus.CANCELED);

        return invoiceMapper.toResponse(invoice);
    }

    private void resolveOverdueRentNotifications(Invoice invoice) {
        List<Notification> notifications =
                notificationRepository.findByInvoiceIdAndTypeAndReadFalse(
                        invoice.getId(),
                        NotificationType.RENT_OVERDUE
                );

        notifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
    }
}
