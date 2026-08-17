-- Fase 1: vencimientos, pagos parciales e idempotencia del ciclo de alquiler.

-- Día de vencimiento configurado por contrato. Los contratos existentes
-- conservan el comportamiento anterior usando el día 1.
ALTER TABLE contract
    ADD COLUMN IF NOT EXISTS payment_due_day INTEGER;

UPDATE contract
SET payment_due_day = 1
WHERE payment_due_day IS NULL;

ALTER TABLE contract
    ALTER COLUMN payment_due_day SET DEFAULT 1;

ALTER TABLE contract
    ALTER COLUMN payment_due_day SET NOT NULL;

ALTER TABLE contract
    ADD CONSTRAINT ck_contract_payment_due_day
    CHECK (payment_due_day BETWEEN 1 AND 31);

-- Invoice.date se modela como LocalDateTime en Java, por lo que debe ser
-- timestamp en PostgreSQL. Los datos existentes se conservan a medianoche.
ALTER TABLE invoice
    ALTER COLUMN date TYPE TIMESTAMP WITHOUT TIME ZONE
    USING date::timestamp;

ALTER TABLE invoice
    ADD COLUMN IF NOT EXISTS due_date DATE;

UPDATE invoice
SET due_date = date::date
WHERE due_date IS NULL;

ALTER TABLE invoice
    ALTER COLUMN due_date SET NOT NULL;

-- Evita duplicar la factura de un mismo contrato para un mismo período,
-- permitiendo facturas manuales sin contrato o sin período.
CREATE UNIQUE INDEX IF NOT EXISTS uk_invoice_contract_billing_period
    ON invoice (contract_id, billing_period)
    WHERE contract_id IS NOT NULL AND billing_period IS NOT NULL;

-- Alineación de restricciones entre la entidad Invoice y la base existente.
ALTER TABLE invoice
    ALTER COLUMN customer_id SET NOT NULL;

ALTER TABLE invoice
    ALTER COLUMN type SET NOT NULL;

ALTER TABLE invoice
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE invoice
    ALTER COLUMN date SET NOT NULL;

ALTER TABLE invoice
    ALTER COLUMN total SET NOT NULL;
