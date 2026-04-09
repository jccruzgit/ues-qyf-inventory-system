ALTER TABLE IF EXISTS laboratories
    ADD COLUMN IF NOT EXISTS code VARCHAR(30),
    ADD COLUMN IF NOT EXISTS name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE laboratories
SET code = 'LAB-' || LPAD(lab_id::text, 4, '0')
WHERE code IS NULL OR BTRIM(code) = '';

UPDATE laboratories
SET name = 'Laboratory ' || COALESCE(code, 'LAB-' || LPAD(lab_id::text, 4, '0'))
WHERE name IS NULL OR BTRIM(name) = '';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_laboratories_code'
    ) THEN
        ALTER TABLE laboratories
            ADD CONSTRAINT uk_laboratories_code UNIQUE (code);
    END IF;
END $$;

ALTER TABLE laboratories
    ALTER COLUMN code SET NOT NULL,
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_laboratories_is_active
    ON laboratories (is_active);

ALTER TABLE IF EXISTS product_batches
    ADD COLUMN IF NOT EXISTS expiration_date DATE NULL,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE product_batches
SET created_at = CURRENT_TIMESTAMP
WHERE created_at IS NULL;

UPDATE product_batches
SET updated_at = CURRENT_TIMESTAMP
WHERE updated_at IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_product_batches_status'
    ) THEN
        ALTER TABLE product_batches
            ADD CONSTRAINT chk_product_batches_status
                CHECK (status IN ('ACTIVE', 'QUARANTINED', 'EXPIRED', 'EXHAUSTED'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM product_batches
        WHERE product_id IS NULL
           OR lab_id IS NULL
           OR batch_code IS NULL
           OR status IS NULL
           OR is_active IS NULL
    ) THEN
        ALTER TABLE product_batches
            ALTER COLUMN product_id SET NOT NULL,
            ALTER COLUMN lab_id SET NOT NULL,
            ALTER COLUMN batch_code SET NOT NULL,
            ALTER COLUMN status SET NOT NULL,
            ALTER COLUMN is_active SET NOT NULL,
            ALTER COLUMN created_at SET NOT NULL,
            ALTER COLUMN updated_at SET NOT NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_product_batches_expiration_date
    ON product_batches (expiration_date);

CREATE INDEX IF NOT EXISTS idx_product_batches_lab_expiration
    ON product_batches (lab_id, expiration_date);

CREATE INDEX IF NOT EXISTS idx_product_batches_lab_active
    ON product_batches (lab_id, is_active);

CREATE INDEX IF NOT EXISTS idx_product_batches_product_lab_active
    ON product_batches (product_id, lab_id, is_active);

CREATE INDEX IF NOT EXISTS idx_inventory_movements_type
    ON inventory_movements (movement_type);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM inventory_movements
        WHERE lab_id IS NULL
           OR movement_type IS NULL
           OR performed_at IS NULL
    ) THEN
        ALTER TABLE inventory_movements
            ALTER COLUMN lab_id SET NOT NULL,
            ALTER COLUMN movement_type SET NOT NULL,
            ALTER COLUMN performed_at SET NOT NULL;
    END IF;
END $$;

ALTER TABLE IF EXISTS inventory_movement_lines
    ADD COLUMN IF NOT EXISTS batch_id BIGINT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_inventory_movement_lines_batch'
    ) THEN
        ALTER TABLE inventory_movement_lines
            ADD CONSTRAINT fk_inventory_movement_lines_batch
                FOREIGN KEY (batch_id)
                REFERENCES product_batches (batch_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM inventory_movement_lines
        WHERE movement_id IS NULL
           OR product_id IS NULL
           OR quantity IS NULL
    ) THEN
        ALTER TABLE inventory_movement_lines
            ALTER COLUMN movement_id SET NOT NULL,
            ALTER COLUMN product_id SET NOT NULL,
            ALTER COLUMN quantity SET NOT NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_inventory_movement_lines_batch_id
    ON inventory_movement_lines (batch_id);

ALTER TABLE IF EXISTS inventory_alerts
    ADD COLUMN IF NOT EXISTS alert_type VARCHAR(30) NOT NULL DEFAULT 'LOW_STOCK',
    ADD COLUMN IF NOT EXISTS product_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS batch_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS message TEXT NOT NULL DEFAULT 'Inventory alert',
    ADD COLUMN IF NOT EXISTS triggered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE inventory_alerts
SET message = 'Inventory alert'
WHERE message IS NULL OR BTRIM(message) = '';

UPDATE inventory_alerts
SET triggered_at = COALESCE(acknowledged_at, CURRENT_TIMESTAMP)
WHERE triggered_at IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_inventory_alerts_type'
    ) THEN
        ALTER TABLE inventory_alerts
            ADD CONSTRAINT chk_inventory_alerts_type
                CHECK (alert_type IN ('LOW_STOCK', 'EXPIRING_BATCH'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_inventory_alerts_product'
    ) THEN
        ALTER TABLE inventory_alerts
            ADD CONSTRAINT fk_inventory_alerts_product
                FOREIGN KEY (product_id)
                REFERENCES products (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_inventory_alerts_batch'
    ) THEN
        ALTER TABLE inventory_alerts
            ADD CONSTRAINT fk_inventory_alerts_batch
                FOREIGN KEY (batch_id)
                REFERENCES product_batches (batch_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM inventory_alerts
        WHERE lab_id IS NULL
           OR alert_type IS NULL
           OR message IS NULL
           OR triggered_at IS NULL
           OR product_id IS NULL
    ) THEN
        ALTER TABLE inventory_alerts
            ALTER COLUMN lab_id SET NOT NULL,
            ALTER COLUMN alert_type SET NOT NULL,
            ALTER COLUMN message SET NOT NULL,
            ALTER COLUMN triggered_at SET NOT NULL,
            ALTER COLUMN product_id SET NOT NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_inventory_alerts_product_id
    ON inventory_alerts (product_id);

CREATE INDEX IF NOT EXISTS idx_inventory_alerts_batch_id
    ON inventory_alerts (batch_id);

CREATE INDEX IF NOT EXISTS idx_inventory_alerts_type
    ON inventory_alerts (alert_type);

CREATE INDEX IF NOT EXISTS idx_inventory_alerts_lab_type_pending
    ON inventory_alerts (lab_id, alert_type, acknowledged_at);

CREATE INDEX IF NOT EXISTS idx_inventory_alerts_triggered_at
    ON inventory_alerts (triggered_at);
