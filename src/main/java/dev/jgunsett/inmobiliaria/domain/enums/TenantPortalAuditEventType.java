package dev.jgunsett.inmobiliaria.domain.enums;

public enum TenantPortalAuditEventType {
    INVITATION_SENT,
    ACCOUNT_ACTIVATED,
    LOGIN_SUCCEEDED,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_RESET_COMPLETED,
    SESSION_REFRESHED,
    LOGOUT,
    ACCOUNT_DISABLED,
    ACCOUNT_ENABLED
}
