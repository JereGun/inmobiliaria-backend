CREATE TABLE tenant_portal_password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES tenant_portal_account(id),
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tenant_portal_password_reset_account ON tenant_portal_password_reset_token(account_id);
CREATE INDEX idx_tenant_portal_password_reset_expiration ON tenant_portal_password_reset_token(expires_at);
