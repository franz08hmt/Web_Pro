"use strict";
const test = require("node:test"), assert = require("node:assert/strict");
const fs = require("node:fs"), vm = require("node:vm"), path = require("node:path");
const root = path.resolve(__dirname, "../.."), read = file => fs.readFileSync(path.join(root, file), "utf8");
const settle = async () => { for (let i = 0; i < 8; i++) await new Promise(r => setImmediate(r)); };
class Element {
  constructor() { this.children = []; this.listeners = {}; this.innerHTML = ""; this.textContent = "";
    this.classList = {toggle() {}}; }
  append(...items) { this.children.push(...items); }
  before(item) { this.beforeElement = item; }
  setAttribute() {}
  replaceChildren() { this.children = []; this.innerHTML = ""; }
  addEventListener(name, callback) { this.listeners[name] = callback; }
}
function boot(id, get) {
  const grid = new Element(), status = new Element(), pagination = new Element(), user = new Element();
  const calls = [], listeners = {}, location = new URL("http://localhost/pages/cua-hang.html#shop-product-" + id);
  const products = Array.from({length: 21}, (_, i) => ({id: "sku" + (i + 1), name: "Product " + (i + 1),
    componentId: "l298n", stockQuantity: 2, priceVnd: 1000}));
  const api = {auth: {me: async () => { throw {status: 401}; }}, cart: {get: async () => []}, shop: {
    list: async params => { calls.push(["list", params.page]);
      return {items: products.slice((params.page - 1) * 20, params.page * 20), meta: {total: 21}}; },
    get: async sku => { calls.push(["get", sku]); return get ? get(sku, products) : products.find(p => p.id === sku); }
  }};
  const document = {querySelector: s => ({"#shop-products": grid, "#shop-status": status,
    "#shop-pagination": pagination, "#shop-user": user})[s],
    createElement: () => new Element(), getElementById: id => ({"shop-products": grid, "shop-status": status,
      "shop-pagination": pagination, "shop-user": user})[id] || null};
  const window = {RobotAssemblyApi: api, location, LearningContext: {componentUrl: id => "linh-kien.html?component=" + id},
    addEventListener: (name, callback) => { listeners[name] = callback; }};
  vm.runInNewContext(read("assets/js/shop.js"), {window, document, location, URL, Intl, console});
  return {grid, status, pagination, calls, listeners, location, products,
    focus: () => grid.beforeElement.children[1], notice: () => grid.beforeElement.children[0]};
}
test("page two SKU uses one bounded product read and keeps the paginated catalog", async () => {
  const page = boot("sku21"); await settle();
  assert.deepEqual(page.calls, [["list", 1], ["get", "sku21"]]);
  assert.match(page.focus().innerHTML, /Product 21/);
  assert.equal((page.grid.innerHTML.match(/id="shop-product-/g) || []).length, 20);
  assert.match(page.pagination.innerHTML, /Trang 1 \/ 2/);
});
test("a SKU on page one never produces duplicate product IDs", async () => {
  const page = boot("sku1"); await settle();
  assert.equal(page.focus().innerHTML, "");
  assert.equal((page.grid.innerHTML.match(/id="shop-product-sku1"/g) || []).length, 1);
});
test("missing/inactive product, API failure and out-of-stock remain distinct", async () => {
  for (const code of [404, 503]) {
    const page = boot("absent", async () => { throw {status: code}; }); await settle();
    assert.match(page.notice().textContent, code === 404 ? /không tồn tại hoặc đã ngừng bán/ : /Chưa tải được/);
    assert.match(page.grid.innerHTML, /Product 1/);
  }
  const page = boot("sku21", async (id, products) => ({...products[20], stockQuantity: 0})); await settle();
  assert.match(page.focus().innerHTML, /disabled/);
  assert.match(page.focus().innerHTML, /Tạm hết hàng/);
});
test("a late deep-link response cannot replace a more recent requested SKU", async () => {
  let release;
  const page = boot("sku21", async (id, products) => {
    if (id === "sku21") return new Promise(resolve => { release = () => resolve(products[20]); });
    return products.find(p => p.id === id);
  });
  await settle();
  page.location.hash = "#shop-product-sku20"; page.listeners.hashchange(); await settle();
  release(); await settle();
  assert.equal(page.focus().innerHTML, "");
  assert.equal(page.grid.beforeElement.hidden, true);
  assert.deepEqual(page.calls, [["list", 1], ["get", "sku21"], ["get", "sku20"]]);
});
test("the additive detail endpoint reads one active product without changing the old catalog contract", () => {
  const servlet = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/ShopProductServlet.java");
  assert.match(servlet, /@WebServlet\("\/api\/shop\/products\/\*"\)/);
  assert.match(servlet, /ShopProductDB.selectProduct\(id\)/);
  assert.match(servlet, /product == null \|\| !product.isActive\(\)/);
  assert.doesNotMatch(servlet, /doPost|doPut|doDelete|setQuantity|checkout/);
  assert.match(servlet, /ResponseUtil.sendDatabaseUnavailable/);
  const data = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ShopProductDB.java");
  assert.match(data, /WHERE p.id = \?"\);\s*ps.setString\(1, id\)/);
  for (const line of servlet.split(/\r?\n/)) assert.ok(line.length <= 120);
});
