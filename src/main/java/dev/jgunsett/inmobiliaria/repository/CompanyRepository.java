package dev.jgunsett.inmobiliaria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {

	Optional<Company> findFirstByOrderByIdAsc();
}
