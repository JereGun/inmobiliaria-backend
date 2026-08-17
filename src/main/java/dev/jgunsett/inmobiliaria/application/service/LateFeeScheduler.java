package dev.jgunsett.inmobiliaria.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LateFeeScheduler {

    private final LateFeeService lateFeeService;

    @Scheduled(cron = "${app.invoices.late-fees.cron:0 0 8 * * *}")
    public void refreshLateFees() {
        lateFeeService.refreshOverdueInvoices();
    }
}
