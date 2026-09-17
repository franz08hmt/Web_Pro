"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const projectRoot = path.resolve(__dirname, "..", "..");
const pageSource = fs.readFileSync(path.join(projectRoot, "pages", "lap-rap-3d.html"), "utf8");
const assemblySource = fs.readFileSync(path.join(projectRoot, "assets", "js", "assembly-3d.js"), "utf8");
const styleSource = fs.readFileSync(path.join(projectRoot, "assets", "css", "style.css"), "utf8");

test("3D assembly uses the local Three.js bundle and gives a readable fallback when unavailable", () => {
  assert.match(pageSource, /src="\/vendor\/three\/three\.min\.js"/);
  assert.doesNotMatch(pageSource, /cdn\.jsdelivr\.net/);
  assert.match(assemblySource, /if \(!window\.THREE\)/);
  assert.match(assemblySource, /Không thể tải trình dựng 3D/);
});

test("3D assembly panel keeps readable dark text on its white surface", () => {
  assert.match(styleSource, /\.assembly-3d-panel\s*\{[^}]*color:\s*#1[0-9a-f]{5};/s);
});

test("3D assembly refocuses the camera after parts change", () => {
  assert.match(
    assemblySource,
    /checkbox\.addEventListener\([\s\S]*?focusRobot\(\);\s*updateProgress\(\);/
  );
});
