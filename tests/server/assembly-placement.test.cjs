"use strict";
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const root = path.join(__dirname, "../..");
const java = path.join(root, "tomcat-app/src/main/java/vn/edu/webpro/robotlab");
const beans = ["AssemblySpec", "AssemblySlot", "AssemblyInstance", "AssemblyPlacement",
  "AssemblyLayout", "AssemblyCheckReport"];
function checkJava(source, name) {
  // Strip comments/string literals so arrows or SQL keywords in prose are not syntax violations.
  const code = source.replace(/\/\*[\s\S]*?\*\/|\/\/[^\n]*|"(?:\\.|[^"\\])*"/g, "");
  assert.doesNotMatch(code, /\.stream\s*\(|\.toList\s*\(|->|::|\b(?:switch|Optional|var|record)\b/);
  assert.doesNotMatch(code, /java\.time|\b(?:List|Set|Map)\.of|computeIfAbsent|getOrDefault|\.forEach\s*\(/);
  for (const [n, line] of source.split(/\r?\n/).entries()) {
    assert.ok(line.length <= 120, `${name}:${n + 1} exceeds 120`);
  }
}
test("placement JavaBeans keep traditional course syntax and readable source", () => {
  for (const name of beans) {
    const source = fs.readFileSync(path.join(java, "business", name + ".java"), "utf8");
    checkJava(source, name);
    assert.match(source, /implements Serializable/);
    assert.match(source, new RegExp(`public ${name}\\(\\)`));
  }
});
test("placement interaction sends intentions and separates object dragging from camera", () => {
  const js = fs.readFileSync(path.join(root, "assets/js/assembly-3d-placement.js"), "utf8");
  assert.match(js, /THREE\.Raycaster/);
  assert.match(js, /intersectPlane/);
  assert.match(js, /onAttach\(instanceId, selectedSlot, orientation, layout\.layoutVersion\)/);
  assert.match(js, /return true;/);
  assert.match(js, /aria-live/);
  assert.doesNotMatch(js, /innerHTML|isCorrect|progressPercent|percent\s*:/);
});
test("placement migration protects physical identities spec ownership and allowed orientations", () => {
  const sql = fs.readFileSync(path.join(root, "database/migrations/013_assembly_placement.sql"), "utf8");
  assert.doesNotMatch(sql, /^\s*(?:DROP|TRUNCATE|DELETE|UPDATE)\b/im);
  assert.match(sql, /DEFAULT 'GROUP'/);
  assert.match(sql, /UNIQUE KEY uq_instance_number \(session_id, component_id, instance_no\)/);
  assert.match(sql, /FOREIGN KEY \(session_id, spec_id\)/);
  assert.match(sql, /FOREIGN KEY \(slot_id, spec_id, component_id\)/);
  assert.match(sql, /FOREIGN KEY \(slot_id, orientation_code\)/);
  assert.match(sql, /'013_assembly_placement'/);
});
test("official assembly seed never edits a published version or preexisting data", () => {
  const seed = fs.readFileSync(path.join(root, "database/seed-assembly-spec-obstacle-avoider.sql"), "utf8");
  assert.doesNotMatch(seed, /\b(?:UPDATE|DELETE|REPLACE|TRUNCATE|DROP)\s+(?:TABLE|FROM|INTO|assembly)/i);
  assert.match(seed, /NOT EXISTS/);
  assert.match(seed, /FOR UPDATE/);
  assert.match(seed, /motor-left/);
  assert.match(seed, /wheel-left/);
});
test("placement syntax guard detects prohibited syntax and excessive line length", () => {
  assert.throws(() => checkJava("items.stream();", "copy"));
  assert.throws(() => checkJava("x".repeat(121), "copy"));
});
