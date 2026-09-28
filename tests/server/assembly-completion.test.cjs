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
const migration = fs.readFileSync(
  path.join(__dirname, "..", "..", "database", "migrations", "005_assembly_completion.sql"),
  "utf8"
);

test("completing an assembly never trusts a client-submitted percentage or flag", () => {
  // Thân request PATCH chỉ đọc "status"; không có nhánh nào đọc phần trăm/cờ hoàn thành từ client.
  assert.match(servlet, /JsonUtil\.stringField\(JsonUtil\.readBody\(request\.getReader\(\), MAX_BODY\), "status"\)/);
  assert.doesNotMatch(servlet, /booleanField\([^)]*"isCompleted"/);
  assert.doesNotMatch(servlet, /stringField\([^)]*"progressPercent"/);

  // Chuyển sang COMPLETED phải đi qua completeAssembly(), không update trực tiếp như các trạng thái khác.
  assert.match(servlet, /AssemblySession\.COMPLETED\.equals\(target\)\)\s*\{\s*if \(!completeAssembly\(session, response\)\) return;/);
});

test("the fast pre-check asks the JavaBean, but the atomic database write is the real authority", () => {
  assert.match(servlet, /RobotDB\.selectRobotComponents\(session\.getRobotId\(\)\)/);
  assert.match(servlet, /session\.canCompleteAssembly\(required\)/);
  assert.match(servlet, /AssemblySessionDB\.completeSession\(session\.getId\(\), session\.getUserId\(\)\)/);

  assert.match(
    session,
    /canCompleteAssembly\(List<RobotComponent> requiredComponents\)\s*\{\s*if \(!isInProgress\(\) \|\| requiredComponents\.isEmpty\(\)\) return false;/
  );
  // So theo tập hợp mã linh kiện (assembledPartIds chứa componentId), không so số lượng.
  assert.match(session, /Set<String> assembled = new HashSet<>\(assembledPartIds\);/);
  assert.match(session, /if \(!assembled\.contains\(required\.getComponentId\(\)\)\) return false;/);
});

test("completeSession() checks state and part-completeness inside one UPDATE so concurrent requests can't corrupt completed_at", () => {
  assert.match(
    database,
    /WHERE s\.id = \? AND s\.user_id = \? AND s\.status = 'IN_PROGRESS'/
  );
  assert.match(database, /SET s\.status = 'COMPLETED', s\.completed_at = CURRENT_TIMESTAMP/);
  // Điều kiện đủ linh kiện nằm ngay trong WHERE (không phải một SELECT riêng rồi mới UPDATE),
  // nên không có khoảng hở giữa lúc kiểm tra và lúc ghi cho hai request chen vào nhau.
  assert.match(database, /AND NOT EXISTS \(/);
  assert.match(database, /SELECT 1 FROM robot_components rc/);
  assert.match(database, /SELECT 1 FROM session_visual_parts svp/);
  assert.match(database, /WHERE svp\.session_id = s\.id AND svp\.component_id = rc\.component_id/);

  // UPDATE không đổi dòng nào (double-click, retry) phải phân biệt được "đã xong" với "chưa đủ"/"sai trạng thái".
  assert.match(database, /public static final int COMPLETE_ALREADY_DONE = 1;/);
  assert.match(database, /public static final int COMPLETE_INCOMPLETE_PARTS = 2;/);
  assert.match(database, /if \(AssemblySession\.COMPLETED\.equals\(status\)\) return COMPLETE_ALREADY_DONE;/);
});

test("completed_at migration is additive — no DROP/TRUNCATE/DELETE that could destroy existing session data", () => {
  assert.match(migration, /ALTER TABLE assembly_sessions ADD COLUMN completed_at TIMESTAMP NULL DEFAULT NULL/);
  assert.doesNotMatch(migration, /DROP\s|TRUNCATE|DELETE FROM/i);
  assert.match(migration, /INSERT INTO schema_migrations \(version\)\s*\nVALUES \('005_assembly_completion'\);/);
});
