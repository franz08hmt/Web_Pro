"use strict";
const fs = require("node:fs");
const path = require("node:path");
const assert = require("node:assert/strict");
const test = require("node:test");
const root = path.resolve(__dirname, "../..");
const read = file => fs.readFileSync(path.join(root, file), "utf8");

test("component name migration only corrects the legacy catalog name and records an idempotent version", () => {
  const sql = read("database/migrations/016_component_name_fix.sql");
  assert.match(sql, /START TRANSACTION/);
  assert.match(sql, /UPDATE components SET name = 'Động cơ DC TT \(hộp số\)'/);
  assert.match(sql, /WHERE id = 'dc-motor' AND name = 'Động cơ DC TT & Bánh xe'/);
  assert.match(sql, /INSERT IGNORE INTO schema_migrations \(version\) VALUES \('016_component_name_fix'\)/);
  assert.match(sql, /COMMIT/);
  assert.doesNotMatch(sql, /^\s*(ALTER|DROP|DELETE|TRUNCATE)\b/im);
  assert.equal((sql.match(/\bUPDATE\b/g) || []).length, 1);
  assert.ok(read("database/schema.sql").replace(/\r/g, "").includes(sql.replace(/\r/g, "").trim()));
});

test("instance and journal labels always use catalog names rather than a motor-specific override", () => {
  const base = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
  for (const name of ["business/DesignInstance.java", "data/AssemblyEventDB.java", "data/AssemblyPlacementDB.java"]) {
    assert.doesNotMatch(read(base + name), /Động cơ DC TT/);
  }
  assert.match(read(base + "business/DesignInstance.java"), /return name \+ " " \+ instanceNo/);
  assert.match(read(base + "data/AssemblyPlacementDB.java"), /String name = rs\.getString\("name"\)/);
  assert.match(read(base + "data/AssemblyEventDB.java"), /String name = rs\.getString\("component_name"\)/);
});

test("fresh database seed contains the corrected UTF-8 component name", () => {
  const seed = read("database/seed.sql");
  assert.ok(seed.includes(Buffer.from("Động cơ DC TT (hộp số)", "utf8").toString("hex")));
  assert.ok(!seed.includes(Buffer.from("Động cơ DC TT & Bánh xe", "utf8").toString("hex")));
  assert.ok(read("assets/js/data.js").includes('name: "Động cơ DC TT (hộp số)"'));
});
