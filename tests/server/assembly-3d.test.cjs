"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");

const projectRoot = path.resolve(__dirname, "..", "..");
const pageSource = fs.readFileSync(path.join(projectRoot, "pages", "lap-rap-3d.html"), "utf8");
const assemblySource = fs.readFileSync(path.join(projectRoot, "assets", "js", "assembly-3d.js"), "utf8");
const styleSource = fs.readFileSync(path.join(projectRoot, "assets", "css", "style.css"), "utf8");
const dataSource = fs.readFileSync(path.join(projectRoot, "assets", "js", "data.js"), "utf8");
const configSource = fs.readFileSync(path.join(projectRoot, "assets", "js", "assembly-3d-config.js"), "utf8");
const partsLibraryPath = path.join(projectRoot, "assets", "js", "assembly-3d-parts.js");

function loadAssemblyContract() {
  const context = { window: {} };
  vm.runInNewContext(dataSource, context);
  vm.runInNewContext(configSource, context);
  return context.window;
}

test("3D assembly uses the local Three.js bundle and gives a readable fallback when unavailable", () => {
  assert.match(pageSource, /src="\/vendor\/three\/three\.min\.js"/);
  assert.doesNotMatch(pageSource, /cdn\.jsdelivr\.net/);
  assert.match(assemblySource, /if \(!window\.THREE \|\| !window\.createAssemblyPart\)/);
  assert.match(assemblySource, /Không thể tải trình dựng 3D/);
});

test("3D assembly panel uses the readable dark laboratory theme", () => {
  assert.match(
    styleSource,
    /\.assembly-3d-page \.assembly-3d-panel\s*\{[^}]*color:\s*var\(--assembly-text\);[^}]*background:\s*linear-gradient/s
  );
});

test("3D assembly room exposes accessible camera and exploded-view controls", () => {
  for (const controlId of [
    "assembly-3d-focus",
    "assembly-3d-rotate-left",
    "assembly-3d-rotate-right",
    "assembly-3d-zoom-in",
    "assembly-3d-zoom-out",
    "assembly-3d-explode"
  ]) {
    assert.match(pageSource, new RegExp(`id=["']${controlId}["']`));
    assert.match(assemblySource, new RegExp(`#${controlId}`));
  }

  assert.match(pageSource, /id="assembly-3d-status"[^>]*role="status"[^>]*aria-live="polite"/);
  assert.match(pageSource, /id="assembly-3d-explode"[^>]*aria-pressed="false"/);
  assert.match(assemblySource, /function setExplodedView\(/);
});

test("3D assembly refocuses the camera after parts change", () => {
  assert.match(
    assemblySource,
    /checkbox\.addEventListener\([\s\S]*?focusRobot\(\);\s*updateProgress\(\);/
  );
});

test("every robot component has enough assembly targets for its quantity", () => {
  const { ROBOT_MODELS, ASSEMBLY_3D_CONFIG } = loadAssemblyContract();

  for (const model of ROBOT_MODELS) {
    const modelConfig = ASSEMBLY_3D_CONFIG[model.id];
    assert.ok(modelConfig, `Thiếu cấu hình 3D cho ${model.id}`);

    for (const part of model.parts) {
      const target = modelConfig[part.id]?.target;
      assert.ok(target, `Thiếu vị trí ${model.id}/${part.id}`);
      const targets = Array.isArray(target) ? target : [target];
      assert.equal(
        targets.length,
        part.quantity,
        `${model.id}/${part.id} cần ${part.quantity} vị trí`
      );
    }
  }
});

test("the procedural 3D library supports every component used by the robots", () => {
  assert.ok(fs.existsSync(partsLibraryPath), "Thiếu thư viện assembly-3d-parts.js");
  const context = { window: {} };
  vm.runInNewContext(dataSource, context);
  vm.runInNewContext(fs.readFileSync(partsLibraryPath, "utf8"), context);

  const requiredIds = new Set(
    context.window.ROBOT_MODELS.flatMap((model) => model.parts.map((part) => part.id))
  );
  const supportedIds = new Set(context.window.ASSEMBLY_3D_SUPPORTED_PARTS);
  assert.deepEqual([...supportedIds].sort(), [...requiredIds].sort());
});

test("the assembly controller loads the parts library and creates every physical item", () => {
  assert.match(
    pageSource,
    /assembly-3d-parts\.js[\s\S]*assembly-3d\.js/,
    "Thư viện linh kiện phải được tải trước controller"
  );
  assert.match(assemblySource, /createAssemblyPart\(THREE, part\.id\)/);
  assert.match(assemblySource, /itemIndex\s*<\s*part\.quantity/);
});

test("3D assembly exposes an accessible procedural checklist", () => {
  assert.match(pageSource, /id="assembly-3d-steps-title"/);
  assert.match(pageSource, /id="assembly-3d-steps"/);
  assert.match(pageSource, /id="assembly-3d-step-counter"/);
  assert.match(
    pageSource,
    /class="assembly-3d-steps-region"[^>]*aria-labelledby="assembly-3d-steps-title"/
  );
  assert.match(
    pageSource,
    /id="assembly-3d-sync-status"[^>]*role="status"[^>]*aria-live="polite"/
  );
});

test("3D assembly loads live content and persists assembly steps through the session API", () => {
  assert.match(
    pageSource,
    /data\.js[\s\S]*content-api\.js[\s\S]*api\.js[\s\S]*assembly-3d\.js/,
    "Dữ liệu và API client phải được tải trước controller 3D"
  );
  assert.match(assemblySource, /RobotContentApi\.loadRobots\(\)/);
  assert.match(assemblySource, /model\.stepRecords/);
  assert.match(assemblySource, /sessionApi\.createOrResume\(model\.id\)/);
  assert.match(assemblySource, /sessionApi\.updateStatus\([^,]+,\s*"IN_PROGRESS"\)/);
  assert.match(assemblySource, /sessionApi\.setStepStatus\(/);
  assert.match(assemblySource, /session\.steps/);
});
