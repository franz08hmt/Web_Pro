"use strict";

/* Giữ cấu trúc Java đúng như slide môn học (Chapter 2 slide 5, Chapter 12).
   Giảng viên kiểm tra việc code có giống cách đã dạy hay không, nên test này
   chặn các tên/kiểu không có trong slide quay trở lại dự án. */

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const root = path.join(__dirname, "..", "..");
const javaRoot = path.join(root, "tomcat-app", "src", "main", "java", "vn", "edu", "webpro", "robotlab");

function javaFiles(dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) return javaFiles(full);
    return entry.name.endsWith(".java") ? [full] : [];
  });
}

test("packages follow the Model 2 layers taught in class", () => {
  const packages = fs.readdirSync(javaRoot).filter((name) =>
    fs.statSync(path.join(javaRoot, name)).isDirectory());

  for (const required of ["business", "controller", "data", "util"]) {
    assert.ok(packages.includes(required), `thiếu package ${required}`);
  }
  for (const foreign of ["dao", "service", "model"]) {
    assert.ok(!packages.includes(foreign), `package ${foreign} không có trong slide`);
  }
});

test("no class uses the Dao or Service naming that the slides never use", () => {
  const names = javaFiles(javaRoot).map((file) => path.basename(file, ".java"));
  const foreign = names.filter((name) => /(Dao|DAO|Service)$/.test(name));
  assert.deepEqual(foreign, []);
});

test("business objects are classes, not records", () => {
  for (const file of javaFiles(path.join(javaRoot, "business"))) {
    const source = fs.readFileSync(file, "utf8");
    assert.doesNotMatch(source, /\brecord\s+\w+\s*\(/, `${path.basename(file)} dùng record`);
    assert.match(source, /implements Serializable/, `${path.basename(file)} chưa Serializable`);
  }
});

test("every XxxDB class uses ConnectionPool and closes resources in finally", () => {
  const dataDir = path.join(javaRoot, "data");
  const dbClasses = fs.readdirSync(dataDir).filter((name) => /DB\.java$/.test(name) && name !== "HealthDB.java");
  assert.ok(dbClasses.includes("UserDB.java"));

  for (const name of dbClasses) {
    const source = fs.readFileSync(path.join(dataDir, name), "utf8");
    const publicMethods = source.match(/^\s{4}public (?!class)[^(]*\(/gm) || [];
    for (const signature of publicMethods) {
      assert.match(signature, /public static /, `${name}: ${signature.trim()} phải là static như UserDB`);
    }
    assert.match(source, /ConnectionPool\.getInstance\(\)/, `${name} không dùng ConnectionPool`);
    assert.match(source, /finally \{[\s\S]*?pool\.freeConnection\(connection\)/, `${name} không trả kết nối trong finally`);
    assert.match(source, /DBUtil\.closePreparedStatement\(ps\)/, `${name} không đóng PreparedStatement bằng DBUtil`);
    assert.doesNotMatch(source, /createStatement\(\)/, `${name} dùng Statement thay vì PreparedStatement`);
  }
});

test("connection pool is configured in context.xml without a committed password", () => {
  const contextXml = fs.readFileSync(
    path.join(root, "tomcat-app", "src", "main", "webapp", "META-INF", "context.xml"), "utf8");
  assert.match(contextXml, /name="jdbc\/robotlab"/);
  assert.match(contextXml, /type="javax\.sql\.DataSource"/);
  assert.match(contextXml, /password="\$\{DB_PASSWORD\}"/);
  assert.match(contextXml, /username="\$\{DB_USER\}"/);
});

test("registration can never choose its own role", () => {
  const userDb = fs.readFileSync(path.join(javaRoot, "data", "UserDB.java"), "utf8");
  const insert = userDb.match(/public static int insert\(User user, String passwordHash\)[\s\S]*?\n {4}\}/);
  assert.ok(insert, "không tìm thấy UserDB.insert");
  assert.match(insert[0], /VALUES \(\?, \?, \?, 'user'\)/);
  assert.doesNotMatch(insert[0], /getRole\(\)/);
});
