"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const javaRoot = path.join(
  __dirname, "..", "..", "tomcat-app", "src", "main", "java", "vn", "edu", "webpro", "robotlab"
);
const quizQuestion = fs.readFileSync(path.join(javaRoot, "business", "QuizQuestion.java"), "utf8");
const quizOption = fs.readFileSync(path.join(javaRoot, "business", "QuizOption.java"), "utf8");
const quizServlet = fs.readFileSync(path.join(javaRoot, "controller", "QuizServlet.java"), "utf8");
const adminQuizServlet = fs.readFileSync(path.join(javaRoot, "controller", "AdminQuizServlet.java"), "utf8");
const quizAttemptDB = fs.readFileSync(path.join(javaRoot, "data", "QuizAttemptDB.java"), "utf8");
const root = path.join(__dirname, "..", "..");

test("public quiz question/option JSON never exposes the correct answer", () => {
  assert.match(quizOption, /public String toPublicJson\(\)\s*\{\s*return "\{\\"id\\":"[\s\S]*?\+\s*",\\"label\\":"/);
  assert.doesNotMatch(
    quizOption.match(/public String toPublicJson\(\)[\s\S]*?\n    \}/)[0],
    /isCorrect/
  );
  assert.match(quizQuestion, /public String toPublicJson\(\)/);
  assert.doesNotMatch(quizQuestion, /toPublicJson\(\)[\s\S]*?explanation[\s\S]*?\n    \}/);

  // Servlet công khai phải gọi toPublicJson(), không phải toJson() (bản đủ đáp án).
  assert.match(quizServlet, /question\.toPublicJson\(\)/);
  assert.doesNotMatch(quizServlet.match(/private void sendQuestions[\s\S]*?\n    \}/)[0], /\.toJson\(\)/);
});

test("submitting an attempt never trusts a client-sent score — grading reads real options from the database", () => {
  assert.doesNotMatch(quizServlet, /stringField\([^)]*"score"/, "Servlet không được đọc điểm số từ request");
  assert.doesNotMatch(quizServlet, /stringField\([^)]*"isCorrect"/, "Servlet không được đọc đúng/sai từ request");
  assert.match(quizServlet, /QuizAttemptDB\.submitAttempt\(user\.getId\(\), robotId, answers\)/);

  assert.match(quizAttemptDB, /List<QuizQuestion> questions = QuizQuestionDB\.selectQuestionsByRobot\(robotId\)/);
  assert.match(quizAttemptDB, /boolean isCorrect = question\.isCorrectOption\(optionId\)/);
  assert.match(quizAttemptDB, /if \(isCorrect\) score\+\+/);
});

test("quiz submission rejects duplicate question ids and requires every question answered exactly once", () => {
  assert.match(quizAttemptDB, /if \(seenQuestionIds\.contains\(answer\[0\]\)\)/);
  assert.match(quizAttemptDB, /seenQuestionIds\.add\(answer\[0\]\)/);
  assert.match(quizAttemptDB, /throw new IllegalArgumentException\("duplicate-question-"/);
  assert.match(quizAttemptDB, /submittedAnswers\.size\(\) != questions\.size\(\)/);
});

test("quiz submission validates that each option actually belongs to its question", () => {
  assert.match(quizAttemptDB, /QuizOption selected = question\.findOption\(optionId\)/);
  assert.match(quizAttemptDB, /if \(selected == null\)/);
});

test("historical attempt answers are snapshotted at submission time, not re-joined from live quiz content", () => {
  assert.match(quizAttemptDB, /graded\.setQuestionPromptSnapshot\(question\.getPrompt\(\)\)/);
  assert.match(quizAttemptDB, /graded\.setSelectedOptionLabelSnapshot\(selected\.getLabel\(\)\)/);
  assert.match(quizAttemptDB, /graded\.setCorrectOptionLabelSnapshot\(/);
  assert.match(quizAttemptDB, /graded\.setExplanationSnapshot\(question\.getExplanation\(\)\)/);

  // selectAttempt() đọc lại lịch sử phải lấy từ quiz_attempt_answers (bảng snapshot),
  // không JOIN sang quiz_questions/quiz_options.
  const selectAttemptBody = quizAttemptDB.match(/public static QuizAttempt selectAttempt[\s\S]*?\n    \}/)[0];
  assert.match(selectAttemptBody, /FROM quiz_attempt_answers WHERE attempt_id = \?/);
  assert.doesNotMatch(selectAttemptBody, /JOIN quiz_questions|JOIN quiz_options/);
});

test("quiz history and detail endpoints are scoped to the logged-in user", () => {
  assert.match(quizServlet, /QuizAttemptDB\.selectAttempts\(user\.getId\(\), robotId/);
  assert.match(quizServlet, /QuizAttemptDB\.selectAttempt\(Long\.parseLong\(rawId\), user\.getId\(\)\)/);
  assert.match(quizAttemptDB, /FROM quiz_attempts WHERE id = \? AND user_id = \?/);
});

test("admin quiz CRUD requires an ADMIN account and CSRF on every write", () => {
  assert.match(adminQuizServlet, /SessionUtil\.requireAdmin\(request, response\)/);
  for (const method of ["save", "doDelete"]) {
    const match = adminQuizServlet.match(new RegExp(`(private|protected) void ${method}\\([\\s\\S]*?\\n    \\}`));
    assert.ok(match, `Không tìm thấy phương thức ${method} trong AdminQuizServlet`);
    assert.match(match[0], /SessionUtil\.hasValidCsrfToken\(request, response\)/, `${method} phải kiểm tra CSRF`);
  }
});

test("a question with existing quiz attempts cannot be deleted (foreign key protects historical data)", () => {
  assert.match(adminQuizServlet, /RELATION_CONFLICT/);
  assert.match(adminQuizServlet, /catch \(SQLException e\) \{[\s\S]*?RELATION_CONFLICT/);
});

test("quiz seed data exists for every current robot model and is additive-only SQL", () => {
  const seed = fs.readFileSync(path.join(root, "database", "seed-quiz.sql"), "utf8");
  assert.doesNotMatch(seed, /DROP\s|TRUNCATE|DELETE FROM|UPDATE\s+users|UPDATE\s+robots/i);
  for (const robotId of ["line-follower", "obstacle-avoider", "mini-arm"]) {
    const hex = Buffer.from(robotId, "utf8").toString("hex").toUpperCase();
    assert.ok(seed.toUpperCase().includes(hex), `Thiếu dữ liệu quiz cho robot ${robotId}`);
  }
});
