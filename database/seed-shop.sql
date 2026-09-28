-- Gia tham khao VND cho demo, khong phai bao gia hay giao dich that.
-- Chay sau database/seed.sql va migration 008_shop_cart_orders.
-- INSERT IGNORE giu seed additive va co the chay lai an toan.
SET NAMES utf8mb4;

INSERT IGNORE INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active)
VALUES ('shop-arduino-uno', 'arduino-uno', 150000, 24, TRUE);

INSERT IGNORE INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active)
VALUES ('shop-hc-sr04', 'hc-sr04', 25000, 18, TRUE);

INSERT IGNORE INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active)
VALUES ('shop-sg90', 'sg90', 30000, 12, TRUE);

INSERT IGNORE INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active)
VALUES ('shop-l298n', 'l298n', 35000, 8, TRUE);

INSERT IGNORE INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active)
VALUES ('shop-chassis-2wd', 'chassis-2wd', 80000, 5, TRUE);

INSERT IGNORE INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active)
VALUES ('shop-dc-motor', 'dc-motor', 40000, 3, TRUE);

INSERT IGNORE INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active)
VALUES ('shop-line-sensor', 'line-sensor', 35000, 0, TRUE);

INSERT IGNORE INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active)
VALUES ('shop-battery-holder', 'battery-holder', 20000, 14, TRUE);

INSERT IGNORE INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active)
VALUES ('shop-wheel', 'wheel', 12000, 22, TRUE);

INSERT IGNORE INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active)
VALUES ('shop-jumper-wire', 'jumper-wire', 15000, 30, TRUE);

INSERT IGNORE INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active)
VALUES ('shop-caster-wheel', 'caster-wheel', 12000, 9, TRUE);

INSERT IGNORE INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active)
VALUES ('shop-power-5v', 'power-5v', 45000, 6, TRUE);
