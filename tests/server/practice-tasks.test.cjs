"use strict";
const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const os = require("node:os");
const root = path.resolve(__dirname, "../..");
const java = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
const read = file => fs.readFileSync(path.join(root,file), "utf8");
const javaFiles = ["business/DiagnosisScenario", "business/DiagnosisCheck", "business/DiagnosisOption", "business/DiagnosisAttempt", "data/DiagnosisDB", "controller/DiagnosisServlet", "controller/AdminDiagnosisServlet", "util/DiagnosisFormUtil", "business/PracticeTask", "business/TaskRecipient", "business/TaskRound", "business/TaskSubmission", "business/TaskReview", "business/TaskRubric", "data/PracticeTaskDB", "data/TaskSubmissionDB", "controller/TaskServlet", "controller/AdminTaskServlet", "controller/AdminTaskReviewServlet"];
const forbidden = /\.stream\s*\(|\.toList\s*\(|::|\w\s*->|\bOptional\b|orElseThrow|\bswitch\s*\(|computeIfAbsent|getOrDefault|\.forEach\s*\(|\b(List|Set|Map)\.of\s*\(|\bvar\s+\w+\s*=|"""|java\.time|\bInstant\b|\bZoneId\b|DateTimeFormatter|\bLocalDate|\brecord\s/;
// Giữ vị trí/dòng nhưng bỏ literal và comment để guard không đọc nhầm SQL hoặc chữ hiển thị.
function codeOnly(source) {
  return source.replace(/"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\/\*[\s\S]*?\*\/|\/\/[^\n]*/g,
    value => value.replace(/[^\r\n]/g, " "));
}
function methodSource(source, name) {
  const code = codeOnly(source);
  const match = new RegExp("\\b" + name + "\\s*\\(").exec(code);
  assert.ok(match, name);
  const start = source.lastIndexOf("\n", match.index) + 1;
  let end = code.indexOf("{", match.index), depth = 1;
  while (depth && ++end < code.length) {
    if (code[end] === "{") depth++;
    if (code[end] === "}") depth--;
  }
  return source.slice(start, end + 1);
}
function formattingJavaSources() {
  const sources = javaFiles.concat(["util/TaskFormUtil"]).map(file => [file, read(java + file + ".java")]);
  const quiz = read(java + "data/QuizAttemptDB.java");
  sources.push(["data/QuizAttemptDB (Đợt 6)", quiz.slice(quiz.indexOf("private static QuizAttempt gradeAnswers"),
    quiz.indexOf("public static QuizAttempt selectAttempt"))]);
  sources.push(["data/UserDB.selectLearners", methodSource(read(java + "data/UserDB.java"), "selectLearners")]);
  sources.push(["util/SessionUtil.hasValidFormCsrfToken",
    methodSource(read(java + "util/SessionUtil.java"), "hasValidFormCsrfToken")]);
  return sources;
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
test("phase 6 Java and JSP formatting remains readable", () => {
  for (const [name, source] of formattingJavaSources()) formattingGuard(source, name);
  const views = fs.readdirSync(path.join(root, "tomcat-app/src/main/webapp/WEB-INF/views"))
    .filter(name => /^(task-|admin-task-|diagnosis-|admin-diagnosis-)/.test(name));
  for (const name of views) {
    read("tomcat-app/src/main/webapp/WEB-INF/views/" + name).split(/\r?\n/).forEach((line, index) => {
      assert.ok(line.length <= 140, `${name}:${index + 1}: ${line.length}`);
    });
  }
  assert.throws(() => formattingGuard("if(x) return;", "copy"));
  assert.throws(() => formattingGuard("first(); second();", "copy"));
  assert.throws(() => formattingGuard("x".repeat(121), "copy"));
});
test("unsupported GET task actions are validation errors instead of not-found errors", () => {
  for (const name of ["TaskServlet", "AdminTaskServlet", "AdminTaskReviewServlet"]) {
    const source = methodSource(read(java + "controller/" + name + ".java"), "doGet");
    assert.doesNotMatch(source, /else\s*(?:\{\s*)?response\.sendError\((?:404|[^)]*SC_NOT_FOUND)\)/);
    assert.match(source, /Thao tác không hợp lệ/);
  }
});
test("current round quiz labels are controlled by the recipient bean only", () => {
  for (const view of ["task-view", "task-submit"]) {
    assert.match(read("tomcat-app/src/main/webapp/WEB-INF/views/" + view + ".jsp"),
      /<c:if test="\$\{recipient\.showCurrentRoundQuiz and not empty taskQuiz\}">/);
  }
  assert.match(read("tomcat-app/src/main/webapp/WEB-INF/views/task-view.jsp"), /taskSubmission\.reusedQuiz/);
});
function guard(source) { assert.doesNotMatch(source.replace(/\/\*[\s\S]*?\*\//g,"").replace(/\/\/[^\n]*/g,""), forbidden); }
test("practice task code uses only course slide syntax and guard catches injected violations", () => {
  for (const file of javaFiles.concat(["util/TaskFormUtil"])) guard(read(java + file + ".java"));
  const quizDb = read(java + "data/QuizAttemptDB.java");
  guard(quizDb.slice(quizDb.indexOf("private static QuizAttempt gradeAnswers"), quizDb.indexOf("public static QuizAttempt selectAttempt")));
  guard(read(java + "util/SessionUtil.java").split("public static boolean hasValidFormCsrfToken")[1].split("public static")[0]);
  guard(read(java + "data/UserDB.java").split("public static List<User> selectLearners")[1].split("private static")[0]);
  const temporary = fs.mkdtempSync(path.join(os.tmpdir(), "practice-task-guard-"));
  try {
    const copy = path.join(temporary, "TaskRubric.java");
    fs.writeFileSync(copy, read(java + "business/TaskRubric.java") + "\nclass Injected { void example() { items.stream(); } }");
    assert.throws(() => guard(fs.readFileSync(copy, "utf8")));
    fs.writeFileSync(copy, read(java + "business/TaskRubric.java") + "\n//" + "x".repeat(121));
    assert.throws(() => formattingGuard(fs.readFileSync(copy, "utf8"), "long-line copy"));
  } finally { fs.rmSync(temporary, {recursive: true, force: true}); }
  const views = fs.readdirSync(path.join(root, "tomcat-app/src/main/webapp/WEB-INF/views")).filter(n => /^(task-|admin-task-|diagnosis-|admin-diagnosis-)/.test(n));
  assert.ok(views.length >= 5);
  for(const view of views) {
    const source = read("tomcat-app/src/main/webapp/WEB-INF/views/" + view);
    assert.doesNotMatch(source, /<fmt:|<fn:|<%(?!@)/);
    for(const tag of source.matchAll(/<c:(\w+)/g)) assert.ok(["out","if","choose","when","otherwise","forEach","set"].includes(tag[1]));
    assert.doesNotMatch(source, /\.state\s*(?:eq|==)/);
  }
});
test("new JSP forms escape hidden fields and run without JavaScript dependencies", () => {
 const views = fs.readdirSync(path.join(root, "tomcat-app/src/main/webapp/WEB-INF/views")).filter(n => /^(task-|admin-task-|diagnosis-|admin-diagnosis-)/.test(n));
 for(const view of views) {
   const source=read("tomcat-app/src/main/webapp/WEB-INF/views/"+view);
   assert.doesNotMatch(source, /<script\b/i);
   if (/method="post"/.test(source)) {
     assert.match(source, /name="csrfToken" value="<c:out value="\$\{sessionScope.csrfToken\}"/);
     assert.match(source, /name="action"/);
   }
   assert.doesNotMatch(source, />\s*\$\{(?:task|taskSubmission|taskReview|learner|recipient)\./);
 }
});
test("task quiz locks one attempt and practice API keeps its original overload", () => {
 const source=read(java+"data/QuizAttemptDB.java");
 assert.match(source,/round.getQuizAttemptId\(\) != 0/);
 assert.match(source,/submitAttempt\(long userId, String robotId, List<String\[\]> submittedAnswers\)/);
 assert.match(source,/submitAttempt\(\s*long userId, String robotId, List<String\[\]> answers, long roundId\)/);
 assert.match(source,/QuizAttempt graded = gradeAnswers\(robotId,/);
 assert.match(source,/INSERT INTO quiz_attempt_answers/);
 assert.match(read(java+"controller/TaskServlet.java"), /recipient.getCurrentRound\(\).isHasQuizAttempt\(\)/);
 assert.match(read(java+"controller/TaskServlet.java"), /recipient.ownsRound\(roundId\)/);
});
test("task controllers protect identity roles and form CSRF", () => {
 for (const name of ["TaskServlet","AdminTaskServlet","AdminTaskReviewServlet"]) {
   const source=read(java+"controller/"+name+".java");
   assert.match(source,/SessionUtil.getCurrentUser/);
   assert.match(source,/hasValidFormCsrfToken/);
   assert.match(source,/no-store/);
   assert.match(source,/String url/);
 }
 assert.doesNotMatch(read(java+"controller/TaskServlet.java"),/getParameter\("userId"\)/);
 assert.match(read(java+"controller/AdminTaskServlet.java"),/isAdmin\(\)/);
 assert.match(read(java+"controller/AdminTaskReviewServlet.java"),/isAdmin\(\)/);
 assert.match(read(java+"controller/AdminTaskServlet.java"),/getParameterValues\("recipientId"\)/);
});
test("new schema is additive and keeps unique evidence and round constraints",()=>{
 const migration=read("database/migrations/009_practice_tasks.sql");
 assert.doesNotMatch(migration,/\b(DROP|TRUNCATE|UPDATE|DELETE FROM)\b/i);
 for(const table of ["practice_tasks","task_recipients","task_rounds","task_submissions","task_reviews"]) assert.match(migration,new RegExp("CREATE TABLE (?:IF NOT EXISTS )?"+table));
 assert.match(migration,/UNIQUE\s*\(task_id, user_id\)/);
 assert.match(migration,/UNIQUE\s*\(recipient_id, round_no\)/);
 assert.match(migration,/quiz_attempt_id[^\n]*UNIQUE/);
 assert.match(migration,/UNIQUE\s*\(round_id\)/);
 assert.match(migration,/ON DELETE RESTRICT/);
});
test("submission transactions lock owners and never accept client totals",()=>{
 const source=read(java+"data/TaskSubmissionDB.java");
 assert.match(source,/setAutoCommit\(false\)/);
 assert.match(source,/FOR UPDATE/);
 assert.match(source,/user_id = \?/);
 assert.match(source,/prepareStatement/);
 assert.match(source,/rollback\(/);
 const jsp=read("tomcat-app/src/main/webapp/WEB-INF/views/task-quiz.jsp");
 assert.doesNotMatch(jsp,/quizQuestion\.(?:explanation|correctOption)|quizOption\.correct|data-correct|isCorrect/);
 assert.match(jsp,/type="radio"/);
 assert.match(jsp,/c:out/);
});
