"use strict";
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const root = path.resolve(__dirname, "../..");
const java = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
const read = file => fs.readFileSync(path.join(root, file), "utf8");

test("new history beans stay within the course syntax and Java line limits", () => {
  for (const name of ["DiagnosisHistoryItem", "DiagnosisHistoryPage"]) {
    const source = read(java + "business/" + name + ".java");
    assert.match(source, /implements Serializable/);
    assert.match(source, new RegExp("public " + name + "\\(\\)"));
    source.split(/\r?\n/).forEach((line, index) => assert.ok(line.length <= 120, name + ":" + (index + 1)));
    const code = source.replace(/"(?:\\.|[^"\\])*"|\/\*[\s\S]*?\*\/|\/\/[^\n]*/g, "");
    assert.doesNotMatch(code, /\.stream\s*\(|::|->|\bswitch\s*\(|java\.time|\brecord\s|\bvar\s+\w|\bOptional\b/);
  }
});

test("diagnosis history stays read-only, owner-scoped and separate from task evidence", () => {
  const servlet = read(java + "controller/DiagnosisServlet.java");
  assert.match(servlet, /SessionUtil.getCurrentUser/);
  assert.match(servlet, /no-store/);
  assert.doesNotMatch(servlet, /getParameter\("(?:userId|user|email)"\)/);
  assert.match(servlet, /countPracticeHistory\(user.getId\(\)\)/);
  assert.match(servlet, /ValidationUtil.parsePositiveInt/);
  const db = read(java + "data/DiagnosisDB.java");
  const history = db.slice(db.indexOf("public static int countPracticeHistory"), db.indexOf("public static DiagnosisAttempt selectForSubmission"));
  assert.equal((history.match(/a?\.?user_id\s*=\s*\?/g) || []).length, 2);
  assert.equal((history.match(/mode\s*=\s*'PRACTICE'/g) || []).length, 2);
  assert.equal((history.match(/state\s*=\s*'SUBMITTED'/g) || []).length, 2);
  assert.match(history, /ORDER BY a.submitted_at DESC, a.id DESC/);
  assert.match(history, /LIMIT \? OFFSET \?/);
  assert.match(history, /item.useAttempt\(attempt\)/);
  assert.doesNotMatch(history, /new (?:java.math.)?BigDecimal|\.divide\(|\.setScale\(|\b(?:INSERT|UPDATE|DELETE)\b/);
});

test("history view uses the existing shell, escaped bean displays and no unsupported JSP tags", () => {
  const jsp = read("tomcat-app/src/main/webapp/WEB-INF/views/diagnosis-history.jsp");
  assert.doesNotMatch(jsp, /<\s*(?:select|option)\b|<%(?!@)/);
  jsp.split(/\r?\n/).forEach(line => assert.ok(line.length <= 140));
  assert.match(jsp, /workspace-header.jspf/);
  assert.match(jsp, /noindex, nofollow/);
  assert.match(jsp, /empty diagnosisHistory/);
  assert.match(jsp, /<caption>/);
  for (const field of ["scenarioTitle", "robotName", "scoreDisplay", "submittedAtDisplay"]) {
    assert.ok(jsp.includes('<c:out value="${historyItem.' + field + '}"/>'), field);
  }
  assert.doesNotMatch(jsp, /<fmt:|<fn:|<style\b|<script\b/);
});

test("history integration does not duplicate cause/action feedback on the result page", () => {
  const jsp = read("tomcat-app/src/main/webapp/WEB-INF/views/diagnosis-result.jsp");
  for (const name of ["chosenCause", "chosenAction"]) {
    assert.equal((jsp.match(new RegExp("diagnosisAttempt\\." + name + "\\.feedbackText", "g")) || []).length, 1);
  }
});
