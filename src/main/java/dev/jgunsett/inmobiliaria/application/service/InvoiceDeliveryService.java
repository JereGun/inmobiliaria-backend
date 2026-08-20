package dev.jgunsett.inmobiliaria.application.service;

import java.time.LocalDateTime;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceBatchItemResponse;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceBatchResponse;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceDeliveryResponse;
import dev.jgunsett.inmobiliaria.application.dto.invoice.WhatsAppDeliveryResponse;
import dev.jgunsett.inmobiliaria.application.dto.company.CompanyResponse;
import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.InvoiceDelivery;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceDeliveryStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.InvoiceDeliveryRepository;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceDeliveryService {

    private static final String EMAIL_CHANNEL = "EMAIL";

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDeliveryRepository deliveryRepository;
    private final InvoicePdfService invoicePdfService;
    private final EmailSenderService emailSenderService;
    private final SystemSettingService systemSettingService;
    private final CompanyService companyService;
    private final WhatsAppNotificationService whatsappNotificationService;

    public void sendIfEnabled(Invoice invoice) {
        if (isEnabled("invoice.auto-send.enabled")) {
            InvoiceDelivery existing = deliveryRepository
                    .findByInvoiceIdAndChannel(invoice.getId(), EMAIL_CHANNEL)
                    .orElse(null);
            if (existing == null || existing.getStatus() != InvoiceDeliveryStatus.SENT) {
                sendInvoiceInternal(invoice);
            }
        }
        if (isEnabled("whatsapp.invoice-auto-send.enabled")) {
            whatsappNotificationService.sendInvoice(invoice);
        }
    }

    public InvoiceDeliveryResponse sendInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró Factura con el ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.DRAFT || invoice.getStatus() == InvoiceStatus.CANCELED) {
            throw new BusinessException("Solo se pueden enviar facturas emitidas, parcialmente pagadas o pagadas");
        }

        return sendInvoiceInternal(invoice);
    }

    public WhatsAppDeliveryResponse sendWhatsAppInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró Factura con el ID: " + invoiceId));

        validateSendable(invoice);
        var result = whatsappNotificationService.sendInvoice(invoice);
        return WhatsAppDeliveryResponse.builder()
                .invoiceId(invoiceId)
                .status(result.skipped() ? "SKIPPED" : result.success() ? "SENT" : "FAILED")
                .success(result.success())
                .message(result.message())
                .providerMessageId(result.providerMessageId())
                .build();
    }

    public void resetForReissue(Invoice invoice) {
        deliveryRepository.findByInvoiceIdAndChannel(invoice.getId(), EMAIL_CHANNEL)
                .ifPresent(delivery -> {
                    delivery.setStatus(InvoiceDeliveryStatus.PENDING);
                    delivery.setSentAt(null);
                    delivery.setLastError(null);
                });
    }

    public InvoiceBatchResponse sendBatch(List<Long> invoiceIds) {
        List<InvoiceBatchItemResponse> results = new ArrayList<>();
        int succeeded = 0;

        for (Long invoiceId : invoiceIds) {
            try {
                InvoiceDeliveryResponse delivery = sendInvoice(invoiceId);
                boolean success = delivery.getStatus() == InvoiceDeliveryStatus.SENT;
                if (success) succeeded++;
                results.add(InvoiceBatchItemResponse.builder()
                        .invoiceId(invoiceId)
                        .code(findCode(invoiceId))
                        .success(success)
                        .message(delivery.getStatus() + (delivery.getLastError() == null ? "" : ": " + delivery.getLastError()))
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

    public InvoiceBatchResponse sendWhatsAppBatch(List<Long> invoiceIds) {
        List<InvoiceBatchItemResponse> results = new ArrayList<>();
        int succeeded = 0;

        for (Long invoiceId : invoiceIds) {
            try {
                WhatsAppDeliveryResponse delivery = sendWhatsAppInvoice(invoiceId);
                if (delivery.isSuccess()) succeeded++;
                results.add(InvoiceBatchItemResponse.builder()
                        .invoiceId(invoiceId)
                        .code(findCode(invoiceId))
                        .success(delivery.isSuccess())
                        .message(delivery.getStatus() + ": " + delivery.getMessage())
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

    @Transactional(readOnly = true)
    public List<InvoiceDeliveryResponse> findByInvoice(Long invoiceId) {
        return deliveryRepository.findByInvoiceIdOrderByCreationDateDesc(invoiceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private InvoiceDeliveryResponse sendInvoiceInternal(Invoice invoice) {
        String recipient = invoice.getCustomer() == null ? null : invoice.getCustomer().getEmail();
        InvoiceDelivery delivery = deliveryRepository
                .findByInvoiceIdAndChannel(invoice.getId(), EMAIL_CHANNEL)
                .orElseGet(() -> InvoiceDelivery.builder()
                        .invoice(invoice)
                        .channel(EMAIL_CHANNEL)
                        .recipientEmail(recipient == null ? "" : recipient)
                        .status(InvoiceDeliveryStatus.PENDING)
                        .attempts(0)
                        .build());

        delivery.setRecipientEmail(recipient == null ? "" : recipient);
        delivery.setAttempts((delivery.getAttempts() == null ? 0 : delivery.getAttempts()) + 1);
        delivery.setLastAttemptAt(LocalDateTime.now());
        delivery.setStatus(InvoiceDeliveryStatus.PENDING);
        delivery.setLastError(null);

        if (recipient == null || recipient.isBlank()) {
            delivery.setStatus(InvoiceDeliveryStatus.FAILED);
            delivery.setLastError("El cliente no tiene email configurado");
            return toResponse(deliveryRepository.save(delivery));
        }

        try {
            byte[] pdf = invoicePdfService.generate(invoice);
            String subject = "Factura " + invoice.getCode();
            CompanyResponse company = companyService.get();
            String body = invoiceEmailBody(invoice, company);

            EmailSendResult result = emailSenderService.sendHtmlEmailWithAttachment(
                    recipient,
                    subject,
                    body,
                    "factura-" + invoice.getCode() + ".pdf",
                    pdf
            );

            if (result.success()) {
                delivery.setStatus(InvoiceDeliveryStatus.SENT);
                delivery.setSentAt(LocalDateTime.now());
                delivery.setLastError(null);
            } else if (result.skipped()) {
                delivery.setStatus(InvoiceDeliveryStatus.SKIPPED);
                delivery.setLastError(result.message());
            } else {
                delivery.setStatus(InvoiceDeliveryStatus.FAILED);
                delivery.setLastError(result.message());
            }
        } catch (RuntimeException ex) {
            delivery.setStatus(InvoiceDeliveryStatus.FAILED);
            delivery.setLastError(ex.getMessage() == null ? "No se pudo generar o enviar la factura" : ex.getMessage());
        }

        return toResponse(deliveryRepository.save(delivery));
    }

    private void validateSendable(Invoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.DRAFT || invoice.getStatus() == InvoiceStatus.CANCELED) {
            throw new BusinessException("Solo se pueden enviar facturas emitidas, parcialmente pagadas o pagadas");
        }
    }

    private boolean isEnabled(String key) {
        try {
            return "true".equalsIgnoreCase(systemSettingService.findEntityByKey(key).getValue());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String invoiceEmailBody(Invoice invoice, CompanyResponse company) {
        String companyName = company == null || company.getName() == null || company.getName().isBlank()
                ? "Inmobiliaria"
                : company.getName();
        String dueDate = invoice.getDueDate() == null ? "-" : invoice.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String total = NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(invoice.getTotal());
        String contact = company == null ? "" : String.join(" | ",
                company.getEmail() == null ? "" : company.getEmail(),
                company.getPhone() == null ? "" : company.getPhone()).replaceAll("(^ \\| | \\| $)", "");
        return """
                <div style="background:#f8fafc;padding:32px 12px;font-family:Arial,Helvetica,sans-serif;color:#0f172a">
                  <div style="max-width:600px;margin:auto;background:#ffffff;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden">
                    <div style="background:#0f172a;color:#ffffff;padding:28px 32px">
                      <div style="font-size:22px;font-weight:700">%s</div>
                      <div style="font-size:13px;color:#cbd5e1;margin-top:8px">Factura %s</div>
                    </div>
                    <div style="padding:28px 32px">
                      <p style="margin:0 0 16px;font-size:16px">Hola %s,</p>
                      <p style="margin:0 0 22px;line-height:1.55">Adjuntamos tu factura. A continuación encontrás el resumen del comprobante.</p>
                      <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:18px;margin-bottom:22px">
                        <div style="font-size:12px;color:#64748b;text-transform:uppercase;letter-spacing:.08em">Importe total</div>
                        <div style="font-size:25px;font-weight:700;margin-top:6px">%s</div>
                        <div style="font-size:13px;color:#475569;margin-top:10px">Vencimiento: %s</div>
                      </div>
                      <p style="margin:0;line-height:1.55">Encontrarás el detalle completo en el PDF adjunto.</p>
                    </div>
                    <div style="border-top:1px solid #e2e8f0;padding:18px 32px;color:#64748b;font-size:12px">%s%s</div>
                  </div>
                </div>
                """.formatted(companyName, invoice.getCode(), invoice.getCustomer().getFullName(), total, dueDate, companyName, contact.isBlank() ? "" : " · " + contact);
    }

    private String findCode(Long invoiceId) {
        return invoiceRepository.findById(invoiceId).map(Invoice::getCode).orElse(null);
    }

    private String findCodeSafely(Long invoiceId) {
        try {
            return findCode(invoiceId);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private InvoiceDeliveryResponse toResponse(InvoiceDelivery delivery) {
        return InvoiceDeliveryResponse.builder()
                .id(delivery.getId())
                .invoiceId(delivery.getInvoice().getId())
                .channel(delivery.getChannel())
                .recipientEmail(delivery.getRecipientEmail())
                .status(delivery.getStatus())
                .attempts(delivery.getAttempts())
                .sentAt(delivery.getSentAt())
                .lastAttemptAt(delivery.getLastAttemptAt())
                .lastError(delivery.getLastError())
                .creationDate(delivery.getCreationDate())
                .build();
    }
}
