"use strict";
const fs = require("node:fs");
const path = require("node:path");
const assert = require("node:assert/strict");
const test = require("node:test");
const root = path.resolve(__dirname, "../..");
function read(file) { return fs.readFileSync(path.join(root, file), "utf8"); }

test("placement preview keeps the original tray angles before a slot is chosen", () => {
  const source = read("assets/js/assembly-3d-placement.js");
  assert.match(source, /trayRotation = object\.rotation\.clone\(\)/);
  assert.match(source, /slot \|\| selectedObject\.userData\.trayRotation/);
  assert.doesNotMatch(source, /selectedObject\.rotation\.y \+=/);
});
