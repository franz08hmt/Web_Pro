"use strict";
const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const root = path.resolve(__dirname, "../..");
const java = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
const views = "tomcat-app/src/main/webapp/WEB-INF/views/";
const read = file => fs.readFileSync(path.join(root, file), "utf8");
const forbidden = /\.stream\s*\(|\.toList\s*\(|::|\w\s*->|\bOptional\b|\bswitch\s*\(|computeIfAbsent|getOrDefault|\.forEach\s*\(|\b(List|Set|Map)\.of\s*\(|\bvar\s+\w+\s*=|"""|java\.time|\brecord\s/;
function codeOnly(source) {
  return source.replace(/"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\/\*[\s\S]*?\*\/|\/\/[^\n]*/g,
    value => value.replace(/[^\r\n]/g, " "));
}
function formattingGuard(source, name) {
  source.split(/\r?\n/).forEach((line, index) => assert.ok(line.length <= 120, `${name}:${index + 1}: ${line.length}`));
  const code = codeOnly(source);
  assert.doesNotMatch(code, /\b(?:if|for|while|try)\(/, name);
  const withoutFor = code.replace(/\bfor\s*\([^)]*\)/g, value => value.replace(/;/g, " "));
  withoutFor.split(/\r?\n/).forEach((line, index) => {
    assert.ok((line.match(/;/g) || []).length <= 1, `${name}:${index + 1}: nhiều câu lệnh`);
  });
  for (const match of code.matchAll(/\b(if|for|while)\s*\(/g)) {
    let end = code.indexOf("(", match.index), depth = 1;
    while (depth && ++end < code.length) {
      if (code[end] === "(") depth++;
      if (code[end] === ")") depth--;
    }
    assert.match(code.slice(end + 1), /^\s*\{/, `${name}: ${match[1]} cần khối`);
  }
  for (const match of code.matchAll(/\belse\b/g)) {
    assert.match(code.slice(match.index + 4), /^\s*(?:\{|if\b)/, `${name}: else cần khối`);
  }
  assert.doesNotMatch(code, /\btry\s*(?!\s*[({])\S/, `${name}: try cần khối`);
}
function practiceViewHtmlGuard(source, name) {
  assert.doesNotMatch(source, /<\s*(?:select|option)\b/i, `${name}: select/option không có trong slide`);
  assert.doesNotMatch(source, /<fmt:|<fn:|<%(?!@)/, `${name}: chỉ dùng directive và JSTL core`);
  assert.doesNotMatch(source, /<\/[a-zA-Z][\w-]*\s+[^>\s][^>]*>/,
    `${name}: thẻ đóng không được có thuộc tính`);
  source.split(/\r?\n/).forEach((line, index) => {
    assert.ok(line.length <= 140, `${name}:${index + 1}: ${line.length}`);
  });
  // Bỏ directive/comment, EL và JSTL trước khi đọc thẻ HTML, kể cả c:out trong thuộc tính.
  const html = source.replace(/<%@[^]*?%>|<%--[^]*?--%>|<!--[^]*?-->/g, "")
    .replace(/\$\{[^}]*\}/g, "")
    .replace(/<\/?c:\w+\b(?:[^>"']|"[^"]*"|'[^']*')*>/g, "");
  const blocks = new Set(["form", "table", "section", "fieldset", "label", "ul", "ol",
    "div", "thead", "tbody", "tr"]);
  const stack = [];
  for (const tag of html.matchAll(/<\/?([a-zA-Z][\w:-]*)\b(?:[^>"']|"[^"]*"|'[^']*')*>/g)) {
    const block = tag[1].toLowerCase();
    if (!blocks.has(block)) continue;
    const line = html.slice(0, tag.index).split("\n").length;
    if (tag[0].startsWith("</")) {
      assert.equal(stack.pop(), block, `${name}:${line}: thẻ ${block} đóng lệch`);
    } else {
      stack.push(block);
    }
  }
  assert.deepEqual(stack, [], `${name}: thẻ khối chưa đóng`);
}

function guard(source, name) {
  formattingGuard(source, name);
  const clean = source.replace(/\/\*[\s\S]*?\*\/|\/\/[^\n]*/g, "");
  assert.doesNotMatch(clean, forbidden, name);
  for (const [i, line] of source.split(/\r?\n/).entries()) {
    assert.ok(line.length <= 120, `${name}:${i + 1}: ${line.length}`);
  }
}
test("wiring application code stays within course syntax and line limits", () => {
  const names = [];
  for (const pkg of ["business", "controller", "data", "util"]) {
    for (const name of fs.readdirSync(path.join(root, java, pkg))) {
      if (/^(?:Admin)?Wiring.*\.java$/.test(name)) {
        names.push(name);
        guard(read(java + pkg + "/" + name), name);
      }
    }
  }
  assert.ok(names.includes("WiringGrade.java"));
  assert.throws(() => guard("items.stream();", "injected"));
  assert.throws(() => guard("x".repeat(121), "injected"));
});
test("wiring forms protect session identity roles token and response caching", () => {
  for (const name of ["WiringServlet", "AdminWiringServlet"]) {
    const s = read(java + "controller/" + name + ".java");
    assert.match(s, /SessionUtil.getCurrentUser/);
    assert.match(s, /hasValidFormCsrfToken/);
    assert.match(s, /no-store/);
    assert.match(s, /isAdmin\(\)/);
    assert.match(s, /getRequestDispatcher\(url\).forward/);
    assert.doesNotMatch(s, /getParameter\("(?:userId|score|correctCount|state)"\)/);
  }
});
test("wiring JDBC keeps owner version and atomic grading with composite foreign keys", () => {
  const s = read(java + "data/WiringDB.java");
  for (const token of ["prepareStatement", "FOR UPDATE", "user_id = ?", "expectedVersion",
    "setAutoCommit(false)", "commit()", "rollback()", "finally", "DBUtil"]) {
    assert.ok(s.includes(token), token);
  }
  const migration = read("database/migrations/011_wiring_practice.sql");
  assert.doesNotMatch(migration, /\b(?:DROP|TRUNCATE|UPDATE)\b|DELETE\s+FROM/i);
  assert.match(migration, /FOREIGN KEY \(exercise_id, terminal_a\)/);
  assert.match(migration, /UNIQUE \(attempt_id, terminal_a, terminal_b\)/);
  assert.match(migration, /ON DELETE RESTRICT/);
});
test("all wiring views escape text and reject unsupported JSP and HTML", () => {
  const files = fs.readdirSync(path.join(root, views)).filter(n => /^(?:admin-)?wiring-.*\.jsp$/.test(n));
  assert.ok(files.length >= 5);
  for (const name of files) {
    const s = read(views + name);
    practiceViewHtmlGuard(s, name);
    assert.doesNotMatch(s, /<select|<option|<fmt:|<fn:|<%(?!@)/i);
    assert.doesNotMatch(s, />\s*\$\{(?:wiring\w*)\./);
    assert.doesNotMatch(s, /<\/[a-z]+\s+[^>\s]/i);
    for (const [i, line] of s.split(/\r?\n/).entries()) assert.ok(line.length <= 140, `${name}:${i + 1}`);
    if (/method="post"/.test(s)) {
      assert.match(s, /name="csrfToken" value="<c:out value="\$\{sessionScope.csrfToken\}"/);
      for (const form of s.matchAll(/<form\b[^>]*method="post"[^>]*>/g)) {
        assert.match(form[0], /action="\$\{pageContext.request.contextPath\}\/(?:admin-)?wiring"/);
      }
    }
  }
  assert.doesNotMatch(read(views + "wiring-play.jsp"), /wiringRule|\.rules|\.grade|\.explanation/);
});
test("wiring enhancement has separate scoped assets and no answers", () => {
  const js = read("assets/js/wiring.js");
  assert.doesNotMatch(js, /REQUIRED|FORBIDDEN|correctCount|\.score|rubric/i);
  assert.match(js, /pointerdown/);
  assert.match(js, /keydown/);
  assert.match(read("assets/css/wiring.css"), /\.wiring/);
  assert.match(read(views + "wiring-play.jsp"), /wiring.js\?v=/);
  assert.match(read("pages/tai-khoan.html"), /user-wiring-link/);
});

test("wiring text remains UTF-8 and guard detects malformed copied views", () => {
  const grade = read(java + "business/WiringGrade.java");
  assert.ok(grade.includes("\u0053\u01a1 \u0111\u1ed3 \u0111\u00fang to\u00e0n b\u1ed9"));
  const play = read(views + "wiring-play.jsp");
  assert.ok(play.includes("N\u1ed9p \u0111\u1ec3 ch\u1ea5m"));
  assert.throws(() => practiceViewHtmlGuard(play + '\n<select></select>', "injected"));
  assert.throws(() => practiceViewHtmlGuard(play + '\n</option value="x">', "injected"));
  assert.throws(() => practiceViewHtmlGuard('<div><form></div></form>', "injected"));
});
