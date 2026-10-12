"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const root = path.join(__dirname, "..", "..");
const page = fs.readFileSync(path.join(root, "pages", "thu-vien.html"), "utf8");
const client = fs.readFileSync(path.join(root, "assets", "js", "library-api.js"), "utf8");
const css = fs.readFileSync(path.join(root, "assets", "css", "library.css"), "utf8");
const servlet = fs.readFileSync(path.join(root, "tomcat-app", "src", "main", "java", "vn", "edu", "webpro", "robotlab", "controller", "LibraryResourceServlet.java"), "utf8");
const librarySeed = fs.readFileSync(path.join(root, "database", "seed-library-resources-phase3.sql"), "utf8");

test("library page provides accessible filter and pagination controls", () => {
  assert.match(page, /<label for="library-robot-filter">Lọc theo mẫu robot<\/label>/);
  assert.match(page, /id="library-pagination"[^>]*aria-label="Phân trang thư viện"/);
  assert.match(page, /id="library-data-status"[^>]*role="status"[^>]*aria-live="polite"/);
  assert.match(client, /robotFilter\.addEventListener\("change"/);
  assert.match(client, /previous\.addEventListener\("click"/);
  assert.match(client, /Không có tài liệu phù hợp/);
  assert.match(page, /href="\.\.\/assets\/css\/library\.css\?v=/);
  assert.match(css, /\.library-page \.library-controls\s*\{[^}]*display: grid;[^}]*grid-template-columns: minmax\(0, 1fr\);/s, "Mobile phải xếp controls theo một cột");
  assert.match(css, /\.library-page \.library-pagination \.library-page-button\s*\{[^}]*flex: 1;[^}]*min-width: 0;/s, "Mobile phải giữ nút phân trang dễ bấm");
  assert.match(css, /min-height: 44px/);
  assert.match(css, /:disabled/);
  assert.match(css, /:focus-visible/);
});

test("library flow has loading, API-error, fallback, and paged API states", () => {
  assert.match(client, /Đang tải tài nguyên thư viện/);
  assert.match(client, /API lỗi:/);
  assert.match(client, /Chế độ dữ liệu dự phòng/);
  assert.match(client, /api\.loadLibraryResourcesPage\(requested\)/);
  assert.match(client, /payload\.meta\?\.total/);
  assert.match(servlet, /getParameter\("page"\)/);
  assert.match(servlet, /getParameter\("robotId"\)/);
});

test("library page and seed point only to existing local files", () => {
  const pagePaths = [...page.matchAll(/(?:href|src)="([^"?#]+)(?:[?#][^"]*)?"/g)]
    .map(match => match[1]).filter(url => !/^(?:[a-z]+:|\/\/)/i.test(url));
  const seedPaths = [...librarySeed.matchAll(/'(\/assets\/[^']+)'/g)].map(match => match[1]);
  for (const url of pagePaths) {
    const local = path.resolve(root, "pages", url);
    assert.ok(fs.existsSync(local) && fs.statSync(local).isFile(), `Missing page asset/link: ${url}`);
  }
  for (const url of seedPaths) {
    const local = path.join(root, url.slice(1));
    assert.ok(fs.existsSync(local) && fs.statSync(local).isFile(), `Missing seed asset: ${url}`);
  }
});

test("library seed covers all five robot models and supplies enough data for page sizes", () => {
  for (const robotId of ["line-follower", "obstacle-avoider", "mini-arm", "line-obstacle", "servo-scout"]) {
    assert.match(librarySeed, new RegExp(`'${robotId}'`), `Thiếu tư liệu cho ${robotId}`);
  }
  assert.equal((librarySeed.match(/^\('/gm) || []).length, 10, "Seed mở rộng phải có 10 tài nguyên");
});
