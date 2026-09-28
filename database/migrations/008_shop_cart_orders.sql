-- Cua hang linh kien va don hang mo phong (008_shop_cart_orders).
-- Run once after 007_troubleshooting on an existing database.
-- DDL implicitly commits in MySQL; do not wrap this migration in a transaction.
-- No payment provider, card data, or real charge is involved.

SET NAMES utf8mb4;

CREATE TABLE shop_products (
    id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL UNIQUE,
    price_vnd DECIMAL(12, 0) UNSIGNED NOT NULL CHECK (price_vnd > 0),
    stock_quantity INT UNSIGNED NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE CHECK (is_active IN (0, 1)),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (component_id)
        REFERENCES components(id)
        ON DELETE RESTRICT,

    INDEX idx_shop_products_active (is_active, id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE cart_items (
    user_id BIGINT UNSIGNED NOT NULL,
    product_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    quantity INT UNSIGNED NOT NULL CHECK (quantity BETWEEN 1 AND 10000),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, product_id),
    INDEX idx_cart_items_product (product_id),

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    FOREIGN KEY (product_id)
        REFERENCES shop_products(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE orders (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    status ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    total_vnd DECIMAL(14, 0) UNSIGNED NOT NULL CHECK (total_vnd >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_orders_user_created (user_id, created_at),

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE order_items (
    order_id BIGINT UNSIGNED NOT NULL,
    product_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    product_name_snapshot VARCHAR(150) NOT NULL,
    unit_price_vnd DECIMAL(12, 0) UNSIGNED NOT NULL CHECK (unit_price_vnd > 0),
    quantity INT UNSIGNED NOT NULL CHECK (quantity BETWEEN 1 AND 10000),

    PRIMARY KEY (order_id, product_id),
    INDEX idx_order_items_product (product_id),

    FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE RESTRICT,

    FOREIGN KEY (product_id)
        REFERENCES shop_products(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


INSERT INTO schema_migrations (version)
VALUES ('008_shop_cart_orders');
