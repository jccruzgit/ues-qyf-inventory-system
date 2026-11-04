UPDATE categories
SET name = 'Materia prima',
    description = 'Materias primas utilizadas en formulas y procesos de laboratorio'
WHERE LOWER(BTRIM(name)) = 'materiales'
  AND NOT EXISTS (
      SELECT 1
      FROM categories existing
      WHERE LOWER(BTRIM(existing.name)) = 'materia prima'
  );

INSERT INTO units_of_measure (name, symbol, type)
SELECT 'Kilogramo', 'kg', 'MASS'
WHERE NOT EXISTS (
    SELECT 1
    FROM units_of_measure
    WHERE LOWER(BTRIM(symbol)) = 'kg'
);

ALTER TABLE recipes
    ADD COLUMN IF NOT EXISTS group_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS cycle VARCHAR(50);

UPDATE recipes recipe
SET group_code = COALESCE(NULLIF(BTRIM(recipe.group_code), ''), NULLIF(BTRIM(product.group_code), ''), 'PENDIENTE'),
    cycle = COALESCE(NULLIF(BTRIM(recipe.cycle), ''), NULLIF(BTRIM(product.cycle), ''), 'PENDIENTE')
FROM manufactured_products product
WHERE recipe.manufactured_product_id = product.manufactured_product_id;

UPDATE recipes
SET group_code = COALESCE(NULLIF(BTRIM(group_code), ''), 'PENDIENTE'),
    cycle = COALESCE(NULLIF(BTRIM(cycle), ''), 'PENDIENTE');

ALTER TABLE recipes
    ALTER COLUMN group_code SET NOT NULL,
    ALTER COLUMN cycle SET NOT NULL;

ALTER TABLE production_runs
    ADD COLUMN IF NOT EXISTS lot_number VARCHAR(50);

UPDATE production_runs production_run
SET lot_number = COALESCE(
        NULLIF(BTRIM(production_run.lot_number), ''),
        NULLIF(BTRIM(product.lot_number), ''),
        'PENDIENTE'
    )
FROM manufactured_products product
WHERE production_run.manufactured_product_id = product.manufactured_product_id;

UPDATE production_runs
SET lot_number = COALESCE(NULLIF(BTRIM(lot_number), ''), 'PENDIENTE');

ALTER TABLE production_runs
    ALTER COLUMN lot_number SET NOT NULL;

ALTER TABLE manufactured_products
    DROP CONSTRAINT IF EXISTS chk_manufactured_products_group_code_not_blank,
    DROP CONSTRAINT IF EXISTS chk_manufactured_products_cycle_not_blank,
    DROP CONSTRAINT IF EXISTS chk_manufactured_products_lot_number_not_blank,
    DROP CONSTRAINT IF EXISTS chk_manufactured_products_lot_number_format;

ALTER TABLE manufactured_products
    ALTER COLUMN group_code DROP NOT NULL,
    ALTER COLUMN cycle DROP NOT NULL,
    ALTER COLUMN lot_number DROP NOT NULL;
