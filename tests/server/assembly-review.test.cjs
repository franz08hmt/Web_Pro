"use strict";
const fs = require("node:fs");
const path = require("node:path");
const assert = require("node:assert/strict");
const test = require("node:test");
const root = path.resolve(__dirname, "../..");
function read(file) { return fs.readFileSync(path.join(root, file), "utf8"); }

test("assembly review sources keep readable Murach Java and core JSP syntax", () => {
  const base = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
  for (const dir of ["business", "data", "controller", "util"]) {
    for (const file of fs.readdirSync(path.join(root, base, dir))) {
      if (!/^(Assembly(Event|Drawing|Review|Layout|Placement|Session)|DrawingItem)/.test(file)) continue;
      if (file === "AssemblySessionDB.java") continue; // Unchanged legacy reader, guarded by existing tests.
      const source = read(base + dir + "/" + file);
      const code = source.replace(/\/\*[\s\S]*?\*\/|\/\/[^\n]*|"(?:\\.|[^"\\])*"/g, "");
      assert.doesNotMatch(code, /\.stream\s*\(|->|::|\b(?:switch|Optional|var|record)\b|java\.time/);
      assert.doesNotMatch(code, /\b(?:List|Set|Map)\.of|computeIfAbsent|getOrDefault|\.forEach\s*\(/);
      for (const [n, line] of source.split(/\r?\n/).entries()) assert.ok(line.length <= 120, file + ":" + (n + 1));
    }
  }
  for (const file of fs.readdirSync(path.join(root, "tomcat-app/src/main/webapp/WEB-INF/views"))) {
    if (!/^assembly-(review|drawing)\./.test(file)) continue;
    const source = read("tomcat-app/src/main/webapp/WEB-INF/views/" + file);
    assert.doesNotMatch(source, /<%(?!@)|<(?:fmt:|fn:|select\b|option\b)|fn:/i);
    for (const line of source.split(/\r?\n/)) assert.ok(line.length <= 140, file);
  }
});

test("placement preview keeps the original tray angles before a slot is chosen", () => {
  const source = read("assets/js/assembly-3d-placement.js");
  assert.match(source, /trayRotation = object\.rotation\.clone\(\)/);
  assert.match(source, /slot \|\| selectedObject\.userData\.trayRotation/);
  assert.doesNotMatch(source, /selectedObject\.rotation\.y \+=/);
});

test("event migration is additive and preserves one undo per event and session ownership", () => {
  const sql = read("database/migrations/014_assembly_events.sql");
  assert.doesNotMatch(sql, /^\s*(?:DROP|TRUNCATE|UPDATE|DELETE)\b/im);
  assert.match(sql, /CHECK \(action IN \('ATTACH','DETACH','RESET','COMPLETE','UNDO'\)\)/);
  assert.match(sql, /CHECK \(outcome IN \('ACCEPTED','REJECTED'\)\)/);
  assert.match(sql, /UNIQUE KEY uq_event_undo \(undo_of_event_id\)/);
  assert.match(sql, /KEY ix_event_session \(session_id, id\)/);
  assert.match(sql, /FOREIGN KEY \(undo_of_event_id, session_id\)/);
  assert.match(sql, /'014_assembly_events'/);
});

test("event persistence shares the placement transaction and caps rejected rows", () => {
  const db = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblyPlacementDB.java");
  const events = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblyEventDB.java");
  assert.match(db, /AssemblyEventDB\.insert\(connection, event, userId\)/);
  assert.match(db, /countRejected\(connection, id, userId\) < 300/);
  assert.match(db, /!"NOT_FOUND"\.equals\(reason\)/);
  assert.match(events, /JOIN assembly_sessions s ON s\.id = e\.session_id/);
  assert.match(events, /s\.user_id = \?/);
  assert.match(events, /finally/);
});
