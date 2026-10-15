"use strict";
const test = require("node:test"), assert = require("node:assert/strict");
const fs = require("node:fs"), vm = require("node:vm"), path = require("node:path");
const source = fs.readFileSync(path.resolve(__dirname, "../../assets/js/learning-context.js"), "utf8");
function boot(url, {loading = false, ids = ["l298n"]} = {}) {
  const listeners = {}, notices = [], cards = ids.map(id => ({
    dataset: {componentId: id}, hidden: false, focusCount: 0, scrollCount: 0,
    setAttribute() {}, querySelector() { return {textContent: "Mạch " + id}; },
    focus() { this.focusCount++; }, scrollIntoView() { this.scrollCount++; }
  }));
  const catalog = {dataset: {catalogState: loading ? "loading" : "fallback"},
    before: el => notices.push(el), querySelectorAll: () => cards};
  const document = {
    scripts: [{src: "http://localhost/assets/js/learning-context.js"}],
    querySelector: s => s === "#component-catalog" ? catalog : null,
    createElement: () => ({setAttribute() {}, textContent: ""}),
    addEventListener: (name, fn) => { listeners[name] = fn; }
  };
  const window = {addEventListener: (name, fn) => { listeners[name] = fn; }};
  vm.runInNewContext(source, {window, document, URL, URLSearchParams, location: new URL(url),
    fetch: async () => { throw Error("Optional context unavailable"); },
    MutationObserver: class { observe() {} }});
  return {cards, catalog, notice: notices[0], listeners, helper: window.LearningContext, window};
}
test("canonical, canonical hash and legacy hash locate and focus the component once", () => {
  for (const suffix of ["?component=l298n#component-l298n", "#component-l298n", "#l298n"]) {
    const page = boot("http://localhost/pages/linh-kien.html" + suffix);
    assert.equal(page.cards[0].id, "component-l298n");
    assert.equal(page.cards[0].focusCount, 1);
    page.listeners["robot:components-updated"]();
    assert.equal(page.cards[0].scrollCount, 1, "unrelated rendering must not scroll again");
  }
});
test("slow catalog waits; context failure does not prevent fallback navigation", () => {
  const page = boot("http://localhost/pages/linh-kien.html#l298n", {loading: true});
  assert.equal(page.cards[0].focusCount, 0);
  page.catalog.dataset.catalogState = "fallback";
  page.listeners["robot:components-updated"]();
  assert.equal(page.cards[0].focusCount, 1);
  assert.match(page.notice.textContent, /dữ liệu dự phòng/);
});
test("unknown, special and malformed identifiers report a missing target safely", () => {
  for (const suffix of ["?component=missing", "?component=%3Cimg%3E", "#%E0%A4%A"]) {
    const page = boot("http://localhost/pages/linh-kien.html" + suffix);
    assert.equal(page.cards[0].focusCount, 0);
    assert.match(page.notice.textContent, /Không tìm thấy|không hợp lệ/);
  }
  const special = boot("http://localhost/pages/linh-kien.html?component=a%2Fb", {ids: ["a/b"]});
  assert.equal(special.cards[0].focusCount, 1);
  assert.match(special.helper.componentUrl("a/b"), /component=a%2Fb#component-a%2Fb$/);
});
test("contextual practice actions fail closed until the server confirms a USER", () => {
  const page = boot("http://localhost/pages/linh-kien.html");
  const action = page.helper.learnerLink("Luyện", "diagnosis?robotId=a%2Fb");
  action.isConnected = true;
  assert.equal(action.hidden, true);
  for (const [status, role, hidden] of [
    ["pending", "USER", true], ["unavailable", "USER", true], ["ready", "ADMIN", true],
    ["ready", null, true], ["ready", "USER", false]
  ]) {
    page.window.SiteAuthState = {status, user: role ? {role} : null};
    page.listeners["robot:auth-ready"]();
    assert.equal(action.hidden, hidden, status + ":" + role);
  }
  assert.match(action.href, /diagnosis\?robotId=a%2Fb$/);
});
