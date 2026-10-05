"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const root = path.join(__dirname, "..", "..");
const javaRoot = path.join(root, "tomcat-app", "src", "main", "java", "vn", "edu", "webpro", "robotlab");
const webRoot = path.join(root, "tomcat-app", "src", "main", "webapp");
const servletPath = path.join(javaRoot, "controller", "LearningProfileServlet.java");
const statsPath = path.join(javaRoot, "data", "StatsDB.java");
const jspPath = path.join(webRoot, "WEB-INF", "views", "learning-profile.jsp");
const cssPath = path.join(root, "assets", "css", "learning-profile.css");
const jsPath = path.join(root, "assets", "js", "learning-profile.js");
const summaryJspPath = path.join(webRoot, "WEB-INF", "views", "learning-summary.jsp");
const accountPagePath = path.join(root, "pages", "tai-khoan.html");

function read(file) {
  return fs.readFileSync(file, "utf8");
}

function methodBody(source, methodName) {
  const start = source.indexOf(methodName);
  assert.notEqual(start, -1, `Không tìm thấy ${methodName}`);
  const open = source.indexOf("{", start);
  let depth = 0;
  for (let index = open; index < source.length; index += 1) {
    if (source[index] === "{") depth += 1;
    if (source[index] === "}" && --depth === 0) return source.slice(open, index + 1);
  }
  throw new Error(`Thiếu dấu đóng method ${methodName}`);
}

