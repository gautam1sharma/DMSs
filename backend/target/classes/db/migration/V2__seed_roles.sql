-- V2: Seed roles — ADMIN, DEALER, CUSTOMER

INSERT IGNORE INTO roles (name, description) VALUES
    ('ADMIN',    'System administrator with full access'),
    ('DEALER',   'Dealer who manages customers and inventory'),
    ('CUSTOMER', 'End customer with read-only portal access');
