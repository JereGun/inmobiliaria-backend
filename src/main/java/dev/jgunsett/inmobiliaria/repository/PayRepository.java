package dev.jgunsett.inmobiliaria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.Pay;

public interface PayRepository extends JpaRepository<Pay, Long> {
	
	List<Pay> findByInvoiceId(Long invoiceId);

}
