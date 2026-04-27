package dev.jgunsett.inmobiliaria.domain.enums;

import java.util.EnumSet;
import java.util.Set;

public enum Role {
	ADMIN(EnumSet.allOf(Permission.class)),
	USER(EnumSet.of(
			Permission.CUSTOMER_READ,
			Permission.CUSTOMER_WRITE,
			Permission.PROPERTY_READ,
			Permission.PROPERTY_WRITE,
			Permission.CONTRACT_READ,
			Permission.CONTRACT_WRITE,
			Permission.INVOICE_READ,
			Permission.INVOICE_WRITE,
			Permission.PAY_READ,
			Permission.PAY_WRITE
	));

	private final Set<Permission> permissions;

	Role(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}
}
