"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");
const root = path.join(__dirname, "..", "..");
const source = fs.readFileSync(path.join(root, "assets/js/library-api.js"), "utf8");
const page = fs.readFileSync(path.join(root, "pages/thu-vien.html"), "utf8");

class Element {
  constructor(tag) {
    this.tagName = tag;
    this.children = [];
    this.attributes = {};
    this.listeners = {};
    this.disabled = false;
    this.hidden = false;
    this.value = "";
    this.textContent = "";
  }
  append(...children) { this.children.push(...children); }
  replaceChildren(...children) { this.children = children; }
  setAttribute(name, value) { this.attributes[name] = String(value); }
  getAttribute(name) { return this.attributes[name] ?? null; }
  addEventListener(name, callback) { (this.listeners[name] ||= []).push(callback); }
  // Deliberately dispatch even on disabled elements to cover already queued events.
  dispatch(name) { for (const callback of this.listeners[name] || []) callback({ target: this }); }
}

function deferred() {
  let resolve, reject;
  const promise = new Promise((yes, no) => { resolve = yes; reject = no; });
  return { promise, resolve, reject };
}
const settle = () => new Promise(resolve => setImmediate(resolve));
function payload(title, { page = 1, limit = 6, total = 18 } = {}) {
  return { data: title ? [{ type: "link", title, description: "Guide", url: "/pages/lap-rap.html" }] : [], meta: { page, limit, total } };
}

function boot({ enabled = true, robots = Promise.resolve([{ id: "mini-arm", name: "Cánh tay robot mini" }]) } = {}) {
  const elements = {};
  for (const id of ["catalog", "data-status", "robot-filter", "page-size", "pagination", "prev", "next", "page-summary"]) {
    elements[id] = new Element(id === "robot-filter" || id === "page-size" ? "select" : "div");
  }
  for (const id of ["robot-filter", "page-size", "prev", "next"]) elements[id].disabled = true;
  elements.pagination.hidden = true;
  elements["page-size"].value = "6";
  const staticMarkup = page.match(/id="library-catalog"[\s\S]*?(?=<nav id="library-pagination")/)[0];
  for (const markup of staticMarkup.match(/<article\b[\s\S]*?<\/article>/g)) {
    const card = new Element("article");
    card.textContent = markup;
    elements.catalog.append(card);
  }
  const staticCards = [...elements.catalog.children];
  const requests = [];
  const load = options => {
    const pending = deferred();
    requests.push({ options: { ...options }, ...pending });
    return pending.promise;
  };
  const api = { enabled, loadLibraryResourcesPage: load, loadRobotOptions: () => robots };
  vm.runInNewContext(source, {
    window: { RobotContentApi: api },
    document: { querySelector: selector => elements[selector.replace("#library-", "")], createElement: tag => new Element(tag) },
    console
  });
  return { elements, requests, staticCards };
}
function assertIdle(elements) {
  assert.equal(elements.catalog.getAttribute("aria-busy"), "false");
  assert.equal(elements["page-size"].disabled, false);
}

test("initial failure retains actual static cards and clears loading states", async () => {
  const { elements, requests, staticCards } = boot();
  assert.equal(staticCards.length, 6);
  assert.match(staticCards[0].textContent, /robot-do-line\.png/);
  assert.deepEqual(elements.catalog.children, staticCards);
  assert.equal(elements.catalog.getAttribute("aria-busy"), "true");
  for (const id of ["robot-filter", "page-size", "prev", "next"]) assert.equal(elements[id].disabled, true);
  requests[0].reject(new Error("offline"));
  await settle();
  assert.deepEqual(elements.catalog.children, staticCards);
  assert.match(elements["data-status"].textContent, /offline.*nội dung tĩnh/);
  assertIdle(elements);
  assert.equal(elements.pagination.hidden, true);
  assert.equal(elements.prev.disabled, true);
  assert.equal(elements.next.disabled, true);
});

