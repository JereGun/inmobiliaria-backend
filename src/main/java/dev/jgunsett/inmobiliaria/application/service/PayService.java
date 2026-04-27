package dev.jgunsett.inmobiliaria.application.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.pay.PayCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.pay.PayResponse;
import dev.jgunsett.inmobiliaria.application.mapper.PayMapper;
import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.Pay;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import dev.jgunsett.inmobiliaria.repository.PayRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PayService {

    private final PayRepository payRepository;
    private final InvoiceRepository invoiceRepository;

    public PayResponse create(PayCreateRequest request) {

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        Pay pay = Pay.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .medium(request.getMedium())
                .invoice(invoice)
                .build();

        return PayMapper.toResponse(payRepository.save(pay));
    }

    @Transactional(readOnly = true)
    public Page<PayResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return payRepository.findAll(pageable)
                .map(PayMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PayResponse findById(Long id) {
        Pay pay = payRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        return PayMapper.toResponse(pay);
    }

    @Transactional(readOnly = true)
    public List<PayResponse> findByInvoice(Long invoiceId) {
        return payRepository.findByInvoiceId(invoiceId)
                .stream()
                .map(PayMapper::toResponse)
                .toList();
    }
}
