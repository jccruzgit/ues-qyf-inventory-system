ALTER TABLE inventory_movement_lines
    ADD COLUMN IF NOT EXISTS unit_price NUMERIC(19, 4) NULL,
    ADD COLUMN IF NOT EXISTS price_unit_id BIGINT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_inventory_movement_lines_unit_price_non_negative'
    ) THEN
        ALTER TABLE inventory_movement_lines
            ADD CONSTRAINT chk_inventory_movement_lines_unit_price_non_negative
                CHECK (unit_price IS NULL OR unit_price >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_inventory_movement_lines_price_unit'
    ) THEN
        ALTER TABLE inventory_movement_lines
            ADD CONSTRAINT fk_inventory_movement_lines_price_unit
                FOREIGN KEY (price_unit_id)
                REFERENCES units_of_measure (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_inventory_movement_lines_price_unit_id
    ON inventory_movement_lines (price_unit_id);
