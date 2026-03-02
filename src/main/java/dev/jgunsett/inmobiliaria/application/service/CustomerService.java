package dev.jgunsett.inmobiliaria.application.service;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.contract.ContractResponse;
import dev.jgunsett.inmobiliaria.application.dto.customer.CustomerCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.customer.CustomerResponse;
import dev.jgunsett.inmobiliaria.application.dto.customer.CustomerUpdateRequest;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertyResponse;
import dev.jgunsett.inmobiliaria.application.mapper.ContractMapper;
import dev.jgunsett.inmobiliaria.application.mapper.CustomerMapper;
import dev.jgunsett.inmobiliaria.application.mapper.PropertyMapper;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.repository.ContractRepository;
import dev.jgunsett.inmobiliaria.repository.CustomerRepository;
import dev.jgunsett.inmobiliaria.repository.PropertyRepository;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import lombok.RequiredArgsConstructor;


/**
 * Servicio encargado de gestionar toda la lógica de negocio relacionada
 * con la entidad Customer.
 *
 * Responsabilidades:
 * - Crear clientes validando reglas de negocio.
 * - Actualizar información de clientes.
 * - Obtener clientes individuales o paginados.
 * - Eliminar clientes.
 * - Obtener contratos y propiedades asociados al cliente.
 *
 * Reglas de arquitectura:
 * - No contiene lógica de persistencia directa (usa repositories).
 * - No expone entidades, solo DTOs.
 * - Centraliza validaciones de negocio.
 *
 * Transaccional:
 * - Por defecto es @Transactional.
 * - Métodos de lectura usan readOnly = true para optimización.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final ContractRepository contractRepository;
    private final PropertyRepository propertyRepository;

    /**
     * Crea un nuevo cliente.
     *
     * Reglas de negocio:
     * - No puede existir otro cliente con el mismo tipo y número de documento.
     * - El email debe ser único.
     *
     * @param request datos del cliente a crear
     * @return CustomerResponse con los datos persistidos
     * @throws BusinessException si ya existe documento o email
     */
    public CustomerResponse create(CustomerCreateRequest request) {

        if (customerRepository.existsByDocumentTypeAndDocumentNumber(
                request.getDocumentType(),
                request.getDocumentNumber())) {
            throw new BusinessException("El cliente con el mismo documento ya existe");
        }

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El Email ya se encuentra en uso");
        }

        Customer customer = customerMapper.toEntity(request);
        Customer saved = customerRepository.save(customer);

        return customerMapper.toResponse(saved);
    }

    /**
     * Actualiza un cliente existente.
     *
     * Reglas de negocio:
     * - El cliente debe existir.
     * - Si se modifica el email, debe seguir siendo único.
     *
     * @param id ID del cliente
     * @param request nuevos datos
     * @return CustomerResponse actualizado
     * @throws ResourceNotFoundException si el cliente no existe
     * @throws BusinessException si el nuevo email ya está en uso
     */
    public CustomerResponse update(Long id, CustomerUpdateRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro Cliente con el ID: " + id));

        if (!customer.getEmail().equals(request.getEmail())
                && customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El Email ya se encuentra en uso");
        }

        customerMapper.updateEntity(customer, request);

        return customerMapper.toResponse(customer);
    }

    /**
     * Obtiene un cliente por ID.
     *
     * @param id identificador del cliente
     * @return CustomerResponse
     * @throws ResourceNotFoundException si no existe
     */
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro Cliente con el ID: " + id));

        return customerMapper.toResponse(customer);
    }

    /**
     * Obtiene lista paginada de clientes.
     *
     * @param page número de página (base 0)
     * @param size cantidad de registros por página
     * @return Page<CustomerResponse>
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAll(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        
        Page<Customer> customersPage = customerRepository.findAll(pageable);
        
        return customersPage.map(customerMapper::toResponse);
    }

    /**
     * Elimina un cliente.
     *
     * Nota:
     * - Actualmente realiza eliminación física.
     * - En el futuro podría migrarse a borrado lógico (soft delete).
     *
     * @param id ID del cliente
     * @throws ResourceNotFoundException si no existe
     */
    public void delete(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro Cliente con el ID: " + id));

        customerRepository.delete(customer);
    }
    
    /**
     * Obtiene todos los contratos donde el cliente participa.
     *
     * Un cliente puede:
     * - Ser propietario (owner)
     * - Ser inquilino (tenant)
     *
     * Este método:
     * - Busca contratos en ambos roles.
     * - Los unifica.
     * - Elimina duplicados.
     *
     * @param customerId ID del cliente
     * @return lista de contratos asociados
     * @throws ResourceNotFoundException si el cliente no existe
     */
    @Transactional(readOnly = true)
    public List<ContractResponse> getCustomerContracts(Long customerId) {

        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<Contract> asOwner = contractRepository.findByOwnerId(customerId);
        List<Contract> asTenant = contractRepository.findByTenantId(customerId);

        return Stream.concat(asOwner.stream(), asTenant.stream())
                .distinct()
                .map(ContractMapper::toResponse)
                .toList();
    }
    
    /**
     * Obtiene todas las propiedades donde el cliente es propietario.
     *
     * @param customerId ID del cliente
     * @return lista de propiedades
     * @throws ResourceNotFoundException si el cliente no existe
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getCustomerProperties(Long customerId) {

        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return propertyRepository.findByOwnerId(customerId)
                .stream()
                .map(PropertyMapper::toResponse)
                .toList();
    }
    
    public Page<CustomerResponse> findOwners(Pageable pageable) {
        return customerRepository
                .findOwners(pageable)
                .map(customerMapper::toResponse);
    }
    
    public Page<CustomerResponse> search(String query, Pageable pageable) {

        if (query == null || query.isBlank()) {
            throw new BusinessException("Query de búsqueda vacía");
        }

        return customerRepository
                .search(query, pageable)
                .map(customerMapper::toResponse);
    }

}
