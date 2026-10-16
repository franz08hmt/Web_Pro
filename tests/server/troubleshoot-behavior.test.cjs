"use strict";
const test = require("node:test"), assert = require("node:assert/strict");
const fs = require("node:fs"), vm = require("node:vm"), path = require("node:path");
const source = fs.readFileSync(path.join(__dirname, "../../assets/js/troubleshooting.js"), "utf8");
class Element {
  constructor(tag) { this.tagName = tag; this.children = []; this.value = ""; this.listeners = {}; }
  append(...items) { this.children.push(...items); }
  replaceChildren(...items) { this.children = items; }
  addEventListener(name, fn) { this.listeners[name] = fn; }
}
test("lookup folds full authored content in native details and retains robot context without parsing steps", async () => {
  const elements = Object.fromEntries(["#troubleshoot-status", "#troubleshoot-list", "#filter-robot", "#filter-group", "#filter-search"]
    .map(id => [id, new Element("div")]));
  elements["#filter-group"].append(new Element("option"));
  const guide = { symptom: "<b>Không chạy</b>", possibleCauses: "A; B. C\nD", resolutionSteps: "Một; hai. Ba\nbốn",
    componentGroup: "Nguồn", robotId: "model one", relatedComponentId: "battery" };
  const calls = [];
  const document = { querySelector: id => elements[id], createElement: tag => new Element(tag) };
  const window = { location: { search: "?robotId=model%20one" }, ROBOT_MODELS: [{ id: "model one", name: "Mẫu thử" }],
    RobotAssemblyApi: { troubleshooting: { search: async query => { calls.push(query); return { items: [guide], meta: { total: 1 } }; } } },
    LearningContext: { componentLink: () => new Element("a"), learnerLink: (label, href) => Object.assign(new Element("a"), { href }) } };
  vm.runInNewContext(source, { window, document, URLSearchParams });
  await new Promise(resolve => setImmediate(resolve));
  const card = elements["#troubleshoot-list"].children[0];
  assert.equal(card.tagName, "details");
  assert.equal(card.children[0].tagName, "summary");
  assert.equal(card.children[0].children[0].textContent, guide.symptom);
  assert.ok(card.children.some(e => e.textContent === guide.possibleCauses));
  assert.ok(card.children.some(e => e.textContent === guide.resolutionSteps));
  assert.ok(card.children.some(e => e.href === "diagnosis?robotId=model%20one"));
  assert.equal(calls[0].robotId, "model one");
  assert.ok(!card.children.some(e => e.tagName === "ol"));
});
