"use strict";

(async () => {
  const params = new URLSearchParams(window.location.search);
  const modelId = params.get("model") || "line-follower";
  let robotModels = window.ROBOT_MODELS || [];
  let contentWarning = "";

  if (window.RobotContentApi?.enabled) {
    try {
      robotModels = await window.RobotContentApi.loadRobots() || robotModels;
    } catch (error) {
      contentWarning = `Không thể tải dữ liệu mới; đang dùng dữ liệu dự phòng. ${error.message}`;
    }
  }

  const model = robotModels.find((item) => item.id === modelId) || robotModels[0];
  const assemblyConfig = window.ASSEMBLY_3D_CONFIG || {};

  const container = document.querySelector("#assembly-3d-canvas");
  const modelName = document.querySelector("#model-name");
  const modelSummary = document.querySelector("#assembly-3d-summary");
  const partsContainer = document.querySelector("#assembly-3d-parts");
  const statusElement = document.querySelector("#assembly-3d-status");
  const progressElement = document.querySelector("#assembly-3d-progress");
  const progressValue = document.querySelector("#assembly-3d-progress-value");
  const progressCounter = document.querySelector("#assembly-3d-counter");
  const stepsContainer = document.querySelector("#assembly-3d-steps");
  const stepCounter = document.querySelector("#assembly-3d-step-counter");
  const syncStatus = document.querySelector("#assembly-3d-sync-status");
  const resetButton = document.querySelector("#assembly-3d-reset");
  const focusButton = document.querySelector("#assembly-3d-focus");
  const rotateLeftButton = document.querySelector("#assembly-3d-rotate-left");
  const rotateRightButton = document.querySelector("#assembly-3d-rotate-right");
  const zoomInButton = document.querySelector("#assembly-3d-zoom-in");
  const zoomOutButton = document.querySelector("#assembly-3d-zoom-out");
  const explodeButton = document.querySelector("#assembly-3d-explode");
  const sessionApi = window.RobotAssemblyApi?.assemblySessions;
  let activeSession = null;

  const stepRecords = Array.isArray(model?.stepRecords) && model.stepRecords.length > 0
    ? model.stepRecords
    : (model?.steps || []).map((instruction, index) => ({
        id: `${model.id}-step-${index + 1}`,
        title: `Bước ${index + 1}`,
        instruction,
        stepOrder: index + 1
      }));

  if (!container || !model) {
    throw new Error("Không thể khởi tạo phòng lắp ráp 3D.");
  }

  const stepStorageKey = `ral.assemblySteps.${model.id}`;
  const visualStorageKey = `ral.assembledParts.${model.id}`;

  function readStoredIds(key) {
    try {
      const value = JSON.parse(window.localStorage.getItem(key) || "[]");
      return new Set(Array.isArray(value) ? value.filter((item) => typeof item === "string") : []);
    } catch {
      window.localStorage.removeItem(key);
      return new Set();
    }
  }

  function writeStoredIds(key, ids) {
    try {
      window.localStorage.setItem(key, JSON.stringify([...ids]));
    } catch {
      // Trình duyệt có thể chặn storage; giao diện vẫn hoạt động trong phiên hiện tại.
    }
  }

  function setSyncStatus(message, state = "") {
    if (!syncStatus) return;
    syncStatus.textContent = message;
    syncStatus.dataset.state = state;
  }

  function updateStepCounter() {
    if (!stepsContainer) return;
    const inputs = [...stepsContainer.querySelectorAll("input[data-step-id]")];
    const completed = inputs.filter((input) => input.checked).length;
    if (stepCounter) stepCounter.textContent = `${completed}/${inputs.length} bước`;
  }

  function setStepInputsDisabled(disabled) {
    stepsContainer?.querySelectorAll("input[data-step-id]").forEach((input) => {
      input.disabled = disabled;
    });
  }

  function applyStepIds(completedIds) {
    stepsContainer?.querySelectorAll("input[data-step-id]").forEach((input) => {
      input.checked = completedIds.has(input.dataset.stepId);
      input.closest("label")?.classList.toggle("is-complete", input.checked);
    });
    writeStoredIds(stepStorageKey, completedIds);
    updateStepCounter();
  }

  function completedStepIdsFromSession(session) {
    return new Set(
      (session.steps || [])
        .filter((step) => step.status === "COMPLETED")
        .map((step) => step.stepId)
    );
  }

  function renderStepsPanel() {
    if (!stepsContainer) return;
    stepsContainer.replaceChildren();

    stepRecords.forEach((step, index) => {
      const label = document.createElement("label");
      label.className = "assembly-3d-step-item";

      const checkbox = document.createElement("input");
      checkbox.type = "checkbox";
      checkbox.dataset.stepId = step.id;
      checkbox.setAttribute("aria-label", `${step.title || `Bước ${index + 1}`}: ${step.instruction}`);

      const order = document.createElement("span");
      order.className = "assembly-3d-step-order";
      order.textContent = String(step.stepOrder || index + 1).padStart(2, "0");

      const copy = document.createElement("span");
      copy.className = "assembly-3d-step-copy";
      const title = document.createElement("strong");
      title.textContent = step.title || `Bước ${index + 1}`;
      const instruction = document.createElement("small");
      instruction.textContent = step.instruction;
      copy.append(title, instruction);

      label.append(checkbox, order, copy);
      stepsContainer.append(label);

      checkbox.addEventListener("change", async () => {
        const previous = !checkbox.checked;
        const localIds = readStoredIds(stepStorageKey);
        if (checkbox.checked) localIds.add(step.id);
        else localIds.delete(step.id);
        writeStoredIds(stepStorageKey, localIds);
        label.classList.toggle("is-complete", checkbox.checked);
        updateStepCounter();

        if (!activeSession || !sessionApi) {
          setSyncStatus("Tiến độ bước đang được lưu trên trình duyệt này.", "local");
          return;
        }

        checkbox.disabled = true;
        setSyncStatus("Đang lưu bước lắp ráp…", "loading");
        try {
          activeSession = await sessionApi.setStepStatus(
            activeSession.id,
            step.id,
            checkbox.checked ? "COMPLETED" : "PENDING"
          );
          applyStepIds(completedStepIdsFromSession(activeSession));
          if (activeSession.status === "COMPLETED") {
            setStepInputsDisabled(true);
            setSyncStatus("Đã hoàn thành và lưu toàn bộ quy trình lắp ráp.", "complete");
          } else {
            setSyncStatus("Đã lưu bước lắp ráp vào tài khoản.", "saved");
          }
        } catch (error) {
          checkbox.checked = previous;
          label.classList.toggle("is-complete", previous);
          const rollbackIds = readStoredIds(stepStorageKey);
          if (previous) rollbackIds.add(step.id);
          else rollbackIds.delete(step.id);
          writeStoredIds(stepStorageKey, rollbackIds);
          updateStepCounter();
          setSyncStatus(`Không thể lưu bước. ${error.message}`, "error");
        } finally {
          if (activeSession?.status !== "COMPLETED") checkbox.disabled = false;
        }
      });
    });

    applyStepIds(readStoredIds(stepStorageKey));
  }

  async function restoreStepSession() {
    if (!sessionApi) {
      setSyncStatus(contentWarning || "Tiến độ bước đang được lưu trên trình duyệt này.", "local");
      return;
    }

    setStepInputsDisabled(true);
    setSyncStatus("Đang khôi phục tiến độ lắp ráp…", "loading");
    try {
      const requestedSessionId = params.get("session");
      let session = requestedSessionId
        ? await sessionApi.get(requestedSessionId)
        : await sessionApi.createOrResume(model.id);

      if (session.robotId !== model.id) {
        session = await sessionApi.createOrResume(model.id);
      }
      if (session.status === "READY") {
        session = await sessionApi.updateStatus(session.id, "IN_PROGRESS");
      }

      activeSession = session;
      applyStepIds(completedStepIdsFromSession(session));

      if (session.status === "PREPARING") {
        setSyncStatus("Hãy chuẩn bị đủ linh kiện ở trang trước để bắt đầu các bước lắp ráp.", "waiting");
      } else if (session.status === "COMPLETED") {
        setSyncStatus("Phiên lắp ráp này đã hoàn thành.", "complete");
      } else if (session.status === "IN_PROGRESS") {
        setStepInputsDisabled(false);
        setSyncStatus("Tiến độ bước đã được đồng bộ với tài khoản.", "saved");
      } else {
        setSyncStatus("Phiên lắp ráp hiện không thể tiếp tục.", "waiting");
      }
    } catch (error) {
      activeSession = null;
      setStepInputsDisabled(false);
      const message = error.code === "AUTH_REQUIRED"
        ? "Đăng nhập để lưu trên tài khoản; hiện tiến độ được lưu trên trình duyệt này."
        : `Không thể đồng bộ tài khoản; đang lưu trên trình duyệt. ${error.message}`;
      setSyncStatus(contentWarning || message, "local");
    }
  }

  renderStepsPanel();
  void restoreStepSession();

  if (!window.THREE || !window.createAssemblyPart) {
    if (statusElement) {
      statusElement.textContent = "Không thể tải trình dựng 3D. Hãy làm mới trang hoặc kiểm tra cấu hình máy chủ.";
    }
    if (resetButton) resetButton.disabled = true;
    return;
  }

  const THREE = window.THREE;
  if (modelName) modelName.textContent = model.name;
  if (modelSummary) modelSummary.textContent = model.summary;

  const scene = new THREE.Scene();
  scene.background = new THREE.Color(0x0b1014);
  scene.fog = new THREE.Fog(0x0b1014, 11, 24);

  const camera = new THREE.PerspectiveCamera(42, 1, 0.1, 100);
  const cameraState = {
    target: new THREE.Vector3(0, 0.8, 0),
    radius: model.id === "mini-arm" ? 7.2 : 6.6,
    azimuth: 0.75,
    polar: 1.02
  };

  const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  renderer.outputColorSpace = THREE.SRGBColorSpace;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.15;
  renderer.domElement.setAttribute("aria-label", `Mô hình 3D ${model.name}`);
  container.appendChild(renderer.domElement);

  function standardMaterial(color, options = {}) {
    return new THREE.MeshStandardMaterial({
      color,
      metalness: options.metalness ?? 0.12,
      roughness: options.roughness ?? 0.72
    });
  }

  function addRoom() {
    const room = new THREE.Group();
    const floor = new THREE.Mesh(
      new THREE.PlaneGeometry(18, 14),
      standardMaterial(0x10171c, { roughness: 0.92 })
    );
    floor.rotation.x = -Math.PI / 2;
    floor.position.y = -0.07;
    floor.receiveShadow = true;
    room.add(floor);

    const grid = new THREE.GridHelper(14, 28, 0x4a5961, 0x263138);
    grid.position.y = -0.055;
    room.add(grid);

    const platform = new THREE.Mesh(
      new THREE.CylinderGeometry(3.25, 3.35, 0.12, 64),
      standardMaterial(0x1a2329, { metalness: 0.35, roughness: 0.55 })
    );
    platform.position.y = 0;
    platform.receiveShadow = true;
    room.add(platform);

    const accentRing = new THREE.Mesh(
      new THREE.TorusGeometry(3.08, 0.025, 8, 96),
      new THREE.MeshBasicMaterial({ color: 0xff8748 })
    );
    accentRing.rotation.x = Math.PI / 2;
    accentRing.position.y = 0.075;
    room.add(accentRing);
    scene.add(room);
  }

  function addLighting() {
    scene.add(new THREE.HemisphereLight(0xd9efff, 0x182027, 1.9));
    const keyLight = new THREE.DirectionalLight(0xffffff, 3.4);
    keyLight.position.set(5, 9, 6);
    keyLight.castShadow = true;
    keyLight.shadow.mapSize.set(2048, 2048);
    keyLight.shadow.camera.left = -6;
    keyLight.shadow.camera.right = 6;
    keyLight.shadow.camera.top = 6;
    keyLight.shadow.camera.bottom = -6;
    scene.add(keyLight);

    const fillLight = new THREE.PointLight(0x38d3c3, 1.8, 14);
    fillLight.position.set(-5, 3, -3);
    scene.add(fillLight);
    const rimLight = new THREE.PointLight(0xff8748, 2.2, 12);
    rimLight.position.set(4, 3, 4);
    scene.add(rimLight);
  }

  addRoom();
  addLighting();

  const robotGroup = new THREE.Group();
  scene.add(robotGroup);
  const assembledParts = new Map();
  let explodedView = false;

  function getTargets(partId) {
    const target = assemblyConfig[model.id]?.[partId]?.target;
    if (!target) return [];
    return Array.isArray(target) ? target : [target];
  }

  function applyTransform(object, target) {
    object.position.set(target.x ?? 0, target.y ?? 0, target.z ?? 0);
    object.rotation.set(target.rx ?? 0, target.ry ?? 0, target.rz ?? 0);
    const scale = target.scale ?? 1;
    object.scale.setScalar(scale);
  }

  function assemblePart(part) {
    const targets = getTargets(part.id);
    const partGroup = new THREE.Group();
    partGroup.name = `${part.id}-group`;

    for (let itemIndex = 0; itemIndex < part.quantity; itemIndex += 1) {
      const object = window.createAssemblyPart(THREE, part.id);
      if (!object) continue;
      applyTransform(object, targets[itemIndex] || targets[0] || {});
      object.name = `${part.id}-${itemIndex + 1}`;
      partGroup.add(object);
    }

    if (partGroup.children.length === 0) return null;
    robotGroup.add(partGroup);
    return partGroup;
  }

  function removePart(partId) {
    const object = assembledParts.get(partId);
    if (!object) return;
    robotGroup.remove(object);
    assembledParts.delete(partId);
  }

  function setExplodedView(nextValue) {
    explodedView = Boolean(nextValue) && assembledParts.size > 0;

    model.parts.forEach((part, index) => {
      const object = assembledParts.get(part.id);
      if (!object) return;
      if (!explodedView) {
        object.position.set(0, 0, 0);
        return;
      }

      const angle = (index / Math.max(model.parts.length, 1)) * Math.PI * 2 - Math.PI / 3;
      const radius = model.id === "mini-arm" ? 1.7 : 1.8;
      object.position.set(
        Math.cos(angle) * radius,
        0.25 + (index % 2) * 0.22,
        Math.sin(angle) * radius
      );
    });

    if (explodeButton) {
      explodeButton.setAttribute("aria-pressed", String(explodedView));
      explodeButton.textContent = explodedView ? "Ghép về vị trí" : "Tách linh kiện";
    }
    focusRobot();
  }

  function updateProgress() {
    const total = model.parts.length;
    const completed = assembledParts.size;
    const percent = total === 0 ? 0 : Math.round((completed / total) * 100);
    if (progressElement) progressElement.value = percent;
    if (progressValue) progressValue.textContent = `${percent}%`;
    if (progressCounter) progressCounter.textContent = `${completed}/${total} nhóm`;
    if (!statusElement) return;
    if (percent === 100) statusElement.textContent = "Đã lắp ráp đầy đủ robot.";
    else if (percent === 0) statusElement.textContent = "Chọn linh kiện theo thứ tự để bắt đầu lắp ráp.";
    else statusElement.textContent = `Đã lắp ${completed}/${total} nhóm linh kiện.`;
  }

  function updateCamera() {
    const { target, radius, azimuth, polar } = cameraState;
    camera.position.set(
      target.x + radius * Math.sin(polar) * Math.cos(azimuth),
      target.y + radius * Math.cos(polar),
      target.z + radius * Math.sin(polar) * Math.sin(azimuth)
    );
    camera.lookAt(target);
  }

  function focusRobot() {
    const box = new THREE.Box3().setFromObject(robotGroup);
    if (box.isEmpty()) {
      cameraState.target.set(0, model.id === "mini-arm" ? 1.15 : 0.65, 0);
      cameraState.radius = model.id === "mini-arm" ? 7.2 : 6.6;
      updateCamera();
      return;
    }
    const sphere = box.getBoundingSphere(new THREE.Sphere());
    cameraState.target.copy(sphere.center);
    cameraState.radius = THREE.MathUtils.clamp(sphere.radius * 3.3, 4.6, 10.5);
    updateCamera();
  }

  function renderPartsPanel() {
    if (!partsContainer) return;
    partsContainer.replaceChildren();

    model.parts.forEach((part, index) => {
      const label = document.createElement("label");
      label.className = "assembly-3d-part-item";

      const checkbox = document.createElement("input");
      checkbox.type = "checkbox";
      checkbox.dataset.partId = part.id;
      checkbox.setAttribute("aria-label", `${part.name}, số lượng ${part.quantity}`);

      const order = document.createElement("span");
      order.className = "assembly-3d-part-order";
      order.textContent = String(index + 1).padStart(2, "0");

      const copy = document.createElement("span");
      copy.className = "assembly-3d-part-copy";
      copy.innerHTML = `<strong>${part.name}</strong><small>${part.quantity} linh kiện vật lý</small>`;

      label.append(checkbox, order, copy);
      partsContainer.append(label);

      checkbox.addEventListener("change", () => {
        if (checkbox.checked) {
          const object = assemblePart(part);
          if (object) assembledParts.set(part.id, object);
          else checkbox.checked = false;
        } else {
          removePart(part.id);
        }
        label.classList.toggle("is-installed", checkbox.checked);
        const installedIds = new Set(
          [...partsContainer.querySelectorAll("input[data-part-id]:checked")]
            .map((input) => input.dataset.partId)
        );
        writeStoredIds(visualStorageKey, installedIds);
        if (explodedView) setExplodedView(true);
        focusRobot();
        updateProgress();
      });
    });
  }

  function restoreVisualAssembly() {
    const installedIds = readStoredIds(visualStorageKey);
    model.parts.forEach((part) => {
      if (!installedIds.has(part.id)) return;
      const checkbox = partsContainer?.querySelector(`input[data-part-id="${CSS.escape(part.id)}"]`);
      const object = assemblePart(part);
      if (!checkbox || !object) return;
      checkbox.checked = true;
      checkbox.closest("label")?.classList.add("is-installed");
      assembledParts.set(part.id, object);
    });
  }

  function resetAssembly() {
    assembledParts.forEach((object) => robotGroup.remove(object));
    assembledParts.clear();
    setExplodedView(false);
    partsContainer?.querySelectorAll("input[type='checkbox']").forEach((checkbox) => {
      checkbox.checked = false;
      checkbox.closest("label")?.classList.remove("is-installed");
    });
    writeStoredIds(visualStorageKey, new Set());
    focusRobot();
    updateProgress();
  }

  function rotateCamera(delta) {
    cameraState.azimuth += delta;
    updateCamera();
  }

  function zoomCamera(delta) {
    cameraState.radius = THREE.MathUtils.clamp(cameraState.radius + delta, 3.4, 10);
    updateCamera();
  }

  function enablePointerControls() {
    let dragging = false;
    let previousX = 0;
    let previousY = 0;
    renderer.domElement.addEventListener("pointerdown", (event) => {
      dragging = true;
      previousX = event.clientX;
      previousY = event.clientY;
      renderer.domElement.setPointerCapture(event.pointerId);
    });
    renderer.domElement.addEventListener("pointermove", (event) => {
      if (!dragging) return;
      cameraState.azimuth -= (event.clientX - previousX) * 0.008;
      cameraState.polar = THREE.MathUtils.clamp(
        cameraState.polar + (event.clientY - previousY) * 0.006,
        0.42,
        1.42
      );
      previousX = event.clientX;
      previousY = event.clientY;
      updateCamera();
    });
    renderer.domElement.addEventListener("pointerup", () => { dragging = false; });
    renderer.domElement.addEventListener("wheel", (event) => {
      event.preventDefault();
      cameraState.radius = THREE.MathUtils.clamp(cameraState.radius + event.deltaY * 0.006, 3.4, 10);
      updateCamera();
    }, { passive: false });
  }

  function resizeRenderer() {
    const width = container.clientWidth;
    const height = container.clientHeight;
    if (!width || !height) return;
    renderer.setSize(width, height, false);
    camera.aspect = width / height;
    camera.updateProjectionMatrix();
  }

  renderPartsPanel();
  restoreVisualAssembly();
  updateProgress();
  focusRobot();
  enablePointerControls();
  resetButton?.addEventListener("click", resetAssembly);
  focusButton?.addEventListener("click", focusRobot);
  rotateLeftButton?.addEventListener("click", () => rotateCamera(-0.35));
  rotateRightButton?.addEventListener("click", () => rotateCamera(0.35));
  zoomInButton?.addEventListener("click", () => zoomCamera(-0.6));
  zoomOutButton?.addEventListener("click", () => zoomCamera(0.6));
  explodeButton?.addEventListener("click", () => setExplodedView(!explodedView));
  window.addEventListener("resize", resizeRenderer);
  resizeRenderer();

  function animate() {
    requestAnimationFrame(animate);
    renderer.render(scene, camera);
  }
  animate();
})();
