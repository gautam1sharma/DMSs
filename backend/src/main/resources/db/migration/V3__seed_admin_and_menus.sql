-- V3: Seed default admin user (password: Admin@123 BCrypt encoded)
-- BCrypt of 'Admin@123' with strength 12

INSERT IGNORE INTO users (first_name, last_name, email, password_hash, phone, enabled, account_locked, failed_attempts, created_at, updated_at, created_by, updated_by)
VALUES (
    'Admin', 'Serene',
    'admin@serene.com',
    '$2a$12$LZcmHLfFYHHTVjFbkLk8xeGU9Ym7PkniwkusPv4wqPKqeRR.GjSOq',
    '+91 9000000000',
    1, 0, 0,
    NOW(), NOW(), 'system', 'system'
);

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@serene.com' AND r.name = 'ADMIN';

-- Seed default menus (three role sets: ADMIN, DEALER, CUSTOMER)
-- ADMIN menus
INSERT IGNORE INTO menus (label, icon, url, sort_order, enabled, roles) VALUES
    ('Dashboard',  'LayoutDashboard', '/admin/dashboard', 1, 1, 'ADMIN'),
    ('Dealers',    'Building2',       '/admin/dealers',   2, 1, 'ADMIN'),
    ('Users',      'Users',           '/admin/users',     3, 1, 'ADMIN'),
    ('Reports',    'BarChart3',       '/admin/reports',   4, 1, 'ADMIN'),
    ('Settings',   'Settings',        '/admin/settings',  5, 1, 'ADMIN');

-- DEALER menus
INSERT IGNORE INTO menus (label, icon, url, sort_order, enabled, roles) VALUES
    ('Dashboard',  'LayoutDashboard', '/dealer/dashboard',  1, 1, 'DEALER'),
    ('Customers',  'Users',           '/dealer/customers',  2, 1, 'DEALER'),
    ('Vehicles',   'Car',             '/dealer/vehicles',   3, 1, 'DEALER'),
    ('Orders',     'ShoppingCart',    '/dealer/orders',     4, 1, 'DEALER'),
    ('Inquiries',  'MessageSquare',   '/dealer/inquiries',  5, 1, 'DEALER'),
    ('Profile',    'UserCircle',      '/dealer/profile',    6, 1, 'DEALER');

-- CUSTOMER menus
INSERT IGNORE INTO menus (label, icon, url, sort_order, enabled, roles) VALUES
    ('My Portal',  'LayoutDashboard', '/customer/dashboard', 1, 1, 'CUSTOMER'),
    ('My Orders',  'ShoppingCart',    '/customer/orders',    2, 1, 'CUSTOMER'),
    ('My Inquiries','MessageSquare',  '/customer/inquiries', 3, 1, 'CUSTOMER'),
    ('Profile',    'UserCircle',      '/customer/profile',   4, 1, 'CUSTOMER');