test("later failure preserves prior results with honest status and restores their filter and paging state", async () => {
  const { elements, requests } = boot();
  requests[0].resolve(payload("All robots"));
  await settle();
  elements["robot-filter"].value = "mini-arm";
  elements["robot-filter"].dispatch("change");
  requests[1].resolve(payload("Arm page 1"));
  await settle();
  elements.next.dispatch("click");
  requests[2].resolve(payload("Arm page 2", { page: 2 }));
  await settle();
  const previousCards = [...elements.catalog.children];
  const summary = elements["page-summary"].textContent;
  elements["page-size"].value = "12";
  elements["page-size"].dispatch("change");
  requests[3].reject(new Error("offline"));
  await settle();
  assert.deepEqual(elements.catalog.children, previousCards);
  assert.match(elements["data-status"].textContent, /kết quả trước/);
  assert.doesNotMatch(elements["data-status"].textContent, /dữ liệu dự phòng/);
  assert.equal(elements["page-size"].value, "6");
  assert.equal(elements["robot-filter"].value, "mini-arm");
  assert.equal(elements["page-summary"].textContent, summary);
  assert.equal(elements.pagination.hidden, false);
  assertIdle(elements);
  elements.next.dispatch("click");
  assert.equal(requests[4].options.page, 3);
  assert.equal(requests[4].options.limit, 6);
  requests[4].resolve(payload("Arm page 3", { page: 3 }));
  await settle();
  assert.equal(elements.next.disabled, true);
});

test("rapid and queued page clicks cannot issue overlapping requests or replace results out of order", async () => {
  const { elements, requests } = boot();
  requests[0].resolve(payload("Page 1"));
  await settle();
  assert.equal(elements.prev.disabled, true);
  assert.equal(elements.next.disabled, false);
  elements.prev.dispatch("click");
  assert.equal(requests.length, 1);
  elements.next.dispatch("click");
  for (const id of ["robot-filter", "page-size", "prev", "next"]) assert.equal(elements[id].disabled, true);
  assert.equal(elements.catalog.getAttribute("aria-busy"), "true");
  elements.next.dispatch("click");
  elements.prev.dispatch("click");
  elements["robot-filter"].dispatch("change");
  elements["page-size"].dispatch("change");
  assert.equal(requests.length, 2, "one in-flight request prevents out-of-order rendering");
  assert.equal(requests[1].options.page, 2);
  requests[1].resolve(payload("Page 2", { page: 2 }));
  await settle();
  assert.equal(elements.catalog.children[0].children[1].textContent, "Page 2");
  assert.match(elements["page-summary"].textContent, /Trang 2\/3/);
  assertIdle(elements);
  assert.equal(elements.prev.disabled, false);
  assert.equal(elements.next.disabled, false);
});

test("empty, filter, and page-size results reset pages and render consistent boundaries", async () => {
  const { elements, requests } = boot();
  requests[0].resolve(payload("Page 1"));
  await settle();
  elements.next.dispatch("click");
  requests[1].resolve(payload("Page 2", { page: 2 }));
  await settle();
  elements["robot-filter"].value = "mini-arm";
  elements["robot-filter"].dispatch("change");
  assert.equal(requests[2].options.page, 1);
  assert.equal(requests[2].options.robotId, "mini-arm");
  requests[2].resolve(payload(null, { total: 0 }));
  await settle();
  assert.equal(elements.catalog.children.length, 1);
  assert.match(elements.catalog.children[0].textContent, /Không có tài liệu phù hợp/);
  assert.equal(elements.pagination.hidden, true);
  assert.equal(elements.prev.disabled, true);
  assert.equal(elements.next.disabled, true);
  assert.match(elements["page-summary"].textContent, /Không có tài nguyên/);
  elements["page-size"].value = "12";
  elements["page-size"].dispatch("change");
  assert.equal(requests[3].options.page, 1);
  assert.equal(requests[3].options.limit, 12);
  requests[3].resolve(payload("Only page", { limit: 12, total: 10 }));
  await settle();
  assert.equal(elements.pagination.hidden, true);
  assert.equal(elements.next.disabled, true);
  assert.match(elements["page-summary"].textContent, /Trang 1\/1/);
  assertIdle(elements);
});

for (const optionsFailFirst of [true, false]) {
  test(`robot-option failure remains visible and disables only unavailable filtering (${optionsFailFirst ? "before" : "after"} resources)`, async () => {
    const robots = deferred();
    const { elements, requests } = boot({ robots: robots.promise });
    if (optionsFailFirst) { robots.reject(new Error("robots offline")); await settle(); }
    requests[0].resolve(payload("Resources"));
    await settle();
    if (!optionsFailFirst) { robots.reject(new Error("robots offline")); await settle(); }
    assert.match(elements["data-status"].textContent, /robots offline/);
    assert.equal(elements["robot-filter"].disabled, true);
    assert.equal(elements["page-size"].disabled, false);
    assert.equal(elements.next.disabled, false);
    elements.next.dispatch("click");
    requests[1].resolve(payload("More resources", { page: 2 }));
    await settle();
    assert.match(elements["data-status"].textContent, /robots offline/);
  });
}

