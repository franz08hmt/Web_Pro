"use strict";
(() => {
  // Only presentation: final placements and absolute slot poses supplied by the owner-scoped Servlet.
  function disposeModel(model) {
    const geometries = new Set(), materials = new Set();
    model?.traverse(object => {
      if (object.geometry) geometries.add(object.geometry);
      for (const material of [].concat(object.material || [])) materials.add(material);
    });
    geometries.forEach(geometry => geometry.dispose());
    materials.forEach(material => material.dispose());
  }

  function buildModel(THREE, layout, createPart) {
    if (layout?.mode !== "DETAILED" || layout.status !== "COMPLETED" || !layout.placements?.length) {
      throw new Error("No saved final layout");
    }
    const slots = new Map(layout.spec.slots.map(slot => [String(slot.id), slot]));
    const instances = new Map(layout.instances.map(item => [String(item.id), item]));
    const usedInstances = new Set(), usedSlots = new Set();
    const poses = layout.placements.map(placed => {
      const slot = slots.get(String(placed.slotId)), item = instances.get(String(placed.instanceId));
      if (!slot || !item || !/^Y(0|90|180|270)$/.test(placed.orientation)
          || ![slot.x, slot.y, slot.z, slot.rx, slot.ry, slot.rz].every(Number.isFinite)
          || usedInstances.has(String(item.id)) || usedSlots.has(String(slot.id))) {
        throw new Error("Incomplete saved pose");
      }
      usedInstances.add(String(item.id)); usedSlots.add(String(slot.id));
      return {placed, slot, item};
    });
    const model = new THREE.Group();
    model.userData = {sessionId: String(layout.sessionId), specId: String(layout.spec.id),
      specVersion: layout.spec.versionNo};
    try {
      for (const {placed, slot, item} of poses) {
        const object = createPart(THREE, item.componentId);
        if (!object) throw new Error("Unsupported saved part");
        model.add(object);
        object.userData.instanceId = String(item.id);
        object.userData.slotId = String(slot.id);
        object.userData.orientation = placed.orientation;
        // Same convention as assembly-3d-placement: slots already contain WORLD coordinates.
        object.position.set(slot.x, slot.y, slot.z);
        object.rotation.set(slot.rx, slot.ry + Number(placed.orientation.slice(1)) * Math.PI / 180, slot.rz);
      }
      return model;
    } catch (error) {
      disposeModel(model);
      throw error;
    }
  }

  let active = null;
  function init() {
    active?.dispose(); active = null;
    const host = document.getElementById("assembly-result-stage");
    const payload = document.getElementById("assembly-result-data");
    if (!host || !payload) return;
    const status = document.getElementById("assembly-result-status");
    const controls = Array.from(document.querySelectorAll("[data-result-camera]"));
    const listeners = [];
    let renderer, model, observer, disposed = false, drag = null;
    const listen = (target, name, fn, options) => {
      target.addEventListener(name, fn, options);
      listeners.push(() => target.removeEventListener(name, fn, options));
    };
    function dispose() {
      if (disposed) return;
      disposed = true;
      listeners.forEach(remove => remove());
      observer?.disconnect();
      drag = null;
      controls.forEach(button => { button.disabled = true; });
      disposeModel(model);
      if (renderer) {
        renderer.dispose();
        renderer.forceContextLoss();
        renderer.domElement.remove();
      }
    }
    function fail() {
      dispose();
      status.textContent = "Chưa hiển thị được mô hình 3D. Bạn vẫn có thể xem bản vẽ 2D, nhật ký và phiếu bên dưới.";
    }
    active = {dispose};
    try {
      const THREE = window.THREE;
      if (!THREE || !window.createAssemblyPart) throw new Error("3D assets unavailable");
      const layout = JSON.parse(payload.textContent);
      model = buildModel(THREE, layout, window.createAssemblyPart);
      const scene = new THREE.Scene();
      scene.background = new THREE.Color(0x151b1f);
      scene.add(model);
      scene.add(new THREE.HemisphereLight(0xffffff, 0x515c64, 2));
      const light = new THREE.DirectionalLight(0xffffff, 2.2);
      light.position.set(5, 10, -7); scene.add(light);
      const fill = new THREE.DirectionalLight(0xffcfa8, 1);
      fill.position.set(-5, 4, 6); scene.add(fill);
      const bounds = new THREE.Box3().setFromObject(model);
      if (bounds.isEmpty()) throw new Error("Empty geometry");
      const center = bounds.getCenter(new THREE.Vector3());
      const radius = Math.max(bounds.getSize(new THREE.Vector3()).length() / 2, 0.1);
      const camera = new THREE.PerspectiveCamera(40, 1, 0.01, radius * 100);
      let azimuth = Math.PI - 0.7, polar = 1.05, distance, fitDistance;
      renderer = new THREE.WebGLRenderer({antialias: true, alpha: false});
      renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
      const canvas = renderer.domElement;
      canvas.tabIndex = 0;
      canvas.setAttribute("role", "img");
      canvas.setAttribute("aria-label", "Mô hình đã lưu. Kéo để xoay góc nhìn; phím mũi tên, cộng, trừ và Home để xem.");
      function render() {
        if (disposed) return;
        camera.position.set(center.x + distance * Math.sin(polar) * Math.sin(azimuth),
          center.y + distance * Math.cos(polar), center.z + distance * Math.sin(polar) * Math.cos(azimuth));
        camera.lookAt(center);
        camera.updateProjectionMatrix();
        renderer.render(scene, camera);
      }
      function resize() {
        if (disposed) return;
        const width = Math.max(host.clientWidth, 1), height = Math.max(host.clientHeight, 1);
        camera.aspect = width / height;
        const halfFov = Math.min(camera.fov * Math.PI / 360,
          Math.atan(Math.tan(camera.fov * Math.PI / 360) * camera.aspect));
        const previousFit = fitDistance;
        fitDistance = radius / Math.sin(halfFov) * 1.12;
        distance = previousFit ? distance * fitDistance / previousFit : fitDistance;
        renderer.setSize(width, height, false);
        render();
      }
      function command(action) {
        if (action === "center") { azimuth = Math.PI - 0.7; polar = 1.05; distance = fitDistance; }
        else distance = Math.max(radius * 1.2, Math.min(fitDistance * 6, distance * (action === "in" ? 0.8 : 1.25)));
        render();
      }
      listen(canvas, "webglcontextlost", event => { event.preventDefault(); fail(); });
      listen(canvas, "pointerdown", event => {
        if (event.button !== 0) return;
        drag = {id: event.pointerId, x: event.clientX, y: event.clientY};
        canvas.setPointerCapture(event.pointerId);
      });
      listen(canvas, "pointermove", event => {
        if (!drag || drag.id !== event.pointerId) return;
        azimuth -= (event.clientX - drag.x) * 0.008;
        polar = Math.max(0.12, Math.min(Math.PI - 0.12, polar + (event.clientY - drag.y) * 0.008));
        drag.x = event.clientX; drag.y = event.clientY;
        render();
      });
      const endDrag = () => { drag = null; };
      listen(canvas, "pointerup", endDrag);
      listen(canvas, "pointercancel", endDrag);
      listen(canvas, "lostpointercapture", endDrag);
      listen(canvas, "keydown", event => {
        if (!["ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown", "+", "=", "-", "Home"].includes(event.key)) return;
        event.preventDefault();
        if (["+", "=", "-", "Home"].includes(event.key)) {
          command(event.key === "Home" ? "center" : event.key === "-" ? "out" : "in"); return;
        }
        azimuth += event.key === "ArrowLeft" ? 0.15 : event.key === "ArrowRight" ? -0.15 : 0;
        polar = Math.max(0.12, Math.min(Math.PI - 0.12,
          polar + (event.key === "ArrowUp" ? -0.15 : event.key === "ArrowDown" ? 0.15 : 0)));
        render();
      });
      controls.forEach(button => {
        listen(button, "click", () => command(button.dataset.resultCamera));
        button.disabled = false;
      });
      host.append(canvas);
      resize();
      if (window.ResizeObserver) {
        observer = new ResizeObserver(resize); observer.observe(host);
      } else listen(window, "resize", resize);
      status.textContent = `${model.children.length} vật thể từ bố cục cuối đã lưu. Kéo để xoay góc nhìn; các nút chỉ đổi camera.`;
    } catch (error) {
      fail();
    }
  }
  window.buildAssemblyResultModel = buildModel;
  window.initAssemblyResultViewer = init;
  window.addEventListener?.("pagehide", () => { active?.dispose(); active = null; });
  window.addEventListener?.("pageshow", event => { if (event.persisted) init(); });
})();
