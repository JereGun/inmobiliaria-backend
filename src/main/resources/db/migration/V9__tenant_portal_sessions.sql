CREATE TABLE tenant_portal_session (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES tenant_portal_account(id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tenant_portal_session_account ON tenant_portal_session(account_id);
CREATE INDEX idx_tenant_portal_session_expiration ON tenant_portal_session(expires_at);
