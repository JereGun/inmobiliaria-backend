package dev.jgunsett.inmobiliaria.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalAccount;

public record TenantPortalPrincipal(Long accountId, Long customerId, String email) implements UserDetails {

    public static TenantPortalPrincipal from(TenantPortalAccount account) {
        return new TenantPortalPrincipal(account.getId(), account.getCustomer().getId(), account.getEmail());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("PORTAL_TENANT"));
    }

    @Override public String getPassword() { return ""; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
