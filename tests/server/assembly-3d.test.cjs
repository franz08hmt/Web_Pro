"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const projectRoot = path.resolve(__dirname, "..", "..");
const pageSource = fs.readFileSync(path.join(projectRoot, "pages", "lap-rap-3d.html"), "utf8");
const assemblySource = fs.readFileSync(path.join(projectRoot, "assets", "js", "assembly-3d.js"), "utf8");

test("3D assembly uses the local Three.js bundle and gives a readable fallback when unavailable", () => {
  assert.match(pageSource, /src="\/vendor\/three\/three\.min\.js"/);
  assert.doesNotMatch(pageSource, /cdn\.jsdelivr\.net/);
  assert.match(assemblySource, /if \(!window\.THREE\)/);
  assert.match(assemblySource, /Không thể tải trình dựng 3D/);
});
