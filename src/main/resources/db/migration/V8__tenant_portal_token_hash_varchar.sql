ALTER TABLE tenant_portal_invitation
    ALTER COLUMN token_hash TYPE VARCHAR(64);

ALTER TABLE tenant_portal_password_reset_token
    ALTER COLUMN token_hash TYPE VARCHAR(64);
