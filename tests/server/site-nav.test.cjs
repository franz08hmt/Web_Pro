"use strict";
const test = require("node:test"), assert = require("node:assert/strict");
const fs = require("node:fs"), path = require("node:path");
const root = path.resolve(__dirname, "../.."), read = f => fs.readFileSync(path.join(root, f), "utf8");
const groups = JSON.parse(read("tests/fixtures/site-nav.json"));
const views = "tomcat-app/src/main/webapp/WEB-INF/views/";
const pages = ["index.html", ...fs.readdirSync(path.join(root, "pages")).filter(f => f.endsWith(".html")).map(f => "pages/" + f)];
test("all static and JSP navigation groups match the single menu fixture", () => {
  for (const file of [...pages, views + "site-nav.jspf"]) {
    const source = read(file), blocks = [...source.matchAll(/<details class="site-nav-group"[\s\S]*?<\/details>/g)];
    assert.equal(blocks.length, groups.length, file);
    groups.forEach((g, index) => {
      const block = blocks[index][0];
      assert.ok(block.includes(`data-nav-group="${g.id}" data-nav-role="${g.role}"`), file);
      assert.ok(block.includes(`<summary>${g.label}</summary>`), file);
      assert.deepEqual([...block.matchAll(/data-nav-target="([^"]+)"/g)].map(m => m[1]), g.items.map(i => i.href), file);
      for (const i of g.items) assert.ok(block.includes(i.label), file + ": " + i.label);
      if (!file.endsWith("jspf") && g.role !== "PUBLIC") assert.match(block.split(">", 1)[0], /hidden/);
    });
  }
});
test("menu links target existing files or servlet mappings and JSP roles are checked on server", () => {
  const java = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/";
  const source = fs.readdirSync(path.join(root, java)).map(f => read(java + f)).join("\n");
  for (const g of groups) for (const item of g.items) {
    assert.ok(fs.existsSync(path.join(root, item.href.slice(1))) || source.includes(`"${item.href}"`), item.href);
  }
  const jsp = read(views + "site-nav.jspf");
  assert.match(jsp, /<c:if test="\$\{not empty sessionScope.user\}">\s*<details[^>]*data-nav-group="mine"/);
  assert.match(jsp, /<c:if test="\$\{not empty sessionScope.user and sessionScope.user.admin\}">\s*<details[^>]*data-nav-group="admin"/);
  assert.match(read(views + "workspace-header.jspf"), /<%@ include file="site-nav.jspf" %>/);
  assert.doesNotMatch(read("assets/js/site-nav.js"), /localStorage/);
  assert.match(read("assets/js/site-nav.js"), /api\/auth\/me/);
  const filter = read("tomcat-app/src/main/java/vn/edu/webpro/robotlab/filter/AdminPageFilter.java");
  for (const item of groups.find(g => g.id === "admin").items.filter(i => i.href.endsWith(".html"))) {
    assert.ok(filter.includes('"' + item.href + '"'), item.href);
  }
  assert.match(filter, /SessionUtil.getCurrentUser\(http\)/);
  assert.match(filter, /if \(!user.isAdmin\(\)\)[\s\S]*sendError\(403\)/);
});
