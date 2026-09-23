"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const javaRoot = path.join(
  __dirname,
  "..",
  "..",
  "tomcat-app",
  "src",
  "main",
  "java",
  "vn",
  "edu",
  "webpro",
  "robotlab"
);
const servlet = fs.readFileSync(path.join(javaRoot, "controller", "AssemblySessionServlet.java"), "utf8");
const session = fs.readFileSync(path.join(javaRoot, "business", "AssemblySession.java"), "utf8");
const database = fs.readFileSync(path.join(javaRoot, "data", "AssemblySessionDB.java"), "utf8");

test("reset endpoint is authenticated, CSRF-protected, and scoped to the session owner", () => {
  assert.match(servlet, /protected void doDelete\(HttpServletRequest request, HttpServletResponse response\)/);
  assert.match(servlet, /SessionUtil\.requireUser\(request, response\)[\s\S]*?SessionUtil\.hasValidCsrfToken\(request, response\)/);
  assert.match(servlet, /findSession\(segments\[0\], user\)/);
  assert.match(servlet, /AssemblySessionDB\.resetProgress\(session\.getId\(\), user\.getId\(\)\)/);
  assert.match(session, /canResetProgress\(\)[\s\S]*?isInPreparation\(\) \|\| isInProgress\(\)/);
});

test("reset clears each progress table and changes the session status in one transaction", () => {
  assert.match(database, /connection\.setAutoCommit\(false\)/);
  assert.match(database, /SELECT status FROM assembly_sessions WHERE id = \? AND user_id = \? FOR UPDATE/);
  for (const table of ["session_components", "session_steps", "session_visual_parts"]) {
    assert.match(database, new RegExp(`deleteProgressRows\\(connection, "${table}", id\\)`));
  }
  assert.match(database, /UPDATE assembly_sessions SET status = 'PREPARING' WHERE id = \? AND user_id = \?/);
  assert.match(database, /connection\.commit\(\)/);
  assert.match(database, /connection\.rollback\(\)/);
});
