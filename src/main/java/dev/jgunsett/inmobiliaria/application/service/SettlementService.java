package dev.jgunsett.inmobiliaria.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.settlement.SettlementCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.settlement.SettlementResponse;
import dev.jgunsett.inmobiliaria.application.mapper.SettlementMapper;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.entity.Settlement;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.ContractRepository;
import dev.jgunsett.inmobiliaria.repository.PayRepository;
import dev.jgunsett.inmobiliaria.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final SettlementRepository settlementRepository;
    private final ContractRepository contractRepository;
    private final PayRepository payRepository;
    private final SettlementMapper settlementMapper;

    public SettlementResponse create(SettlementCreateRequest request) {
        YearMonth period = parsePeriod(request.getPeriod());
        String normalizedPeriod = period.toString();

        if (settlementRepository.existsByContractIdAndPeriod(request.getContractId(), normalizedPeriod)) {
            throw new BusinessException("Ya existe una liquidacion para el contrato y periodo indicado");
        }

        Contract contract = findContract(request.getContractId());

        LocalDate from = period.atDay(1);
        LocalDate to = period.atEndOfMonth();
        BigDecimal totalCharged = money(payRepository.sumAmountByContractAndDateBetween(contract.getId(), from, to));

        if (totalCharged.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("No existen pagos cobrados para liquidar en el periodo indicado");
        }

        BigDecimal commission = percentageOf(totalCharged, request.getCommissionPercentage());
        BigDecimal tax = percentageOf(totalCharged, request.getTaxPercentage());
        BigDecimal netPay = money(totalCharged.subtract(commission).subtract(tax));

        if (netPay.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("La comision e impuestos superan el total cobrado");
        }

        Settlement settlement = Settlement.builder()
                .ownerId(contract.getOwner().getId())
                .contractId(contract.getId())
                .period(normalizedPeriod)
                .totalCharged(totalCharged)
                .commission(commission)
                .tax(tax)
                .netPay(netPay)
                .build();

        return settlementMapper.toResponse(settlementRepository.save(settlement), contract);
    }

    @Transactional(readOnly = true)
    public SettlementResponse findById(Long id) {
        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Liquidacion no encontrada"));

        return settlementMapper.toResponse(settlement, findContract(settlement.getContractId()));
    }

    @Transactional(readOnly = true)
    public SettlementResponse findByContractAndPeriod(Long contractId, String period) {
        String normalizedPeriod = parsePeriod(period).toString();

        Settlement settlement = settlementRepository.findByContractIdAndPeriod(contractId, normalizedPeriod)
                .orElseThrow(() -> new ResourceNotFoundException("Liquidacion no encontrada"));

        return settlementMapper.toResponse(settlement, findContract(contractId));
    }

    @Transactional(readOnly = true)
    public Page<SettlementResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return settlementRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SettlementResponse> findByOwner(Long ownerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return settlementRepository.findByOwnerId(ownerId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SettlementResponse> findByContract(Long contractId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return settlementRepository.findByContractId(contractId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SettlementResponse> findByPeriod(String period, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String normalizedPeriod = parsePeriod(period).toString();

        return settlementRepository.findByPeriod(normalizedPeriod, pageable)
                .map(this::toResponse);
    }

    private SettlementResponse toResponse(Settlement settlement) {
        return settlementMapper.toResponse(settlement, findContract(settlement.getContractId()));
    }

    private Contract findContract(Long contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));
    }

    private YearMonth parsePeriod(String period) {
        try {
            return YearMonth.parse(period);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("El periodo debe tener formato YYYY-MM");
        }
    }

    private BigDecimal percentageOf(BigDecimal amount, BigDecimal percentage) {
        return money(amount.multiply(percentage).divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP));
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
