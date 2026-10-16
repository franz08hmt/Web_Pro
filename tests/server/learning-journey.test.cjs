"use strict";
const test = require("node:test"), assert = require("node:assert/strict");
const fs = require("node:fs"), path = require("node:path");
const root = path.join(__dirname, "../..");
const java = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
test("learning summary production code uses course syntax and delegates aggregation to a bean", () => {
  for (const name of ["controller/LearningSummaryServlet.java", "business/LearningSummary.java"]) {
    const source = fs.readFileSync(path.join(root, java, name), "utf8");
    assert.doesNotMatch(source, /\.stream\(|::|\bswitch\s*\(|\bvar\s|java\.time|\bOptional\b|(?:List|Map)\.of\(/);
    assert.ok(source.split(/\r?\n/).every(line => [...line].length <= 120), name);
  }
  const servlet = fs.readFileSync(path.join(root, java, "controller/LearningSummaryServlet.java"), "utf8");
  assert.match(servlet, /summary\.summarizeSessions\(sessionCounts\)/);
  assert.match(servlet, /setAttribute\("totalSessions", summary\.getTotalSessions\(\)\)/);
});

test("wiring history filters its owner before deterministic database pagination", () => {
  const db = fs.readFileSync(path.join(root, java, "data/WiringDB.java"), "utf8");
  assert.match(db, /WHERE a.user_id = \? ORDER BY a.id DESC LIMIT \? OFFSET \?/);
  assert.match(db, /ps.setLong\(1, owner\);\s*ps.setInt\(2, limit\);\s*ps.setInt\(3, offset\)/);
  assert.match(db, /SELECT COUNT\(\*\) FROM wiring_attempts WHERE user_id = \?/);
  const servlet = fs.readFileSync(path.join(root, java, "controller/WiringServlet.java"), "utf8");
  assert.match(servlet, /selectAttempts\(user.getId\(\), history.getPageSize\(\), history.getOffset\(\)\)/);
  assert.doesNotMatch(servlet, /subList\(/);
});
