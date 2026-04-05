-- V1: Initial schema for Serene DMS
-- Roles: ADMIN, DEALER, CUSTOMER (no Employee)

CREATE TABLE IF NOT EXISTS roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(30)  NOT NULL UNIQUE,
    description VARCHAR(200)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name              VARCHAR(60)  NOT NULL,
    last_name               VARCHAR(60)  NOT NULL,
    email                   VARCHAR(150) NOT NULL UNIQUE,
    password_hash           VARCHAR(255) NOT NULL,
    phone                   VARCHAR(20),
    enabled                 TINYINT(1)   NOT NULL DEFAULT 1,
    account_locked          TINYINT(1)   NOT NULL DEFAULT 0,
    failed_attempts         INT          NOT NULL DEFAULT 0,
    account_expires_at      DATETIME,
    credentials_expires_at  DATETIME,
    created_at              DATETIME(6),
    updated_at              DATETIME(6),
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dealers (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    code       VARCHAR(20)  NOT NULL UNIQUE,
    address    VARCHAR(300),
    city       VARCHAR(80),
    state      VARCHAR(80),
    phone      VARCHAR(20),
    email      VARCHAR(150),
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    user_id    BIGINT UNIQUE,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_dealer_code (code),
    INDEX idx_dealer_user (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS customers (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    dealer_id    BIGINT       NOT NULL,
    user_id      BIGINT UNIQUE,
    first_name   VARCHAR(60)  NOT NULL,
    last_name    VARCHAR(60)  NOT NULL,
    email        VARCHAR(150),
    phone        VARCHAR(20),
    address      VARCHAR(300),
    city         VARCHAR(80),
    state        VARCHAR(80),
    date_of_birth DATE,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    notes        TEXT,
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    INDEX idx_customer_dealer (dealer_id),
    INDEX idx_customer_email  (email),
    INDEX idx_customer_user   (user_id),
    FOREIGN KEY (dealer_id) REFERENCES dealers(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vehicles (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    dealer_id    BIGINT       NOT NULL,
    model        VARCHAR(100) NOT NULL,
    variant      VARCHAR(80),
    vin          VARCHAR(17) UNIQUE,
    year         INT         NOT NULL,
    color        VARCHAR(50),
    price        DECIMAL(12,2) NOT NULL,
    mileage      VARCHAR(30),
    fuel_type    VARCHAR(30),
    transmission VARCHAR(30),
    status       VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    description  TEXT,
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    INDEX idx_vehicle_dealer (dealer_id),
    INDEX idx_vehicle_vin    (vin),
    INDEX idx_vehicle_status (status),
    FOREIGN KEY (dealer_id) REFERENCES dealers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(30)   NOT NULL UNIQUE,
    dealer_id    BIGINT        NOT NULL,
    customer_id  BIGINT        NOT NULL,
    vehicle_id   BIGINT,
    amount       DECIMAL(12,2) NOT NULL,
    discount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    final_amount DECIMAL(12,2) NOT NULL,
    status       VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    notes        TEXT,
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    INDEX idx_order_number   (order_number),
    INDEX idx_order_dealer   (dealer_id),
    INDEX idx_order_customer (customer_id),
    INDEX idx_order_status   (status),
    FOREIGN KEY (dealer_id)   REFERENCES dealers(id)   ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id)  REFERENCES vehicles(id)  ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inquiries (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    dealer_id   BIGINT NOT NULL,
    customer_id BIGINT,
    vehicle_id  BIGINT,
    name        VARCHAR(120),
    email       VARCHAR(150),
    phone       VARCHAR(20),
    subject     VARCHAR(200),
    message     TEXT NOT NULL,
    response    TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    INDEX idx_inquiry_dealer   (dealer_id),
    INDEX idx_inquiry_customer (customer_id),
    INDEX idx_inquiry_status   (status),
    FOREIGN KEY (dealer_id)   REFERENCES dealers(id)   ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    FOREIGN KEY (vehicle_id)  REFERENCES vehicles(id)  ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS menus (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id  BIGINT,
    label      VARCHAR(80)  NOT NULL,
    icon       VARCHAR(50),
    url        VARCHAR(200),
    sort_order INT          NOT NULL DEFAULT 0,
    enabled    TINYINT(1)   NOT NULL DEFAULT 1,
    roles      VARCHAR(100),
    INDEX idx_menu_parent (parent_id),
    INDEX idx_menu_sort   (sort_order),
    FOREIGN KEY (parent_id) REFERENCES menus(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
