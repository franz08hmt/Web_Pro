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
test("placement routes stay in Servlet with session CSRF and owner-scoped locked persistence", () => {
  const servlet = fs.readFileSync(path.join(java, "controller/AssemblySessionServlet.java"), "utf8");
  const db = fs.readFileSync(path.join(java, "data/AssemblyPlacementDB.java"), "utf8");
  for (const name of ["placements", "layout", "check", "expectedLayoutVersion"]) assert.ok(servlet.includes(name));
  assert.match(servlet, /requireUser\(request, response\)/);
  assert.match(servlet, /hasValidCsrfToken\(request, response\)/);
  assert.doesNotMatch(servlet, /getParameter\("userId"\)/);
  assert.match(db, /WHERE id = \? AND user_id = \?/);
  assert.match(db, /FOR UPDATE/);
  assert.match(db, /layout\.validateAttach/);
  assert.match(db, /layout\.validateDetach/);
  assert.match(db, /layout\.validateChange\(action, expectedVersion\)/);
  const layout = fs.readFileSync(path.join(java, "business/AssemblyLayout.java"), "utf8");
  assert.match(layout, /check\(\)\.isComplete\(\)/);
  assert.match(db, /connection\.commit\(\)/);
  assert.match(db, /connection\.rollback\(\)/);
  for (const file of ["data/AssemblyPlacementDB.java", "controller/AssemblySessionServlet.java",
    "data/AssemblySessionDB.java", "business/AssemblySession.java", "util/AssemblyPlacementJson.java",
    "controller/AssemblyReceiptPageServlet.java"]) {
    checkJava(fs.readFileSync(path.join(java, file), "utf8"), file);
  }
});

test("rejection copy uses the checked transaction layout and escapes response and browser text", () => {
  const servlet = fs.readFileSync(path.join(java, "controller/AssemblySessionServlet.java"), "utf8");
  const db = fs.readFileSync(path.join(java, "data/AssemblyPlacementDB.java"), "utf8");
  assert.match(db, /AssemblyPlacementException\(reason, layout\)/);
  assert.match(servlet, /getLayout\(\)/);
  assert.match(servlet, /layout\.messageFor\(code, instanceId, slotId\)/);
  assert.match(servlet, /JsonUtil\.quote\(message\)/);
  checkJava(fs.readFileSync(path.join(java, "util/AssemblyPlacementException.java"), "utf8"), "placement error");
});

test("locked layouts normalize parse failures without masking ownership or editable validation", () => {
  const source = fs.readFileSync(path.join(java, "controller/AssemblySessionServlet.java"), "utf8");
  assert.match(source, /current != null && !\(e instanceof AssemblyPlacementException\)/);
  assert.match(source, /current\.isCanReset\(\)/);
  assert.match(source, /current\.isCanEdit\(\)/);
  assert.match(source, /error\(response, "SESSION_LOCKED"/);
  assert.match(source, /direction\.matches\("Y0\|Y90\|Y180\|Y270"\)/);
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
test("detailed receipt uses escaped bean facts and core JSTL without unsupported view syntax", () => {
  const view = fs.readFileSync(path.join(root,
    "tomcat-app/src/main/webapp/WEB-INF/views/assembly-receipt.jsp"), "utf8");
  assert.doesNotMatch(view, /<%(?!@)|<(?:fmt:|fn:|select\b|option\b)|fn:/i);
  assert.match(view, /session\.detailed/);
  assert.match(view, /c:out value="\$\{assemblyPlaced\.instanceLabel\}"/);
  assert.match(view, /c:out value="\$\{assemblyPlaced\.slotLabel\}"/);
  assert.match(view, /assemblyLayout\.spec\.versionNo/);
  for (const line of view.split(/\r?\n/)) assert.ok(line.length <= 140);
});
test("preparation reset keeps GROUP behavior and sends version for DETAILED", () => {
  const main = fs.readFileSync(path.join(root, "assets/js/main.js"), "utf8");
  assert.match(main, /activeSession\?\.assemblyMode === "DETAILED"/);
  assert.match(main, /sessionApi\.resetLayout\(activeSession\.id, activeSession\.layoutVersion\)/);
  assert.match(main, /sessionApi\.resetProgress\(activeSession\.id\)/);
  assert.match(main, /window\.confirm/);
});

test("spec v2 is additive idempotent and requires real discrete rotation", () => {
  const seed = fs.readFileSync(path.join(root,
    "database/seed-assembly-spec-obstacle-avoider-v2.sql"), "utf8");
  assert.match(seed, /START TRANSACTION/);
  assert.match(seed, /FOR UPDATE/);
  assert.match(seed, /version_no = 2/);
  assert.match(seed, /@create_assembly_spec_v2/);
  assert.match(seed, /NOT EXISTS/);
  assert.doesNotMatch(seed, /^\s*(?:UPDATE|DELETE|REPLACE|DROP|TRUNCATE)\b/im);
  assert.match(seed, /'motor-left'[\s\S]*'Y90'/);
  assert.match(seed, /'motor-right'[\s\S]*'Y270'/);
  assert.match(seed, /'sensor'[\s\S]*'Y180'/);
  assert.match(seed, /COMMIT/);
  const v1 = fs.readFileSync(path.join(root, "database/seed-assembly-spec-obstacle-avoider.sql"), "utf8");
  const angles = { chassis: 0, "motor-left": 90, "motor-right": 270, "wheel-left": 0,
    "wheel-right": 0, uno: 0, driver: 0, battery: 90, caster: 0, sensor: 180 };
  for (const [code, degrees] of Object.entries(angles)) {
    const row = new RegExp(`'${code}',[^;]*?TRUE,\\s*([-\\d.,\\s]+?)\\s*(?:WHERE|FROM)`);
    const oldPose = v1.match(row)[1].split(",").map(Number);
    const newPose = seed.match(row)[1].split(",").map(Number);
    for (const index of [0, 1, 2, 3, 5, 6]) assert.equal(newPose[index], oldPose[index], code);
    assert.ok(Math.abs(newPose[4] + degrees * Math.PI / 180 - oldPose[4]) < 1e-9, code);
  }
});
