-- V1__init.sql
-- Sistema de Inventario QYF - UES
-- PostgreSQL + Flyway

-- =========================
-- 0) EXTENSIONS
-- =========================
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- 1) ENUM TYPES
-- =========================
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'movement_type_enum') THEN
CREATE TYPE movement_type_enum AS ENUM ('IN','OUT','ADJUST');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'alert_type_enum') THEN
CREATE TYPE alert_type_enum AS ENUM ('LOW_STOCK','EXPIRY_SOON','EXPIRED');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'alert_severity_enum') THEN
CREATE TYPE alert_severity_enum AS ENUM ('INFO','WARN','CRITICAL');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'alert_status_enum') THEN
CREATE TYPE alert_status_enum AS ENUM ('OPEN','ACK','CLOSED');
END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_status_enum') THEN
CREATE TYPE user_status_enum AS ENUM ('ACTIVE','INACTIVE');
END IF;
END$$;

-- =========================
-- 2) SECURITY TABLES
-- =========================
CREATE TABLE IF NOT EXISTS roles (
                                     role_id      BIGSERIAL PRIMARY KEY,
                                     code         VARCHAR(50) NOT NULL UNIQUE, -- ADMIN, ENCARGADO, ESTUDIANTE
    name         VARCHAR(120) NOT NULL
    );

CREATE TABLE IF NOT EXISTS users (
                                     user_id       BIGSERIAL PRIMARY KEY,
                                     username      VARCHAR(80) NOT NULL UNIQUE,
    email         VARCHAR(180) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status        user_status_enum NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(role_id) ON DELETE RESTRICT,
    PRIMARY KEY (user_id, role_id)
    );

-- =========================
-- 3) ACADEMIC STRUCTURE
-- =========================
CREATE TABLE IF NOT EXISTS laboratories (
                                            lab_id    BIGSERIAL PRIMARY KEY,
                                            code      VARCHAR(50) NOT NULL UNIQUE,
    name      VARCHAR(180) NOT NULL,
    location  VARCHAR(180),
    status    VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
    );

-- =========================
-- 4) UOM (DIMENSIONS & UNITS)
-- =========================
CREATE TABLE IF NOT EXISTS uom_dimensions (
                                              dimension_id BIGSERIAL PRIMARY KEY,
                                              code         VARCHAR(30) NOT NULL UNIQUE,  -- MASS, VOLUME, COUNT
    name         VARCHAR(120) NOT NULL
    );

CREATE TABLE IF NOT EXISTS uom_units (
                                         uom_id         BIGSERIAL PRIMARY KEY,
                                         dimension_id   BIGINT NOT NULL REFERENCES uom_dimensions(dimension_id) ON DELETE RESTRICT,
    code           VARCHAR(30) NOT NULL UNIQUE, -- g, kg, mg, ml, l, unit, dozen
    name           VARCHAR(120),
    to_base_factor NUMERIC(18,8) NOT NULL CHECK (to_base_factor > 0),
    is_base        BOOLEAN NOT NULL DEFAULT FALSE
    );

-- Solo 1 unidad base por dimensión
CREATE UNIQUE INDEX IF NOT EXISTS ux_uom_units_one_base_per_dim
    ON uom_units(dimension_id)
    WHERE is_base = TRUE;

-- =========================
-- 5) PRODUCTS & INVENTORY
-- =========================
CREATE TABLE IF NOT EXISTS products (
                                        product_id              BIGSERIAL PRIMARY KEY,
                                        sku                     VARCHAR(80) UNIQUE,
    name                    VARCHAR(220) NOT NULL,
    description             TEXT,
    dimension_id            BIGINT NOT NULL REFERENCES uom_dimensions(dimension_id) ON DELETE RESTRICT,
    base_uom_id             BIGINT NOT NULL REFERENCES uom_units(uom_id) ON DELETE RESTRICT,
    barcode                 VARCHAR(120) UNIQUE,
    qr_code                 VARCHAR(120) UNIQUE,
    reorder_point_base_qty  NUMERIC(18,6) NOT NULL DEFAULT 0 CHECK (reorder_point_base_qty >= 0),
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS inventory_by_lab (
                                                lab_id            BIGINT NOT NULL REFERENCES laboratories(lab_id) ON DELETE RESTRICT,
    product_id        BIGINT NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
    on_hand_base_qty  NUMERIC(18,6) NOT NULL DEFAULT 0 CHECK (on_hand_base_qty >= 0),
    reserved_base_qty NUMERIC(18,6) NOT NULL DEFAULT 0 CHECK (reserved_base_qty >= 0),
    PRIMARY KEY (lab_id, product_id)
    );

-- =========================
-- 6) BATCHES (EXPIRATION)
-- =========================
CREATE TABLE IF NOT EXISTS product_batches (
                                               batch_id          BIGSERIAL PRIMARY KEY,
                                               product_id        BIGINT NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
    lab_id            BIGINT NOT NULL REFERENCES laboratories(lab_id) ON DELETE RESTRICT,
    batch_code        VARCHAR(120),
    expiration_date   DATE,
    barcode           VARCHAR(120) UNIQUE,
    qr_code           VARCHAR(120) UNIQUE,
    received_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    initial_base_qty  NUMERIC(18,6) NOT NULL DEFAULT 0 CHECK (initial_base_qty >= 0),
    current_base_qty  NUMERIC(18,6) NOT NULL DEFAULT 0 CHECK (current_base_qty >= 0),
    -- evita duplicados de lote por producto/lab cuando existe batch_code
    CONSTRAINT uq_batches_product_lab_code UNIQUE (product_id, lab_id, batch_code)
    );

