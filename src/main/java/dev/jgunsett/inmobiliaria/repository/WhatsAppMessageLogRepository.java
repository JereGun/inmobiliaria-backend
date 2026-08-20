package dev.jgunsett.inmobiliaria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.WhatsAppMessageLog;

public interface WhatsAppMessageLogRepository extends JpaRepository<WhatsAppMessageLog, Long> {

    Optional<WhatsAppMessageLog> findByDeduplicationKey(String deduplicationKey);

    List<WhatsAppMessageLog> findByInvoiceIdOrderByCreationDateDesc(Long invoiceId);
}
