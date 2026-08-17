package dev.jgunsett.inmobiliaria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalAccount;

public interface TenantPortalAccountRepository extends JpaRepository<TenantPortalAccount, Long> {
    Optional<TenantPortalAccount> findByEmailIgnoreCase(String email);
    Optional<TenantPortalAccount> findByCustomerId(Long customerId);

    @Query("select account from TenantPortalAccount account join fetch account.customer where account.id = :id")
    Optional<TenantPortalAccount> findWithCustomerById(@Param("id") Long id);
}
