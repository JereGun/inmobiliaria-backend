package dev.jgunsett.inmobiliaria.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.InvoiceReminder;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceReminderStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceReminderType;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceType;
import dev.jgunsett.inmobiliaria.repository.InvoiceReminderRepository;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceReminderService {

    private static final List<InvoiceStatus> PENDING_STATUSES = List.of(
            InvoiceStatus.ISSUED,
            InvoiceStatus.PARTIALLY_PAID
    );

    private final InvoiceRepository invoiceRepository;
    private final InvoiceReminderRepository reminderRepository;
    private final EmailSenderService emailSenderService;
    private final SystemSettingService systemSettingService;
    private final LateFeeService lateFeeService;
    private final WhatsAppNotificationService whatsappNotificationService;

    public int sendScheduledReminders() {
        return sendScheduledReminders(LocalDate.now());
    }

    public int sendScheduledReminders(LocalDate today) {
        if (!isEnabled("invoice.reminders.enabled") && !isEnabled("whatsapp.reminders.enabled")) {
            log.debug("Recordatorios de vencimiento deshabilitados");
            return 0;
        }

        int daysBefore = Math.max(0, readInt("invoice.reminders.days-before", 2));
        boolean dueTodayEnabled = isEnabled("invoice.reminders.on-due-date.enabled");
        boolean overdueEnabled = isEnabled("invoice.reminders.overdue.enabled");
        int overdueRepeatDays = Math.max(1, readInt("invoice.reminders.overdue-repeat-days", 7));

        List<Invoice> invoices = invoiceRepository.findByTypeAndStatusInAndDueDateLessThanEqual(
                InvoiceType.RENT,
                PENDING_STATUSES,
                today.plusDays(daysBefore)
        );

        int sent = 0;
        for (Invoice invoice : invoices) {
            LocalDate dueDate = invoice.getDueDate();
            if (dueDate == null) continue;

            lateFeeService.updateLateFeeAutomatically(invoice, today);

            if (daysBefore > 0 && dueDate.equals(today.plusDays(daysBefore))) {
                sent += sendReminder(invoice, InvoiceReminderType.DUE_SOON, dueDate, daysBefore);
            }
            if (dueTodayEnabled && dueDate.equals(today)) {
                sent += sendReminder(invoice, InvoiceReminderType.DUE_TODAY, dueDate, 0);
            }
            if (overdueEnabled && dueDate.isBefore(today)
                    && ChronoUnit.DAYS.between(dueDate, today) % overdueRepeatDays == 0) {
                sent += sendReminder(invoice, InvoiceReminderType.OVERDUE, today, 0);
            }
        }

        log.info("Recordatorios de facturas: {} notificaciones enviadas el {}", sent, today);
        return sent;
    }

    private int sendReminder(
            Invoice invoice,
            InvoiceReminderType type,
            LocalDate scheduledFor,
            int daysBefore
    ) {
        InvoiceReminder reminder = reminderRepository
                .findByInvoiceIdAndTypeAndScheduledFor(invoice.getId(), type, scheduledFor)
                .orElseGet(() -> InvoiceReminder.builder()
                        .invoice(invoice)
                        .type(type)
                        .scheduledFor(scheduledFor)
                        .recipientEmail("")
                        .status(InvoiceReminderStatus.PENDING)
                        .attempts(0)
                        .build());

        if (reminder.getStatus() == InvoiceReminderStatus.SENT) {
            return 0;
        }

        String recipient = invoice.getCustomer() == null ? null : invoice.getCustomer().getEmail();
        reminder.setRecipientEmail(recipient == null ? "" : recipient);
        reminder.setStatus(InvoiceReminderStatus.PENDING);
        reminder.setAttempts((reminder.getAttempts() == null ? 0 : reminder.getAttempts()) + 1);
        reminder.setLastAttemptAt(LocalDateTime.now());
        reminder.setLastError(null);

        EmailSendResult result = null;
        boolean emailSent = false;
        if (isEnabled("invoice.reminders.enabled") && recipient != null && !recipient.isBlank()) {
            result = emailSenderService.sendNotificationEmail(
                    recipient,
                    subjectFor(invoice, type),
                    messageFor(invoice, type, daysBefore)
            );
            emailSent = result.success();
        }

        WhatsAppSendResult whatsappResult = whatsappNotificationService.sendReminder(
                invoice, type, scheduledFor, daysBefore);
        if (emailSent || whatsappResult.success()) {
            reminder.setStatus(InvoiceReminderStatus.SENT);
            reminder.setSentAt(LocalDateTime.now());
            reminder.setLastError(null);
            reminderRepository.save(reminder);
            return 1;
        }

        reminder.setStatus((result != null && result.skipped()) || whatsappResult.skipped()
                ? InvoiceReminderStatus.SKIPPED
                : InvoiceReminderStatus.FAILED);
        reminder.setLastError(result != null ? result.message() : whatsappResult.message());
        reminderRepository.save(reminder);
        return 0;
    }

    private String subjectFor(Invoice invoice, InvoiceReminderType type) {
        return switch (type) {
            case DUE_SOON -> "Recordatorio de vencimiento - factura " + invoice.getCode();
            case DUE_TODAY -> "La factura " + invoice.getCode() + " vence hoy";
            case OVERDUE -> "Factura vencida pendiente de pago - " + invoice.getCode();
        };
    }

    private String messageFor(Invoice invoice, InvoiceReminderType type, int daysBefore) {
        String name = invoice.getCustomer() == null ? "" : invoice.getCustomer().getFullName();
        String greeting = name == null || name.isBlank() ? "Hola," : "Hola " + name + ",";
        String amount = invoice.getTotal() == null ? "" : " por un total de " + invoice.getTotal();
        String dueDate = invoice.getDueDate() == null ? "" : invoice.getDueDate().toString();

        return switch (type) {
            case DUE_SOON -> greeting + "\n\nTe recordamos que la factura " + invoice.getCode()
                    + amount + " vence el " + dueDate + " (dentro de " + daysBefore + " días)."
                    + "\n\nSaludos.";
            case DUE_TODAY -> greeting + "\n\nLa factura " + invoice.getCode() + amount
                    + " vence hoy, " + dueDate + "."
                    + "\n\nSaludos.";
            case OVERDUE -> greeting + "\n\nLa factura " + invoice.getCode() + amount
                    + " venció el " + dueDate + " y continúa pendiente de pago."
                    + "\n\nSi ya realizaste el pago, podés desestimar este mensaje.\n\nSaludos.";
        };
    }

    private boolean isEnabled(String key) {
        try {
            return "true".equalsIgnoreCase(systemSettingService.findEntityByKey(key).getValue());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private int readInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(systemSettingService.findEntityByKey(key).getValue());
        } catch (RuntimeException ex) {
            return defaultValue;
        }
    }
}
