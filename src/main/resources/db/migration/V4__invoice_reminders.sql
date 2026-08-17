CREATE TABLE IF NOT EXISTS invoice_reminder (
    id                BIGSERIAL PRIMARY KEY,
    invoice_id        BIGINT NOT NULL REFERENCES invoice (id) ON DELETE CASCADE,
    reminder_type     VARCHAR(30) NOT NULL,
    scheduled_for     DATE NOT NULL,
    recipient_email   VARCHAR(255) NOT NULL,
    status            VARCHAR(30) NOT NULL,
    attempts          INTEGER NOT NULL DEFAULT 0,
    sent_at           TIMESTAMP,
    last_attempt_at   TIMESTAMP,
    last_error        VARCHAR(1000),
    creation_date     TIMESTAMP NOT NULL,
    modification_date TIMESTAMP,
    CONSTRAINT uk_invoice_reminder_invoice_type_date
        UNIQUE (invoice_id, reminder_type, scheduled_for)
);

CREATE INDEX IF NOT EXISTS idx_invoice_reminder_invoice
    ON invoice_reminder (invoice_id);

CREATE INDEX IF NOT EXISTS idx_invoice_reminder_status
    ON invoice_reminder (status);
