"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const javaRoot = path.join(
  __dirname, "..", "..", "tomcat-app", "src", "main", "java", "vn", "edu", "webpro", "robotlab"
);
const root = path.join(__dirname, "..", "..");
const publicServlet = fs.readFileSync(path.join(javaRoot, "controller", "TroubleshootingServlet.java"), "utf8");
const adminServlet = fs.readFileSync(path.join(javaRoot, "controller", "AdminTroubleshootingServlet.java"), "utf8");
const guideDB = fs.readFileSync(path.join(javaRoot, "data", "TroubleshootingGuideDB.java"), "utf8");
const migration = fs.readFileSync(
  path.join(root, "database", "migrations", "007_troubleshooting.sql"), "utf8"
);

test("troubleshooting search is public read (no login required) but admin CRUD requires ADMIN + CSRF", () => {
  assert.doesNotMatch(publicServlet, /SessionUtil\.requireUser|SessionUtil\.requireAdmin/,
    "Tra cứu là nội dung tham khảo công khai, không nên yêu cầu đăng nhập");
  assert.match(adminServlet, /SessionUtil\.requireAdmin\(request, response\)/);
  const saveBody = adminServlet.match(/private void save\([\s\S]*?\n    \}/)[0];
  const deleteBody = adminServlet.match(/protected void doDelete\([\s\S]*?\n    \}/)[0];
  assert.match(saveBody, /SessionUtil\.hasValidCsrfToken\(request, response\)/);
  assert.match(deleteBody, /SessionUtil\.hasValidCsrfToken\(request, response\)/);
});

test("robot_id is optional so one situation can apply to several shared-component robots", () => {
  assert.match(migration, /robot_id VARCHAR\(64\)[^\n]*NULL,/);
  assert.match(guideDB, /AND \(robot_id = \? OR robot_id IS NULL\)/,
    "Lọc theo robot phải khớp cả tình huống dùng chung (robot_id NULL)");
  assert.match(adminServlet, /guide\.setRobotId\(robotId\.isEmpty\(\) \? null : robotId\)/);
});

test("deleting a referenced component only detaches the troubleshooting link, never blocks the delete", () => {
  assert.match(migration, /FOREIGN KEY \(related_component_id\)\s*\n\s*REFERENCES components\(id\)\s*\n\s*ON DELETE SET NULL/);
});

test("search/filter query is parameterized, not string-concatenated user input", () => {
  assert.doesNotMatch(guideDB, /"%"\s*\+\s*search\s*\+\s*"%"[\s\S]{0,40}\+.*query/);
  assert.match(guideDB, /ps\.setString\(index[+][+], "%" \+ search \+ "%"\)/,
    "Từ khóa tìm kiếm phải đi qua PreparedStatement, không nối thẳng vào SQL");
  assert.match(guideDB, /query\.append\(" AND symptom LIKE \?"\)/);
});

test("troubleshooting content explicitly frames itself as a reference checklist, not live robot telemetry", () => {
  const page = fs.readFileSync(path.join(root, "pages", "tra-cuu-loi.html"), "utf8");
  assert.match(page, /không đọc tín hiệu từ[\s\S]{0,20}robot thật/);
  assert.match(page, /ngắt nguồn trước khi/i);
});

test("seed data covers 10-15 situations across all three robots and is additive-only SQL", () => {
  const seed = fs.readFileSync(path.join(root, "database", "seed-troubleshooting.sql"), "utf8");
  assert.doesNotMatch(seed, /DROP\s|TRUNCATE|DELETE FROM|UPDATE\s+users|UPDATE\s+robots/i);
  const inserts = seed.match(/INSERT INTO troubleshooting_guides/g) || [];
  assert.ok(inserts.length >= 10 && inserts.length <= 15,
    `Cần khoảng 10-15 tình huống, hiện có ${inserts.length}`);
  for (const robotId of ["line-follower", "obstacle-avoider", "mini-arm"]) {
    const hex = Buffer.from(robotId, "utf8").toString("hex").toUpperCase();
    assert.ok(seed.toUpperCase().includes(hex), `Thiếu tình huống dành riêng cho ${robotId}`);
  }
});
