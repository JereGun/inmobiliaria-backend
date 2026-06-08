-- =============================================================
-- V1 — Schema inicial del sistema de gestión inmobiliaria
-- =============================================================

-- -------------------------------------------------------------
-- CUSTOMER
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer (
    id                BIGSERIAL    PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    middle_name       VARCHAR(255),
    surname           VARCHAR(255) NOT NULL,
    second_surname    VARCHAR(255),
    document_type     VARCHAR(50),
    document_number   VARCHAR(255),
    cuit              VARCHAR(255),
    birthdate         DATE,
    email             VARCHAR(255),
    phone             VARCHAR(255),
    creation_date     TIMESTAMP,
    modification_date TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_customer_email
    ON customer (email)
    WHERE email IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_customer_document
    ON customer (document_type, document_number)
    WHERE document_type IS NOT NULL AND document_number IS NOT NULL;

-- -------------------------------------------------------------
-- APP_USER
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_user (
    id           BIGSERIAL    PRIMARY KEY,
    email        VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(50)  NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    last_login   TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_email ON app_user (email);

-- -------------------------------------------------------------
-- PASSWORD_RESET_TOKEN
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS password_reset_token (
    id         BIGSERIAL    PRIMARY KEY,
    token      VARCHAR(255) NOT NULL,
    user_id    BIGINT       NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_password_reset_token ON password_reset_token (token);

-- -------------------------------------------------------------
-- COMPANY
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS company (
    id                BIGSERIAL    PRIMARY KEY,
    name              VARCHAR(255),
    tax_id            VARCHAR(255),
    fiscal_condition  VARCHAR(255),
    street            VARCHAR(255),
    number            VARCHAR(255),
    city              VARCHAR(255),
    province          VARCHAR(255),
    country           VARCHAR(255),
    email             VARCHAR(255),
    phone             VARCHAR(255),
    website           VARCHAR(255),
    logo_url          VARCHAR(255),
    creation_date     TIMESTAMP,
    modification_date TIMESTAMP
);

-- -------------------------------------------------------------
-- SYSTEM_SETTING
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system_setting (
    id                BIGSERIAL    PRIMARY KEY,
    setting_key       VARCHAR(255) NOT NULL,
    value             TEXT,
    type              VARCHAR(50),
    description       VARCHAR(255),
    editable          BOOLEAN      NOT NULL DEFAULT TRUE,
    creation_date     TIMESTAMP,
    modification_date TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_system_setting_key ON system_setting (setting_key);

-- -------------------------------------------------------------
-- PROPERTY
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS property (
    id                BIGSERIAL      PRIMARY KEY,
    name              VARCHAR(255),
    owner_id          BIGINT         REFERENCES customer (id),
    property_type     VARCHAR(100),
    status            VARCHAR(50),
    sale_price        NUMERIC(15, 2),
    rent_price        NUMERIC(15, 2),
    street            VARCHAR(255),
    numeration        VARCHAR(50),
    floor             VARCHAR(50),
    department        VARCHAR(50),
    zip_code          VARCHAR(20),
    city              VARCHAR(100),
    province          VARCHAR(100),
    country           VARCHAR(100),
    bathrooms         INTEGER,
    bedrooms          INTEGER,
    furnished         BOOLEAN,
    construction_year INTEGER,
    total_area        NUMERIC(10, 2),
    covered_area      NUMERIC(10, 2),
    description       TEXT,
    creation_date     TIMESTAMP,
    modification_date TIMESTAMP
);

-- ElementCollections de Property
CREATE TABLE IF NOT EXISTS property_operation_type (
    property_id    BIGINT      NOT NULL REFERENCES property (id) ON DELETE CASCADE,
    operation_type VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS property_amenity (
    property_id BIGINT       NOT NULL REFERENCES property (id) ON DELETE CASCADE,
    amenity     VARCHAR(100) NOT NULL
);

-- -------------------------------------------------------------
-- PROPERTY_IMAGE
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS property_image (
    id                BIGSERIAL    PRIMARY KEY,
    url               VARCHAR(255),
    is_cover          BOOLEAN      NOT NULL DEFAULT FALSE,
    property_id       BIGINT       REFERENCES property (id) ON DELETE CASCADE,
    creation_date     TIMESTAMP,
    modification_date TIMESTAMP
);

-- -------------------------------------------------------------
-- CONTRACT
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS contract (
    id                    BIGSERIAL      PRIMARY KEY,
    property_id           BIGINT         REFERENCES property (id),
    owner_id              BIGINT         REFERENCES customer (id),
    tenant_id             BIGINT         REFERENCES customer (id),
    start_date            DATE,
    end_date              DATE,
    base_rental_amount    NUMERIC(15, 2),
    adjustment_frequency  VARCHAR(50),
    first_adjustment_date DATE,
    currency              VARCHAR(10),
    billing_frequency     VARCHAR(50),
    status                VARCHAR(50)    NOT NULL DEFAULT 'DRAFT',
    contract_type         VARCHAR(50),
    late_fee_percentage   NUMERIC(5, 2),
    creation_date         TIMESTAMP,
    modification_date     TIMESTAMP
);

-- -------------------------------------------------------------
-- CONTRACT_ADJUSTMENT
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS contract_adjustment (
    id              BIGSERIAL      PRIMARY KEY,
    contract_id     BIGINT         NOT NULL REFERENCES contract (id) ON DELETE CASCADE,
    effective_date  DATE,
    adjustment_type VARCHAR(50),
    value           NUMERIC(15, 2),
    active          BOOLEAN        NOT NULL DEFAULT TRUE,
    creation_date   TIMESTAMP
);

-- -------------------------------------------------------------
-- CONTRACT_EVENT
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS contract_event (
    id           BIGSERIAL    PRIMARY KEY,
    contract_id  BIGINT       NOT NULL REFERENCES contract (id) ON DELETE CASCADE,
    event_type   VARCHAR(100),
    details      VARCHAR(500),
    performed_by VARCHAR(255),
    occurred_at  TIMESTAMP
);

-- -------------------------------------------------------------
-- INVOICE
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoice (
    id                BIGSERIAL      PRIMARY KEY,
    code              VARCHAR(255)   NOT NULL,
    customer_id       BIGINT         REFERENCES customer (id),
    contract_id       BIGINT         REFERENCES contract (id),
    type              VARCHAR(50),
    status            VARCHAR(50),
    date              DATE,
    total             NUMERIC(15, 2),
    auto_generated    BOOLEAN        NOT NULL DEFAULT FALSE,
    billing_period    VARCHAR(7),
    creation_date     TIMESTAMP,
    modification_date TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_invoice_code       ON invoice (code);
CREATE        INDEX IF NOT EXISTS idx_invoice_status     ON invoice (status);
CREATE        INDEX IF NOT EXISTS idx_invoice_type       ON invoice (type);
CREATE        INDEX IF NOT EXISTS idx_invoice_customer   ON invoice (customer_id);

-- -------------------------------------------------------------
-- INVOICE_LINE
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoice_line (
    id                BIGSERIAL      PRIMARY KEY,
    invoice_id        BIGINT         NOT NULL REFERENCES invoice (id) ON DELETE CASCADE,
    concept           VARCHAR(255),
    unit_price        NUMERIC(15, 2),
    quantity          INTEGER,
    subtotal          NUMERIC(15, 2),
    creation_date     TIMESTAMP,
    modification_date TIMESTAMP
);

-- -------------------------------------------------------------
-- PAY
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pay (
    id                BIGSERIAL      PRIMARY KEY,
    amount            NUMERIC(15, 2),
    date              DATE,
    medium            VARCHAR(50),
    invoice_id        BIGINT         REFERENCES invoice (id),
    creation_date     TIMESTAMP,
    modification_date TIMESTAMP
);

-- -------------------------------------------------------------
-- SETTLEMENT
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS settlement (
    id                BIGSERIAL      PRIMARY KEY,
    owner_id          BIGINT,
    contract_id       BIGINT,
    period            VARCHAR(7),
    total_charged     NUMERIC(15, 2),
    commission        NUMERIC(15, 2),
    tax               NUMERIC(15, 2),
    net_pay           NUMERIC(15, 2),
    creation_date     TIMESTAMP,
    modification_date TIMESTAMP,
    CONSTRAINT uk_settlement_contract_period UNIQUE (contract_id, period)
);

CREATE INDEX IF NOT EXISTS idx_settlement_owner    ON settlement (owner_id);
CREATE INDEX IF NOT EXISTS idx_settlement_contract ON settlement (contract_id);
CREATE INDEX IF NOT EXISTS idx_settlement_period   ON settlement (period);

-- -------------------------------------------------------------
-- NOTIFICATION
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification (
    id                BIGSERIAL     PRIMARY KEY,
    user_id           BIGINT        REFERENCES app_user (id) ON DELETE CASCADE,
    contract_id       BIGINT        REFERENCES contract (id),
    invoice_id        BIGINT        REFERENCES invoice (id),
    type              VARCHAR(100),
    title             VARCHAR(255),
    message           VARCHAR(1000),
    due_date          DATE,
    read_status       BOOLEAN       NOT NULL DEFAULT FALSE,
    read_at           TIMESTAMP,
    creation_date     TIMESTAMP,
    modification_date TIMESTAMP,
    CONSTRAINT uk_notification_user_contract_type_due_date
        UNIQUE (user_id, contract_id, type, due_date)
);

CREATE INDEX IF NOT EXISTS idx_notification_user_read ON notification (user_id, read_status);
CREATE INDEX IF NOT EXISTS idx_notification_due_date  ON notification (due_date);
