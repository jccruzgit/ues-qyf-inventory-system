CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE units_of_measure (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_units_of_measure_type
        CHECK (type IN ('MASS', 'VOLUME', 'COUNT'))
);

CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE unit_conversions (
    id BIGSERIAL PRIMARY KEY,
    source_unit_id BIGINT NOT NULL,
    target_unit_id BIGINT NOT NULL,
    conversion_factor NUMERIC(19, 6) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_unit_conversions_source_target
        UNIQUE (source_unit_id, target_unit_id),
    CONSTRAINT chk_unit_conversions_different_units
        CHECK (source_unit_id <> target_unit_id),
    CONSTRAINT chk_unit_conversions_factor_positive
        CHECK (conversion_factor > 0),
    CONSTRAINT fk_unit_conversions_source_unit
        FOREIGN KEY (source_unit_id)
        REFERENCES units_of_measure (id),
    CONSTRAINT fk_unit_conversions_target_unit
        FOREIGN KEY (target_unit_id)
        REFERENCES units_of_measure (id)
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    category_id BIGINT NOT NULL,
    base_unit_id BIGINT NOT NULL,
    minimum_stock NUMERIC(19, 4) NOT NULL DEFAULT 0,
    current_stock NUMERIC(19, 4) NOT NULL DEFAULT 0,
    location_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_products_minimum_stock_non_negative
        CHECK (minimum_stock >= 0),
    CONSTRAINT chk_products_current_stock_non_negative
        CHECK (current_stock >= 0),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories (id),
    CONSTRAINT fk_products_base_unit
        FOREIGN KEY (base_unit_id)
        REFERENCES units_of_measure (id),
    CONSTRAINT fk_products_location
        FOREIGN KEY (location_id)
        REFERENCES locations (id)
);

INSERT INTO categories (name, description) VALUES
    ('Reactivos', 'Sustancias y reactivos quimicos para laboratorio'),
    ('Materiales', 'Materiales de apoyo para procesos de laboratorio'),
    ('Equipos', 'Equipos e instrumentos de trabajo'),
    ('Vidriería', 'Material de vidrio para uso academico y experimental');

INSERT INTO units_of_measure (name, symbol, type) VALUES
    ('Gram', 'g', 'MASS'),
    ('Milligram', 'mg', 'MASS'),
    ('Liter', 'L', 'VOLUME'),
    ('Milliliter', 'mL', 'VOLUME'),
    ('Unit', 'unit', 'COUNT');

INSERT INTO unit_conversions (source_unit_id, target_unit_id, conversion_factor)
VALUES
    ((SELECT id FROM units_of_measure WHERE symbol = 'g'),
     (SELECT id FROM units_of_measure WHERE symbol = 'mg'),
     1000.000000),
    ((SELECT id FROM units_of_measure WHERE symbol = 'L'),
     (SELECT id FROM units_of_measure WHERE symbol = 'mL'),
     1000.000000);

INSERT INTO locations (name, description) VALUES
    ('Bodega Central', 'Ubicacion principal de almacenamiento'),
    ('Laboratorio de Química General', 'Area de practicas de quimica general'),
    ('Laboratorio de Microbiología', 'Area de practicas y almacenamiento de microbiologia');
