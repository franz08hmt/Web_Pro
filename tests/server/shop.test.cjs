"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const root = path.join(__dirname, "..", "..");
const javaRoot = path.join(root, "tomcat-app", "src", "main", "java", "vn", "edu", "webpro", "robotlab");
const migrationPath = path.join(root, "database", "migrations", "008_shop_cart_orders.sql");
const schemaPath = path.join(root, "database", "schema.sql");

function read(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), "utf8");
}

test("the approved ERD describes commerce tables before the additive 008 migration", () => {
  const erd = read("docs/erd.md");
  assert.match(erd, /SHOP_PRODUCTS[\s\S]*?CART_ITEMS[\s\S]*?ORDERS[\s\S]*?ORDER_ITEMS/);
  assert.match(erd, /product_name_snapshot/);
  assert.match(erd, /unit_price_vnd/);
  assert.match(erd, /HttpSession/);
  assert.ok(fs.existsSync(migrationPath), "thiếu migration đợt 4 sau khi ERD đã được duyệt");
});

test("schema and migration define VND prices, positive quantities, ownership, and preserved order history", () => {
  const migration = read("database/migrations/008_shop_cart_orders.sql");
  const schema = read("database/schema.sql");
  for (const source of [migration, schema]) {
    for (const table of ["shop_products", "cart_items", "orders", "order_items"]) {
      assert.match(source, new RegExp(`CREATE TABLE ${table}\\s*\\(`), `thiếu ${table}`);
    }
    assert.match(source, /price_vnd DECIMAL\(12, 0\)/i);
    assert.match(source, /product_name_snapshot/);
    assert.match(source, /unit_price_vnd DECIMAL\(12, 0\)/i);
    assert.match(source, /FOREIGN KEY \(user_id\)[\s\S]*?REFERENCES users\(id\)[\s\S]*?ON DELETE RESTRICT/i);
    assert.match(source, /FOREIGN KEY \(product_id\)[\s\S]*?REFERENCES shop_products\(id\)[\s\S]*?ON DELETE RESTRICT/i);
    assert.match(source, /VALUES \('008_shop_cart_orders'\)/);
  }
  assert.match(migration, /PRIMARY KEY \(user_id, product_id\)/i);
  assert.match(migration, /CHECK \(quantity BETWEEN 1 AND 10000\)/i);
});

test("catalog is public; cart and order APIs require a session, with CSRF on writes", () => {
  const catalog = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/ShopServlet.java");
  const cart = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/CartServlet.java");
  const orders = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/OrderServlet.java");
  assert.match(catalog, /@WebServlet\("\/api\/shop\/products"\)/);
  assert.doesNotMatch(catalog, /SessionUtil\.requireUser|SessionUtil\.requireAdmin/);
  for (const servlet of [cart, orders]) {
    assert.match(servlet, /SessionUtil\.requireUser\(request, response\)/);
    assert.match(servlet, /SessionUtil\.hasValidCsrfToken\(request, response\)/);
  }
});

test("cart/order queries derive ownership from the authenticated user and never accept client totals", () => {
  const cartServlet = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/CartServlet.java");
  const orderServlet = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/OrderServlet.java");
  const cartDb = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/CartDB.java");
  const orderDb = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/OrderDB.java");
  assert.match(cartServlet, /CartDB\.[\s\S]*?user\.getId\(\)/);
  assert.match(orderServlet, /OrderDB\.[\s\S]*?user\.getId\(\)/);
  assert.match(cartDb, /user_id = \?/);
  assert.match(orderDb, /user_id = \?/);
  assert.doesNotMatch(cartServlet + orderServlet, /stringField\([^\n]*(?:userId|price|total|stock)/i);
});

test("checkout checks and decrements stock inside one database transaction with row locks", () => {
  const orderDb = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/OrderDB.java");
  assert.match(orderDb, /setAutoCommit\(false\)/);
  assert.match(orderDb, /FOR UPDATE/i);
  assert.match(orderDb, /SELECT[\s\S]{0,240}price_vnd[\s\S]{0,240}stock_quantity/i);
  assert.match(orderDb, /connection\.commit\(\)/);
  assert.match(orderDb, /connection\.rollback\(\)/);
  assert.match(orderDb, /UPDATE shop_products SET stock_quantity = stock_quantity - \?/i);
  assert.match(orderDb, /product_name_snapshot/);
  assert.match(orderDb, /unit_price_vnd/);
});

test("admin shop CRUD requires ADMIN and CSRF; delete preserves the row by deactivating it", () => {
  const adminServlet = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AdminShopServlet.java");
  const shopDb = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ShopProductDB.java");
  assert.match(adminServlet, /SessionUtil\.requireAdmin\(request, response\)/);
  assert.match(adminServlet, /SessionUtil\.hasValidCsrfToken\(request, response\)/);
  assert.match(shopDb, /UPDATE shop_products SET is_active = FALSE WHERE id = \?/i);
  assert.doesNotMatch(shopDb, /DELETE FROM shop_products/i);
});

test("shop seed is additive, contains 8–12 products, and advertises demo-only orders", () => {
  const seed = read("database/seed-shop.sql");
  assert.doesNotMatch(seed, /DROP\s|TRUNCATE|DELETE FROM|UPDATE\s/i);
  const inserts = seed.match(/INSERT(?: IGNORE)? INTO shop_products/g) || [];
  assert.ok(inserts.length >= 8 && inserts.length <= 12, `cần 8–12 sản phẩm, hiện có ${inserts.length}`);
  const shopPage = read("pages/cua-hang.html");
  const cartPage = read("pages/gio-hang.html");
  assert.match(shopPage + cartPage, /đơn mô phỏng|không thanh toán thật/i);
  assert.match(read("pages/linh-kien.html"), /href="cua-hang\.html"/);
});

test("shop pages and APIs are documented with the existing Servlet/JSP Model 2 contract", () => {
  const apiDocs = read("docs/API_CONVENTIONS.md");
  const architecture = read("docs/ARCHITECTURE.md");
  const readme = read("README.md");
  assert.match(apiDocs, /\/api\/shop\/products/);
  assert.match(apiDocs, /\/api\/cart/);
  assert.match(apiDocs, /\/api\/orders/);
  assert.match(architecture, /OrderDB|CartDB|ShopProductDB/);
  assert.match(readme, /008_shop_cart_orders/);
});

test("shop screens load their controllers and render user/database text as escaped text", () => {
  const pages = [
    ["pages/cua-hang.html", "assets/js/shop.js"],
    ["pages/gio-hang.html", "assets/js/cart.js"],
    ["pages/admin-shop.html", "assets/js/admin-shop.js"]
  ];
  for (const [pagePath, scriptPath] of pages) {
    assert.ok(fs.existsSync(path.join(root, scriptPath)), `thiếu ${scriptPath}`);
    const page = read(pagePath);
    assert.match(page, new RegExp(scriptPath.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")));
    assert.match(page, /defer/);
  }
  for (const scriptPath of ["assets/js/shop.js", "assets/js/cart.js", "assets/js/admin-shop.js"]) {
    const source = read(scriptPath);
    assert.match(source, /const escapeHtml\s*=/);
  }
  const shopUi = read("assets/js/shop.js");
  const adminUi = read("assets/js/admin-shop.js");
  assert.match(shopUi, /startsWith\("\/assets\/images\/"\)/);
  assert.match(adminUi, /role\s*\|\|\s*""\)\.toUpperCase\(\)\s*!==\s*"ADMIN"/);
});
