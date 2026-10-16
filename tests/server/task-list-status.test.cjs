"use strict";
const test = require("node:test"), assert = require("node:assert/strict");
const fs = require("node:fs"), path = require("node:path");
const root = path.join(__dirname, "../..");
const java = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
const read = file => fs.readFileSync(path.join(root, file), "utf8");
test("task list recipient metadata uses one owner-scoped query and existing latest submission/review order", () => {
  const db = read(java + "data/PracticeTaskDB.java");
  const method = db.match(/public static Map<Long, TaskRecipient> selectListRecipients[\s\S]*?\n    \}/)?.[0];
  assert.ok(method, "A batch reader is required, never selectRecipients inside a task loop");
  assert.equal((method.match(/executeQuery\(/g) || []).length, 1);
  assert.match(method, /MAX\(sx.submission_no\)/);
  assert.match(method, /MAX\(rx.id\)/);
  for (const predicate of ["own_submission.user_id = ?", "own_review.user_id = ?", "tr.user_id = ?"])
    assert.ok(method.includes(predicate), predicate);
  for (const index of [1, 2, 3]) assert.ok(method.includes(`ps.setLong(${index}, userId)`));
  assert.doesNotMatch(method, /hydrate\(|TaskSubmissionDB\.latest|selectRecipients\(|executeUpdate\(|FOR UPDATE/);
  assert.match(method, /DBUtil.closeResultSet\(rs\)/);
  assert.match(method, /finally\s*\{/);
  const metadata = db.slice(db.indexOf("static TaskRecipient mapListRecipient"), db.indexOf("static TaskRound round"));
  assert.doesNotMatch(metadata, /\.stream\(|::|\bswitch\s*\(|\bvar\s|java\.time|\bOptional\b|(?:List|Map)\.of\(/);
  assert.ok(metadata.split(/\r?\n/).every(line => [...line].length <= 120));
});
test("task list obtains status from TaskRecipient and servlet never accepts a URL owner or changes POST", () => {
  const servlet = read(java + "controller/TaskServlet.java");
  const get = servlet.slice(servlet.indexOf("protected void doGet"), servlet.indexOf("protected void doPost"));
  assert.match(get, /setAttribute\("taskListRecipients",/);
  assert.match(get, /selectListRecipients\(user.getId\(\), TaskFormUtil.text\(request, "state"\)\)/);
  assert.doesNotMatch(get, /getParameter\("userId"\)|statusLabel\(|setConclusion\(/);
  assert.doesNotMatch(servlet.slice(servlet.indexOf("protected void doPost")), /taskListRecipients|selectListRecipients/);
  const jsp = read("tomcat-app/src/main/webapp/WEB-INF/views/task-list.jsp");
  assert.match(jsp, /taskListRecipients\[task.id\]/);
  assert.match(jsp, /<c:out value="\$\{taskListRecipient.statusLabel\}"/);
  assert.doesNotMatch(jsp, /(?:totalPoints|quizScore)\s*(?:gt|ge|>|&gt;)|NEEDS_REVISION|SUBMITTED/);
  assert.doesNotMatch(jsp, /<%(?!@)|<fmt:|<fn:|<(?:select|option)\b/);
  assert.ok(jsp.split(/\r?\n/).every(line => [...line].length <= 140));
});
