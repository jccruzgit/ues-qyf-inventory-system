CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles (id)
);

INSERT INTO roles (name, description) VALUES
    ('ADMIN', 'System administrator with full access'),
    ('INVENTORY_MANAGER', 'Inventory manager responsible for stock operations'),
    ('LAB_TECHNICIAN', 'Laboratory technician with operational access'),
    ('VIEWER', 'Read-only access for inventory data');