test("filter stays disabled while options are still loading, then becomes available", async () => {
  const robots = deferred();
  const { elements, requests } = boot({ robots: robots.promise });
  requests[0].resolve(payload("Resources"));
  await settle();
  assert.equal(elements["robot-filter"].disabled, true);
  assert.equal(elements["page-size"].disabled, false);
  robots.resolve([{ id: "mini-arm", name: "Cánh tay robot mini" }]);
  await settle();
  assert.equal(elements["robot-filter"].disabled, false);
  assert.equal(elements["robot-filter"].children[0].value, "mini-arm");
});

test("static preview keeps static cards and all API-only controls disabled", async () => {
  const { elements, requests, staticCards } = boot({ enabled: false });
  await settle();
  assert.equal(requests.length, 0);
  assert.deepEqual(elements.catalog.children, staticCards);
  assert.match(elements["data-status"].textContent, /Chế độ dữ liệu dự phòng/);
  for (const id of ["robot-filter", "page-size", "prev", "next"]) assert.equal(elements[id].disabled, true);
  assert.equal(elements.catalog.getAttribute("aria-busy"), "false");
});

test("failed filter change restores the selection matching the preserved page", async () => {
  const { elements, requests } = boot();
  requests[0].resolve(payload("All models"));
  await settle();
  elements.next.dispatch("click");
  requests[1].resolve(payload("All models page 2", { page: 2 }));
  await settle();
  const cards = [...elements.catalog.children];
  elements["robot-filter"].value = "mini-arm";
  elements["robot-filter"].dispatch("change");
  requests[2].reject(new Error("filter offline"));
  await settle();
  assert.equal(elements["robot-filter"].value, "");
  assert.deepEqual(elements.catalog.children, cards);
  assert.match(elements["page-summary"].textContent, /Trang 2\/3/);
  assert.match(elements["data-status"].textContent, /filter offline.*kết quả trước/);
  assertIdle(elements);
});

for (const retryFails of [false, true]) {
  test(`shrinking catalog keeps the loading lock through the corrected page request (${retryFails ? "failure" : "success"})`, async () => {
    const { elements, requests } = boot();
    requests[0].resolve(payload("Page 1"));
    await settle();
    elements.next.dispatch("click");
    requests[1].resolve(payload("Page 2", { page: 2 }));
    await settle();
    const cards = [...elements.catalog.children];
    elements.next.dispatch("click");
    requests[2].resolve(payload(null, { page: 3, total: 1 }));
    await settle();
    assert.equal(requests[3].options.page, 1);
    assert.deepEqual(elements.catalog.children, cards);
    assert.equal(elements.catalog.getAttribute("aria-busy"), "true");
    for (const id of ["robot-filter", "page-size", "prev", "next"]) assert.equal(elements[id].disabled, true);
    elements.next.dispatch("click");
    assert.equal(requests.length, 4);
    if (retryFails) requests[3].reject(new Error("retry offline"));
    else requests[3].resolve(payload("Remaining resource", { total: 1 }));
    await settle();
    assertIdle(elements);
    if (retryFails) {
      assert.deepEqual(elements.catalog.children, cards);
      assert.match(elements["page-summary"].textContent, /Trang 2\/3/);
      assert.match(elements["data-status"].textContent, /retry offline.*kết quả trước/);
    } else {
      assert.equal(elements.catalog.children[0].children[1].textContent, "Remaining resource");
      assert.match(elements["page-summary"].textContent, /Trang 1\/1/);
      assert.equal(elements.pagination.hidden, true);
      assert.equal(elements.prev.disabled, true);
      assert.equal(elements.next.disabled, true);
    }
  });
}

test("malformed initial data keeps static cards and can recover on a later request", async () => {
  const { elements, requests, staticCards } = boot();
  requests[0].resolve({ data: [], meta: { total: "unknown" } });
  await settle();
  assert.deepEqual(elements.catalog.children, staticCards);
  assert.match(elements["data-status"].textContent, /không hợp lệ.*nội dung tĩnh/);
  assertIdle(elements);
  elements["page-size"].value = "12";
  elements["page-size"].dispatch("change");
  requests[1].resolve(payload("Recovered", { limit: 12, total: 1 }));
  await settle();
  assert.equal(elements.catalog.children[0].children[1].textContent, "Recovered");
  assert.equal(elements["page-size"].value, "12");
  assert.doesNotMatch(elements["data-status"].textContent, /API lỗi/);
});