-- =========================
-- 7) MOVEMENTS (AUDIT / TRACEABILITY)
-- =========================
CREATE TABLE IF NOT EXISTS inventory_movements (
                                                   movement_id    BIGSERIAL PRIMARY KEY,
                                                   lab_id         BIGINT NOT NULL REFERENCES laboratories(lab_id) ON DELETE RESTRICT,
    movement_type  movement_type_enum NOT NULL,
    created_by     BIGINT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    reference      VARCHAR(180),
    notes          TEXT
    );

CREATE TABLE IF NOT EXISTS inventory_movement_lines (
                                                        line_id          BIGSERIAL PRIMARY KEY,
                                                        movement_id      BIGINT NOT NULL REFERENCES inventory_movements(movement_id) ON DELETE CASCADE,
    product_id       BIGINT NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
    captured_uom_id  BIGINT NOT NULL REFERENCES uom_units(uom_id) ON DELETE RESTRICT,
    captured_qty     NUMERIC(18,6) NOT NULL CHECK (captured_qty > 0),
    base_qty         NUMERIC(18,6) NOT NULL CHECK (base_qty > 0)
    );

CREATE TABLE IF NOT EXISTS inventory_movement_batch_allocations (
                                                                    allocation_id BIGSERIAL PRIMARY KEY,
                                                                    line_id       BIGINT NOT NULL REFERENCES inventory_movement_lines(line_id) ON DELETE CASCADE,
    batch_id      BIGINT NOT NULL REFERENCES product_batches(batch_id) ON DELETE RESTRICT,
    base_qty      NUMERIC(18,6) NOT NULL CHECK (base_qty > 0)
    );

-- =========================
-- 8) ALERTS
-- =========================
CREATE TABLE IF NOT EXISTS inventory_alerts (
                                                alert_id     BIGSERIAL PRIMARY KEY,
                                                lab_id       BIGINT NOT NULL REFERENCES laboratories(lab_id) ON DELETE RESTRICT,
    product_id   BIGINT NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
    batch_id     BIGINT REFERENCES product_batches(batch_id) ON DELETE SET NULL,
    alert_type   alert_type_enum NOT NULL,
    severity     alert_severity_enum NOT NULL,
    message      TEXT NOT NULL,
    status       alert_status_enum NOT NULL DEFAULT 'OPEN',
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    closed_at    TIMESTAMP
    );

-- =========================
-- 9) INDEXES (PERFORMANCE)
-- =========================
-- Movements
CREATE INDEX IF NOT EXISTS ix_movements_lab_created_at
    ON inventory_movements(lab_id, created_at DESC);

CREATE INDEX IF NOT EXISTS ix_movements_created_by_created_at
    ON inventory_movements(created_by, created_at DESC);

CREATE INDEX IF NOT EXISTS ix_movement_lines_movement_id
    ON inventory_movement_lines(movement_id);

CREATE INDEX IF NOT EXISTS ix_movement_lines_product_id
    ON inventory_movement_lines(product_id);

-- FEFO / Expiration queries
CREATE INDEX IF NOT EXISTS ix_batches_product_lab_exp
    ON product_batches(product_id, lab_id, expiration_date);

-- Alerts
CREATE INDEX IF NOT EXISTS ix_alerts_status_type_created
    ON inventory_alerts(status, alert_type, created_at DESC);

CREATE INDEX IF NOT EXISTS ix_alerts_lab_product_status
    ON inventory_alerts(lab_id, product_id, status);

