package dev.jgunsett.inmobiliaria.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jgunsett.inmobiliaria.application.dto.contract.ContractAdjustmentCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.contract.ContractCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.contract.ContractUpdateRequest;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.entity.ContractAdjustment;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Property;
import dev.jgunsett.inmobiliaria.domain.enums.AdjustmentFrequency;
import dev.jgunsett.inmobiliaria.domain.enums.AdjustmentType;
import dev.jgunsett.inmobiliaria.domain.enums.BillingFrequency;
import dev.jgunsett.inmobiliaria.domain.enums.ContractStatus;
import dev.jgunsett.inmobiliaria.domain.enums.ContractType;
import dev.jgunsett.inmobiliaria.domain.enums.Currency;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyStatus;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.ContractAdjustmentRepository;
import dev.jgunsett.inmobiliaria.repository.ContractEventRepository;
import dev.jgunsett.inmobiliaria.repository.ContractRepository;
import dev.jgunsett.inmobiliaria.repository.CustomerRepository;
import dev.jgunsett.inmobiliaria.repository.NotificationRepository;
import dev.jgunsett.inmobiliaria.repository.PropertyRepository;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock private ContractRepository contractRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ContractAdjustmentRepository contractAdjustmentRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private ContractEventRepository contractEventRepository;

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Test
    void createSavesContractWhenAllRulesPass() {
        ContractCreateRequest req = validCreateRequest();
        Property property = availableProperty(owner());
        Customer tenant = tenant();
        Contract saved = savedContract(property, owner(), tenant);

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(tenant));
        when(contractRepository.existsByPropertyIdAndStatus(1L, ContractStatus.ACTIVE)).thenReturn(false);
        when(contractRepository.save(any())).thenReturn(saved);
        when(contractEventRepository.save(any())).thenReturn(null);

        var response = service().create(req);

        verify(contractRepository).save(any(Contract.class));
        assertThat(response.getStatus()).isEqualTo(ContractStatus.DRAFT);
    }

    @Test
    void createRejectsWhenPropertyNotFound() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().create(validCreateRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Propiedad");

        verify(contractRepository, never()).save(any());
    }

    @Test
    void createRejectsWhenTenantNotFound() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(availableProperty(owner())));
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().create(validCreateRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Inquilino");

        verify(contractRepository, never()).save(any());
    }

    @Test
    void createRejectsWhenPropertyNotAvailable() {
        Property rented = availableProperty(owner());
        rented.setStatus(PropertyStatus.RENTED);

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(rented));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(tenant()));

        assertThatThrownBy(() -> service().create(validCreateRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("disponible");

        verify(contractRepository, never()).save(any());
    }

    @Test
    void createRejectsWhenPropertyAlreadyHasActiveContract() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(availableProperty(owner())));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(tenant()));
        when(contractRepository.existsByPropertyIdAndStatus(1L, ContractStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> service().create(validCreateRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("contrato activo");

        verify(contractRepository, never()).save(any());
    }

    @Test
    void createRejectsWhenEndDateNotAfterStartDate() {
        ContractCreateRequest req = validCreateRequest();
        req.setEndDate(req.getStartDate()); // equal dates — invalid

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(availableProperty(owner())));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(tenant()));
        when(contractRepository.existsByPropertyIdAndStatus(1L, ContractStatus.ACTIVE)).thenReturn(false);

        assertThatThrownBy(() -> service().create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("posterior");

        verify(contractRepository, never()).save(any());
    }

    @Test
    void createRejectsWhenOwnerAndTenantAreTheSamePerson() {
        Customer same = owner();
        Property property = availableProperty(same);

        ContractCreateRequest req = validCreateRequest();
        req.setTenantId(same.getId()); // tenant == owner

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(customerRepository.findById(same.getId())).thenReturn(Optional.of(same));
        when(contractRepository.existsByPropertyIdAndStatus(1L, ContractStatus.ACTIVE)).thenReturn(false);

        assertThatThrownBy(() -> service().create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("misma persona");

        verify(contractRepository, never()).save(any());
    }

    @Test
    void createRejectsWhenBaseRentalAmountIsZero() {
        ContractCreateRequest req = validCreateRequest();
        req.setBaseRentalAmount(BigDecimal.ZERO);

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(availableProperty(owner())));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(tenant()));
        when(contractRepository.existsByPropertyIdAndStatus(1L, ContractStatus.ACTIVE)).thenReturn(false);

        assertThatThrownBy(() -> service().create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mayor a cero");

        verify(contractRepository, never()).save(any());
    }

    @Test
    void createRejectsWhenFirstAdjustmentDateIsBeforeStartDate() {
        ContractCreateRequest req = validCreateRequest();
        req.setFirstAdjustmentDate(req.getStartDate().minusDays(1));

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(availableProperty(owner())));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(tenant()));
        when(contractRepository.existsByPropertyIdAndStatus(1L, ContractStatus.ACTIVE)).thenReturn(false);

        assertThatThrownBy(() -> service().create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("primer ajuste");

        verify(contractRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // activate()
    // -------------------------------------------------------------------------

    @Test
    void activateSetsStatusToActiveWhenDraft() {
        Contract contract = draftContract();
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
        when(contractEventRepository.save(any())).thenReturn(null);

        var response = service().activate(10L);

        assertThat(response.getStatus()).isEqualTo(ContractStatus.ACTIVE);
    }

    @Test
    void activateRejectsWhenNotDraft() {
        Contract contract = draftContract();
        contract.setStatus(ContractStatus.ACTIVE);
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> service().activate(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");
    }

    // -------------------------------------------------------------------------
    // suspend()
    // -------------------------------------------------------------------------

    @Test
    void suspendSetsStatusToSuspendedWhenActive() {
        Contract contract = draftContract();
        contract.setStatus(ContractStatus.ACTIVE);
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
        when(contractEventRepository.save(any())).thenReturn(null);

        var response = service().suspend(10L);

        assertThat(response.getStatus()).isEqualTo(ContractStatus.SUSPENDED);
    }

    @Test
    void suspendRejectsWhenNotActive() {
        Contract contract = draftContract();
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> service().suspend(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("activo");
    }

    // -------------------------------------------------------------------------
    // finish()
    // -------------------------------------------------------------------------

    @Test
    void finishSetsStatusToFinishedWhenActive() {
        Contract contract = draftContract();
        contract.setStatus(ContractStatus.ACTIVE);
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
        when(contractEventRepository.save(any())).thenReturn(null);

        var response = service().finish(10L);

        assertThat(response.getStatus()).isEqualTo(ContractStatus.FINISHED);
    }

    @Test
    void finishRejectsWhenNotActive() {
        Contract contract = draftContract();
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> service().finish(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("activo");
    }

    // -------------------------------------------------------------------------
    // terminate()
    // -------------------------------------------------------------------------

    @Test
    void terminateRejectsAlreadyFinishedContract() {
        Contract contract = draftContract();
        contract.setStatus(ContractStatus.FINISHED);
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> service().terminate(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("finalizado");
    }

    @Test
    void terminateRejectsAlreadyTerminatedContract() {
        Contract contract = draftContract();
        contract.setStatus(ContractStatus.TERMINATED);
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> service().terminate(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("rescindido");
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Test
    void deleteRejectsActiveContract() {
        Contract contract = draftContract();
        contract.setStatus(ContractStatus.ACTIVE);
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> service().delete(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("activo");

        verify(contractRepository, never()).delete(any());
    }

    @Test
    void deleteSucceedsForDraftContract() {
        Contract contract = draftContract();
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

        service().delete(10L);

        verify(contractRepository).delete(contract);
    }

    // -------------------------------------------------------------------------
    // update()
    // -------------------------------------------------------------------------

    @Test
    void updateRejectsFinishedContract() {
        Contract contract = draftContract();
        contract.setStatus(ContractStatus.FINISHED);
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> service().update(10L, new ContractUpdateRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("finalizado");
    }

    @Test
    void updateRejectsNegativeLateFeePercentage() {
        Contract contract = draftContract();
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

        ContractUpdateRequest req = new ContractUpdateRequest();
        req.setLateFeePercentage(new BigDecimal("-1"));

        assertThatThrownBy(() -> service().update(10L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mora");
    }

    @Test
    void updateRejectsEndDateNotAfterStartDate() {
        Contract contract = draftContract();
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

        ContractUpdateRequest req = new ContractUpdateRequest();
        req.setEndDate(contract.getStartDate()); // equal = invalid

        assertThatThrownBy(() -> service().update(10L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    void updateRejectsZeroBaseRentalAmount() {
        Contract contract = draftContract();
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

        ContractUpdateRequest req = new ContractUpdateRequest();
        req.setBaseRentalAmount(BigDecimal.ZERO);

        assertThatThrownBy(() -> service().update(10L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mayor a cero");
    }

    // -------------------------------------------------------------------------
    // calculateRentalAmount()
    // -------------------------------------------------------------------------

    @Test
    void calculateRentalAmountReturnsBaseWhenNoAdjustments() {
        Contract contract = draftContract();
        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
        when(contractAdjustmentRepository.findActiveAdjustmentsUpToDate(any(), any()))
                .thenReturn(List.of());

        BigDecimal result = service().calculateRentalAmount(10L, LocalDate.now());

        assertThat(result).isEqualByComparingTo("50000.00");
    }

    @Test
    void calculateRentalAmountAppliesPercentageAdjustment() {
        Contract contract = draftContract();
        ContractAdjustment adjustment = ContractAdjustment.builder()
                .adjustmentType(AdjustmentType.PERCENTAGE)
                .value(new BigDecimal("10"))
                .active(true)
                .build();

        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
        when(contractAdjustmentRepository.findActiveAdjustmentsUpToDate(any(), any()))
                .thenReturn(List.of(adjustment));

        BigDecimal result = service().calculateRentalAmount(10L, LocalDate.now());

        assertThat(result).isEqualByComparingTo("55000.00"); // 50000 + 10%
    }

    @Test
    void calculateRentalAmountAppliesFixedAmountAdjustment() {
        Contract contract = draftContract();
        ContractAdjustment adjustment = ContractAdjustment.builder()
                .adjustmentType(AdjustmentType.FIXED_AMOUNT)
                .value(new BigDecimal("5000"))
                .active(true)
                .build();

        when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
        when(contractAdjustmentRepository.findActiveAdjustmentsUpToDate(any(), any()))
                .thenReturn(List.of(adjustment));

        BigDecimal result = service().calculateRentalAmount(10L, LocalDate.now());

        assertThat(result).isEqualByComparingTo("55000.00"); // 50000 + 5000
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ContractService service() {
        return new ContractService(
                contractRepository, propertyRepository, customerRepository,
                contractAdjustmentRepository, notificationRepository, contractEventRepository);
    }

    private Customer owner() {
        return Customer.builder().id(1L).name("Laura").surname("Gomez").build();
    }

    private Customer tenant() {
        return Customer.builder().id(2L).name("Marcos").surname("Lopez").build();
    }

    private Property availableProperty(Customer owner) {
        return Property.builder()
                .id(1L)
                .name("Depto Centro")
                .owner(owner)
                .status(PropertyStatus.AVAILABLE)
                .build();
    }

    private Contract savedContract(Property property, Customer owner, Customer tenant) {
        return Contract.builder()
                .id(99L)
                .property(property)
                .owner(owner)
                .tenant(tenant)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2027, 1, 1))
                .baseRentalAmount(new BigDecimal("50000.00"))
                .status(ContractStatus.DRAFT)
                .adjustmentFrequency(AdjustmentFrequency.MONTHLY)
                .currency(Currency.ARS)
                .billingFrequency(BillingFrequency.MONTHLY)
                .contractType(ContractType.RESIDENTIAL)
                .build();
    }

    private Contract draftContract() {
        return Contract.builder()
                .id(10L)
                .property(availableProperty(owner()))
                .owner(owner())
                .tenant(tenant())
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2027, 1, 1))
                .baseRentalAmount(new BigDecimal("50000.00"))
                .status(ContractStatus.DRAFT)
                .adjustmentFrequency(AdjustmentFrequency.MONTHLY)
                .currency(Currency.ARS)
                .billingFrequency(BillingFrequency.MONTHLY)
                .contractType(ContractType.RESIDENTIAL)
                .build();
    }

    private ContractCreateRequest validCreateRequest() {
        ContractCreateRequest req = new ContractCreateRequest();
        req.setPropertyId(1L);
        req.setTenantId(2L);
        req.setStartDate(LocalDate.of(2026, 1, 1));
        req.setEndDate(LocalDate.of(2027, 1, 1));
        req.setBaseRentalAmount(new BigDecimal("50000.00"));
        req.setFirstAdjustmentDate(LocalDate.of(2026, 7, 1));
        req.setAdjustmentFrequency(AdjustmentFrequency.MONTHLY);
        req.setCurrency(Currency.ARS);
        req.setBillingFrequency(BillingFrequency.MONTHLY);
        req.setContractType(ContractType.RESIDENTIAL);
        return req;
    }
}
