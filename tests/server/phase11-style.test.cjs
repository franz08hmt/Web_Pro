const test = require("node:test"), assert = require("node:assert/strict"), fs = require("node:fs");
const root = require("node:path").resolve(__dirname, "../.."), read = f => fs.readFileSync(root + "/" + f, "utf8");
const java = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
const files = ["business/DashboardCard", "business/DashboardSummary", "business/ComponentContext",
  "business/DesignCartResult", "business/RobotLearningLink", "business/CourseMapItem",
  "controller/AccountPageServlet", "controller/ArchitectureServlet", "controller/DesignCartServlet",
  "controller/LearningLinkServlet", "data/LearningLinkDB", "filter/PageErrorFilter", "filter/AdminPageFilter",
  "filter/WorkspaceCacheFilter"];
test("Phase11 Java keeps traditional syntax and new JSP fragments stay presentation-only", () => {
  for (const file of files) {
    const source = read(java + file + ".java");
    const code = source.replace(/\/\*[\s\S]*?\*\/|\/\/[^\n]*|"(?:\\.|[^"\\])*"/g, "");
    assert.doesNotMatch(code, /\.stream\s*\(|->|::|\b(?:switch|Optional|var|record)\b|java\.time/);
    assert.doesNotMatch(code, /\b(?:List|Set|Map)\.of|computeIfAbsent|getOrDefault|\.forEach\s*\(/);
    source.split(/\r?\n/).forEach((s,i) => assert.ok(s.length <= 120, file + ":" + (i+1)));
  }
  for (const file of ["dashboard.jsp", "architecture.jsp", "site-nav.jspf", "site-nav-assets.jspf",
    "workspace-breadcrumb.jspf", "error-404.jsp", "error-500.jsp"]) {
    const s = read("tomcat-app/src/main/webapp/WEB-INF/views/" + file);
    assert.doesNotMatch(s, /<%(?!@)|<fmt:|<fn:|<select\b|<option\b/);
    s.split(/\r?\n/).forEach((line,i) => assert.ok(line.length <= 140, file + ":" + (i+1)));
  }
});
