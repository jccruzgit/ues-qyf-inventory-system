CREATE TABLE IF NOT EXISTS user_laboratories (
    user_lab_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lab_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_user_laboratories_user_lab UNIQUE (user_id, lab_id),
    CONSTRAINT fk_user_laboratories_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

DO $$
BEGIN
    IF to_regclass('public.laboratories') IS NOT NULL
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

CREATE INDEX IF NOT EXISTS idx_user_laboratories_user_id
    ON user_laboratories (user_id);

CREATE INDEX IF NOT EXISTS idx_user_laboratories_lab_id
    ON user_laboratories (lab_id);

CREATE TABLE IF NOT EXISTS audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(120) NOT NULL,
    record_id BIGINT NOT NULL,
    action VARCHAR(30) NOT NULL,
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    old_values TEXT NULL,
    new_values TEXT NULL,
    description TEXT NULL,
    lab_id BIGINT NULL,
    CONSTRAINT fk_audit_log_changed_by
        FOREIGN KEY (changed_by)
        REFERENCES users (id)
);

DO $$
BEGIN
    IF to_regclass('public.laboratories') IS NOT NULL
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

CREATE INDEX IF NOT EXISTS idx_audit_log_table_record
    ON audit_log (table_name, record_id);

CREATE INDEX IF NOT EXISTS idx_audit_log_changed_by
    ON audit_log (changed_by);

CREATE INDEX IF NOT EXISTS idx_audit_log_changed_at
    ON audit_log (changed_at);

CREATE INDEX IF NOT EXISTS idx_audit_log_lab_id
    ON audit_log (lab_id);

CREATE TABLE IF NOT EXISTS product_documents (
    document_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    description TEXT NULL,
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    CONSTRAINT fk_product_documents_product
        FOREIGN KEY (product_id)
        REFERENCES products (id),
    CONSTRAINT fk_product_documents_uploaded_by
        FOREIGN KEY (uploaded_by)
        REFERENCES users (id),
    CONSTRAINT fk_product_documents_deleted_by
        FOREIGN KEY (deleted_by)
        REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_product_documents_product_id
    ON product_documents (product_id);

CREATE INDEX IF NOT EXISTS idx_product_documents_uploaded_by
    ON product_documents (uploaded_by);

CREATE INDEX IF NOT EXISTS idx_product_documents_deleted_by
    ON product_documents (deleted_by);

CREATE INDEX IF NOT EXISTS idx_product_documents_is_active
    ON product_documents (is_active);

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS observations TEXT NULL,
    ADD COLUMN IF NOT EXISTS storage_condition VARCHAR(120) NULL,
    ADD COLUMN IF NOT EXISTS requires_expiration BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS requires_batch_control BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS deleted_by BIGINT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'products'
          AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE products
            ADD COLUMN updated_at TIMESTAMP NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_products_deleted_by'
    ) THEN
        ALTER TABLE products
            ADD CONSTRAINT fk_products_deleted_by
                FOREIGN KEY (deleted_by)
                REFERENCES users (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_products_deleted_by
    ON products (deleted_by);

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

DO $$
BEGIN
    IF to_regclass('public.laboratories') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS idx_laboratories_deleted_by
            ON laboratories (deleted_by);
    END IF;
END $$;

ALTER TABLE IF EXISTS product_batches
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
            AND EXISTS (
                SELECT 1
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'product_batches'
                  AND column_name = 'product_id'
            )
            AND EXISTS (
                SELECT 1
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'product_batches'
                  AND column_name = 'lab_id'
            )
            AND EXISTS (
                SELECT 1
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'product_batches'
                  AND column_name = 'batch_code'
            )
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

DO $$
BEGIN
    IF to_regclass('public.product_batches') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS idx_product_batches_certificate_document_id
            ON product_batches (certificate_document_id);
        CREATE INDEX IF NOT EXISTS idx_product_batches_deleted_by
            ON product_batches (deleted_by);
        CREATE INDEX IF NOT EXISTS idx_product_batches_status
            ON product_batches (status);
    END IF;
END $$;

ALTER TABLE IF EXISTS inventory_movements
    ADD COLUMN IF NOT EXISTS attachment_document_id BIGINT NULL;

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

DO $$
BEGIN
    IF to_regclass('public.inventory_movements') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS idx_inventory_movements_attachment_document_id
            ON inventory_movements (attachment_document_id);
    END IF;
END $$;

ALTER TABLE IF EXISTS inventory_movement_lines
    ADD COLUMN IF NOT EXISTS line_notes TEXT NULL;

ALTER TABLE IF EXISTS inventory_alerts
    ADD COLUMN IF NOT EXISTS acknowledged_by BIGINT NULL,
    ADD COLUMN IF NOT EXISTS acknowledged_at TIMESTAMP NULL;

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

DO $$
BEGIN
    IF to_regclass('public.inventory_alerts') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS idx_inventory_alerts_acknowledged_by
            ON inventory_alerts (acknowledged_by);
        CREATE INDEX IF NOT EXISTS idx_inventory_alerts_acknowledged_at
            ON inventory_alerts (acknowledged_at);
    END IF;
END $$;
