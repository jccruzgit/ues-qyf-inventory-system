CREATE TABLE IF NOT EXISTS laboratories (
    lab_id BIGSERIAL PRIMARY KEY,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    CONSTRAINT fk_laboratories_deleted_by
        FOREIGN KEY (deleted_by)
        REFERENCES users (id)
);

ALTER TABLE IF EXISTS laboratories
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS deleted_by BIGINT NULL;

DO $$
BEGIN
    IF to_regclass('public.laboratories') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_laboratories_deleted_by'
            ) THEN
        ALTER TABLE laboratories
            ADD CONSTRAINT fk_laboratories_deleted_by
                FOREIGN KEY (deleted_by)
                REFERENCES users (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_laboratories_deleted_by
    ON laboratories (deleted_by);

DO $$
BEGIN
    IF to_regclass('public.user_laboratories') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_user_laboratories_lab'
            ) THEN
        ALTER TABLE user_laboratories
            ADD CONSTRAINT fk_user_laboratories_lab
                FOREIGN KEY (lab_id)
                REFERENCES laboratories (lab_id);
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.audit_log') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_audit_log_lab'
            ) THEN
        ALTER TABLE audit_log
            ADD CONSTRAINT fk_audit_log_lab
                FOREIGN KEY (lab_id)
                REFERENCES laboratories (lab_id);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS product_batches (
    batch_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NULL,
    lab_id BIGINT NULL,
    batch_code VARCHAR(100) NOT NULL,
    certificate_document_id BIGINT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    CONSTRAINT uk_product_batches_product_lab_batch
        UNIQUE (product_id, lab_id, batch_code),
    CONSTRAINT fk_product_batches_product
        FOREIGN KEY (product_id)
        REFERENCES products (id),
    CONSTRAINT fk_product_batches_lab
        FOREIGN KEY (lab_id)
        REFERENCES laboratories (lab_id),
    CONSTRAINT fk_product_batches_certificate_document
        FOREIGN KEY (certificate_document_id)
        REFERENCES product_documents (document_id),
    CONSTRAINT fk_product_batches_deleted_by
        FOREIGN KEY (deleted_by)
        REFERENCES users (id)
);

ALTER TABLE IF EXISTS product_batches
    ADD COLUMN IF NOT EXISTS product_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS lab_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS batch_code VARCHAR(100) NOT NULL DEFAULT 'UNSPECIFIED',
    ADD COLUMN IF NOT EXISTS certificate_document_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS notes TEXT NULL,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS deleted_by BIGINT NULL;

DO $$
BEGIN
    IF to_regclass('public.product_batches') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_product_batches_product'
            ) THEN
        ALTER TABLE product_batches
            ADD CONSTRAINT fk_product_batches_product
                FOREIGN KEY (product_id)
                REFERENCES products (id);
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.product_batches') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_product_batches_lab'
            ) THEN
        ALTER TABLE product_batches
            ADD CONSTRAINT fk_product_batches_lab
                FOREIGN KEY (lab_id)
                REFERENCES laboratories (lab_id);
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.product_batches') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_product_batches_certificate_document'
            ) THEN
        ALTER TABLE product_batches
            ADD CONSTRAINT fk_product_batches_certificate_document
                FOREIGN KEY (certificate_document_id)
                REFERENCES product_documents (document_id);
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.product_batches') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_product_batches_deleted_by'
            ) THEN
        ALTER TABLE product_batches
            ADD CONSTRAINT fk_product_batches_deleted_by
                FOREIGN KEY (deleted_by)
                REFERENCES users (id);
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.product_batches') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'uk_product_batches_product_lab_batch'
            ) THEN
        ALTER TABLE product_batches
            ADD CONSTRAINT uk_product_batches_product_lab_batch
                UNIQUE (product_id, lab_id, batch_code);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_product_batches_product_id
    ON product_batches (product_id);

CREATE INDEX IF NOT EXISTS idx_product_batches_lab_id
    ON product_batches (lab_id);

CREATE INDEX IF NOT EXISTS idx_product_batches_certificate_document_id
    ON product_batches (certificate_document_id);

