"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const javaRoot = path.join(
  __dirname, "..", "..", "tomcat-app", "src", "main", "java", "vn", "edu", "webpro", "robotlab"
);
const root = path.join(__dirname, "..", "..");
const summaryServlet = fs.readFileSync(path.join(javaRoot, "controller", "LearningSummaryServlet.java"), "utf8");
const adminStatsServlet = fs.readFileSync(path.join(javaRoot, "controller", "AdminStatsServlet.java"), "utf8");
const statsDB = fs.readFileSync(path.join(javaRoot, "data", "StatsDB.java"), "utf8");
const summaryJsp = fs.readFileSync(path.join(root, "tomcat-app", "src", "main", "webapp",
  "WEB-INF", "views", "learning-summary.jsp"), "utf8");
const adminStatsJsp = fs.readFileSync(path.join(root, "tomcat-app", "src", "main", "webapp",
  "WEB-INF", "views", "admin-stats.jsp"), "utf8");

test("personal learning summary always scopes queries to the logged-in user's own id, never a request parameter", () => {
  assert.match(summaryServlet, /SessionUtil\.getCurrentUser\(request\)/);
  assert.doesNotMatch(summaryServlet, /getParameter\("userId"\)|getParameter\("user"\)/,
    "Không được nhận userId từ query string — chỉ dùng user đang đăng nhập");
  assert.match(summaryServlet, /StatsDB\.countSessionsByStatusForUser\(user\.getId\(\)\)/);
  assert.match(summaryServlet, /StatsDB\.selectCompletedRobots\(user\.getId\(\)\)/);
  assert.match(summaryServlet, /StatsDB\.selectQuizScoresByRobot\(user\.getId\(\)\)/);
});

test("admin stats page rejects non-admin accounts with 403, and anonymous visitors are redirected to login", () => {
  assert.match(adminStatsServlet, /if \(user == null\)[\s\S]{0,80}sendRedirect/);
  assert.match(adminStatsServlet, /if \(!user\.isAdmin\(\)\)[\s\S]{0,60}SC_FORBIDDEN/);
});

test("every aggregate is computed by MySQL GROUP BY / COUNT DISTINCT in one query, not summed across rows in Java", () => {
  assert.match(statsDB, /GROUP BY status/);
  assert.match(statsDB, /COUNT\(DISTINCT user_id\) AS total FROM \(/,
    "Số người có hoạt động phải gộp bằng UNION + COUNT DISTINCT để không đếm trùng một tài khoản có cả hai loại hoạt động");
  assert.match(statsDB, /UNION/);
  assert.match(statsDB, /GROUP BY r\.id, r\.name/);
  assert.doesNotMatch(statsDB, /for \(.*rs\.next\(\)\)[\s\S]{0,100}\+\+|running[Tt]otal/,
    "Không nên có vòng lặp Java tự cộng dồn số liệu — để MySQL tính bằng GROUP BY/COUNT");
});

test("the high-miss-rate question ranking excludes questions with too few attempts to be meaningful", () => {
  assert.match(statsDB, /HAVING COUNT\(\*\) >= \?/);
  assert.match(adminStatsServlet, /MIN_QUESTION_ATTEMPTS = 3/);
  assert.match(adminStatsServlet, /StatsDB\.selectHighMissRateQuestions\(MIN_QUESTION_ATTEMPTS, TOP_QUESTIONS_LIMIT\)/);
});

test("both statistics JSPs render every number from a request attribute — none are hardcoded literals", () => {
  for (const jsp of [summaryJsp, adminStatsJsp]) {
    // Không có số nguyên trần đứng một mình làm nội dung hiển thị (ngoại trừ % literal đi kèm EL).
    assert.doesNotMatch(jsp, /<td>\d+<\/td>|<dd>\d+<\/dd>/,
      "Số liệu hiển thị phải luôn tới từ ${...} do servlet gán, không phải số cứng trong JSP");
  }
  assert.match(summaryJsp, /\$\{totalSessions\}/);
  assert.match(summaryJsp, /\$\{sessionCounts\.COMPLETED\}/);
  assert.match(adminStatsJsp, /\$\{totalLearners\}/);
  assert.match(adminStatsJsp, /\$\{activeLearners\}/);
});

test("both statistics JSPs handle the empty-data case explicitly instead of showing a blank table", () => {
  assert.match(summaryJsp, /empty completedRobots/);
  assert.match(summaryJsp, /empty quizScores/);
  assert.match(adminStatsJsp, /empty popularRobots/);
  assert.match(adminStatsJsp, /empty quizAggregates/);
  assert.match(adminStatsJsp, /empty highMissQuestions/);
});
