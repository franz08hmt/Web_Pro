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
  assert.match(servlet, /catch \(SQLException e\) \{\s*request.setAttribute\("reviewLayoutWarning"/);
  const jsp = read(views + "assembly-review.jsp");
  const fragment = read(views + "assembly-result-viewer.jspf");
  assert.ok(jsp.indexOf('include file="assembly-result-viewer.jspf"') < jsp.indexOf("Ảnh tham khảo"));
  assert.match(fragment, /<div id="assembly-result-data" hidden><c:out value="\$\{reviewLayoutJson\}"\s*\/><\/div>/);
  assert.match(fragment, /Mô hình em đã lắp — Phiên #/);
  assert.match(fragment, /noscript/);
  assert.match(fragment, /Phiên lắp theo nhóm cũ không lưu vị trí/);
  assert.match(fragment, /c:out value="\$\{reviewLayoutWarning\}"/);
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

test("viewer initialization, camera controls, context loss and bfcache release their resources", () => {
  const realThree = require(path.join(root, "assets/vendor/three/three.min.js"));
  class Element {
    constructor() { this.handlers = new Map(); this.children = []; this.dataset = {}; }
    addEventListener(name, fn) { if (!this.handlers.has(name)) this.handlers.set(name, new Set()); this.handlers.get(name).add(fn); }
    removeEventListener(name, fn) { this.handlers.get(name)?.delete(fn); }
    emit(name, detail = {}) { for (const fn of this.handlers.get(name) || []) fn(detail); }
    setAttribute() {}
    append(child) { this.children.push(child); child.parent = this; }
    remove() { this.parent.children = this.parent.children.filter(x => x !== this); }
    setPointerCapture() {}
  }
  const host = new Element(); host.clientWidth = 800; host.clientHeight = 400;
  const payload = {textContent: JSON.stringify(fixture())}, status = {};
  const controls = ["in", "out", "center"].map(action => { const e = new Element(); e.dataset.resultCamera = action; return e; });
  const renderers = [], geometries = [], materials = [];
  class Renderer {
    constructor() { this.domElement = new Element(); renderers.push(this); this.draws = 0; }
    setPixelRatio() {}
    setSize() {}
    render(scene, camera) { this.scene = scene; this.camera = camera; this.draws++; }
    dispose() { this.disposed = true; }
    forceContextLoss() { this.lost = true; }
  }
  class Observer { observe() {} disconnect() { this.disconnected = true; } }
  const window = new Element();
  Object.assign(window, {THREE: {...realThree, WebGLRenderer: Renderer}, ResizeObserver: Observer,
    createAssemblyPart() {
      const g = new realThree.BoxGeometry(1, 1, 1), m = new realThree.MeshStandardMaterial();
      g.addEventListener("dispose", () => geometries.push(g)); m.addEventListener("dispose", () => materials.push(m));
      return new realThree.Mesh(g, m);
    }});
  const document = {getElementById: id => ({"assembly-result-stage": host, "assembly-result-data": payload,
    "assembly-result-status": status})[id], querySelectorAll: () => controls};
  vm.runInNewContext(read("assets/js/assembly-result-viewer.js"), {window, document, ResizeObserver: Observer});
  window.initAssemblyResultViewer();
  assert.equal(host.children.length, 1); assert.ok(controls.every(b => !b.disabled));
  const original = renderers[0].scene.children[0].children.map(o => o.position.toArray());
  const distance = renderers[0].camera.position.length();
  controls[0].emit("click"); assert.notEqual(renderers[0].camera.position.length(), distance);
  assert.deepEqual(renderers[0].scene.children[0].children.map(o => o.position.toArray()), original);
  window.initAssemblyResultViewer();
  assert.equal(host.children.length, 1); assert.ok(renderers[0].disposed && renderers[0].lost);
  assert.equal(geometries.length, 2); assert.equal(materials.length, 2);
  const canvas = host.children[0];
  assert.equal(canvas.handlers.get("pointerdown").size, 1);
  canvas.emit("webglcontextlost", {preventDefault() {}});
  assert.match(status.textContent, /Chưa hiển thị/); assert.equal(host.children.length, 0);
  assert.ok(controls.every(b => b.disabled));
  assert.equal(canvas.handlers.get("pointerdown").size, 0);
  window.emit("pageshow", {persisted: true}); assert.equal(host.children.length, 1);
  window.emit("pagehide"); assert.equal(host.children.length, 0);
  assert.ok(renderers.every(r => r.disposed));
  assert.equal(geometries.length, 6); assert.equal(materials.length, 6);
  payload.textContent = "<broken>";
  window.initAssemblyResultViewer(); assert.match(status.textContent, /Chưa hiển thị/);
  assert.equal(host.children.length, 0);
});

test("real part factory handles the saved four-servo instances without sharing objects", () => {
  const {THREE, build} = renderer();
  const context = {window: {}};
  vm.runInNewContext(read("assets/js/assembly-3d-parts.js"), context);
  const layout = fixture();
  layout.instances = []; layout.placements = []; layout.spec.slots = [];
  for (let n = 1; n <= 4; n++) {
    layout.instances.push({id: String(n), componentId: "sg90"});
    layout.placements.push({instanceId: String(n), slotId: String(n), orientation: "Y" + ((n - 1) * 90)});
    layout.spec.slots.push({id: String(n), x: 0, y: n * 2, z: 0, rx: 0, ry: 0, rz: 0});
  }
  const model = build(THREE, layout, context.window.createAssemblyPart);
  assert.equal(model.children.length, 4);
  assert.equal(new Set(model.children.map(o => o.uuid)).size, 4);
  for (let n = 0; n < 4; n++) {
    assert.equal(model.children[n].position.y, (n + 1) * 2);
    assert.equal(model.children[n].rotation.y, n * Math.PI / 2);
  }
});
