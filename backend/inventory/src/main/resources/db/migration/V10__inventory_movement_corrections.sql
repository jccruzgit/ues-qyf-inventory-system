ALTER TABLE inventory_movements
    ADD COLUMN IF NOT EXISTS correction_type VARCHAR(20) NULL,
    ADD COLUMN IF NOT EXISTS related_movement_id BIGINT NULL,
    ADD COLUMN IF NOT EXISTS correction_reason TEXT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_inventory_movements_correction_type'
    ) THEN
        ALTER TABLE inventory_movements
            ADD CONSTRAINT chk_inventory_movements_correction_type
                CHECK (correction_type IS NULL OR correction_type IN ('NORMAL', 'REVERSAL'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_inventory_movements_related_movement'
    ) THEN
        ALTER TABLE inventory_movements
            ADD CONSTRAINT fk_inventory_movements_related_movement
                FOREIGN KEY (related_movement_id)
                REFERENCES inventory_movements (movement_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_inventory_movements_correction_type
    ON inventory_movements (correction_type);

CREATE INDEX IF NOT EXISTS idx_inventory_movements_related_movement_id
    ON inventory_movements (related_movement_id);
