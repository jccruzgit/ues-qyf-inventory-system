ALTER TABLE manufactured_products
    ADD COLUMN IF NOT EXISTS group_code VARCHAR(50);

ALTER TABLE manufactured_products
    ADD COLUMN IF NOT EXISTS cycle VARCHAR(50);

ALTER TABLE manufactured_products
    ADD COLUMN IF NOT EXISTS lot_number VARCHAR(50);

UPDATE manufactured_products
SET group_code = COALESCE(NULLIF(BTRIM(group_code), ''), 'PENDIENTE'),
    cycle = COALESCE(NULLIF(BTRIM(cycle), ''), 'PENDIENTE'),
    lot_number = COALESCE(NULLIF(BTRIM(lot_number), ''), 'PENDIENTE');

ALTER TABLE manufactured_products
    ALTER COLUMN group_code SET NOT NULL;

ALTER TABLE manufactured_products
    ALTER COLUMN cycle SET NOT NULL;

ALTER TABLE manufactured_products
    ALTER COLUMN lot_number SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_manufactured_products_group_code_not_blank'
    ) THEN
        ALTER TABLE manufactured_products
            ADD CONSTRAINT chk_manufactured_products_group_code_not_blank
                CHECK (BTRIM(group_code) <> '');
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_manufactured_products_cycle_not_blank'
    ) THEN
        ALTER TABLE manufactured_products
            ADD CONSTRAINT chk_manufactured_products_cycle_not_blank
                CHECK (BTRIM(cycle) <> '');
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_manufactured_products_lot_number_not_blank'
    ) THEN
        ALTER TABLE manufactured_products
            ADD CONSTRAINT chk_manufactured_products_lot_number_not_blank
                CHECK (BTRIM(lot_number) <> '');
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_manufactured_products_lot_number_format'
    ) THEN
        ALTER TABLE manufactured_products
            ADD CONSTRAINT chk_manufactured_products_lot_number_format
                CHECK (lot_number ~ '^[A-Za-z0-9][A-Za-z0-9._/-]*$');
    END IF;
END $$;
