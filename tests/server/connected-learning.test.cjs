const test = require("node:test"), assert = require("node:assert/strict"), fs = require("node:fs");
const root = require("node:path").resolve(__dirname, "../.."), read = f => fs.readFileSync(root + "/" + f, "utf8");
const java = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
test("missing material form keeps CSRF, owner scope, one transaction and cart stock rules", () => {
  const servlet = read(java + "controller/DesignCartServlet.java");
  assert.match(servlet, /hasValidFormCsrfToken\(request, response\)/);
  assert.match(servlet, /CartDB.ensureMissing\(user.getId\(\), id\)/);
  assert.match(servlet, /sendError\(404\)/);
  assert.match(servlet, /sendRedirect\(request.getContextPath\(\) \+ "\/pages\/gio-hang.html\?designNotice=1"\)/);
  const db = read(java + "data/CartDB.java").split("public static DesignCartResult ensureMissing")[1].split("private static int selectActiveStock")[0];
  for (const s of ["setAutoCommit(false)", "lockUser(connection, userId)", "DesignDB.read(connection, designId, userId, true)",
    "selectActiveStockForUpdate", "FOR UPDATE", "targetQuantity", "connection.commit()", "rollback(connection)",
    "restoreAutoCommit(connection, original)", "pool.freeConnection(connection)"]) assert.ok(db.includes(s), s);
  assert.doesNotMatch(db, /UPDATE shop_products|checkout\(/);
  const jsp = read("tomcat-app/src/main/webapp/WEB-INF/views/design-sheet.jsp");
  assert.match(jsp, /<c:url var="designCartAction" value="\/design-cart"/);
  assert.match(jsp, /name="csrfToken"/);
});
test("context API reads personal kit only from the session and DOM links encode IDs", () => {
  assert.match(read(java + "controller/LearningLinkServlet.java"), /user == null \? 0 : user.getId\(\)/);
  assert.match(read(java + "data/LearningLinkDB.java"), /k.user_id = \?/);
  assert.match(read("assets/js/learning-context.js"), /function componentLink\(id\)[\s\S]*encodeURIComponent\(id\)/);
  assert.doesNotMatch(read("assets/js/learning-context.js"), /innerHTML/);
  assert.match(read("assets/js/assembly-3d-placement.js"), /LearningContext\?\.errorLinks/);
});
