"use strict";
const fs = require("node:fs"), path = require("node:path"), vm = require("node:vm");
const assert = require("node:assert/strict"), test = require("node:test");
const root = path.resolve(__dirname, "../..");
const read = file => fs.readFileSync(path.join(root, file), "utf8");
const sandbox = { window: {} };
vm.runInNewContext(read("assets/js/data.js"), sandbox);
vm.runInNewContext(read("assets/js/assembly-3d-config.js"), sandbox);
const models = ["line-follower", "mini-arm", "line-obstacle", "servo-scout"];

test("all remaining models have additive per-instance specs matching the physical kit and existing poses", () => {
  const sql = read("database/seed-assembly-spec-all-models.sql");
  assert.doesNotMatch(sql, /^\s*(?:UPDATE|DELETE|DROP|ALTER|TRUNCATE|REPLACE)\b/im);
  assert.match(sql, /START TRANSACTION/); assert.match(sql, /FOR UPDATE/); assert.match(sql, /COMMIT/);
  for (const id of models) {
    const prefix = id.replaceAll("-", "_");
    assert.ok(sql.includes("SELECT '" + id + "', 1, 'PUBLISHED'"));
    assert.ok(sql.includes("robot_id = '" + id + "' AND version_no = 1"));
    const rows = [...sql.matchAll(new RegExp("SELECT @" + prefix
      + "_spec, '([^']+)', (NULL|@\\w+), '([^']+)', '([^']+)', TRUE, ([\\d., -]+) WHERE @"
      + prefix + "_new", "g"))].map(row => ({code: row[1], parent: row[2], component: row[3],
      label: row[4], pose: row[5].split(",").map(Number)}));
    const model = sandbox.window.ROBOT_MODELS.find(model => model.id === id);
    assert.equal(rows.length, model.parts.reduce((sum, part) => sum + part.quantity, 0), id + " instance count");
    const codes = new Set();
    for (const part of model.parts) {
      const actual = rows.filter(row => row.component === part.id);
      assert.equal(actual.length, part.quantity, id + " " + part.id);
      const value = sandbox.window.ASSEMBLY_3D_CONFIG[id][part.id].target;
      const targets = Array.isArray(value) ? value : [value];
      actual.forEach((row, index) => {
        assert.ok(!codes.has(row.code)); codes.add(row.code);
        const orientation = sql.match(new RegExp("SELECT id, '(Y\\d+)' FROM assembly_slots WHERE spec_id = @"
          + prefix + "_spec AND slot_code = '" + row.code + "' AND @" + prefix + "_new"));
        assert.ok(orientation, id + " orientation " + row.code);
        const angle = Number(orientation[1].slice(1)) * Math.PI / 180;
        const target = targets[index];
        ["x", "y", "z", "rx", "ry", "rz"].forEach((axis, n) => {
          const actualValue = row.pose[n] + (axis === "ry" ? angle : 0);
          assert.ok(Math.abs(actualValue - (target[axis] || 0)) < 0.00002, id + " pose " + row.code + " " + axis);
        });
        if (row.parent !== "NULL") assert.ok(sql.includes("SET " + row.parent + " = (SELECT id FROM assembly_slots"));
      });
    }
  }
});

test("explicit detailed start is optional, owner scoped, locked and does not rewrite GROUP sessions", () => {
  const servlet = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblySessionServlet.java");
  const db = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblyPlacementDB.java");
  assert.match(servlet, /newDetailedSession/);
  assert.match(servlet, /AssemblyPlacementDB\.createSession\(user\.getId\(\), robotId, true\)/);
  assert.match(db, /boolean detailedOnly/);
  assert.match(db, /SELECT id FROM users WHERE id = \? FOR UPDATE/);
  assert.ok(db.indexOf("SELECT id FROM users") < db.indexOf("if (detailedOnly)"), "both entry paths lock user before spec");
  assert.match(db, /assembly_mode = 'DETAILED'/);
  assert.doesNotMatch(db, /UPDATE assembly_sessions SET assembly_mode/);
});

test("old GROUP workflow offers an explicit accessible new detailed session without silent migration", () => {
  const js = read("assets/js/assembly-mode.js");
  assert.match(js, /createElement\("button"\)/);
  assert.match(js, /createDetailed/); assert.match(js, /session\.assemblyMode !== "GROUP"/);
  assert.match(js, /textContent/); assert.doesNotMatch(js, /innerHTML|localStorage/);
  for (const file of ["pages/lap-rap.html", "pages/lap-rap-3d.html"]) assert.ok(read(file).includes("assembly-mode.js?v="));
});
