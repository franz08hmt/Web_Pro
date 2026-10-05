"use strict";
const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const root = path.resolve(__dirname, "../..");
const java = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
const read = name => fs.readFileSync(path.join(root, name), "utf8");
test("diagnosis routes protect session identity, roles and form CSRF", () => {
  for (const name of ["DiagnosisServlet", "AdminDiagnosisServlet"]) {
    const source = read(java + "controller/" + name + ".java");
    assert.match(source, /SessionUtil.getCurrentUser/);
    assert.match(source, /hasValidFormCsrfToken/);
    assert.match(source, /no-store/);
    assert.match(source, /isAdmin\(/);
    assert.doesNotMatch(source, /getParameter\("userId"\)/);
  }
});
test("diagnosis play only presents persisted observations without answer flags", () => {
  const source = read("tomcat-app/src/main/webapp/WEB-INF/views/diagnosis-play.jsp");
  assert.doesNotMatch(source, /observationText|is_correct|is_required|isCorrect|\.correct|\.required|feedback|explanation/i);
  assert.match(source, /observationDisplay/);
  assert.match(source, /type="radio"/);
  assert.match(source, /csrfToken/);
});
test("diagnosis transactions record choices once, enforce owner and lock task before round", () => {
  const source = read(java + "data/DiagnosisDB.java");
  assert.match(source, /prepareStatement/);
  assert.match(source, /user_id = \?/);
  assert.match(source, /FOR UPDATE/);
  assert.match(source, /INSERT IGNORE INTO diagnosis_attempt_checks/);
  assert.match(source, /setAutoCommit\(false\)/);
  assert.match(source, /rollback\(/);
  assert.match(read(java + "data/TaskSubmissionDB.java"), /validTaskEvidence/);
});
test("migration 010 is additive with two explicit check replacements", () => {
  const source = read("database/migrations/010_diagnosis_practice.sql");
  assert.doesNotMatch(source, /\bDROP\s+TABLE|\bTRUNCATE|\bDELETE\s+FROM|\bUPDATE\s+\w+\s+SET/i);
  for (const table of ["diagnosis_scenarios", "diagnosis_checks", "diagnosis_options", "diagnosis_attempts", "diagnosis_attempt_checks"]) {
    assert.match(source, new RegExp("CREATE TABLE IF NOT EXISTS " + table));
  }
  assert.match(source, /CHECK_CONSTRAINTS/);
  assert.match(source, /automatic_points BETWEEN 30 AND 80/);
  assert.match(source, /total_points BETWEEN 30 AND 100/);
  assert.match(source, /UNIQUE\s*\(attempt_id, check_id\)/);
  assert.match(source, /ENUM\('A','B'\)/);
});
test("diagnosis and task forms use radio inputs and rubric getters instead of select or hard-coded weights", () => {
  const folder = "tomcat-app/src/main/webapp/WEB-INF/views/";
  for (const name of fs.readdirSync(path.join(root, folder))) {
    if (!/^(task-|admin-task-|diagnosis-|admin-diagnosis-).*\.jsp$/.test(name)) continue;
    assert.doesNotMatch(read(folder + name), /<select\b|<script\b/i, name);
  }
  assert.match(read(folder + "admin-task-form.jsp"), /name="rubricTemplate" value="B"/);
  assert.match(read(folder + "task-view.jsp"), /task\.rubric\.label/);
  assert.match(read(folder + "task-preview.jsp"), /taskSubmission\.rubric\.automaticMaximum/);
});