-- =========================
-- 10) SAFETY TRIGGERS (AVOID WRONG CONVERSIONS)
-- =========================
-- 10.1) products: base_uom must match product dimension
CREATE OR REPLACE FUNCTION trg_products_validate_base_uom_dimension()
RETURNS TRIGGER AS $$
DECLARE
uom_dim_id BIGINT;
BEGIN
SELECT dimension_id INTO uom_dim_id
FROM uom_units
WHERE uom_id = NEW.base_uom_id;

IF uom_dim_id IS NULL THEN
    RAISE EXCEPTION 'base_uom_id % not found in uom_units', NEW.base_uom_id;
END IF;

  IF uom_dim_id <> NEW.dimension_id THEN
    RAISE EXCEPTION 'Invalid base_uom_id %. Unit dimension (%) differs from product dimension (%)',
      NEW.base_uom_id, uom_dim_id, NEW.dimension_id;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tg_products_validate_base_uom_dimension ON products;
CREATE TRIGGER tg_products_validate_base_uom_dimension
    BEFORE INSERT OR UPDATE OF dimension_id, base_uom_id ON products
    FOR EACH ROW EXECUTE FUNCTION trg_products_validate_base_uom_dimension();

-- 10.2) movement_lines: captured_uom must match product dimension
CREATE OR REPLACE FUNCTION trg_movement_lines_validate_uom_dimension()
RETURNS TRIGGER AS $$
DECLARE
prod_dim_id BIGINT;
  uom_dim_id  BIGINT;
BEGIN
SELECT dimension_id INTO prod_dim_id
FROM products
WHERE product_id = NEW.product_id;

IF prod_dim_id IS NULL THEN
    RAISE EXCEPTION 'product_id % not found in products', NEW.product_id;
END IF;

SELECT dimension_id INTO uom_dim_id
FROM uom_units
WHERE uom_id = NEW.captured_uom_id;

IF uom_dim_id IS NULL THEN
    RAISE EXCEPTION 'captured_uom_id % not found in uom_units', NEW.captured_uom_id;
END IF;

  IF uom_dim_id <> prod_dim_id THEN
    RAISE EXCEPTION 'Invalid captured_uom_id %. Unit dimension (%) differs from product dimension (%)',
      NEW.captured_uom_id, uom_dim_id, prod_dim_id;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tg_movement_lines_validate_uom_dimension ON inventory_movement_lines;
CREATE TRIGGER tg_movement_lines_validate_uom_dimension
    BEFORE INSERT OR UPDATE OF product_id, captured_uom_id ON inventory_movement_lines
    FOR EACH ROW EXECUTE FUNCTION trg_movement_lines_validate_uom_dimension();

-- =========================
-- 11) SEEDS (MINIMUM REQUIRED)
-- =========================
-- Roles
INSERT INTO roles(code, name)
VALUES
    ('ADMIN', 'Administrador'),
    ('ENCARGADO', 'Encargado de Laboratorio'),
    ('ESTUDIANTE', 'Estudiante')
    ON CONFLICT (code) DO NOTHING;

-- Dimensions
INSERT INTO uom_dimensions(code, name)
VALUES
    ('MASS',   'Masa'),
    ('VOLUME', 'Volumen'),
    ('COUNT',  'Conteo (Unidades)')
    ON CONFLICT (code) DO NOTHING;

-- Units
-- MASS base: g
-- VOLUME base: ml
-- COUNT base: unit
WITH dims AS (
    SELECT code, dimension_id FROM uom_dimensions
)
INSERT INTO uom_units(dimension_id, code, name, to_base_factor, is_base)
VALUES
  ((SELECT dimension_id FROM dims WHERE code='MASS'),   'g',     'Gramo',              1,      TRUE),
  ((SELECT dimension_id FROM dims WHERE code='MASS'),   'kg',    'Kilogramo',          1000,   FALSE),
  ((SELECT dimension_id FROM dims WHERE code='MASS'),   'mg',    'Miligramo',          0.001,  FALSE),

  ((SELECT dimension_id FROM dims WHERE code='VOLUME'), 'ml',    'Mililitro',          1,      TRUE),
  ((SELECT dimension_id FROM dims WHERE code='VOLUME'), 'l',     'Litro',              1000,   FALSE),

  ((SELECT dimension_id FROM dims WHERE code='COUNT'),  'unit',  'Unidad',             1,      TRUE),
  ((SELECT dimension_id FROM dims WHERE code='COUNT'),  'dozen', 'Docena',             12,     FALSE)
ON CONFLICT (code) DO NOTHING;