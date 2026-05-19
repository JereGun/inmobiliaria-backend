package dev.jgunsett.inmobiliaria.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jgunsett.inmobiliaria.application.dto.settlement.SettlementCreateRequest;
import dev.jgunsett.inmobiliaria.application.mapper.SettlementMapper;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Property;
import dev.jgunsett.inmobiliaria.domain.entity.Settlement;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.repository.ContractRepository;
import dev.jgunsett.inmobiliaria.repository.PayRepository;
import dev.jgunsett.inmobiliaria.repository.SettlementRepository;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private PayRepository payRepository;

    private final SettlementMapper settlementMapper = new SettlementMapper();

    @Test
    void createCalculatesSettlementFromPayments() {
        SettlementCreateRequest request = request("2026-04", "10", "5");
        Contract contract = contract();

        when(settlementRepository.existsByContractIdAndPeriod(1L, "2026-04")).thenReturn(false);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(payRepository.sumAmountByContractAndDateBetween(
                1L,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).thenReturn(new BigDecimal("100000.00"));
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> {
            Settlement settlement = invocation.getArgument(0);
            settlement.setId(9L);
            return settlement;
        });

        SettlementService service = service();

        var response = service.create(request);

        ArgumentCaptor<Settlement> captor = ArgumentCaptor.forClass(Settlement.class);
        verify(settlementRepository).save(captor.capture());

        assertThat(captor.getValue().getOwnerId()).isEqualTo(2L);
        assertThat(captor.getValue().getContractId()).isEqualTo(1L);
        assertThat(captor.getValue().getPeriod()).isEqualTo("2026-04");
        assertThat(captor.getValue().getTotalCharged()).isEqualByComparingTo("100000.00");
        assertThat(captor.getValue().getCommission()).isEqualByComparingTo("10000.00");
        assertThat(captor.getValue().getTax()).isEqualByComparingTo("5000.00");
        assertThat(captor.getValue().getNetPay()).isEqualByComparingTo("85000.00");

        assertThat(response.getId()).isEqualTo(9L);
        assertThat(response.getOwnerName()).isEqualTo("Ana Perez");
        assertThat(response.getPropertyName()).isEqualTo("Departamento Centro");
    }

    @Test
    void createRejectsDuplicatedContractPeriod() {
        SettlementCreateRequest request = request("2026-04", "10", "5");

        when(settlementRepository.existsByContractIdAndPeriod(1L, "2026-04")).thenReturn(true);

        SettlementService service = service();

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ya existe una liquidacion");

        verify(settlementRepository, never()).save(any(Settlement.class));
    }

    @Test
    void createRejectsPeriodWithoutPayments() {
        SettlementCreateRequest request = request("2026-04", "10", "5");
        Contract contract = contract();

        when(settlementRepository.existsByContractIdAndPeriod(1L, "2026-04")).thenReturn(false);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(payRepository.sumAmountByContractAndDateBetween(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        SettlementService service = service();

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No existen pagos");

        verify(settlementRepository, never()).save(any(Settlement.class));
    }

    @Test
    void createRejectsInvalidPeriodFormat() {
        SettlementCreateRequest request = request("04-2026", "10", "5");
        SettlementService service = service();

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("YYYY-MM");

        verify(settlementRepository, never()).save(any(Settlement.class));
    }

    private SettlementService service() {
        return new SettlementService(
                settlementRepository,
                contractRepository,
                payRepository,
                settlementMapper
        );
    }

    private SettlementCreateRequest request(String period, String commission, String tax) {
        SettlementCreateRequest request = new SettlementCreateRequest();
        request.setContractId(1L);
        request.setPeriod(period);
        request.setCommissionPercentage(new BigDecimal(commission));
        request.setTaxPercentage(new BigDecimal(tax));
        return request;
    }

    private Contract contract() {
        Customer owner = Customer.builder()
                .id(2L)
                .name("Ana")
                .surname("Perez")
                .build();

        Property property = Property.builder()
                .id(3L)
                .name("Departamento Centro")
                .owner(owner)
                .build();

        return Contract.builder()
                .id(1L)
                .owner(owner)
                .property(property)
                .build();
    }
}
