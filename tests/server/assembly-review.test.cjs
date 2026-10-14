"use strict";
const fs = require("node:fs");
const path = require("node:path");
const assert = require("node:assert/strict");
const test = require("node:test");
const vm = require("node:vm");
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

test("completed review uses owner scoped Servlet attributes core JSP and real model quiz links", () => {
  const servlet = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblyReviewPageServlet.java");
  assert.match(servlet, /selectSession\(sessionId, user\.getId\(\)\)/);
  assert.match(servlet, /setHeader\("Cache-Control", "no-store"\)/);
  assert.match(servlet, /setAttribute\("assemblyReview"/);
  assert.match(servlet, /String url = "\/WEB-INF\/views\/assembly-review.jsp"/);
  assert.match(servlet, /getRequestDispatcher\(url\)\.forward\(request, response\)/);
  const view = read("tomcat-app/src/main/webapp/WEB-INF/views/assembly-review.jsp");
  for (const id of ["review-model", "review-process", "review-receipt", "review-quiz"]) {
    assert.ok(view.includes('id="' + id + '"'));
  }
  assert.match(view, /var="reviewQuizUrl" value="\/pages\/kiem-tra\.html"/);
  assert.match(view, /c:param name="model" value="\$\{session\.robotId\}"/);
  assert.match(view, /c:out/);
  assert.doesNotMatch(view, /<%(?!@)|fmt:|fn:|<select/);
  for (const file of ["assets/js/account.js", "assets/js/assembly-3d.js", "assets/js/personalization.js"]) {
    assert.match(read(file), /assembly-review\?session=/);
  }
});

test("drawing fragment declares UTF-8 explicitly for Jasper static inclusion", () => {
  assert.match(read("tomcat-app/src/main/webapp/WEB-INF/views/assembly-drawing.jspf"), /pageEncoding="UTF-8"/);
});

test("replay derives read-only frames from accepted events including undo and reset", () => {
  const source = read("assets/js/assembly-3d-replay.js");
  assert.doesNotMatch(source, /fetch\(|sessionApi|onAttach|onDetach|innerHTML/);
  assert.match(source, /clearInterval/);
  assert.match(source, /prefers-reduced-motion/);
  const context = {window: {}};
  vm.runInNewContext(source, context);
  const frames = context.window.assemblyReplayFrames({spec: {}, placements: []}, [
    {action: "ATTACH", instanceId: "1", slotId: "2", orientation: "Y90"},
    {action: "UNDO", instanceId: "1", slotId: "2", orientation: null},
    {action: "ATTACH", instanceId: "1", slotId: "2", orientation: "Y180"},
    {action: "RESET"}
  ]);
  assert.equal(frames.length, 5);
  assert.equal(frames[1].placements[0].orientation, "Y90");
  assert.equal(frames[2].placements.length, 0);
  assert.equal(frames[4].placements.length, 0);
  assert.ok(frames.every(frame => frame.canEdit === false));
});

test("events endpoint verifies owner and replay bypasses automatic session mutations", () => {
  const source = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblySessionServlet.java");
  assert.match(source, /AssemblyEventDB\.selectBySession\(sessionId, user\.getId\(\)\)/);
  const front = read("assets/js/assembly-3d.js");
  assert.match(front, /if \(replayMode\) \{[\s\S]*?restoreReplay\(\);[\s\S]*?return;/);
  assert.match(front, /replayController\?\.dispose\(\)/);
});

test("undo is owner scoped versioned CSRF protected and shares existing bean validation", () => {
  const base = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
  const db = read(base + "data/AssemblyPlacementDB.java");
  assert.match(db, /selectUndoTarget\(connection, id, userId\)/);
  assert.match(db, /layout\.validateUndo\(undoTarget, expectedVersion\)/);
  assert.match(db, /setUndoOfEventId\(undoTarget\.getId\(\)\)/);
  const servlet = read(base + "controller/AssemblySessionServlet.java");
  assert.match(servlet, /"undo"\.equals\(segments\[1\]\)/);
  assert.match(servlet, /hasValidCsrfToken\(request, response\)/);
  const front = read("assets/js/assembly-3d-placement.js");
  assert.match(front, /event\.ctrlKey/);
  assert.match(front, /contenteditable/);
  assert.match(front, /options\.onUndo\(layout\.layoutVersion\)/);
});