CREATE INDEX IF NOT EXISTS idx_product_batches_deleted_by
    ON product_batches (deleted_by);

CREATE INDEX IF NOT EXISTS idx_product_batches_status
    ON product_batches (status);

CREATE INDEX IF NOT EXISTS idx_product_batches_is_active
    ON product_batches (is_active);

CREATE TABLE IF NOT EXISTS inventory_movements (
    movement_id BIGSERIAL PRIMARY KEY,
    lab_id BIGINT NULL,
    attachment_document_id BIGINT NULL,
    CONSTRAINT fk_inventory_movements_lab
        FOREIGN KEY (lab_id)
        REFERENCES laboratories (lab_id),
    CONSTRAINT fk_inventory_movements_attachment_document
        FOREIGN KEY (attachment_document_id)
        REFERENCES product_documents (document_id)
);

ALTER TABLE IF EXISTS inventory_movements
    ADD COLUMN IF NOT EXISTS lab_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS attachment_document_id BIGINT NULL;

DO $$
BEGIN
    IF to_regclass('public.inventory_movements') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_inventory_movements_lab'
            ) THEN
        ALTER TABLE inventory_movements
            ADD CONSTRAINT fk_inventory_movements_lab
                FOREIGN KEY (lab_id)
                REFERENCES laboratories (lab_id);
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.inventory_movements') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_inventory_movements_attachment_document'
            ) THEN
        ALTER TABLE inventory_movements
            ADD CONSTRAINT fk_inventory_movements_attachment_document
                FOREIGN KEY (attachment_document_id)
                REFERENCES product_documents (document_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_inventory_movements_lab_id
    ON inventory_movements (lab_id);

CREATE INDEX IF NOT EXISTS idx_inventory_movements_attachment_document_id
    ON inventory_movements (attachment_document_id);

CREATE TABLE IF NOT EXISTS inventory_movement_lines (
    movement_line_id BIGSERIAL PRIMARY KEY,
    line_notes TEXT NULL
);

ALTER TABLE IF EXISTS inventory_movement_lines
    ADD COLUMN IF NOT EXISTS line_notes TEXT NULL;

CREATE TABLE IF NOT EXISTS inventory_alerts (
    alert_id BIGSERIAL PRIMARY KEY,
    lab_id BIGINT NULL,
    acknowledged_by BIGINT NULL,
    acknowledged_at TIMESTAMP NULL,
    CONSTRAINT fk_inventory_alerts_lab
        FOREIGN KEY (lab_id)
        REFERENCES laboratories (lab_id),
    CONSTRAINT fk_inventory_alerts_acknowledged_by
        FOREIGN KEY (acknowledged_by)
        REFERENCES users (id)
);

ALTER TABLE IF EXISTS inventory_alerts
    ADD COLUMN IF NOT EXISTS lab_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS acknowledged_by BIGINT NULL,
    ADD COLUMN IF NOT EXISTS acknowledged_at TIMESTAMP NULL;

DO $$
BEGIN
    IF to_regclass('public.inventory_alerts') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_inventory_alerts_lab'
            ) THEN
        ALTER TABLE inventory_alerts
            ADD CONSTRAINT fk_inventory_alerts_lab
                FOREIGN KEY (lab_id)
                REFERENCES laboratories (lab_id);
    END IF;
END $$;

DO $$
BEGIN
    IF to_regclass('public.inventory_alerts') IS NOT NULL
            AND NOT EXISTS (
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'fk_inventory_alerts_acknowledged_by'
            ) THEN
        ALTER TABLE inventory_alerts
            ADD CONSTRAINT fk_inventory_alerts_acknowledged_by
                FOREIGN KEY (acknowledged_by)
                REFERENCES users (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_inventory_alerts_lab_id
    ON inventory_alerts (lab_id);

CREATE INDEX IF NOT EXISTS idx_inventory_alerts_acknowledged_by
    ON inventory_alerts (acknowledged_by);

CREATE INDEX IF NOT EXISTS idx_inventory_alerts_acknowledged_at
    ON inventory_alerts (acknowledged_at);
