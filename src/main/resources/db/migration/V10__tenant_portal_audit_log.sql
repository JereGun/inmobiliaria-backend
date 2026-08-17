CREATE TABLE tenant_portal_audit_log (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES tenant_portal_account(id),
    event_type VARCHAR(50) NOT NULL,
    detail VARCHAR(500),
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tenant_portal_audit_account_date ON tenant_portal_audit_log(account_id, occurred_at DESC);
