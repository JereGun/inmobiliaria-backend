package dev.jgunsett.inmobiliaria.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InvoiceReminderScheduler {

    private final InvoiceReminderService invoiceReminderService;

    @Scheduled(cron = "${app.invoices.reminders.cron:0 15 9 * * *}")
    public void sendScheduledReminders() {
        invoiceReminderService.sendScheduledReminders();
    }
}
