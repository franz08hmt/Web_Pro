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
test("dashboard decisions live in pure JavaBeans and reads avoid N+1 and unbounded histories", () => {
  const servlet = read(java + "controller/AccountPageServlet.java");
  assert.doesNotMatch(servlet, /for\s*\(|addCard\(|setSummary\(|setAction\(|countAttempts\(|countQuestions\(/);
  assert.match(servlet, /dashboard.addAssemblyCards/);
  assert.match(servlet, /dashboard.addQuizCard/);
  assert.match(servlet, /dashboard.addKitCard/);
  const bean = read(java + "business/DashboardSummary.java");
  const beanCode = bean.replace(/\/\*[\s\S]*?\*\/|\/\/[^\n]*/g, "");
  assert.doesNotMatch(beanCode, /HttpServletRequest|java.sql|ConnectionPool|\w+DB\./);
  assert.match(bean, /candidate.getAttemptCount\(\) == 0 && candidate.getQuestionCount\(\) > 0/);
  const data = read(java + "data/StatsDB.java");
  const query = data.slice(data.indexOf("selectDashboardQuizCandidates"), data.indexOf("countSessionsByStatusForUser"));
  assert.match(query, /WHERE user_id = \? AND status = 'COMPLETED'/);
  assert.match(query, /WHERE user_id = \? GROUP BY robot_id/);
  assert.match(query, /ORDER BY r.name,r.id/);
  assert.equal((query.match(/executeQuery\(/g) || []).length, 1);
  const designs = read(java + "data/DesignDB.java");
  assert.match(designs, /selectLatestDesign[\s\S]*WHERE d.user_id = \? ORDER BY d.id DESC LIMIT 1/);
  const tasks = read(java + "data/PracticeTaskDB.java");
  assert.match(tasks, /selectDashboardCounts[\s\S]*WHERE tr.user_id = \? AND t.state = 'OPEN'/);
  for (const file of ["business/DashboardQuizCandidate.java", "controller/ShopProductServlet.java"]) {
    const s = read(java + file);
    assert.doesNotMatch(s, /\.stream\s*\(|->|::|\b(?:switch|Optional|var|record)\b|java\.time/);
    s.split(/\r?\n/).forEach((line, i) => assert.ok(line.length <= 120, file + ":" + (i + 1)));
  }
});
