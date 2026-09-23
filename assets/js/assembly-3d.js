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
  const syncStatus = document.querySelector("#assembly-3d-sync-status");
  const resetButton = document.querySelector("#assembly-3d-reset");
  const focusButton = document.querySelector("#assembly-3d-focus");
  const rotateLeftButton = document.querySelector("#assembly-3d-rotate-left");
  const rotateRightButton = document.querySelector("#assembly-3d-rotate-right");
  const zoomInButton = document.querySelector("#assembly-3d-zoom-in");
  const zoomOutButton = document.querySelector("#assembly-3d-zoom-out");
  const explodeButton = document.querySelector("#assembly-3d-explode");
  const sessionApi = window.RobotAssemblyApi?.assemblySessions;
  const requestedSessionId = params.get("session");
  let activeSession = null;

  if (!container || !model) {
    throw new Error("Không thể khởi tạo phòng lắp ráp 3D.");
  }

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

  function setPartInputsDisabled(disabled) {
    partsContainer?.querySelectorAll("input[data-part-id]").forEach((input) => {
      input.disabled = disabled;
    });
    if (resetButton) resetButton.disabled = disabled;
  }

  /* Mở/tiếp tục phiên lắp ráp của tài khoản: quản lý trạng thái phiên và
     danh sách linh kiện. Không còn danh sách bước lắp ráp trong phòng 3D. */
  async function restoreSession() {
    if (!sessionApi) {
      if (requestedSessionId) {
        setPartInputsDisabled(true);
        setSyncStatus("Không thể mở phiên đã chọn vì API phiên chưa sẵn sàng.", "error");
        return;
      }
      if (window.THREE && window.createAssemblyPart) restoreVisualAssembly(readStoredIds(visualStorageKey));
      setSyncStatus(contentWarning || "Tiến độ linh kiện đang được lưu trên trình duyệt này.", "local");
      return;
    }

    setPartInputsDisabled(true);
    setSyncStatus("Đang khôi phục tiến độ lắp ráp…", "loading");
    try {
      let session = requestedSessionId
        ? await sessionApi.get(requestedSessionId)
        : await sessionApi.createOrResume(model.id);

      if (session.robotId !== model.id) {
        throw new Error("Phiên này không thuộc mẫu robot đã chọn.");
      }
      if (session.status === "READY") {
        session = await sessionApi.updateStatus(session.id, "IN_PROGRESS");
      }

      activeSession = session;
      if (window.THREE && window.createAssemblyPart) {
        restoreVisualAssembly(new Set(session.assembledPartIds || []));
      }

      if (session.status === "PREPARING") {
        setSyncStatus("Hãy chuẩn bị đủ linh kiện ở trang trước để bắt đầu lắp ráp.", "waiting");
      } else if (session.status === "COMPLETED") {
        setSyncStatus("Phiên lắp ráp này đã hoàn thành.", "complete");
      } else if (session.status === "IN_PROGRESS") {
        setPartInputsDisabled(false);
        setSyncStatus("Tiến độ đã được đồng bộ với tài khoản.", "saved");
      } else {
        setSyncStatus("Phiên lắp ráp hiện không thể tiếp tục.", "waiting");
      }
    } catch (error) {
      activeSession = null;
      const canUseLocal = !requestedSessionId;
      setPartInputsDisabled(canUseLocal ? false : true);
      if (canUseLocal && window.THREE && window.createAssemblyPart) {
        restoreVisualAssembly(readStoredIds(visualStorageKey));
      }
      const message = error.code === "AUTH_REQUIRED" && canUseLocal
        ? "Đăng nhập để lưu trên tài khoản; hiện tiến độ được lưu trên trình duyệt này."
        : canUseLocal
          ? `Không thể đồng bộ tài khoản; đang lưu trên trình duyệt. ${error.message}`
          : `Không thể mở phiên đã chọn. ${error.message}`;
      setSyncStatus(canUseLocal ? contentWarning || message : message, canUseLocal ? "local" : "error");
    }
  }

  if (!window.THREE || !window.createAssemblyPart) {
    void restoreSession();
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

  /* Chuyển động ngắn để người xem thấy rõ linh kiện nào vừa thay đổi.
     Mọi tween đều chạy trong vòng render sẵn có, không tạo thêm vòng lặp mới.
     Khi người dùng chọn giảm chuyển động, tween nhảy thẳng tới trạng thái cuối. */
  const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
  const tweens = new Set();

  function easeOut(ratio) {
    return 1 - Math.pow(1 - ratio, 3);
  }

  function tween(duration, onUpdate, onDone) {
    if (reducedMotion.matches) {
      onUpdate(1);
      onDone?.();
      return;
    }
    tweens.add({ start: performance.now(), duration, onUpdate, onDone });
  }

  function advanceTweens(now) {
    for (const item of tweens) {
      const ratio = item.duration > 0 ? Math.min(1, (now - item.start) / item.duration) : 1;
      item.onUpdate(easeOut(ratio));
      if (ratio < 1) continue;
      tweens.delete(item);
      item.onDone?.();
    }
  }

  // Làm nổi linh kiện vừa lắp bằng ánh sáng phát ra rồi trả lại màu gốc.
  // Vật liệu được tạo riêng cho từng linh kiện nên không ảnh hưởng phần khác.
  function highlightPart(group) {
    const materials = [];
    group.traverse((object) => {
      if (object.material?.emissive) {
        materials.push([object.material, object.material.emissive.getHex()]);
        object.material.emissive.setHex(0xff8b4b);
      }
    });
    if (materials.length === 0) return;
    tween(700, (ratio) => {
      materials.forEach(([material]) => {
        material.emissiveIntensity = 1 - ratio;
      });
    }, () => {
      materials.forEach(([material, hex]) => {
        material.emissive.setHex(hex);
        material.emissiveIntensity = 1;
      });
    });
  }

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

  function assemblePart(part, animated = false) {
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

    // Chỉ chạy khi người dùng tự tick: lúc khôi phục phiên đã lưu, cả mô hình
    // phải hiện ngay ở trạng thái cuối thay vì diễn lại toàn bộ quá trình lắp.
    if (animated && !explodedView) {
      partGroup.position.y = 1.05;
      partGroup.scale.setScalar(0.88);
      tween(340, (ratio) => {
        partGroup.position.y = 1.05 * (1 - ratio);
        partGroup.scale.setScalar(0.88 + 0.12 * ratio);
      }, () => {
        partGroup.position.y = 0;
        partGroup.scale.setScalar(1);
        highlightPart(partGroup);
      });
    }

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

    // Gom sẵn điểm đầu và điểm cuối rồi chạy một tween duy nhất: các nhóm linh
    // kiện phải giãn ra và khép lại cùng nhịp, không lệch pha từng cái.
    const moves = [];
    model.parts.forEach((part, index) => {
      const object = assembledParts.get(part.id);
      if (!object) return;

      const to = new THREE.Vector3(0, 0, 0);
      if (explodedView) {
        const angle = (index / Math.max(model.parts.length, 1)) * Math.PI * 2 - Math.PI / 3;
        const radius = model.id === "mini-arm" ? 1.7 : 1.8;
        to.set(Math.cos(angle) * radius, 0.25 + (index % 2) * 0.22, Math.sin(angle) * radius);
      }
      moves.push({ object, from: object.position.clone(), to });
    });

    if (moves.length > 0) {
      tween(420, (ratio) => {
        moves.forEach((move) => move.object.position.lerpVectors(move.from, move.to, ratio));
      }, focusRobot);
    }

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

    // Mô tả linh kiện nằm ở danh mục chung; tra theo ID để chú thích nói đúng
    // vai trò của bộ phận thay vì lặp lại tên đã hiển thị sẵn.
    const descriptions = new Map(
      (window.COMPONENTS_DATA || []).map((item) => [item.id, item.description])
    );

    model.parts.forEach((part, index) => {
      const label = document.createElement("label");
      label.className = "assembly-3d-part-item";

      const description = descriptions.get(part.id);
      label.dataset.tooltip = [
        `${part.name} · ${part.quantity} linh kiện`,
        description
      ].filter(Boolean).join(" — ");

      const checkbox = document.createElement("input");
      checkbox.type = "checkbox";
      checkbox.dataset.partId = part.id;
      checkbox.setAttribute("aria-label", `${part.name}, số lượng ${part.quantity}`);

      const order = document.createElement("span");
      order.className = "assembly-3d-part-order";
      order.textContent = String(index + 1).padStart(2, "0");

      const copy = document.createElement("span");
      copy.className = "assembly-3d-part-copy";
      const partName = document.createElement("strong");
      partName.textContent = part.name;
      const partQuantity = document.createElement("small");
      partQuantity.textContent = `${part.quantity} linh kiện vật lý`;
      copy.append(partName, partQuantity);

      label.append(checkbox, order, copy);
      partsContainer.append(label);

      checkbox.addEventListener("change", async () => {
        if (checkbox.checked) {
          const object = assemblePart(part, true);
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
        if (!activeSession) writeStoredIds(visualStorageKey, installedIds);
        if (explodedView) setExplodedView(true);
        focusRobot();
        updateProgress();
        if (!activeSession || !sessionApi) return;

        setPartInputsDisabled(true);
        setSyncStatus("Đang lưu mô hình 3D…", "loading");
        let verified = true;
        try {
          activeSession = await sessionApi.setVisualPart(activeSession.id, part.id, checkbox.checked);
          setSyncStatus("Đã lưu mô hình 3D vào tài khoản.", "saved");
        } catch (error) {
          try {
            activeSession = await sessionApi.get(activeSession.id);
            restoreVisualAssembly(new Set(activeSession.assembledPartIds || []));
          } catch {
            verified = false;
            setSyncStatus("Không thể xác nhận trạng thái 3D. Hãy tải lại trang trước khi tiếp tục.", "error");
            return;
          }
          setSyncStatus(`Không thể lưu mô hình 3D. ${error.message}`, "error");
        } finally {
          setPartInputsDisabled(!verified || activeSession?.status !== "IN_PROGRESS");
        }
      });
    });
  }

  function restoreVisualAssembly(installedIds) {
    assembledParts.forEach((object) => robotGroup.remove(object));
    assembledParts.clear();
    partsContainer?.querySelectorAll("input[data-part-id]").forEach((checkbox) => {
      checkbox.checked = false;
      checkbox.closest("label")?.classList.remove("is-installed");
    });
    model.parts.forEach((part) => {
      if (!installedIds.has(part.id)) return;
      const checkbox = partsContainer?.querySelector(`input[data-part-id="${CSS.escape(part.id)}"]`);
      const object = assemblePart(part);
      if (!checkbox || !object) return;
      checkbox.checked = true;
      checkbox.closest("label")?.classList.add("is-installed");
      assembledParts.set(part.id, object);
    });
    if (explodedView) setExplodedView(true);
    focusRobot();
    updateProgress();
  }

  async function resetAssembly() {
    const previousIds = [...assembledParts.keys()];
    restoreVisualAssembly(new Set());
    setExplodedView(false);
    if (!activeSession || !sessionApi) {
      writeStoredIds(visualStorageKey, new Set());
      return;
    }

    setPartInputsDisabled(true);
    setSyncStatus("Đang đặt lại mô hình 3D…", "loading");
    let verified = true;
    try {
      for (const partId of previousIds) {
        activeSession = await sessionApi.setVisualPart(activeSession.id, partId, false);
      }
      setSyncStatus("Đã đặt lại mô hình 3D trong tài khoản.", "saved");
    } catch (error) {
      try {
        activeSession = await sessionApi.get(activeSession.id);
        restoreVisualAssembly(new Set(activeSession.assembledPartIds || []));
      } catch {
        verified = false;
        restoreVisualAssembly(new Set(previousIds));
      }
      setSyncStatus(`Không thể đặt lại toàn bộ mô hình. ${error.message}`, "error");
    } finally {
      setPartInputsDisabled(!verified || activeSession?.status !== "IN_PROGRESS");
    }
  }

  // Nút bấm đổi góc nhìn theo từng nấc nên được nội suy; kéo chuột thì không,
  // vì thao tác kéo đã bám sát con trỏ theo thời gian thực.
  function rotateCamera(delta) {
    const from = cameraState.azimuth;
    tween(260, (ratio) => {
      cameraState.azimuth = from + delta * ratio;
      updateCamera();
    });
  }

  function zoomCamera(delta) {
    const from = cameraState.radius;
    const to = THREE.MathUtils.clamp(from + delta, 3.4, 10);
    tween(260, (ratio) => {
      cameraState.radius = from + (to - from) * ratio;
      updateCamera();
    });
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
  void restoreSession();

  function animate(now) {
    requestAnimationFrame(animate);
    advanceTweens(now ?? performance.now());
    renderer.render(scene, camera);
  }
  animate();
})();