test("phase 5 application code only uses the techniques listed in the course slides", () => {
  function withoutComments(source) {
    return source.replace(/\/\*[\s\S]*?\*\//g, "").replace(/\/\/[^\r\n]*/g, "");
  }

  const sources = [
    "LearningProfile", "ProfileRobotEntry", "ProfileSessionStat", "ProfileQuizAttempt", "ProfileSkill"
  ].map(name => [name, read(path.join(javaRoot, "business", `${name}.java`))]);
  sources.push(["LearningProfileServlet", read(servletPath)]);
  for (const name of ["selectLearningProfileSessionStats", "selectLearningProfileQuizAttempts", "selectCompletedRobotComponentNames"]) {
    sources.push([`StatsDB.${name}`, methodBody(read(statsPath), name)]);
  }
  const forbidden = [
    /\.stream\s*\(/, /\.toList\s*\(/, /::/, /\w\s*->/, /\)\s*->/, /\bOptional\b/, /orElseThrow/,
    /\bswitch\s*\(/, /computeIfAbsent|getOrDefault/, /\.forEach\s*\(/,
    /\b(List|Set|Map)\.of\s*\(/, /\bvar\s+\w+\s*=/, /"""/,
    /java\.time|\bInstant\b|\bZoneId\b|DateTimeFormatter|\bLocalDate|\.toInstant\s*\(/,
    /\brecord\s+\w+/, /\b\w*(?:Dao|DAO|Service)\b/
  ];
  const violations = [];
  for (const [name, rawSource] of sources) {
    const source = withoutComments(rawSource);
    for (const pattern of forbidden) {
      if (pattern.test(source)) violations.push(`${name}: ${pattern}`);
    }
  }
  const jsp = withoutComments(read(jspPath));
  const allowedTags = new Set(["out", "if", "choose", "when", "otherwise", "forEach", "set"]);
  for (const tag of jsp.matchAll(/<\/?c:(\w+)\b/g)) {
    if (!allowedTags.has(tag[1])) violations.push(`JSP: c:${tag[1]}`);
  }
  if (/<\/?(?:fmt|fn):|<%(?!@)/.test(jsp)) violations.push("JSP: fmt/fn/scriptlet");
  assert.deepEqual(violations, [], "Code Đợt 5 phải bám đúng bảng kiến thức chapter/slide");
});

test("learning profile is an authenticated GET with private no-store response and a literal login redirect", () => {
  const servlet = read(servletPath);
  assert.match(servlet, /@WebServlet\("\/learning-profile"\)/);
  assert.match(servlet, /protected void doGet\(HttpServletRequest request, HttpServletResponse response\)/);
  assert.doesNotMatch(servlet, /protected void do(?:Post|Put|Delete)\(/);
  assert.match(servlet, /SessionUtil\.getCurrentUser\(request\)/);
  assert.match(servlet, /response\.setHeader\("Cache-Control",\s*"no-store"\)/);
  assert.match(servlet, /request\.getContextPath\(\)\s*\+\s*"\/pages\/tai-khoan\.html"/);
  assert.doesNotMatch(servlet, /getParameter\s*\(/,
    "Chủ sở hữu hồ sơ chỉ được lấy từ HttpSession, không từ query/form parameters");
  assert.match(servlet, /SC_SERVICE_UNAVAILABLE/);
  assert.match(servlet, /learning-profile\.jsp/);
  assert.match(servlet, /String url = "\/WEB-INF\/views\/learning-profile\.jsp";/);
  assert.match(servlet, /getServletContext\(\)\.getRequestDispatcher\(url\)\.forward\(request, response\)/);
});

test("profile facts are fetched in a fixed number of prepared, user-scoped queries without cross-joining session and quiz rows", () => {
  const stats = read(statsPath);
  const sessionQuery = methodBody(stats, "selectLearningProfileSessionStats");
  const quizQuery = methodBody(stats, "selectLearningProfileQuizAttempts");
  const componentQuery = methodBody(stats, "selectCompletedRobotComponentNames");
  assert.match(sessionQuery, /WHERE\s+s\.user_id\s*=\s*\?/);
  assert.match(sessionQuery, /GROUP BY\s+s\.robot_id\s*,\s*s\.status/);
  assert.match(sessionQuery, /prepareStatement/);
  assert.match(quizQuery, /WHERE\s+a\.user_id\s*=\s*\?/);
  assert.match(quizQuery, /prepareStatement/);
  assert.match(componentQuery, /WHERE\s+s\.user_id\s*=\s*\?/);
  assert.match(componentQuery, /SELECT\s+DISTINCT/i);
  assert.match(componentQuery, /prepareStatement/);
  assert.doesNotMatch(sessionQuery, /quiz_attempts/);
  assert.doesNotMatch(quizQuery, /assembly_sessions/);
  assert.doesNotMatch(componentQuery, /quiz_attempts/);
});

test("profile JSP escapes dynamic text and uses only profile fields, never personal identifiers or purchase data", () => {
  const jsp = read(jspPath);
  assert.match(jsp, /<html lang="vi">/);
  assert.match(jsp, /<h1>Hồ sơ học tập Robot Assembly Lab<\/h1>/);
  assert.match(jsp, /<title>\s*Hồ sơ học tập - <c:out value="\$\{profile\.fullName\}"\/>/);
  for (const expression of [
    "${profile.fullName}", "${profile.generatedAtDisplay}", "${profile.completionSummary}",
    "${profileRobot.robot.name}", "${profileRobot.robot.summary}", "${profileRobot.statusLabel}",
    "${profileRobot.completionDate}", "${profileRobot.latestSessionStatusLabel}",
    "${skillLine.content}", "${skillLine.evidence}"
  ]) {
    assert.ok(jsp.includes(`<c:out value="${expression}"/>`), `Thiếu c:out cho ${expression}`);
  }
  const withoutEscapedOutputTags = jsp.replace(/<c:out\b[^>]*\/>/g, "");
  for (const match of withoutEscapedOutputTags.matchAll(/\$\{([^}]+)\}/g)) {
    const expression = match[1];
    if (expression.includes("pageContext.request.contextPath")) continue;
    const owningTag = withoutEscapedOutputTags.slice(0, match.index)
      .split("<").at(-1) || "";
    assert.match(owningTag, /\b(?:test|items)="[^\"]*$/,
      `EL chỉ được dùng làm điều kiện/danh sách nếu không được escape: ${expression}`);
  }
  assert.doesNotMatch(jsp, /\$\{\s*(?:user\.(?:id|email|role|sessionVersion)|profile\.(?:userId|email|role|sessionVersion|password))\b/);
  assert.doesNotMatch(jsp, /\$\{[^}]*order|shop_|purchase/i);
  assert.doesNotMatch(jsp, /202\d-\d\d-\d\dT\d\d:\d\d:\d\dZ/,
    "JSP không in timestamp UTC thô");
  assert.match(jsp, /<h2[^>]*>[^<]*Tổng quan/);
  assert.match(jsp, /<h2[^>]*>[^<]*Kết quả theo từng mẫu robot/);
  assert.match(jsp, /<h2[^>]*>[^<]*Kỹ năng và kiến thức đã thực hành/);
  assert.match(jsp, /<h2[^>]*>[^<]*Nhận xét\/xác nhận của giảng viên/);
  assert.match(jsp, /<noscript>[\s\S]*Ctrl\+P/);
  assert.match(jsp, /window\.print|learning-profile\.js\?v=\d{8}\.\d+/);
});

test("profile presents explicit empty states, real quiz fractions, and the documented average formula", () => {
  const jsp = read(jspPath);
  assert.match(jsp, /empty profile\.robotEntries|empty profileRobot/);
  assert.match(jsp, /empty profile\.skillLines/);
  assert.match(jsp, /Chưa có dữ liệu/);
  assert.match(jsp, /quizDataAvailable|hasQuizAttempts/);
  assert.match(jsp, /<c:when test="\$\{profileRobot\.completed\}">/);
  assert.match(jsp, /<c:when test="\$\{profileRobot\.inProgress or profileRobot\.stopped\}">/);
  assert.doesNotMatch(jsp, /profileRobot\.statusKey/);
  assert.match(jsp, /bestQuizScore/);
  assert.match(jsp, /bestQuizTotalQuestions/);
  assert.match(jsp, /Ghi chú|Công thức/);
  assert.match(jsp, /lượt tốt nhất|tỷ lệ cao nhất/i);
  assert.doesNotMatch(jsp, /<td>\d+<\/td>|<dd>\d+<\/dd>/);
});

test("print stylesheet is page-local, A4 portrait, readable without color, and protects table/robot blocks", () => {
  const css = read(cssPath);
  const globalCss = read(path.join(root, "assets", "css", "server-view.css"));
  assert.match(css, /@page\s*\{[^}]*size:\s*A4\s+portrait/i);
  assert.match(css, /@page\s*\{[^}]*margin:\s*16mm/i);
  assert.match(css, /@media\s+print/);
  assert.match(css, /\.learning-profile\s+\.no-print[^}]*display:\s*none/i);
  assert.match(css, /\.learning-profile\s+thead\s*\{[^}]*display:\s*table-header-group/i);
  assert.match(css, /break-inside:\s*avoid/i);
  assert.match(css, /overflow-wrap:\s*anywhere/i);
  assert.match(css, /\.learning-profile/);
  assert.doesNotMatch(globalCss, /@media\s+print/);
});

test("print button uses the small dedicated script and both existing entry points link to the profile", () => {
  const jsp = read(jspPath);
  const script = read(jsPath);
  const account = read(accountPagePath);
  const summary = read(summaryJspPath);
  assert.match(jsp, /id="print-profile"/);
  assert.match(script, /window\.print\(\)/);
  assert.match(script, /print-profile/);
  assert.match(account, /href="\.\.\/learning-profile"[^>]*>Hồ sơ học tập \(in\/PDF\)</);
  assert.match(account, /account-session-actions/);
  assert.match(summary, /href="\$\{pageContext\.request\.contextPath\}\/learning-profile"[^>]*>Hồ sơ học tập/);
});

test("the learning profile adds no schema migration", () => {
  const migrationDir = path.join(root, "database", "migrations");
  // Đợt 6 có migration riêng cho nhiệm vụ; hồ sơ học tập vẫn không thêm bảng.
  const phaseFiveMigrations = fs.readdirSync(migrationDir).filter(name => (/^00[9-9]|^0[1-9]\d/.test(name)) && name !== "009_practice_tasks.sql" && name !== "010_diagnosis_practice.sql" && name !== "011_wiring_practice.sql");
  assert.deepEqual(phaseFiveMigrations, [], "Đợt 5 dùng dữ liệu đang có, không thêm migration");
});
