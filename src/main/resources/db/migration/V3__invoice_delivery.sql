CREATE TABLE IF NOT EXISTS invoice_delivery (
    id                BIGSERIAL PRIMARY KEY,
    invoice_id        BIGINT NOT NULL REFERENCES invoice (id) ON DELETE CASCADE,
    channel           VARCHAR(30) NOT NULL DEFAULT 'EMAIL',
    recipient_email   VARCHAR(255) NOT NULL,
    status            VARCHAR(30) NOT NULL,
    attempts          INTEGER NOT NULL DEFAULT 0,
    sent_at           TIMESTAMP,
    last_attempt_at   TIMESTAMP,
    last_error        VARCHAR(1000),
    creation_date     TIMESTAMP NOT NULL,
    modification_date TIMESTAMP,
    CONSTRAINT uk_invoice_delivery_invoice_channel UNIQUE (invoice_id, channel)
);

CREATE INDEX IF NOT EXISTS idx_invoice_delivery_status
    ON invoice_delivery (status);

CREATE INDEX IF NOT EXISTS idx_invoice_delivery_invoice
    ON invoice_delivery (invoice_id);
