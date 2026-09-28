"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const projectRoot = path.resolve(__dirname, "..", "..");
const controllerPath = path.join(projectRoot, "assets", "js", "assembly-3d-drive.js");
const controllerSource = fs.existsSync(controllerPath) ? fs.readFileSync(controllerPath, "utf8") : "";

function makeEventTarget() {
  const listeners = new Map();
  return {
    addEventListener(type, listener) {
      if (!listeners.has(type)) listeners.set(type, new Set());
      listeners.get(type).add(listener);
    },
    removeEventListener(type, listener) {
      listeners.get(type)?.delete(listener);
    },
    dispatch(type, values = {}) {
      const event = {
        type,
        repeat: false,
        target: null,
        defaultPrevented: false,
        preventDefault() { this.defaultPrevented = true; },
        ...values
      };
      for (const listener of [...(listeners.get(type) || [])]) listener(event);
      return event;
    },
    listenerCount(type) {
      return listeners.get(type)?.size || 0;
    }
  };
}

function loadDriveController({ reducedMotion = false } = {}) {
  const window = makeEventTarget();
  const document = makeEventTarget();
  document.hidden = false;
  document.activeElement = null;
  const context = { window, document, Math, Number, Set };
  vm.runInNewContext(controllerSource, context);
  assert.equal(typeof window.AssemblyDrive?.create, "function", "thiếu AssemblyDrive.create()");
  return { create: window.AssemblyDrive.create, window, document, reducedMotion: { matches: reducedMotion } };
}

function makeRobot({ x = 0, z = 0, rotationY = 0 } = {}) {
  return {
    position: { x, y: 0, z },
    rotation: { y: rotationY },
    translateZ(distance) {
      this.position.x += Math.sin(this.rotation.y) * distance;
      this.position.z += Math.cos(this.rotation.y) * distance;
    }
  };
}

function createDrive(runtime, overrides = {}) {
  const { canDrive: initialCanDrive = true, ...options } = overrides;
  const robotGroup = options.robotGroup || makeRobot();
  let canDrive = initialCanDrive;
  const drive = runtime.create({
    robotGroup,
    platformRadius: 3.25,
    getFootprintRadius: () => 0.4,
    reducedMotion: runtime.reducedMotion,
    canDrive: () => canDrive,
    ...options
  });
  return { drive, robotGroup, setCanDrive(value) { canDrive = value; } };
}

test("the drive controller requires the server-confirmed completed-session gate", () => {
  const runtime = loadDriveController();
  const { drive, robotGroup, setCanDrive } = createDrive(runtime, { canDrive: false });

  assert.equal(drive.start(), false);
  assert.equal(drive.isActive(), false);
  assert.equal(runtime.window.listenerCount("keydown"), 0);

  setCanDrive(true);
  assert.equal(drive.start(), true);
  runtime.window.dispatch("keydown", { code: "KeyW" });
  setCanDrive(false);
  drive.update(1000);
  runtime.window.dispatch("keydown", { code: "KeyW" });

  assert.equal(drive.isActive(), false);
  assert.equal(runtime.window.listenerCount("keydown"), 0);
  assert.deepEqual(robotGroup.position, { x: 0, y: 0, z: 0 });
});

test("WASD and arrow keys move/rotate in the existing frame update and stay inside the platform", () => {
  const runtime = loadDriveController();
  const robotGroup = makeRobot({ x: 2.84, rotationY: -Math.PI / 2 });
  const { drive } = createDrive(runtime, { robotGroup, moveSpeed: 4.8 });

  assert.equal(drive.start(), true);
  const forwardEvent = runtime.window.dispatch("keydown", { code: "KeyW" });
  assert.equal(forwardEvent.defaultPrevented, true);
  drive.update(1000);
  drive.update(1050);
  assert.ok(Math.hypot(robotGroup.position.x, robotGroup.position.z) <= 2.85 + 1e-9);

  runtime.window.dispatch("keyup", { code: "KeyW" });
  runtime.window.dispatch("keydown", { code: "ArrowLeft" });
  drive.update(1100);
  assert.ok(robotGroup.rotation.y > -Math.PI / 2, "phím trái phải xoay mô hình sang trái");

  runtime.window.dispatch("keyup", { code: "ArrowLeft" });
  runtime.window.dispatch("keydown", { code: "ArrowDown" });
  drive.update(1150);
  assert.ok(Number.isFinite(robotGroup.position.z));
  drive.stop();
});

test("reduced-motion uses one discrete step per non-repeat keydown", () => {
  const runtime = loadDriveController({ reducedMotion: true });
  const { drive, robotGroup } = createDrive(runtime, {
    moveStep: 0.25,
    rotationStep: Math.PI / 18
  });
  drive.start();

  runtime.window.dispatch("keydown", { code: "KeyW" });
  const firstPosition = { ...robotGroup.position };
  runtime.window.dispatch("keydown", { code: "KeyW", repeat: true });
  drive.update(1000);

  assert.equal(robotGroup.position.z, firstPosition.z);
  assert.equal(Math.abs(robotGroup.position.z), 0.25);

  runtime.window.dispatch("keydown", { code: "KeyD" });
  assert.equal(robotGroup.rotation.y, -Math.PI / 18);
  drive.stop();
});

test("typing targets do not start robot movement or suppress their normal keys", () => {
  const runtime = loadDriveController();
  const { drive, robotGroup } = createDrive(runtime);
  drive.start();

  for (const selector of ["input", "textarea", "select", "[contenteditable='true']"]) {
    const editableTarget = {
      matches: (query) => query.includes(selector),
      closest: (query) => query.includes(selector) ? editableTarget : null
    };
    const event = runtime.window.dispatch("keydown", { code: "KeyW", target: editableTarget });
    drive.update(1000);
    assert.equal(event.defaultPrevented, false);
    assert.equal(robotGroup.position.z, 0);
  }

  drive.stop();
});

test("blur and hidden-tab events clear held keys; page exit removes keyboard listeners", () => {
  const runtime = loadDriveController();
  const { drive, robotGroup } = createDrive(runtime, { moveSpeed: 2 });
  drive.start();

  runtime.window.dispatch("keydown", { code: "KeyW" });
  runtime.window.dispatch("blur");
  drive.update(1000);
  drive.update(1050);
  assert.equal(robotGroup.position.z, 0);

  runtime.window.dispatch("keydown", { code: "KeyW" });
  runtime.document.hidden = true;
  runtime.document.dispatch("visibilitychange");
  drive.update(1100);
  assert.equal(robotGroup.position.z, 0);

  runtime.window.dispatch("keydown", { code: "KeyW" });
  runtime.window.dispatch("pagehide");
  assert.equal(drive.isActive(), false);
  assert.equal(runtime.window.listenerCount("keydown"), 0);
  assert.equal(runtime.window.listenerCount("keyup"), 0);
  assert.equal(runtime.document.listenerCount("visibilitychange"), 0);
  drive.update(1150);
  assert.equal(robotGroup.position.z, 0);
});
