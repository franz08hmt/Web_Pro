"use strict";
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const test = require("node:test");
const assert = require("node:assert/strict");
const root = path.resolve(__dirname, "../..");
const read = p => fs.readFileSync(path.join(root, p), "utf8");
const java = "tomcat-app/src/main/java/vn/edu/webpro/robotlab/";
const views = "tomcat-app/src/main/webapp/WEB-INF/views/";

test("completed review embeds escaped owner layout before the reference image without a second query", () => {
  const servlet = read(java + "controller/AssemblyReviewPageServlet.java");
  assert.equal((servlet.match(/AssemblyPlacementDB.selectLayout\(/g) || []).length, 1);
  assert.match(servlet, /review.canDisplayLayout\(layout\)/);
  assert.match(servlet, /setAttribute\("reviewLayoutJson", AssemblyPlacementJson.reviewLayout\(layout\)\)/);
  assert.match(servlet, /selectSession\(sessionId, user.getId\(\)\)/);
  const jsp = read(views + "assembly-review.jsp");
  const fragment = read(views + "assembly-result-viewer.jspf");
  assert.ok(jsp.indexOf('include file="assembly-result-viewer.jspf"') < jsp.indexOf("Ảnh tham khảo"));
  assert.match(fragment, /<div id="assembly-result-data" hidden><c:out value="\$\{reviewLayoutJson\}"\s*\/><\/div>/);
  assert.match(fragment, /Mô hình em đã lắp — Phiên #/);
  assert.match(fragment, /noscript/);
  assert.doesNotMatch(fragment, /<iframe|application\/json|<%(?!@)|fmt:|fn:|<select/);
  for (const line of fragment.split(/\r?\n/)) assert.ok(line.length <= 140, line);
});

function renderer() {
  const THREE = require(path.join(root, "assets/vendor/three/three.min.js"));
  const context = {window: {}, document: {getElementById: () => null}, console};
  vm.runInNewContext(read("assets/js/assembly-result-viewer.js"), context);
  return {THREE, build: context.window.buildAssemblyResultModel};
}
function fixture(version = 1) {
  return {sessionId: "42", mode: "DETAILED", status: "COMPLETED",
    spec: {id: String(100 + version), versionNo: version, slots: [
      {id: "7", x: 1, y: 0.5, z: -2, rx: 0.2, ry: version, rz: -0.1, parentSlotId: "0"},
      {id: "8", x: -1, y: 3, z: 0, rx: 0, ry: 0, rz: 0, parentSlotId: "7"}]},
    instances: [{id: "11", componentId: "wheel"}, {id: "12", componentId: "wheel"},
      {id: "13", componentId: "sg90"}],
    placements: [{instanceId: "11", slotId: "7", orientation: "Y90"},
      {instanceId: "12", slotId: "8", orientation: "Y270"}]};
}

test("result model uses only final placements with absolute slot pose and separate repeated instances", () => {
  const {THREE, build} = renderer();
  const layout = fixture();
  const before = JSON.stringify(layout);
  const model = build(THREE, layout, () => new THREE.Group());
  assert.equal(model.children.length, 2); // Never fills the unplaced servo from a catalog.
  const [a, b] = model.children;
  assert.equal(a.userData.instanceId, "11");
  assert.equal(b.userData.instanceId, "12");
  assert.equal(a.userData.slotId, "7");
  assert.equal(a.rotation.y, 1 + Math.PI / 2);
  assert.equal(a.rotation.x, 0.2);
  assert.deepEqual(b.position.toArray(), [-1, 3, 0]); // No parent transform added twice.
  assert.equal(b.rotation.y, Math.PI * 1.5);
  assert.equal(JSON.stringify(layout), before);
  const newer = build(THREE, fixture(2), () => new THREE.Group());
  assert.equal(newer.children[0].rotation.y - a.rotation.y, 1);
  assert.equal(model.userData.specId, "101");
});

test("invalid layout and unknown part fail honestly rather than constructing a default robot", () => {
  const {THREE, build} = renderer();
  for (const mutate of [l => l.placements[0].slotId = "999", l => l.spec.slots[0].x = null,
    l => l.placements[0].orientation = "Y45", l => l.placements.push(l.placements[0])]) {
    const layout = fixture(); mutate(layout);
    assert.throws(() => build(THREE, layout, () => new THREE.Group()));
  }
  assert.throws(() => build(THREE, fixture(), () => null));
});

test("viewer has no business API, placement handlers or persistent animation loop", () => {
  const source = read("assets/js/assembly-result-viewer.js");
  assert.doesNotMatch(source, /fetch\(|XMLHttpRequest|sessionApi|localStorage|innerHTML|onAttach|onDetach|setInterval/);
  assert.match(source, /JSON.parse\(payload.textContent\)/);
  assert.match(source, /webglcontextlost/);
  assert.match(source, /removeEventListener/);
  assert.match(source, /renderer.dispose\(\)/);
  assert.match(source, /geometry.*dispose/);
  assert.match(source, /pagehide/);
  assert.match(source, /pageshow/);
});
