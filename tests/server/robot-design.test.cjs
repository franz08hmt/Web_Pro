"use strict";
const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const root = path.resolve(__dirname, "../..");
const read = file => fs.readFileSync(path.join(root, file), "utf8");

test("design schema is additive owner scoped and pins versioned fits and unique mounts", () => {
  const sql = read("database/migrations/015_robot_design.sql");
  assert.doesNotMatch(sql, /^\s*(DROP|TRUNCATE|ALTER|UPDATE|DELETE)\b/im);
  for (const table of ["design_rule_sets", "design_mounts", "design_fits", "user_kit_items", "robot_designs",
    "design_instances", "design_placements", "design_events"]) assert.ok(sql.includes("CREATE TABLE IF NOT EXISTS " + table));
  assert.match(sql, /UNIQUE KEY uq_design_undo \(undo_of_event_id\)/);
  assert.match(sql, /UNIQUE KEY uq_design_mount \(design_id, host_instance_id, mount_code\)/);
  assert.match(sql, /REFERENCES design_fits/);
  assert.match(sql, /CHECK \(orientation_code IN \('Y0','Y90','Y180','Y270'\)\)/);
  assert.match(sql, /'015_robot_design'/);
});

test("design seed inserts one immutable version and leaves unsupported models without a fit", () => {
  const seed = read("database/seed-design-rules-v1.sql");
  assert.match(seed, /START TRANSACTION/);
  assert.match(seed, /FOR UPDATE/);
  assert.match(seed, /@create_design_rules_v1/);
  assert.doesNotMatch(seed, /^\s*(UPDATE|DELETE|REPLACE|DROP)\b/im);
  for (const goal of ["BASIC_2WD", "LINE_FOLLOWER", "OBSTACLE_AVOIDER", "SCANNING_AVOIDER", "COMBO"]) {
    assert.ok(seed.includes(goal));
  }
  assert.doesNotMatch(seed, /'arm-frame'|'mini-gripper'/);
});

test("design Java and JSP use classic readable course syntax", () => {
  const base = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
  for (const dir of ["business", "controller", "data", "util"]) {
    for (const name of fs.readdirSync(path.join(root, base, dir))) {
      if (!/^(Design|RobotDesign|KitItem|MyKit|UserKit)/.test(name)) continue;
      const source = read(base + dir + "/" + name);
      const code = source.replace(/\/\*[\s\S]*?\*\/|\/\/[^\n]*|"(?:\\.|[^"\\])*"/g, "");
      assert.doesNotMatch(code, /\.stream\s*\(|->|::|\b(switch|Optional|var|record)\b|java\.time/);
      assert.doesNotMatch(code, /\b(List|Map|Set)\.of|computeIfAbsent|getOrDefault/);
      for (const [n, line] of source.split(/\r?\n/).entries()) assert.ok(line.length <= 120, name + ":" + (n + 1));
    }
  }
  const views = "tomcat-app/src/main/webapp/WEB-INF/views/";
  for (const name of fs.readdirSync(path.join(root, views))) {
    if (!/^(my-kit|design-)/.test(name)) continue;
    const source = read(views + name);
    assert.doesNotMatch(source, /<%(?!@)|<fmt:|<fn:|<select\b|<option\b/);
    for (const line of source.split(/\r?\n/)) assert.ok(line.length <= 140, name);
  }
});

test("kit forms use session owner CSRF PRG and imports confirmed purchases without adding twice", () => {
  const base = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
  const servlet = read(base + "controller/MyKitServlet.java");
  assert.match(servlet, /hasValidFormCsrfToken/);
  assert.match(servlet, /user\.getId\(\)/);
  assert.match(servlet, /sendRedirect/);
  assert.match(servlet, /getRequestDispatcher\(url\)\.forward/);
  const db = read(base + "data/UserKitDB.java");
  assert.match(db, /GREATEST\(quantity, \?\)/);
  assert.match(db, /o\.user_id = \?/);
  assert.match(db, /o\.status = 'CONFIRMED'/);
  assert.match(db, /FOR UPDATE/);
  const view = read("tomcat-app/src/main/webapp/WEB-INF/views/my-kit.jsp");
  assert.match(view, /name="csrfToken"/);
  assert.match(view, /type="number"/);
  assert.match(view, /c:out/);
});

test("design creation snapshots quantities under owner lock and mutations keep ownership and form CSRF", () => {
  const base = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
  const db = read(base + "data/DesignDB.java");
  assert.match(db, /UserKitDB\.lockOwner\(connection, userId\)/);
  assert.match(db, /validateCreation/);
  assert.match(db, /INSERT INTO design_instances/);
  assert.match(db, /d\.id = \? AND d\.user_id = \?/);
  const servlet = read(base + "controller/DesignPageServlet.java");
  assert.match(servlet, /hasValidFormCsrfToken/);
  assert.match(servlet, /confirmDelete/);
  assert.match(servlet, /setAttribute/);
  assert.match(servlet, /getRequestDispatcher\(url\)\.forward/);
});

test("workshop reuses placement view with string mount ids and keeps all writes owner scoped and CSRF protected", () => {
  const base = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
  const db = read(base + "data/DesignPlacementDB.java");
  assert.match(db, /load\(connection, id, userId, true\)/);
  assert.match(db, /DesignEventDB\.insert\(connection, event, userId\)/);
  assert.match(db, /validateUndo|validateAttach/);
  const servlet = read(base + "controller/DesignApiServlet.java");
  assert.match(servlet, /requireUser/);
  assert.match(servlet, /hasValidCsrfToken/);
  assert.match(servlet, /"no-store"/);
  const js = read("assets/js/design-room.js");
  assert.match(js, /createAssemblyPlacementView/);
  assert.match(js, /slotId: slot/);
  assert.doesNotMatch(js, /innerHTML|isCorrect|compatible:|percent:/);
});

test("design check is calculated under owner version lock and mutations invalidate the saved summary", () => {
  const base = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
  const db = read(base + "data/DesignPlacementDB.java");
  assert.match(db, /report\.evaluate\(layout\)/);
  assert.match(db, /last_check_summary = \?/);
  assert.match(db, /last_check_summary = NULL/);
  assert.match(db, /WHERE id = \? AND user_id = \? AND layout_version = \?/);
  const check = read(base + "business/DesignCheck.java");
  assert.match(check, /findPlacement/);
  assert.match(check, /"UNSUPPORTED"/);
  assert.match(check, /"SERVO_HORN"/);
});

test("design sheet forwards owner scoped snapshot and reuses drawing and shared learning error classification", () => {
  const base = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
  const servlet = read(base + "controller/DesignSheetServlet.java");
  assert.match(servlet, /user\.getId\(\)/);
  assert.match(servlet, /setAttribute/);
  assert.match(servlet, /getRequestDispatcher\(url\)\.forward/);
  assert.match(servlet, /"no-store"/);
  assert.match(read(base + "business/DesignSheet.java"), /rule\.isLearningError\(event\.getReasonCode\(\)\)/);
  const drawing = read(base + "business/AssemblyDrawing.java");
  assert.doesNotMatch(drawing, /throw new IllegalArgumentException/);
  assert.match(read("tomcat-app/src/main/webapp/WEB-INF/views/design-sheet.jsp"), /assembly-drawing\.jspf/);
});
