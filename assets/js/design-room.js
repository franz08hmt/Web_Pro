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
  scene.background = new THREE.Color(0x151b1f);
  const camera = new THREE.PerspectiveCamera(45, 1, .1, 150);
  const renderer = new THREE.WebGLRenderer({antialias: true});
  renderer.setPixelRatio(Math.min(devicePixelRatio, 2));
  stage.append(renderer.domElement);
  const root = new THREE.Group(); scene.add(root);
  scene.add(new THREE.AmbientLight(0xffffff, .8));
  const light = new THREE.DirectionalLight(0xffffff, 1); light.position.set(5, 8, 4); scene.add(light);
  scene.add(new THREE.GridHelper(18, 18, 0xff8b4b, 0x2c363b));
  let yaw = .7, pitch = .75, radius = 12, orbit = null;
  function cameraUpdate() {
    camera.position.set(Math.sin(yaw) * Math.cos(pitch) * radius, Math.sin(pitch) * radius,
      Math.cos(yaw) * Math.cos(pitch) * radius + 1.5);
    camera.lookAt(0, .8, 1.5);
  }
  cameraUpdate();
  function sync(layout) {
    latest = layout;
    const sheet = document.getElementById("design-sheet-link");
    sheet.href = "../design-sheet?id=" + encodeURIComponent(layout.designId); sheet.hidden = false;
    document.getElementById("design-name").textContent = layout.name;
    status.textContent = `${layout.statusLabel} · ${layout.goalLabel} · luật phiên bản ${layout.ruleVersion}.`;
    view.sync(layout);
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
    onChange: layout => { if (layout) { latest = layout; status.textContent = layout.statusLabel + " · " + layout.goalLabel; showCheck(layout); } }
  });
  const canvas = renderer.domElement;
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
  canvas.addEventListener("wheel", event => { event.preventDefault(); radius = Math.max(4, Math.min(25, radius + event.deltaY * .01)); cameraUpdate(); }, {passive: false});
  function draw() {
    if (stopped) return;
    const {width, height} = stage.getBoundingClientRect();
    if (canvas.width !== Math.round(width * renderer.getPixelRatio()) || canvas.height !== Math.round(height * renderer.getPixelRatio())) {
      renderer.setSize(width, height, false); camera.aspect = width / height; camera.updateProjectionMatrix();
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
