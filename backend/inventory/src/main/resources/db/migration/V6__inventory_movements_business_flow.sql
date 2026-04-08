ALTER TABLE inventory_movements
    ADD COLUMN IF NOT EXISTS movement_type VARCHAR(20) NOT NULL DEFAULT 'ENTRY',
    ADD COLUMN IF NOT EXISTS performed_by BIGINT NULL,
    ADD COLUMN IF NOT EXISTS performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS observation TEXT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_inventory_movements_type'
    ) THEN
        ALTER TABLE inventory_movements
            ADD CONSTRAINT chk_inventory_movements_type
                CHECK (movement_type IN ('ENTRY', 'EXIT'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_inventory_movements_performed_by'
    ) THEN
        ALTER TABLE inventory_movements
            ADD CONSTRAINT fk_inventory_movements_performed_by
                FOREIGN KEY (performed_by)
                REFERENCES users (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_inventory_movements_performed_by
    ON inventory_movements (performed_by);

CREATE INDEX IF NOT EXISTS idx_inventory_movements_performed_at
    ON inventory_movements (performed_at);

ALTER TABLE inventory_movement_lines
    ADD COLUMN IF NOT EXISTS movement_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS product_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS quantity NUMERIC(19, 4) NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_inventory_movement_lines_quantity_positive'
    ) THEN
        ALTER TABLE inventory_movement_lines
            ADD CONSTRAINT chk_inventory_movement_lines_quantity_positive
                CHECK (quantity IS NULL OR quantity > 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_inventory_movement_lines_movement'
    ) THEN
        ALTER TABLE inventory_movement_lines
            ADD CONSTRAINT fk_inventory_movement_lines_movement
                FOREIGN KEY (movement_id)
                REFERENCES inventory_movements (movement_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_inventory_movement_lines_product'
    ) THEN
        ALTER TABLE inventory_movement_lines
            ADD CONSTRAINT fk_inventory_movement_lines_product
                FOREIGN KEY (product_id)
                REFERENCES products (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_inventory_movement_lines_movement_id
    ON inventory_movement_lines (movement_id);

CREATE INDEX IF NOT EXISTS idx_inventory_movement_lines_product_id
    ON inventory_movement_lines (product_id);
