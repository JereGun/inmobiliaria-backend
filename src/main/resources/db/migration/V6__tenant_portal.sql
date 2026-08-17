CREATE TABLE tenant_portal_account (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES customer(id),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    activated_at TIMESTAMP,
    last_login_at TIMESTAMP,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_date TIMESTAMP
);

CREATE INDEX idx_tenant_portal_account_email ON tenant_portal_account(email);

CREATE TABLE tenant_portal_invitation (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES tenant_portal_account(id),
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    consumed_at TIMESTAMP,
    revoked_at TIMESTAMP
);

CREATE INDEX idx_tenant_portal_invitation_account ON tenant_portal_invitation(account_id);
CREATE INDEX idx_tenant_portal_invitation_expiration ON tenant_portal_invitation(expires_at);
