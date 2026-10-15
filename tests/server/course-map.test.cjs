const test = require("node:test"), assert = require("node:assert/strict"), fs = require("node:fs");
const root = require("node:path").resolve(__dirname, "../.."), read = f => fs.readFileSync(root + "/" + f, "utf8");
test("public course map connects verified source locations to real features", () => {
  const s = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/ArchitectureServlet.java");
  assert.match(s, /setAttribute\("courseItems", items\)/);
  assert.match(s, /getRequestDispatcher\("\/WEB-INF\/views\/architecture.jsp"\).forward/);
  assert.doesNotMatch(s, /requireUser|Chapter \d|slide \d/i);
  const refs = [...s.matchAll(/"((?:tomcat-app|assets)\/[^"\n]+)", (\d+), (?:true|false),/g)];
  assert.ok(refs.length >= 15);
  for (const [,file,line] of refs) assert.ok(Number(line) <= read(file).split(/\r?\n/).length, file + ":" + line);
  const jsp = read("tomcat-app/src/main/webapp/WEB-INF/views/architecture.jsp");
  assert.match(jsp, /Cùng dữ liệu, hai cách/);
  assert.match(jsp, /Luồng chính trong 3 phút/);
  assert.match(jsp, /Cần đăng nhập/);
  assert.doesNotMatch(jsp, /<%(?!@)|<fmt:|<fn:|<select\b/);
});
