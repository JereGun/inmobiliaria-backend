package dev.jgunsett.inmobiliaria.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RentalAdjustmentNotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(cron = "${app.notifications.rental-adjustment.cron:0 0 8 * * *}")
    public void notifyRentalAmountUpdates() {
        notificationService.createRentalAdjustmentNotifications();
    }

    @Scheduled(cron = "${app.notifications.overdue-rent.cron:0 5 8 * * *}")
    public void notifyOverdueRents() {
        notificationService.createOverdueRentNotifications();
    }
}
