"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const projectRoot = path.resolve(__dirname, "../..");

test("project tooling is pinned to UTF-8", () => {
  const editorConfig = fs.readFileSync(path.join(projectRoot, ".editorconfig"), "utf8");

  assert.match(editorConfig, /^charset\s*=\s*utf-8$/im);
});

test("request encoding filter does not rewrite static frontend resources", () => {
  const filterSource = fs.readFileSync(
    path.join(
      projectRoot,
      "tomcat-app",
      "src",
      "main",
      "java",
      "vn",
      "edu",
      "webpro",
      "robotlab",
      "filter",
      "RequestContextFilter.java"
    ),
    "utf8"
  );

  assert.doesNotMatch(filterSource, /@WebFilter\("\/\*"\)/);
  assert.match(filterSource, /"\/api\/\*"/);
});

test("Tomcat exploded frontend preserves the source bytes", (context) => {
  const sourcePath = path.join(projectRoot, "index.html");
  const deployedPath = path.join(
    projectRoot,
    "tomcat-app",
    "target",
    "robot-assembly-lab",
    "index.html"
  );

  if (!fs.existsSync(deployedPath)) {
    context.skip("exploded WAR has not been built");
    return;
  }

  const source = fs.readFileSync(sourcePath);
  const deployed = fs.readFileSync(deployedPath);

  assert.deepEqual(deployed, source);
  assert.match(deployed.toString("utf8"), /Khám phá/);
  assert.doesNotMatch(deployed.toString("utf8"), /KhÃ/);
});
