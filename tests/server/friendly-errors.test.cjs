const test = require("node:test"), assert = require("node:assert/strict"), fs = require("node:fs");
const root = require("node:path").resolve(__dirname, "../.."), read = f => fs.readFileSync(root + "/" + f, "utf8");
test("container errors use private friendly views while APIs retain JSON and HTTP status", () => {
  const xml = read("tomcat-app/src/main/webapp/WEB-INF/web.xml");
  for (const code of [404,500]) {
    assert.match(xml, new RegExp(`<error-code>${code}</error-code>\\s*<location>/WEB-INF/views/error-${code}.jsp`));
    const jsp = read(`tomcat-app/src/main/webapp/WEB-INF/views/error-${code}.jsp`);
    assert.match(jsp, /Mở Bảng điều khiển/);
    assert.doesNotMatch(jsp, /stackTrace|exception\.message|error\.exception|<%(?!@)/);
  }
  assert.match(xml, /<exception-type>java.lang.Throwable<\/exception-type>/);
  const filter = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/filter/PageErrorFilter.java");
  assert.match(filter, /DispatcherType.ERROR/);
  assert.match(filter, /javax.servlet.error.request_uri/);
  assert.match(filter, /ResponseUtil.sendError\(result, status/);
  assert.match(filter, /getContextPath\(\) \+ "\/api\/"/);
});
