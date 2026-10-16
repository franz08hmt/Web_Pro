"use strict";
const test = require("node:test"), assert = require("node:assert/strict");
const fs = require("node:fs"), vm = require("node:vm"), path = require("node:path");
const source = fs.readFileSync(path.join(__dirname, "../../assets/js/quiz.js"), "utf8");
class Element {
  constructor(tag = "div") {
    this.tagName = tag; this.children = []; this.hidden = false; this.listeners = {}; this.dataset = {};
  }
  append(...items) { this.children.push(...items); }
  replaceChildren(...items) { this.children = items; }
  addEventListener(name, fn) { this.listeners[name] = fn; }
  setAttribute(name, value) { this[name] = value; }
}
const settle = async () => { for (let i = 0; i < 8; i++) await new Promise(resolve => setImmediate(resolve)); };
function boot({ model = "", robots = [{ id: "sample", name: "Mẫu thử" }], questionCount = 1, error, authError } = {}) {
  const elements = {}, calls = [];
  const document = { title: "Quiz", createElement: tag => new Element(tag),
    querySelector: selector => elements[selector] ||= new Element() };
  const api = { auth: { me: async () => { if (authError) throw authError; return { role: "USER" }; } }, quiz: {
    questions: async id => { calls.push(["questions", id]); if (error) throw error;
      return { questions: Array.from({ length: questionCount }, (_, i) => ({ id: "q" + i, prompt: "Nội dung", options: [] })) }; },
    history: async id => { calls.push(["history", id]); return { items: [], meta: { total: 0 } }; },
    submit: () => { throw Error("Opening a quiz must never submit"); }
  } };
  const window = { location: { search: model ? "?model=" + encodeURIComponent(model) : "" }, RobotAssemblyApi: api,
    RobotContentApi: { loadRobotOptions: async () => robots, loadRobots: async () => robots } };
  vm.runInNewContext(source, { window, document, URLSearchParams, console });
  return { elements, calls, document };
}
function text(element) { return [element?.textContent || "", ...(element?.children || []).map(text)].join(" "); }
test("quiz without model offers only confirmed questions and never requests empty-model history", async () => {
  const b = boot(); await settle();
  assert.match(b.elements["#quiz-robot-name"].textContent, /Chọn bài/);
  assert.ok(text(b.elements["#quiz-selection"]).includes("Mẫu thử"));
  assert.deepEqual(b.calls, [["questions", "sample"]]);
  const links = b.elements["#quiz-selection"].children.flatMap(e => e.children).filter(e => e.tagName === "a");
  assert.ok(links.some(e => e.href === "kiem-tra.html?model=sample"));
});
test("a model with no questions has no start link", async () => {
  const b = boot({ questionCount: 0 }); await settle();
  assert.match(text(b.elements["#quiz-selection"]), /chưa có bài kiểm tra/i);
  assert.ok(!b.calls.some(([kind]) => kind === "history"));
});
test("empty catalog ends loading with an explicit empty state", async () => {
  const b = boot({ robots: [] }); await settle();
  assert.match(b.elements["#quiz-status"].textContent, /Chưa có mẫu/);
});
test("quiz API errors never become a confirmed absence of a quiz", async () => {
  const b = boot({ error: { status: 503 } }); await settle();
  assert.match(text(b.elements["#quiz-selection"]), /Chưa xác nhận/);
  assert.equal(b.elements["#quiz-load-retry"].hidden, false);
});
test("guest invitation differs from unavailable authentication and neither grants access", async () => {
  for (const status of [401, 503]) {
    const b = boot({ authError: { status } }); await settle();
    assert.equal(b.calls.length, 0);
    assert.match(b.elements["#quiz-status"].textContent, status === 401 ? /Đăng nhập/ : /Chưa xác minh/);
  }
});
test("valid model keeps questions and owner history while opening sends no submission", async () => {
  const b = boot({ model: "sample" }); await settle();
  assert.deepEqual(b.calls, [["questions", "sample"], ["history", "sample"]]);
  assert.equal(b.elements["#quiz-form"].hidden, false);
});
test("invalid model and empty question set finish with a way to choose again, without history", async () => {
  for (const config of [{ error: { status: 422 } }, { questionCount: 0 }]) {
    const b = boot({ model: "unknown", ...config }); await settle();
    assert.equal(b.elements["#quiz-form"].hidden, true);
    assert.ok(!b.calls.some(([kind]) => kind === "history"));
    assert.doesNotMatch(b.elements["#quiz-status"].textContent, /Đang tải/);
  }
});
