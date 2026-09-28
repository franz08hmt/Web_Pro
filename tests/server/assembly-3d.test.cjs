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
  assert.match(pageSource, /src="\.\.\/assets\/vendor\/three\/three\.min\.js\?v=20260921\.1"/);
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

test("3D assembly room has no steps list at all — parts panel is the only interactive UI", () => {
  assert.doesNotMatch(pageSource, /assembly-3d-steps/);
  assert.doesNotMatch(pageSource, /assembly-3d-step-counter/);
  assert.doesNotMatch(pageSource, /class="assembly-3d-left"/);
  assert.match(
    pageSource,
    /id="assembly-3d-sync-status"[^>]*role="status"[^>]*aria-live="polite"/
  );

  assert.doesNotMatch(assemblySource, /assembly-3d-steps/);
  assert.doesNotMatch(assemblySource, /stepRecords/);
  assert.doesNotMatch(assemblySource, /data-step-id/);
  assert.doesNotMatch(assemblySource, /sessionApi\.setStepStatus\(/);

  assert.doesNotMatch(styleSource, /\.assembly-3d-step/);
  assert.doesNotMatch(styleSource, /\.assembly-3d-left\b/);
});

test("3D assembly loads live content and persists visual assembly through the session API", () => {
  assert.match(
    pageSource,
    /data\.js[\s\S]*content-api\.js[\s\S]*api\.js[\s\S]*assembly-3d\.js/,
    "Dữ liệu và API client phải được tải trước controller 3D"
  );
  assert.match(assemblySource, /RobotContentApi\.loadRobots\(\)/);
  assert.match(assemblySource, /sessionApi\.createOrResume\(model\.id\)/);
  assert.match(assemblySource, /sessionApi\.updateStatus\([^,]+,\s*"IN_PROGRESS"\)/);
});

test("3D assembly restores visual parts from the owned session and persists each toggle", () => {
  assert.match(assemblySource, /session\.assembledPartIds/);
  assert.match(assemblySource, /sessionApi\.setVisualPart\(/);
  assert.match(assemblySource, /requestedSessionId/);
  assert.doesNotMatch(assemblySource, /session\.robotId !== model\.id\)\s*\{\s*session = await sessionApi\.createOrResume/);
});

test("3D assembly renders API-provided component text without HTML injection", () => {
  assert.doesNotMatch(assemblySource, /copy\.innerHTML/);
  assert.match(assemblySource, /partName\.textContent\s*=\s*part\.name/);
  assert.match(assemblySource, /partQuantity\.textContent/);
});

test("completion button only unlocks once every part group is assembled in an IN_PROGRESS account session", () => {
  assert.match(pageSource, /id="assembly-3d-complete"[^>]*disabled/);
  assert.match(
    assemblySource,
    /function updateCompleteAvailability\(\)[\s\S]*?activeSession\.status === "IN_PROGRESS"[\s\S]*?assembledParts\.size === model\.parts\.length/
  );
  // updateProgress() phải gọi lại hàm này mỗi khi tick/gỡ linh kiện thay đổi assembledParts.
  assert.match(assemblySource, /progressCounter\.textContent[\s\S]*?updateCompleteAvailability\(\);/);
});

test("completing an assembly disables the button first, then asks the server — never trusts a client-side percentage", () => {
  assert.match(
    assemblySource,
    /async function completeAssembly\(\)\s*\{\s*if \(!activeSession \|\| !sessionApi \|\| !completeButton\) return;\s*\n\s*completeButton\.disabled = true;/,
    "Nút phải bị khóa ngay dòng đầu tiên để double-click không gửi hai request"
  );
  assert.match(assemblySource, /sessionApi\.updateStatus\(activeSession\.id, "COMPLETED"\)/);
  // Không có logic nào tự tính phần trăm/đếm part rồi gửi lên server — server tự đối chiếu lại.
  assert.doesNotMatch(assemblySource, /isCompleted["']?\s*:\s*(true|percent)/);
});

test("confetti fires only on a genuine transition to COMPLETED — never on page reload or revisit", () => {
  assert.match(assemblySource, /function showCompletionResult\(justCompleted\)/);
  assert.match(assemblySource, /if \(justCompleted\) window\.RobotConfetti\?\.celebrate\(\);/);

  // Điểm gọi thứ nhất: vừa hoàn tất thành công trong lượt bấm này -> true.
  assert.match(assemblySource, /activeSession\.status === "COMPLETED"\) \{\s*showCompletionResult\(true\);/);
  // Điểm gọi thứ hai: mở lại một phiên đã COMPLETED từ trước lúc tải trang -> false, không ăn mừng lại.
  // (biến cục bộ trong restoreSession() tên là "session", không phải "activeSession", nên anchor
  // vào đúng tiền tố này để không lẫn với nhánh completeAssembly() ở trên.)
  assert.match(
    assemblySource,
    /session\.status === "COMPLETED"\) \{[\s\S]{0,120}showCompletionResult\(false\);/
  );
});

test("confetti effect respects prefers-reduced-motion and cleans up its own DOM after finishing", () => {
  const confettiSource = fs.readFileSync(path.join(projectRoot, "assets", "js", "confetti.js"), "utf8");
  assert.match(confettiSource, /prefers-reduced-motion:\s*reduce/);
  assert.match(confettiSource, /if \(window\.matchMedia\("\(prefers-reduced-motion: reduce\)"\)\.matches\) return;/);
  assert.match(confettiSource, /ral-confetti-layer/);
  assert.match(confettiSource, /window\.setTimeout\(\(\) => layer\.remove\(\), DURATION_MS\)/);
  assert.doesNotMatch(confettiSource, /new Audio\(|\.play\(\)/, "Không được tự phát âm thanh");

  assert.match(styleSource, /\.ral-confetti-layer\s*\{[^}]*pointer-events:\s*none;/s);
});

test("3D view has an accessible fullscreen toggle that resizes after every browser fullscreen transition", () => {
  assert.match(pageSource, /<div class="assembly-3d-toolbar"[\s\S]*?id="assembly-3d-fullscreen"[^>]*aria-label="Mở toàn màn hình"[^>]*aria-pressed="false"/);
  assert.match(pageSource, /id="assembly-3d-fullscreen-status"[^>]*role="status"[^>]*aria-live="polite"/);
  assert.match(assemblySource, /async function toggleFullscreen\(\)/);
  assert.match(assemblySource, /fullscreenView\.requestFullscreen\(\)/);
  assert.match(assemblySource, /document\.exitFullscreen\(\)/);
  assert.match(assemblySource, /document\.addEventListener\("fullscreenchange",[\s\S]*?resizeRenderer\(\)/);
  assert.match(assemblySource, /setAttribute\("aria-pressed", String\(isFullscreen\)\)/);
  assert.match(assemblySource, /const label = isFullscreen \? "Thoát toàn màn hình" : "Mở toàn màn hình"/);
  assert.match(assemblySource, /setAttribute\("aria-label", label\)/);
  assert.match(assemblySource, /fullscreenButton\.hidden\s*=\s*true/);
  assert.match(styleSource, /\.assembly-3d-view:fullscreen\s*\{[^}]*width:\s*100vw;[^}]*height:\s*100vh;/s);
  assert.match(styleSource, /aria-pressed="true"\].*fullscreen-exit-icon/s);
});
