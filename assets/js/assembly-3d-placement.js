"use strict";

// Chỉ hiển thị pose và gửi ý định; mọi luật gắn/tháo nằm ở JavaBean trên server.
window.createAssemblyPlacementView = function (options) {
  const { THREE, camera, root, canvas, panel, onAttach, onDetach, onChange } = options;
  canvas.classList.add("assembly-placement-canvas");
  const raycaster = new THREE.Raycaster();
  const pointer = new THREE.Vector2();
  const plane = new THREE.Plane(new THREE.Vector3(0, 1, 0), -0.9);
  const point = new THREE.Vector3();
  const objects = new Map();
  const markers = new Map();
  const markerGroup = new THREE.Group();
  root.add(markerGroup);
  let layout = null;
  let selectedInstance = null;
  let selectedSlot = null;
  let orientation = "Y0";
  let drag = null;
  let busy = false;
  let exploded = false;
  let unsynced = false;
  let selectedObject = null;

  const heading = document.createElement("h3");
  heading.textContent = "Vật thể và điểm gắn";
  const help = document.createElement("p");
  help.textContent = "Chọn vật thể → điểm gắn → hướng → Gắn. Có thể kéo vật thể; Q/E xoay, Escape hủy.";
  const live = document.createElement("p");
  live.setAttribute("role", "status");
  live.setAttribute("aria-live", "polite");
  const tray = document.createElement("fieldset");
  const slots = document.createElement("fieldset");
  const directions = document.createElement("fieldset");
  const installed = document.createElement("section");
  installed.setAttribute("aria-label", "Danh sách vật thể đã gắn");
  const attach = document.createElement("button");
  attach.type = "button";
  attach.textContent = "Gắn vật thể";
  const cancel = document.createElement("button");
  cancel.type = "button";
  cancel.textContent = "Hủy chọn";
  const reload = document.createElement("button");
  reload.type = "button";
  reload.textContent = "Tải lại mô hình từ server";
  reload.addEventListener("click", () => options.onReload());
  const retry = document.createElement("button");
  retry.type = "button";
  retry.textContent = "Thử lại thao tác chưa đồng bộ";
  retry.hidden = true;
  const rotateLeft = document.createElement("button");
  rotateLeft.type = "button";
  rotateLeft.textContent = "Xoay trái 90°";
  rotateLeft.addEventListener("click", () => rotate(-1));
  const rotateRight = document.createElement("button");
  rotateRight.type = "button";
  rotateRight.textContent = "Xoay phải 90°";
  rotateRight.addEventListener("click", () => rotate(1));
  panel.replaceChildren(heading, help, live, tray, slots, directions,
    rotateLeft, rotateRight, attach, cancel, retry, reload, installed);
  panel.classList.add("assembly-placement");

  function message(text, state) {
    live.textContent = text;
    live.dataset.state = state;
  }

  function highlight(slotId, color) {
    for (const [id, marker] of markers) {
      const saved = layout?.placements.some(placed => String(placed.slotId) === id);
      marker.material.color.setHex(id === String(slotId) ? color : saved ? 0x4ade80 : 0xa9b6b4);
    }
  }

  function clearSelection() {
    selectedInstance = null;
    selectedSlot = null;
    selectedObject = null;
    orientation = "Y0";
    highlight(null, 0xa9b6b4);
    if (layout) sync(layout);
    message("Đã hủy chọn; mô hình trở về dữ liệu server đã lưu.", "neutral");
  }

  function choose(instanceId) {
    selectedInstance = String(instanceId);
    selectedObject = objects.get(selectedInstance);
    message("Đã chọn vật thể. Chọn điểm gắn và hướng rồi nhấn Gắn.", "preview");
  }

  function rotate(delta) {
    if (!selectedInstance || busy || !layout?.canEdit) return;
    const codes = ["Y0", "Y90", "Y180", "Y270"];
    orientation = codes[(codes.indexOf(orientation) + delta + 4) % 4];
    if (selectedObject) selectedObject.rotation.y += delta * Math.PI / 2;
    message(`Hướng xem trước: ${orientation.slice(1)}°. Chưa đồng bộ.`, "preview");
    directions.querySelectorAll("input").forEach(input => { input.checked = input.value === orientation; });
  }

  async function mutate(kind, instanceId = selectedInstance) {
    if (busy || !layout?.canEdit || !instanceId || (kind === "attach" && !selectedSlot)) return;
    if (exploded) setExploded(false);
    busy = true;
    message("Đang gửi ý định thao tác; chưa đồng bộ…", "preview");
    updateControls();
    try {
      const result = kind === "attach"
        ? await onAttach(instanceId, selectedSlot, orientation, layout.layoutVersion)
        : await onDetach(instanceId, layout.layoutVersion);
      unsynced = false;
      retry.hidden = true;
      sync(result);
      highlight(selectedSlot, 0x4ade80);
      message(result.unchanged ? "Thao tác đã có; không tạo bản ghi mới." : "Server đã xác nhận và lưu thao tác.", "saved");
    } catch (error) {
      sync(layout);
      highlight(selectedSlot, 0xef4444);
      unsynced = !error.status;
      retry.hidden = !unsynced;
      retry.onclick = () => mutate(kind, instanceId);
      message(unsynced ? `Chưa đồng bộ. ${error.message} Có thể thử lại hoặc tải lại.` : error.message,
        unsynced ? "preview" : "error");
    } finally {
      busy = false;
      updateControls();
    }
  }

  function updateControls() {
    const disabled = busy || !layout?.canEdit;
    tray.disabled = disabled;
    slots.disabled = disabled;
    directions.disabled = disabled;
    attach.disabled = disabled;
    cancel.disabled = busy;
    retry.disabled = disabled;
    rotateLeft.disabled = disabled;
    rotateRight.disabled = disabled;
    installed.querySelectorAll("button").forEach(button => { button.disabled = disabled; });
    onChange?.(layout, busy, unsynced);
  }

  function radio(fieldset, name, value, text, checked, change) {
    const label = document.createElement("label");
    const input = document.createElement("input");
    input.type = "radio";
    input.name = name;
    input.value = value;
    input.checked = checked;
    input.addEventListener("change", change);
    label.append(input, document.createTextNode(text));
    fieldset.append(label);
  }

  function resetFieldset(fieldset, text) {
    const legend = document.createElement("legend");
    legend.textContent = text;
    fieldset.replaceChildren(legend);
  }

  function sync(next) {
    if (!next) return;
    layout = next;
    resetFieldset(tray, "Khay linh kiện chưa gắn");
    resetFieldset(slots, "Chọn điểm gắn");
    resetFieldset(directions, "Hướng tương đối với vị trí gốc");
    installed.replaceChildren();
    for (const object of objects.values()) {
      root.remove(object);
      object.traverse(child => {
        child.geometry?.dispose();
        if (Array.isArray(child.material)) child.material.forEach(material => material.dispose());
        else child.material?.dispose();
      });
    }
    objects.clear();
    for (const marker of markers.values()) {
      marker.geometry.dispose();
      marker.material.dispose();
    }
    markers.clear();
    markerGroup.clear();
    let trayIndex = 0;
    for (const item of next.instances) {
      const placed = next.placements.find(p => String(p.instanceId) === String(item.id));
      let object = window.createAssemblyPart(THREE, item.componentId);
      if (!object) object = new THREE.Mesh(new THREE.BoxGeometry(.3, .3, .3), new THREE.MeshStandardMaterial());
      object.userData.instanceId = String(item.id);
      const slot = placed && next.spec.slots.find(s => String(s.id) === String(placed.slotId));
      if (slot) {
        object.position.set(slot.x, slot.y, slot.z);
        object.rotation.set(slot.rx, slot.ry + Number(placed.orientation.slice(1)) * Math.PI / 180, slot.rz);
        const row = document.createElement("p");
        row.append(document.createTextNode(`${item.label} → ${slot.label} · ${placed.orientation.slice(1)}° `));
        const remove = document.createElement("button");
        remove.type = "button";
        remove.textContent = "Tháo";
        remove.setAttribute("aria-label", `Tháo ${item.label}`);
        remove.addEventListener("click", () => mutate("detach", String(item.id)));
        row.append(remove);
        installed.append(row);
      } else {
        object.position.set(-2.4 + (trayIndex % 4) * 1.5, .9, 2.9 + Math.floor(trayIndex / 4) * 1.6);
        object.scale.setScalar(.48);
        trayIndex++;
        radio(tray, "assembly-instance", String(item.id), item.label, selectedInstance === String(item.id),
          () => choose(item.id));
      }
      root.add(object);
      objects.set(String(item.id), object);
    }
    for (const slot of next.spec.slots) {
      const marker = new THREE.Mesh(new THREE.SphereGeometry(.13, 12, 8),
        new THREE.MeshBasicMaterial({
          color: next.placements.some(placed => String(placed.slotId) === String(slot.id)) ? 0x4ade80 : 0xa9b6b4,
          depthTest: false, transparent: true, opacity: .85
        }));
      marker.position.set(slot.x, slot.y + .23, slot.z);
      marker.userData.slotId = String(slot.id);
      markers.set(String(slot.id), marker);
      markerGroup.add(marker);
      radio(slots, "assembly-slot", String(slot.id),
        `${slot.label} · hướng ${slot.orientations.map(code => code.slice(1) + "°").join(", ")}`,
        selectedSlot === String(slot.id), () => {
          selectedSlot = String(slot.id);
          highlight(slot.id, 0xfbbf24);
          message("Điểm gắn xem trước; server chưa xác nhận.", "preview");
        });
    }
    for (const code of ["Y0", "Y90", "Y180", "Y270"]) {
      radio(directions, "assembly-orientation", code, code.slice(1) + "°", orientation === code,
        () => {
          if (selectedInstance) rotate((Number(code.slice(1)) - Number(orientation.slice(1))) / 90);
          else orientation = code;
        });
    }
    if (!trayIndex) tray.append(document.createTextNode("Khay không còn vật thể."));
    if (!next.placements.length) installed.append(document.createTextNode("Chưa có vật thể được gắn."));
    selectedObject = objects.get(selectedInstance);
    updateControls();
  }

  function ray(event) {
    const rect = canvas.getBoundingClientRect();
    pointer.set((event.clientX - rect.left) / rect.width * 2 - 1,
      -(event.clientY - rect.top) / rect.height * 2 + 1);
    raycaster.setFromCamera(pointer, camera);
  }

  function pointerDown(event) {
    if (!layout || busy || !layout.canEdit || exploded) return false;
    ray(event);
    const markerHit = raycaster.intersectObjects([...markers.values()], false)[0];
    if (markerHit && selectedInstance) {
      selectedSlot = markerHit.object.userData.slotId;
      highlight(selectedSlot, 0xfbbf24);
      message("Đã chọn điểm gắn. Nhấn Gắn để gửi server.", "preview");
      return true;
    }
    const hit = raycaster.intersectObjects([...objects.values()], true)[0];
    if (!hit) return false;
    let object = hit.object;
    while (!object.userData.instanceId && object.parent) object = object.parent;
    if (!object.userData.instanceId) return false;
    choose(object.userData.instanceId);
    drag = { object, oldPosition: object.position.clone(), pointerId: event.pointerId };
    canvas.setPointerCapture(event.pointerId);
    return true;
  }

  function pointerMove(event) {
    if (!drag || event.pointerId !== drag.pointerId) return false;
    ray(event);
    if (raycaster.ray.intersectPlane(plane, point)) {
      root.worldToLocal(point);
      drag.object.position.set(THREE.MathUtils.clamp(point.x, -6, 6), .9,
        THREE.MathUtils.clamp(point.z, -5, 6));
      let nearest = null;
      let distance = .9;
      for (const slot of layout.spec.slots) {
        const d = Math.hypot(point.x - slot.x, point.z - slot.z);
        if (d < distance) { distance = d; nearest = slot; }
      }
      selectedSlot = nearest ? String(nearest.id) : null;
      highlight(selectedSlot, 0xfbbf24);
      message(nearest ? `Xem trước: ${nearest.label}. Chưa đồng bộ.` : "Kéo gần một điểm gắn hoặc hủy chọn.", "preview");
    }
    return true;
  }

  function pointerUp(event) {
    if (!drag || event.pointerId !== drag.pointerId) return false;
    drag.object.position.copy(drag.oldPosition);
    drag = null;
    if (selectedSlot) void mutate("attach");
    else message("Chưa chọn điểm gắn; vật thể đã trở về vị trí trước.", "neutral");
    return true;
  }

  function setExploded(value) {
    exploded = Boolean(value);
    sync(layout);
    if (exploded) {
      let index = 0;
      for (const placed of layout.placements) {
        const object = objects.get(String(placed.instanceId));
        object.position.x += Math.cos(index) * 1.3;
        object.position.z += Math.sin(index) * 1.3;
        object.position.y += .6;
        index++;
      }
    }
    markerGroup.visible = !exploded;
    options.onExplodedChange?.(exploded);
    message(exploded ? "Đang tách hình để quan sát; không thay đổi dữ liệu. Ghép lại trước khi kéo." : "Đã ghép hình.", "neutral");
  }

  function keydown(event) {
    if (!selectedInstance || busy || !layout?.canEdit) return;
    if (event.key === "Escape") { drag = null; clearSelection(); }
    else if (event.target.matches("textarea, input:not([type='radio']):not([type='checkbox'])")) return;
    else if (event.key.toLowerCase() === "q") rotate(-1);
    else if (event.key.toLowerCase() === "e") rotate(1);
    else return;
    event.preventDefault();
  }
  window.addEventListener("keydown", keydown);
  attach.addEventListener("click", () => mutate("attach"));
  cancel.addEventListener("click", clearSelection);
  function restore(next) {
    unsynced = false;
    retry.hidden = true;
    sync(next);
    message("Đã tải lại mô hình từ server.", "saved");
  }
  return { sync, restore, pointerDown, pointerMove, pointerUp, setExploded,
    isBusy: () => busy, isUnsynced: () => unsynced,
    cancelDrag: () => { drag = null; if (layout) sync(layout); },
    dispose: () => window.removeEventListener("keydown", keydown) };
};
