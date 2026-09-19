"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const root = path.join(__dirname, "..", "..");

test("account page loads the shared API client before its controller", () => {
  const html = fs.readFileSync(path.join(root, "pages", "tai-khoan.html"), "utf8");
  assert.match(html, /name="fullName"/);
  assert.match(html, /id="account-session"/);
  assert.ok(html.indexOf("../assets/js/api.js") < html.indexOf("../assets/js/account.js"));
});

test("account controller uses real auth endpoints instead of preview-only messages", () => {
  const source = fs.readFileSync(path.join(root, "assets", "js", "account.js"), "utf8");
  assert.match(source, /RobotAssemblyApi/);
  assert.match(source, /auth\.login/);
  assert.match(source, /auth\.register/);
  assert.match(source, /auth\.logout/);
  assert.doesNotMatch(source, /giai đoạn phát triển tiếp theo/);
});

test("account page lists saved sessions with preparation and step progress", () => {
  const html = fs.readFileSync(path.join(root, "pages", "tai-khoan.html"), "utf8");
  const source = fs.readFileSync(path.join(root, "assets", "js", "account.js"), "utf8");
  assert.match(html, /id="account-history"/);
  assert.match(source, /assemblySessions\.list/);
  assert.match(source, /completedStepCount/);
  assert.match(source, /totalStepCount/);
  assert.match(source, /session\.id/);
});
