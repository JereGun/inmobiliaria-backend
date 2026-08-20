ALTER TABLE customer
    ADD COLUMN IF NOT EXISTS whatsapp_phone VARCHAR(30),
    ADD COLUMN IF NOT EXISTS whatsapp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS whatsapp_invoice_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS whatsapp_payment_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS whatsapp_reminder_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS whatsapp_opted_in_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS whatsapp_opted_out_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS whatsapp_opt_in_source VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_customer_whatsapp_enabled
    ON customer (whatsapp_enabled);
