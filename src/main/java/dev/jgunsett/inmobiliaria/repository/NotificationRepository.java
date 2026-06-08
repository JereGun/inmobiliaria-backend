package dev.jgunsett.inmobiliaria.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.Notification;
import dev.jgunsett.inmobiliaria.domain.enums.NotificationType;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserEmailOrderByCreationDateDesc(String email, Pageable pageable);

    Page<Notification> findByUserEmailAndReadOrderByCreationDateDesc(String email, Boolean read, Pageable pageable);

    long countByUserEmailAndRead(String email, Boolean read);

    boolean existsByUserIdAndContractIdAndTypeAndDueDate(
            Long userId,
            Long contractId,
            NotificationType type,
            LocalDate dueDate
    );

    boolean existsByUserIdAndInvoiceIdAndType(
            Long userId,
            Long invoiceId,
            NotificationType type
    );

    List<Notification> findByContractIdAndTypeAndDueDateAndReadFalse(
            Long contractId,
            NotificationType type,
            LocalDate dueDate
    );

    List<Notification> findByInvoiceIdAndTypeAndReadFalse(
            Long invoiceId,
            NotificationType type
    );
}
