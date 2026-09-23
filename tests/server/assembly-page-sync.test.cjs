"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const root = path.join(__dirname, "..", "..");
const html = fs.readFileSync(path.join(root, "pages", "lap-rap.html"), "utf8");
const source = fs.readFileSync(path.join(root, "assets", "js", "main.js"), "utf8");

test("assembly preparation loads content and session clients before its controller", () => {
  const dataIndex = html.indexOf("../assets/js/data.js");
  const contentIndex = html.indexOf("../assets/js/content-api.js");
  const apiIndex = html.indexOf("../assets/js/api.js");
  const mainIndex = html.indexOf("../assets/js/main.js");
  assert.ok(dataIndex >= 0 && dataIndex < contentIndex);
  assert.ok(contentIndex < apiIndex && apiIndex < mainIndex);
});

test("assembly preparation restores and persists component IDs through the session API", () => {
  assert.match(source, /RobotContentApi\.loadRobots/);
  assert.match(source, /sessionApi\.createOrResume/);
  assert.match(source, /sessionApi\.setComponentPrepared/);
  assert.match(source, /data-component-id/);
  assert.match(source, /AUTH_REQUIRED/);
});

test("assembly preparation restores the exact requested session instead of creating another", () => {
  assert.match(source, /params\.get\("session"\)/);
  assert.match(source, /sessionApi\.get\(requestedSessionId\)/);
  assert.match(source, /session\.robotId !== modelSelect\.value/);
});

test("assembly reset remains available after entering 3D and resets the owned session atomically", () => {
  assert.match(source, /sessionApi\.resetProgress\(activeSession\.id\)/);
  assert.match(source, /\["PREPARING", "READY", "IN_PROGRESS"\]\.includes\(activeSession\.status\)/);
});
