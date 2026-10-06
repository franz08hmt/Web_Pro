"use strict";
const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const root = path.resolve(__dirname, "../..");
const folder = "tomcat-app/src/main/webapp/WEB-INF/views/";
const read = file => fs.readFileSync(path.join(root, file), "utf8");
const contracts = JSON.parse(read("tests/fixtures/ui-view-contracts.json"));

function controlContract(source) {
  const clean = source.replace(/<c:out\s+value="([^"]*)"\s*\/>/g, "$1")
    .replace(/<\/?c:\w+\b[^>]*>/g, "");
  return [...clean.matchAll(/<(form|input|textarea|button)\b(?:[^>"']|"[^"]*"|'[^']*')*>/g)]
    .map(tag => {
      const attributes = {};
      for (const a of tag[0].matchAll(/\b(method|action|name|type|value|required|minlength|maxlength|min|max|checked|disabled|id)(?:="([^"]*)")?(?=\s|>|$)/g)) {
        attributes[a[1]] = a[2] === undefined ? true : a[2].replace(/\s+/g, " ");
      }
      return {tag: tag[1], attributes};
    });
}

test("redesign preserves every existing form contract and business binding", () => {
  for (const [name, contract] of Object.entries(contracts)) {
    const source = read(folder + name);
    assert.deepEqual(controlContract(source), contract.controls, name + ": form fields/action/validation changed");
    const expressions = [...source.matchAll(/\$\{([^}]+)\}/g)].map(m => m[1].replace(/\s+/g, " "));
    for (const binding of contract.bindings) {
      assert.ok(expressions.includes(binding.replace(/\s+/g, " ")), name + ": missing " + binding);
    }
  }
});

test("all independent learning views use the scoped shell with private indexing rules", () => {
  const pages = fs.readdirSync(path.join(root, folder)).filter(n => n.endsWith(".jsp") && n !== "wiring-diagram.jsp");
  assert.equal(pages.length, 34);
  for (const name of pages) {
    const s = read(folder + name);
    assert.match(s, /learning-workspace\.css\?v=/, name);
    assert.match(s, /class="learning-workspace/, name);
    assert.match(s, /workspace-header\.jspf/, name);
    assert.match(s, /workspace-footer\.jspf/, name);
    assert.match(s, /id="workspace-main"/, name);
    assert.equal((s.match(/<main\b/g) || []).length, 1, name);
    if (!["robots.jsp", "components.jsp", "architecture.jsp"].includes(name)) {
      assert.match(s, /name="robots" content="noindex, nofollow"/, name);
    }
    assert.doesNotMatch(s, /assets\/css\/style\.css|canonical|application\/ld\+json/, name);
  }
});

test("shell fragments stay semantic and wiring targets exclude icons and snapshots", () => {
  for (const name of ["workspace-header.jspf", "workspace-footer.jspf"]) {
    const source = read(folder + name);
    assert.doesNotMatch(source, /<\/?(?:html|body|main)\b|<script|<select|<option|<fmt:|<fn:|<%(?!@)/i);
    source.split(/\r?\n/).forEach(line => assert.ok(line.length <= 140, name));
  }
  assert.match(read("assets/js/wiring.js"), /#wiring-interactive-diagram \.wiring-diagram > svg/);
  assert.doesNotMatch(read("assets/css/wiring.css"), /\.wiring\s+svg\s*\{/);
  assert.match(read(folder + "wiring-play.jsp"), /id="wiring-interactive-diagram"/);
  assert.doesNotMatch(read(folder + "wiring-diagram.jsp"), /role="button"|tabindex="0"[^>]*data-terminal-id/);
  assert.match(read("assets/js/wiring.js"), /node\.setAttribute\("role", "button"\)/);
  assert.doesNotMatch(read(folder + "wiring-support-view.jsp"), /wiring-interactive-diagram|wiring\.js/);
  const css = read("assets/css/learning-workspace.css");
  for (const weight of [400, 500, 600, 700]) assert.ok(css.includes(`poppins-${weight}.ttf`));
  assert.match(css, /prefers-reduced-motion/);
  assert.match(css, /\.learning-workspace \[hidden\]/);
  assert.match(css, /\.learning-workspace \.no-print/);
});

module.exports = {controlContract};

test("UI-2 navigation and status labels keep native accessible presentation", () => {
  const css = read("assets/css/learning-workspace.css");
  assert.match(css, /workspace-more > summary\s*\{[^}]*font-weight:\s*500/);
  assert.match(css, /workspace-more > summary\s*\{[^}]*min-height:\s*44px/);
  assert.match(css, /\.status-badge--success/);
  assert.match(css, /\.status-badge--warning/);
  assert.match(css, /\.status-badge--danger/);
  for (const name of ["task-view.jsp", "wiring-result.jsp", "wiring-support-view.jsp"]) {
    assert.match(read(folder + name), /status-badge/, name);
  }
  assert.doesNotMatch(read(folder + "account.jsp"), /<c:out\s+value="\$\{user\.role\}"/);
  assert.doesNotMatch(read(folder + "admin-wiring-view.jsp"), /<c:out\s+value="\$\{wiringRule\.kind\}"/);
});

test("UI-2 explains wiring counts plainly and keeps the formula in native details", () => {
  const result = read(folder + "wiring-result.jsp");
  for (const label of ["Dây đúng:", "Dây sai/thừa:", "Còn thiếu:", "Tổng dây bắt buộc:"]) {
    assert.ok(result.includes(label), label);
  }
  assert.match(result, /<details[^>]*>[\s\S]*?<summary>Cách tính điểm<\/summary>[\s\S]*?max\(C − W, 0\)[\s\S]*?<\/details>/);
  assert.doesNotMatch(read(folder + "task-view.jsp"), /HALF_UP/);
  assert.match(read(folder + "wiring-diagram.jsp"), /class="terminal-code"/);
  assert.match(read(folder + "wiring-diagram.jsp"), /wiringTerminal\.code/);
});
