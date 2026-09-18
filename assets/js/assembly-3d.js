"use strict";

(() => {
  const params = new URLSearchParams(window.location.search);
  const modelId = params.get("model") || "line-follower";
  const model = (window.ROBOT_MODELS || []).find((item) => item.id === modelId)
    || (window.ROBOT_MODELS || [])[0];
  const assemblyConfig = window.ASSEMBLY_3D_CONFIG || {};

  const container = document.querySelector("#assembly-3d-canvas");
  const modelName = document.querySelector("#model-name");
  const modelSummary = document.querySelector("#assembly-3d-summary");
  const partsContainer = document.querySelector("#assembly-3d-parts");
  const statusElement = document.querySelector("#assembly-3d-status");
  const progressElement = document.querySelector("#assembly-3d-progress");
  const progressValue = document.querySelector("#assembly-3d-progress-value");
  const progressCounter = document.querySelector("#assembly-3d-counter");
  const resetButton = document.querySelector("#assembly-3d-reset");
  const focusButton = document.querySelector("#assembly-3d-focus");
  const rotateLeftButton = document.querySelector("#assembly-3d-rotate-left");
  const rotateRightButton = document.querySelector("#assembly-3d-rotate-right");
  const zoomInButton = document.querySelector("#assembly-3d-zoom-in");
  const zoomOutButton = document.querySelector("#assembly-3d-zoom-out");
  const explodeButton = document.querySelector("#assembly-3d-explode");

  if (!container || !model) {
    throw new Error("Không thể khởi tạo phòng lắp ráp 3D.");
  }

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
        if (explodedView) setExplodedView(true);
        focusRobot();
        updateProgress();
      });
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
