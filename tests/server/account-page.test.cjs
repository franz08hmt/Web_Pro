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
