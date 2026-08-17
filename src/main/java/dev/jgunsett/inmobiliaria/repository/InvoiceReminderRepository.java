package dev.jgunsett.inmobiliaria.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.InvoiceReminder;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceReminderType;

public interface InvoiceReminderRepository extends JpaRepository<InvoiceReminder, Long> {

    Optional<InvoiceReminder> findByInvoiceIdAndTypeAndScheduledFor(
            Long invoiceId,
            InvoiceReminderType type,
            LocalDate scheduledFor
    );
}
