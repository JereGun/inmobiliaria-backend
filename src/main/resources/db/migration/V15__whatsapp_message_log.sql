CREATE TABLE IF NOT EXISTS whatsapp_message_log (
    id                    BIGSERIAL PRIMARY KEY,
    customer_id           BIGINT NOT NULL REFERENCES customer (id) ON DELETE CASCADE,
    invoice_id            BIGINT REFERENCES invoice (id) ON DELETE SET NULL,
    message_type          VARCHAR(40) NOT NULL,
    deduplication_key     VARCHAR(160) NOT NULL UNIQUE,
    recipient_phone       VARCHAR(30) NOT NULL,
    status                VARCHAR(20) NOT NULL,
    attempts              INTEGER NOT NULL DEFAULT 0,
    provider_message_id   VARCHAR(160),
    last_error            VARCHAR(1000),
    sent_at               TIMESTAMP,
    last_attempt_at       TIMESTAMP,
    creation_date         TIMESTAMP NOT NULL,
    modification_date     TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_whatsapp_log_customer
    ON whatsapp_message_log (customer_id);

CREATE INDEX IF NOT EXISTS idx_whatsapp_log_invoice
    ON whatsapp_message_log (invoice_id);

CREATE INDEX IF NOT EXISTS idx_whatsapp_log_status
    ON whatsapp_message_log (status);
