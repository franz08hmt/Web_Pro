"use strict";
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const assert = require("node:assert/strict");
const test = require("node:test");
const rootPath = path.resolve(__dirname, "../..");
const THREE = require(path.join(rootPath, "assets/vendor/three/three.min.js"));
const source = fs.readFileSync(path.join(rootPath, "assets/js/design-room.js"), "utf8");

test("workshop framing fits real mesh bounds in portrait and landscape and excludes unrelated markers", () => {
  const start = source.indexOf("  function cameraUpdate()");
  const end = source.indexOf("  function sync(layout)");
  assert.ok(start >= 0 && end > start);
  for (const aspect of [912 / 580, 358 / 400, 720 / 400]) {
    const root = new THREE.Group();
    for (const x of [-1.7, 0, 1.7]) {
      const object = new THREE.Mesh(new THREE.BoxGeometry(.7, 1, 1.8));
      object.position.set(x, .8, 0);
      object.userData.instanceId = String(root.children.length + 1);
      root.add(object);
    }
    const marker = new THREE.Mesh(new THREE.BoxGeometry(100, 100, 100));
    marker.position.set(1000, 1000, 1000);
    root.add(marker);
    const camera = new THREE.PerspectiveCamera(42, aspect, .1, 150);
    const scope = {THREE, root, camera, cameraTarget: new THREE.Vector3(),
      radius: 12, yaw: Math.PI / 2 - .75, pitch: Math.PI / 2 - 1.02};
    vm.createContext(scope);
    vm.runInContext(source.slice(start, end) + "\nfocusModel();", scope);
    camera.updateMatrixWorld(true);
    assert.ok(scope.radius < 12, "completed robot should fill more of the frame");
    assert.ok(scope.cameraTarget.distanceTo(new THREE.Vector3(0, .8, 0)) < .00001);
    for (const object of root.children.slice(0, 3)) {
      const box = new THREE.Box3().setFromObject(object);
      for (const x of [box.min.x, box.max.x]) {
        for (const y of [box.min.y, box.max.y]) {
          for (const z of [box.min.z, box.max.z]) {
            const projected = new THREE.Vector3(x, y, z).project(camera);
            assert.ok(Math.abs(projected.x) < 1 && Math.abs(projected.y) < 1);
            assert.ok(projected.z >= -1 && projected.z <= 1);
          }
        }
      }
    }
    root.clear();
    vm.runInContext("focusModel();", scope);
    assert.equal(scope.radius, 6.6);
  }
});

test("workshop reframes loaded and changed layouts with a native focus button and scoped existing stage style", () => {
  assert.match(source, /view\.sync\(layout\);\s+focusModel\(\)/);
  assert.match(source, /framedVersion !== layout\.layoutVersion/);
  assert.match(source, /getElementById\("design-focus"\)\.addEventListener\("click", focusModel\)/);
  const html = fs.readFileSync(path.join(rootPath, "pages/xuong-thiet-ke.html"), "utf8");
  assert.match(html, /id="design-stage" class="assembly-3d-view"/);
  assert.match(html, /id="design-focus" type="button"/);
  assert.doesNotMatch(html, /assets\/css\/style\.css/);
  assert.match(source, /CylinderGeometry\(3\.25, 3\.35, \.12, 64\)/);
});
