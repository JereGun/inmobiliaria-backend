package dev.jgunsett.inmobiliaria.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.notification.NotificationResponse;
import dev.jgunsett.inmobiliaria.application.mapper.NotificationMapper;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.Notification;
import dev.jgunsett.inmobiliaria.domain.entity.User;
import dev.jgunsett.inmobiliaria.domain.enums.ContractStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceType;
import dev.jgunsett.inmobiliaria.domain.enums.NotificationType;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.ContractAdjustmentRepository;
import dev.jgunsett.inmobiliaria.repository.ContractRepository;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import dev.jgunsett.inmobiliaria.repository.NotificationRepository;
import dev.jgunsett.inmobiliaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ContractRepository contractRepository;
    private final ContractAdjustmentRepository contractAdjustmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final EmailSenderService emailSenderService;

    @Value("${app.notifications.rental-adjustment.lookahead-days:7}")
    private int rentalAdjustmentLookaheadDays;

    @Value("${app.notifications.overdue-rent.grace-days:0}")
    private int overdueRentGraceDays;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> findForUser(String email, Boolean read, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notifications = read == null
                ? notificationRepository.findByUserEmailOrderByCreationDateDesc(email, pageable)
                : notificationRepository.findByUserEmailAndReadOrderByCreationDateDesc(email, read, pageable);

        return notifications.map(NotificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public long countUnreadForUser(String email) {
        return notificationRepository.countByUserEmailAndRead(email, false);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String email) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion no encontrada"));

        if (!notification.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Notificacion no encontrada");
        }

        if (!Boolean.TRUE.equals(notification.getRead())) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        return NotificationMapper.toResponse(notification);
    }

    @Transactional
    public int createRentalAdjustmentNotifications() {
        return createRentalAdjustmentNotifications(LocalDate.now());
    }

    @Transactional
    public int createRentalAdjustmentNotifications(LocalDate today) {
        LocalDate limitDate = today.plusDays(rentalAdjustmentLookaheadDays);
        List<User> users = userRepository.findByActiveTrue();

        if (users.isEmpty()) {
            return 0;
        }

        return contractRepository.findByStatus(ContractStatus.ACTIVE, Pageable.unpaged())
                .stream()
                .mapToInt(contract -> createRentalAdjustmentNotifications(contract, users, today, limitDate))
                .sum();
    }

    @Transactional
    public int createOverdueRentNotifications() {
        return createOverdueRentNotifications(LocalDate.now());
    }

    @Transactional
    public int createOverdueRentNotifications(LocalDate today) {
        LocalDateTime overdueLimit = today.minusDays(overdueRentGraceDays).atStartOfDay();
        List<User> users = userRepository.findByActiveTrue();

        if (users.isEmpty()) {
            return 0;
        }

        return invoiceRepository.findByTypeAndStatusAndDateBefore(
                        InvoiceType.RENT,
                        InvoiceStatus.ISSUED,
                        overdueLimit
                )
                .stream()
                .filter(invoice -> invoice.getContract() != null)
                .mapToInt(invoice -> createOverdueRentNotifications(invoice, users))
                .sum();
    }

    private int createRentalAdjustmentNotifications(
            Contract contract,
            List<User> users,
            LocalDate today,
            LocalDate limitDate
    ) {
        LocalDate nextAdjustmentDate = calculatePendingAdjustmentDate(contract, today);

        if (nextAdjustmentDate == null || nextAdjustmentDate.isAfter(limitDate)) {
            return 0;
        }

        return users.stream()
                .mapToInt(user -> createRentalAdjustmentNotification(user, contract, nextAdjustmentDate))
                .sum();
    }

    private int createRentalAdjustmentNotification(User user, Contract contract, LocalDate dueDate) {
        if (notificationRepository.existsByUserIdAndContractIdAndTypeAndDueDate(
                user.getId(),
                contract.getId(),
                NotificationType.RENTAL_AMOUNT_UPDATE,
                dueDate
        )) {
            return 0;
        }

        String title = "Actualizar monto de alquiler";
        String message = buildRentalAdjustmentMessage(contract, dueDate);

        notificationRepository.save(Notification.builder()
                .user(user)
                .contract(contract)
                .type(NotificationType.RENTAL_AMOUNT_UPDATE)
                .title(title)
                .message(message)
                .dueDate(dueDate)
                .read(false)
                .build());

        emailSenderService.sendNotificationEmail(user.getEmail(), title, message);
        return 1;
    }

    private LocalDate calculatePendingAdjustmentDate(Contract contract, LocalDate today) {
        if (contract.getFirstAdjustmentDate() == null || contract.getAdjustmentFrequency() == null) {
            return null;
        }

        LocalDate adjustmentDate = contract.getFirstAdjustmentDate();
        int frequencyMonths = contract.getAdjustmentFrequency().getMonths();

        while (adjustmentDate.isBefore(today)
                && contractAdjustmentRepository.existsByContractIdAndEffectiveDate(contract.getId(), adjustmentDate)) {
            adjustmentDate = adjustmentDate.plusMonths(frequencyMonths);
        }

        if (adjustmentDate.isAfter(contract.getEndDate())) {
            return null;
        }

        return adjustmentDate;
    }

    private String buildRentalAdjustmentMessage(Contract contract, LocalDate dueDate) {
        String propertyName = contract.getProperty() != null ? contract.getProperty().getName() : "la propiedad";

        return "El contrato #" + contract.getId()
                + " de " + propertyName
                + " tiene un ajuste de alquiler programado para el "
                + dueDate
                + ". Actualiza el monto de alquiler y registra el ajuste correspondiente.";
    }

    private int createOverdueRentNotifications(Invoice invoice, List<User> users) {
        return users.stream()
                .mapToInt(user -> createOverdueRentNotification(user, invoice))
                .sum();
    }

    private int createOverdueRentNotification(User user, Invoice invoice) {
        if (notificationRepository.existsByUserIdAndInvoiceIdAndType(
                user.getId(),
                invoice.getId(),
                NotificationType.RENT_OVERDUE
        )) {
            return 0;
        }

        LocalDate dueDate = invoice.getDate().toLocalDate();
        String title = "Alquiler vencido";
        String message = buildOverdueRentMessage(invoice, dueDate);

        notificationRepository.save(Notification.builder()
                .user(user)
                .contract(invoice.getContract())
                .invoice(invoice)
                .type(NotificationType.RENT_OVERDUE)
                .title(title)
                .message(message)
                .dueDate(dueDate)
                .read(false)
                .build());

        emailSenderService.sendNotificationEmail(user.getEmail(), title, message);
        return 1;
    }

    private String buildOverdueRentMessage(Invoice invoice, LocalDate dueDate) {
        Contract contract = invoice.getContract();
        String propertyName = contract.getProperty() != null ? contract.getProperty().getName() : "la propiedad";

        return "La factura " + invoice.getCode()
                + " del contrato #" + contract.getId()
                + " de " + propertyName
                + " vencio el " + dueDate
                + " y sigue pendiente de pago.";
    }
}
