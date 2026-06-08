package dev.jgunsett.inmobiliaria.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoInvoiceGenerationScheduler {

    private final AutoInvoiceGenerationService autoInvoiceGenerationService;

    @Scheduled(cron = "${app.invoices.auto-generation.cron:0 0 9 1 * *}")
    public void generateMonthlyInvoices() {
        autoInvoiceGenerationService.generateForCurrentPeriod();
    }
}
