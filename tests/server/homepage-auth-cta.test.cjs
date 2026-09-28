"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const projectRoot = path.resolve(__dirname, "..", "..");
const homeSource = fs.readFileSync(path.join(projectRoot, "index.html"), "utf8");
const personalizationSource = fs.readFileSync(path.join(projectRoot, "assets", "js", "personalization.js"), "utf8");
const styleSource = fs.readFileSync(path.join(projectRoot, "assets", "css", "style.css"), "utf8");

test("homepage gives guests a direct, visible path to login and registration without blocking browsing", () => {
  assert.match(homeSource, /<a href="pages\/tai-khoan\.html"[^>]*data-guest="true"[^>]*aria-label="Đăng nhập hoặc đăng ký"[\s\S]*?<span>Đăng nhập \/ Đăng ký<\/span>/);
  assert.match(homeSource, /class="button" href="pages\/mau-robot\.html"/);
  assert.match(styleSource, /\.nav-list a\[data-guest="true"\][\s\S]*?color:\s*var\(--accent-strong\)/);
});

test("homepage account navigation switches from the guest call-to-action to the signed-in greeting", () => {
  assert.match(personalizationSource, /accountLink\.querySelector\("span"\)\.textContent\s*=\s*`Chào, \$\{shortName\}`/);
  assert.match(personalizationSource, /accountLink\.removeAttribute\("data-guest"\)/);
  assert.match(personalizationSource, /accountLink\.dataset\.authenticated\s*=\s*"true"/);
  assert.match(homeSource, /assets\/js\/personalization\.js\?v=\d{8}\.\d+/);
});
