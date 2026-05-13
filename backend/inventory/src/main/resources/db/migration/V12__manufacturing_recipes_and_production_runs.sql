CREATE TABLE IF NOT EXISTS manufactured_products (
    manufactured_product_id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_manufactured_products_code'
    ) THEN
        ALTER TABLE manufactured_products
            ADD CONSTRAINT uk_manufactured_products_code
                UNIQUE (code);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_manufactured_products_deleted_by'
    ) THEN
        ALTER TABLE manufactured_products
            ADD CONSTRAINT fk_manufactured_products_deleted_by
                FOREIGN KEY (deleted_by)
                REFERENCES users (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_manufactured_products_name
    ON manufactured_products (name);

CREATE TABLE IF NOT EXISTS recipes (
    recipe_id BIGSERIAL PRIMARY KEY,
    manufactured_product_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_recipes_code'
    ) THEN
        ALTER TABLE recipes
            ADD CONSTRAINT uk_recipes_code
                UNIQUE (code);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_recipes_manufactured_product'
    ) THEN
        ALTER TABLE recipes
            ADD CONSTRAINT fk_recipes_manufactured_product
                FOREIGN KEY (manufactured_product_id)
                REFERENCES manufactured_products (manufactured_product_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_recipes_deleted_by'
    ) THEN
        ALTER TABLE recipes
            ADD CONSTRAINT fk_recipes_deleted_by
                FOREIGN KEY (deleted_by)
                REFERENCES users (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_recipes_manufactured_product_id
    ON recipes (manufactured_product_id);

CREATE TABLE IF NOT EXISTS recipe_items (
    recipe_item_id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    unit_of_measure_id BIGINT NOT NULL,
    quantity NUMERIC(19, 4) NOT NULL,
    item_order INTEGER NOT NULL DEFAULT 1,
    observations TEXT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_recipe_items_quantity_positive'
    ) THEN
        ALTER TABLE recipe_items
            ADD CONSTRAINT chk_recipe_items_quantity_positive
                CHECK (quantity > 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_recipe_items_item_order_positive'
    ) THEN
        ALTER TABLE recipe_items
            ADD CONSTRAINT chk_recipe_items_item_order_positive
                CHECK (item_order > 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_recipe_items_recipe'
    ) THEN
        ALTER TABLE recipe_items
            ADD CONSTRAINT fk_recipe_items_recipe
                FOREIGN KEY (recipe_id)
                REFERENCES recipes (recipe_id)
                ON DELETE CASCADE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_recipe_items_product'
    ) THEN
        ALTER TABLE recipe_items
            ADD CONSTRAINT fk_recipe_items_product
                FOREIGN KEY (product_id)
                REFERENCES products (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_recipe_items_unit_of_measure'
    ) THEN
        ALTER TABLE recipe_items
            ADD CONSTRAINT fk_recipe_items_unit_of_measure
                FOREIGN KEY (unit_of_measure_id)
                REFERENCES units_of_measure (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_recipe_items_recipe_product'
    ) THEN
        ALTER TABLE recipe_items
            ADD CONSTRAINT uk_recipe_items_recipe_product
                UNIQUE (recipe_id, product_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_recipe_items_recipe_id
    ON recipe_items (recipe_id);

CREATE TABLE IF NOT EXISTS production_runs (
    production_run_id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    manufactured_product_id BIGINT NOT NULL,
    lab_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    confirmed_by BIGINT NULL,
    inventory_movement_id BIGINT NULL,
    run_status VARCHAR(20) NOT NULL,
    group_name VARCHAR(150) NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_production_runs_status'
    ) THEN
        ALTER TABLE production_runs
            ADD CONSTRAINT chk_production_runs_status
                CHECK (run_status IN ('DRAFT', 'CONFIRMED'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_production_runs_recipe'
    ) THEN
        ALTER TABLE production_runs
            ADD CONSTRAINT fk_production_runs_recipe
                FOREIGN KEY (recipe_id)
                REFERENCES recipes (recipe_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_production_runs_manufactured_product'
    ) THEN
        ALTER TABLE production_runs
            ADD CONSTRAINT fk_production_runs_manufactured_product
                FOREIGN KEY (manufactured_product_id)
                REFERENCES manufactured_products (manufactured_product_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_production_runs_laboratory'
    ) THEN
        ALTER TABLE production_runs
            ADD CONSTRAINT fk_production_runs_laboratory
                FOREIGN KEY (lab_id)
                REFERENCES laboratories (lab_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_production_runs_created_by'
    ) THEN
        ALTER TABLE production_runs
            ADD CONSTRAINT fk_production_runs_created_by
                FOREIGN KEY (created_by)
                REFERENCES users (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_production_runs_confirmed_by'
    ) THEN
        ALTER TABLE production_runs
            ADD CONSTRAINT fk_production_runs_confirmed_by
                FOREIGN KEY (confirmed_by)
                REFERENCES users (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_production_runs_inventory_movement'
    ) THEN
        ALTER TABLE production_runs
            ADD CONSTRAINT fk_production_runs_inventory_movement
                FOREIGN KEY (inventory_movement_id)
                REFERENCES inventory_movements (movement_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_production_runs_inventory_movement'
    ) THEN
        ALTER TABLE production_runs
            ADD CONSTRAINT uk_production_runs_inventory_movement
                UNIQUE (inventory_movement_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_production_runs_recipe_id
    ON production_runs (recipe_id);

CREATE INDEX IF NOT EXISTS idx_production_runs_lab_id
    ON production_runs (lab_id);

CREATE INDEX IF NOT EXISTS idx_production_runs_status
    ON production_runs (run_status);
