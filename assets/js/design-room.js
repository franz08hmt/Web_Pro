"use strict";

(() => {
  const id = new URLSearchParams(location.search).get("design");
  const status = document.getElementById("design-status");
  const stage = document.getElementById("design-stage");
  const panel = document.getElementById("design-tools");
  let token;
  let view;
  let animation;
  let stopped = false;
  let latest;
  const THREE = window.THREE;
  if (!id || !THREE || !window.createAssemblyPlacementView) {
    status.textContent = "Không thể mở mô hình. Hãy về danh sách thiết kế hoặc tải lại trang.";
    return;
  }
  async function api(path, method = "GET", body) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 8000);
    try {
      const response = await fetch(new URL("../api/" + path, location.href), {
        method, credentials: "same-origin", cache: "no-store", signal: controller.signal,
        headers: {"Content-Type": "application/json", ...(method === "GET" ? {} : {"X-CSRF-Token": token})},
        body: body === undefined ? undefined : JSON.stringify(body)
      });
      const payload = await response.json();
      if (!response.ok) {
        const error = new Error(payload.error?.message || "Không thể thực hiện thao tác.");
        error.status = response.status; error.code = payload.error?.code; throw error;
      }
      return payload;
    } catch (error) {
      if (error.status) throw error;
      const unavailable = new Error("Không thể kết nối máy chủ; thao tác chưa đồng bộ. Hãy thử lại hoặc tải lại.");
      unavailable.status = 0; throw unavailable;
    } finally { clearTimeout(timeout); }
  }
  const scene = new THREE.Scene();
  scene.background = new THREE.Color(0x0b1014);
  const camera = new THREE.PerspectiveCamera(42, 1, .1, 150);
  const renderer = new THREE.WebGLRenderer({antialias: true});
  renderer.setPixelRatio(Math.min(devicePixelRatio, 2));
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  renderer.outputColorSpace = THREE.SRGBColorSpace;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.15;
  renderer.domElement.setAttribute("aria-label", "Mô hình thiết kế robot 3D");
  stage.append(renderer.domElement);
  const root = new THREE.Group(); scene.add(root);
  scene.add(new THREE.AmbientLight(0xffffff, .8));
  const light = new THREE.DirectionalLight(0xffffff, 1); light.position.set(5, 8, 4); scene.add(light);
  // Bệ/sàn và vật liệu lấy cùng thông số addRoom của phòng lắp mẫu; không đổi theme toàn site.
  const floor = new THREE.Mesh(new THREE.PlaneGeometry(18, 14),
    new THREE.MeshStandardMaterial({color: 0x10171c, metalness: .12, roughness: .92}));
  floor.rotation.x = -Math.PI / 2; floor.position.y = -.07; floor.receiveShadow = true; scene.add(floor);
  const grid = new THREE.GridHelper(14, 28, 0x4a5961, 0x263138);
  grid.position.y = -.055; scene.add(grid);
  const platform = new THREE.Mesh(new THREE.CylinderGeometry(3.25, 3.35, .12, 64),
    new THREE.MeshStandardMaterial({color: 0x1a2329, metalness: .35, roughness: .55}));
  platform.receiveShadow = true; scene.add(platform);
  const ring = new THREE.Mesh(new THREE.TorusGeometry(3.08, .025, 8, 96),
    new THREE.MeshBasicMaterial({color: 0xff8748}));
  ring.rotation.x = Math.PI / 2; ring.position.y = .075; scene.add(ring);
  const cameraTarget = new THREE.Vector3(0, .8, 0);
  let yaw = Math.PI / 2 - .75, pitch = Math.PI / 2 - 1.02, radius = 6.6, orbit = null;
  let framedVersion = null;
  function cameraUpdate() {
    camera.position.set(cameraTarget.x + Math.sin(yaw) * Math.cos(pitch) * radius,
      cameraTarget.y + Math.sin(pitch) * radius,
      cameraTarget.z + Math.cos(yaw) * Math.cos(pitch) * radius);
    camera.lookAt(cameraTarget);
  }
  cameraUpdate();
  function focusModel() {
    const bounds = new THREE.Box3();
    root.updateMatrixWorld(true);
    // Chỉ khối vật thể, gồm khay; không lấy marker/bệ/sàn làm mô hình cần căn.
    for (const object of root.children) {
      if (object.userData.instanceId) bounds.expandByObject(object);
    }
    if (bounds.isEmpty()) {
      cameraTarget.set(0, .8, 0); radius = 6.6;
    } else {
      const sphere = bounds.getBoundingSphere(new THREE.Sphere());
      cameraTarget.copy(sphere.center);
      const vertical = THREE.MathUtils.degToRad(camera.fov) / 2;
      const horizontal = Math.atan(Math.tan(vertical) * camera.aspect);
      radius = Math.max(2, sphere.radius / Math.sin(Math.min(vertical, horizontal)) * 1.1);
    }
    camera.far = Math.max(150, radius * 4);
    camera.updateProjectionMatrix();
    cameraUpdate();
  }
  function sync(layout) {
    latest = layout;
    const sheet = document.getElementById("design-sheet-link");
    sheet.href = "../design-sheet?id=" + encodeURIComponent(layout.designId); sheet.hidden = false;
    document.getElementById("design-name").textContent = layout.name;
    status.textContent = `${layout.statusLabel} · ${layout.goalLabel} · luật phiên bản ${layout.ruleVersion}.`;
    view.sync(layout);
    focusModel();
    showCheck(layout);
  }
  function showCheck(layout) {
    document.getElementById("design-check").disabled = !layout.canEdit;
    const target = document.getElementById("design-check-result");
    target.replaceChildren();
    const summary = document.createElement("p");
    summary.textContent = layout.check?.summary || "Chưa có kết quả cho bố cục hiện tại. Hãy bấm Kiểm tra thiết kế.";
    target.append(summary);
    if (!layout.check) return;
    const list = document.createElement("ul");
    for (const item of layout.check.items) {
      const row = document.createElement("li");
      row.textContent = `${item.layer} · ${item.label}: ${item.message}`; list.append(row);
    }
    target.append(list);
  }
  const base = "designs/" + encodeURIComponent(id);
  view = window.createAssemblyPlacementView({THREE, camera, root, canvas: renderer.domElement, panel,
    onAttach: async (item, slot, orientation, version) => (await api(base + "/placements", "POST", {
      instanceId: Number(item), slotId: slot, orientation, expectedLayoutVersion: version
    })).data,
    onDetach: async (item, version) => (await api(base + "/placements/" + encodeURIComponent(item)
      + "?expectedLayoutVersion=" + version, "DELETE")).data,
    onUndo: async version => (await api(base + "/undo", "POST", {expectedLayoutVersion: version})).data,
    onReload: async () => { try { sync((await api(base + "/layout")).data); } catch (e) { status.textContent = e.message; } },
    onChange: layout => {
      if (layout) {
        latest = layout; status.textContent = layout.statusLabel + " · " + layout.goalLabel; showCheck(layout);
        if (framedVersion !== layout.layoutVersion) { framedVersion = layout.layoutVersion; focusModel(); }
      }
    }
  });
  const canvas = renderer.domElement;
  document.getElementById("design-focus").addEventListener("click", focusModel);
  document.getElementById("design-check").addEventListener("click", async event => {
    if (!latest) return;
    event.currentTarget.disabled = true;
    try { sync((await api(base + "/check", "POST", {expectedLayoutVersion: latest.layoutVersion})).data); }
    catch (error) { status.textContent = error.message; }
    finally { document.getElementById("design-check").disabled = !latest.canEdit; }
  });
  canvas.addEventListener("pointerdown", event => {
    if (view.pointerDown(event)) return;
    orbit = {x: event.clientX, y: event.clientY}; canvas.setPointerCapture(event.pointerId);
  });
  canvas.addEventListener("pointermove", event => {
    if (view.pointerMove(event) || !orbit) return;
    yaw -= (event.clientX - orbit.x) * .008; pitch = Math.max(.15, Math.min(1.3, pitch + (event.clientY - orbit.y) * .008));
    orbit = {x: event.clientX, y: event.clientY}; cameraUpdate();
  });
  canvas.addEventListener("pointerup", event => { view.pointerUp(event); orbit = null; });
  canvas.addEventListener("pointercancel", () => { view.cancelDrag(); orbit = null; });
  canvas.addEventListener("wheel", event => { event.preventDefault(); radius = Math.max(.8, Math.min(60, radius + event.deltaY * .01)); cameraUpdate(); }, {passive: false});
  function draw() {
    if (stopped) return;
    const {width, height} = stage.getBoundingClientRect();
    if (canvas.width !== Math.round(width * renderer.getPixelRatio()) || canvas.height !== Math.round(height * renderer.getPixelRatio())) {
      renderer.setSize(width, height, false); camera.aspect = width / height; camera.updateProjectionMatrix();
      focusModel();
    }
    renderer.render(scene, camera); animation = requestAnimationFrame(draw);
  }
  draw();
  window.addEventListener("pagehide", () => { stopped = true; cancelAnimationFrame(animation); view.dispose(); renderer.dispose(); });
  (async () => {
    try { const me = await api("auth/me"); token = me.csrfToken; sync((await api(base + "/layout")).data); }
    catch (error) { status.textContent = error.message; }
  })();
})();
