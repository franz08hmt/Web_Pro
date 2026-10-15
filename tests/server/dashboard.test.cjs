const test = require("node:test"), assert = require("node:assert/strict"), fs = require("node:fs");
const root = require("node:path").resolve(__dirname, "../.."), read = f => fs.readFileSync(root + "/" + f, "utf8");
const java = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
test("dashboard reads owned data and uses the Servlet request/forward flow", () => {
  const s = read(java + "controller/AccountPageServlet.java");
  assert.match(s, /SessionUtil.getCurrentUser\(request\)/);
  assert.match(s, /setHeader\("Cache-Control", "no-store"\)/);
  assert.match(s, /setAttribute\("dashboard", dashboard\)/);
  assert.match(s, /\/WEB-INF\/views\/dashboard.jsp/);
  for (const db of ["AssemblySessionDB", "QuizAttemptDB", "PracticeTaskDB", "DesignDB", "UserKitDB", "OrderDB"]) {
    assert.match(s, new RegExp(db + "\\.[^(]+\\(user.getId\\(\\)"));
  }
  const jsp = read("tomcat-app/src/main/webapp/WEB-INF/views/dashboard.jsp");
  assert.doesNotMatch(jsp, /<%(?!@)|<fmt:|<fn:|<select\b/);
  assert.match(jsp, /<c:out value="\$\{dashboardCard.summary\}"/);
  const db = read(java + "data/AssemblySessionDB.java");
  assert.match(db, /WHERE user_id = \? AND [\s\S]*LIMIT 1/);
});
