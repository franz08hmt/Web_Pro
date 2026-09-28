"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const root = path.join(__dirname, "..", "..");

test("account page loads the shared API client before its controller", () => {
  const html = fs.readFileSync(path.join(root, "pages", "tai-khoan.html"), "utf8");
  assert.match(html, /name="fullName"/);
  assert.match(html, /id="account-session"/);
  assert.ok(html.indexOf("../assets/js/api.js") < html.indexOf("../assets/js/account.js"));
});

test("account controller uses real auth endpoints instead of preview-only messages", () => {
  const source = fs.readFileSync(path.join(root, "assets", "js", "account.js"), "utf8");
  assert.match(source, /RobotAssemblyApi/);
  assert.match(source, /auth\.login/);
  assert.match(source, /auth\.register/);
  assert.match(source, /auth\.logout/);
  assert.doesNotMatch(source, /giai đoạn phát triển tiếp theo/);
});

/* AccountPageServlet từng redirect tới /pages/dang-nhap.html — một file không
   tồn tại — nên người chưa đăng nhập nhận 404 thay vì trang đăng nhập. Trình
   biên dịch không bắt được vì đường dẫn chỉ là chuỗi, vì vậy kiểm tra ở đây. */
test("every servlet redirect points at a page that exists", () => {
  const controllers = path.join(root, "tomcat-app", "src", "main", "java",
    "vn", "edu", "webpro", "robotlab", "controller");
  const pattern = /sendRedirect\(\s*request\.getContextPath\(\)\s*\+\s*"([^"]+)"/g;
  const checked = [];

  for (const name of fs.readdirSync(controllers)) {
    const source = fs.readFileSync(path.join(controllers, name), "utf8");
    for (const match of source.matchAll(pattern)) {
      const target = match[1].replace(/^\//, "");
      checked.push(`${name} -> ${match[1]}`);
      assert.ok(
        fs.existsSync(path.join(root, target)),
        `${name} chuyển hướng tới ${match[1]} nhưng file đó không tồn tại.`
      );
    }
  }

  assert.ok(checked.length > 0, "Không tìm thấy sendRedirect nào để kiểm tra.");
});

test("account page lists saved sessions with preparation and step progress", () => {
  const html = fs.readFileSync(path.join(root, "pages", "tai-khoan.html"), "utf8");
  const source = fs.readFileSync(path.join(root, "assets", "js", "account.js"), "utf8");
  assert.match(html, /id="account-history"/);
  assert.match(source, /assemblySessions\.list/);
  assert.match(source, /completedStepCount/);
  assert.match(source, /totalStepCount/);
  assert.match(source, /session\.id/);
});

test("server-rendered account view escapes user-controlled fields", () => {
  const jsp = fs.readFileSync(path.join(root, "tomcat-app", "src", "main", "webapp",
    "WEB-INF", "views", "account.jsp"), "utf8");
  assert.match(jsp, /<c:out value="\$\{user\.fullName\}"\s*\/>/);
  assert.match(jsp, /<c:out value="\$\{user\.email\}"\s*\/>/);
});

/* /assembly-receipt là servlet gốc ("/"), không nằm dưới /pages/ — link tương
   đối từ tai-khoan.html (đang ở /pages/) phải có tiền tố "../" nếu không sẽ
   trỏ nhầm sang /pages/assembly-receipt (404). */
test("completed sessions link to the results receipt with a path that resolves from /pages/", () => {
  const source = fs.readFileSync(path.join(root, "assets", "js", "account.js"), "utf8");
  assert.match(source, /session\.status === "COMPLETED"/);
  assert.match(source, /\.\.\/assembly-receipt\?session=\$\{encodeURIComponent\(session\.id\)\}/);
  assert.doesNotMatch(source, /href = `assembly-receipt\?session=/, "Thiếu \"../\" sẽ trỏ vào /pages/assembly-receipt");
});

test("assembly receipt page is only reachable by its owner and escapes robot text", () => {
  const servlet = fs.readFileSync(path.join(root, "tomcat-app", "src", "main", "java",
    "vn", "edu", "webpro", "robotlab", "controller", "AssemblyReceiptPageServlet.java"), "utf8");
  assert.match(servlet, /AssemblySessionDB\.selectSession\(sessionId, user\.getId\(\)\)/,
    "Phải lọc phiên theo user_id của người đang đăng nhập, không chỉ theo id trên URL");

  const jsp = fs.readFileSync(path.join(root, "tomcat-app", "src", "main", "webapp",
    "WEB-INF", "views", "assembly-receipt.jsp"), "utf8");
  for (const expression of ["${robot.name}", "${robot.level}", "${robot.summary}"]) {
    const raw = [...jsp.matchAll(new RegExp(expression.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"), "g"))];
    const escaped = [...jsp.matchAll(new RegExp(`value="${expression.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")}"`, "g"))];
    assert.equal(raw.length, escaped.length,
      `${expression} phải luôn xuất hiện trong <c:out value="...">, không nội suy EL trực tiếp ra HTML`);
    assert.ok(raw.length > 0, `${expression} không xuất hiện trong phiếu kết quả`);
  }
});
