package dev.jgunsett.inmobiliaria.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.InvoiceReminder;
import dev.jgunsett.inmobiliaria.domain.entity.Pay;
import dev.jgunsett.inmobiliaria.domain.entity.WhatsAppMessageLog;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceReminderType;
import dev.jgunsett.inmobiliaria.domain.enums.WhatsAppMessageStatus;
import dev.jgunsett.inmobiliaria.domain.enums.WhatsAppMessageType;
import dev.jgunsett.inmobiliaria.repository.WhatsAppMessageLogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WhatsAppNotificationService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final WhatsAppApiService whatsappApiService;
    private final WhatsAppMessageLogRepository logRepository;

    @Value("${app.portal.url:http://localhost:3000/portal}")
    private String portalUrl;

    public WhatsAppSendResult sendInvoice(Invoice invoice) {
        Customer customer = invoice.getCustomer();
        if (!isAllowed(customer, customer != null && Boolean.TRUE.equals(customer.getWhatsappInvoiceEnabled()))) {
            return WhatsAppSendResult.skipped("El cliente no autorizó facturas por WhatsApp");
        }
        return send(customer, invoice, WhatsAppMessageType.INVOICE, "invoice-" + invoice.getId(),
                "whatsapp.template.invoice", List.of(
                        customer.getFullName(), invoice.getCode(), amount(invoice.getTotal()), date(invoice.getDueDate()), invoiceUrl(invoice)));
    }

    public WhatsAppSendResult sendPayment(Pay payment) {
        Invoice invoice = payment.getInvoice();
        Customer customer = invoice == null ? null : invoice.getCustomer();
        if (!isAllowed(customer, customer != null && Boolean.TRUE.equals(customer.getWhatsappPaymentEnabled()))) {
            return WhatsAppSendResult.skipped("El cliente no autorizó comprobantes de pago por WhatsApp");
        }
        return send(customer, invoice, WhatsAppMessageType.PAYMENT_RECEIPT, "payment-" + payment.getId(),
                "whatsapp.template.payment-receipt", List.of(
                        customer.getFullName(), invoice.getCode(), amount(payment.getAmount()), date(payment.getDate()), invoiceUrl(invoice)));
    }

    public WhatsAppSendResult sendReminder(
            Invoice invoice,
            InvoiceReminderType type,
            LocalDate scheduledFor,
            int daysBefore
    ) {
        Customer customer = invoice.getCustomer();
        if (!isAllowed(customer, customer != null && Boolean.TRUE.equals(customer.getWhatsappReminderEnabled()))) {
            return WhatsAppSendResult.skipped("El cliente no autorizó recordatorios por WhatsApp");
        }
        String key = "reminder-" + invoice.getId() + "-" + type + "-" + scheduledFor;
        String detail = switch (type) {
            case DUE_SOON -> "vence en " + daysBefore + " día(s)";
            case DUE_TODAY -> "vence hoy";
            case OVERDUE -> "se encuentra vencida";
        };
        return send(customer, invoice, WhatsAppMessageType.PAYMENT_REMINDER, key,
                "whatsapp.template.reminder", List.of(
                        customer.getFullName(), invoice.getCode(), amount(invoice.getTotal()), date(invoice.getDueDate()), detail));
    }

    private WhatsAppSendResult send(
            Customer customer,
            Invoice invoice,
            WhatsAppMessageType type,
            String key,
            String templateKey,
            List<String> parameters
    ) {
        WhatsAppMessageLog log = logRepository.findByDeduplicationKey(key)
                .orElseGet(() -> WhatsAppMessageLog.builder()
                        .customer(customer)
                        .invoice(invoice)
                        .type(type)
                        .deduplicationKey(key)
                        .recipientPhone(recipientPhone(customer))
                        .status(WhatsAppMessageStatus.PENDING)
                        .attempts(0)
                        .build());

        if (log.getStatus() == WhatsAppMessageStatus.SENT) {
            return WhatsAppSendResult.skipped("El mensaje ya fue enviado");
        }

        log.setRecipientPhone(recipientPhone(customer));
        log.setAttempts((log.getAttempts() == null ? 0 : log.getAttempts()) + 1);
        log.setLastAttemptAt(LocalDateTime.now());
        WhatsAppSendResult result = whatsappApiService.sendTemplate(log.getRecipientPhone(), templateKey, parameters);
        log.setStatus(result.success()
                ? WhatsAppMessageStatus.SENT
                : result.skipped() ? WhatsAppMessageStatus.SKIPPED : WhatsAppMessageStatus.FAILED);
        log.setLastError(result.success() ? null : result.message());
        if (result.success()) {
            log.setSentAt(LocalDateTime.now());
            log.setProviderMessageId(result.providerMessageId());
        }
        logRepository.save(log);
        return result;
    }

    private boolean isAllowed(Customer customer, boolean categoryEnabled) {
        return customer != null
                && Boolean.TRUE.equals(customer.getWhatsappEnabled())
                && categoryEnabled
                && !recipientPhone(customer).isBlank();
    }

    private String recipientPhone(Customer customer) {
        if (customer == null) return "";
        String phone = customer.getWhatsappPhone();
        return phone == null || phone.isBlank() ? customer.getPhone() : phone;
    }

    private String amount(BigDecimal value) {
        return value == null ? "-" : value.toPlainString();
    }

    private String date(LocalDate value) {
        return value == null ? "-" : value.format(DATE_FORMAT);
    }

    private String invoiceUrl(Invoice invoice) {
        return portalUrl.replaceAll("/$", "") + "/facturas/" + invoice.getId();
    }
}
