"use strict";

// View chỉ quản lý cặp chân và form; mọi validation/lưu/chấm thuộc server.
(() => {
  const form = document.querySelector(".wiring #wiring-form");
  if (!form) return;
  const svg = document.querySelector(".wiring svg");
  const payload = document.querySelector("#wiring-payload");
  const list = document.querySelector("#wiring-connections");
  const status = document.querySelector("#wiring-status");
  const cancel = document.querySelector("#wiring-cancel");
  const remove = document.querySelector("#wiring-delete");
  const terminals = new Map();
  const wires = new Map();
  let pending = null;
  let selectedWire = null;
  let pointerStart = null;
  let dirty = false;
  for (const node of svg.querySelectorAll("[data-terminal-id]")) {
    terminals.set(node.dataset.terminalId, node);
  }
  for (const input of payload.querySelectorAll("input[name=pair]")) {
    wires.set(input.value, input.value.split(":"));
  }
  const announce = message => {
    status.textContent = message + (dirty ? " Chưa lưu; hãy bấm Lưu nháp hoặc Nộp để chấm." : "");
  };
  const label = id => terminals.get(id).getAttribute("aria-label");
  const clearPending = () => {
    pending = null;
    for (const node of terminals.values()) node.classList.remove("is-selected");
  };
  function render() {
    payload.replaceChildren();
    list.replaceChildren();
    for (const old of svg.querySelectorAll(".wiring-wire")) old.remove();
    for (const [key, ends] of wires) {
      const input = document.createElement("input");
      input.type = "hidden";
      input.name = "pair";
      input.value = key;
      payload.append(input);
      const row = document.createElement("label");
      const choice = document.createElement("input");
      choice.type = "radio";
      choice.name = "removePair";
      choice.value = key;
      row.append(choice, document.createTextNode(label(ends[0]) + " ↔ " + label(ends[1])));
      list.append(row);
      const line = document.createElementNS("http://www.w3.org/2000/svg", "line");
      const a = terminals.get(ends[0]);
      const b = terminals.get(ends[1]);
      line.setAttribute("x1", a.dataset.x);
      line.setAttribute("y1", a.dataset.y);
      line.setAttribute("x2", b.dataset.x);
      line.setAttribute("y2", b.dataset.y);
      line.setAttribute("class", "wiring-wire");
      line.setAttribute("tabindex", "0");
      line.setAttribute("role", "button");
      line.setAttribute("aria-label", "Chọn dây " + label(ends[0]) + " ↔ " + label(ends[1]));
      line.dataset.pair = key;
      svg.insertBefore(line, svg.firstElementChild);
    }
  }
  function connect(a, b) {
    if (a === b) {
      clearPending();
      announce("Không thể nối một chân với chính nó.");
      return;
    }
    const ends = [a, b].sort((x, y) => Number(x) - Number(y));
    const key = ends.join(":");
    if (!wires.has(key) && wires.size >= 80) {
      announce("Tối đa 80 dây; xóa bớt dây trước khi nối.");
      return;
    }
    const existed = wires.has(key);
    wires.set(key, ends);
    dirty = dirty || !existed;
    clearPending();
    render();
    announce(existed ? "Cặp này đã có; không tạo dây trùng." : "Đã nối " + label(a) + " ↔ " + label(b) + ".");
  }
  function choose(node) {
    if (node.dataset.terminalId) {
      selectedWire = null;
      const id = node.dataset.terminalId;
      if (pending) connect(pending, id);
      else {
        pending = id;
        node.classList.add("is-selected");
        announce("Đã chọn " + label(id) + ". Chọn chân thứ hai hoặc Escape để hủy.");
      }
    } else if (node.dataset.pair) {
      selectedWire = node.dataset.pair;
      announce("Đã chọn dây; bấm Xóa dây đang chọn.");
      for (const line of svg.querySelectorAll(".wiring-wire")) {
        line.classList.toggle("is-selected", line === node);
      }
    }
  }
  svg.addEventListener("pointerdown", event => {
    pointerStart = event.target.closest("[data-terminal-id]");
  });
  svg.addEventListener("pointerup", event => {
    const target = document.elementFromPoint(event.clientX, event.clientY)?.closest("[data-terminal-id],[data-pair]");
    if (target && svg.contains(target)) {
      if (pointerStart && target.dataset.terminalId && pointerStart !== target) {
        connect(pointerStart.dataset.terminalId, target.dataset.terminalId);
      } else choose(target);
    }
    pointerStart = null;
  });
  svg.addEventListener("pointercancel", () => { pointerStart = null; });
  svg.addEventListener("keydown", event => {
    if (event.key === "Escape") {
      clearPending();
      announce("Đã hủy chân đang chọn.");
    } else if (["Enter", " "].includes(event.key)) {
      const node = event.target.closest("[data-terminal-id],[data-pair]");
      if (node) {
        event.preventDefault();
        choose(node);
      }
    }
  });
  cancel.hidden = false;
  remove.hidden = false;
  form.querySelector('button[value="add"]').textContent = "Thêm dây (chưa lưu)";
  form.querySelector('button[value="remove"]').textContent = "Xóa dây đã chọn (chưa lưu)";
  cancel.addEventListener("click", () => {
    clearPending();
    announce("Đã hủy chân đang chọn.");
  });
  function deleteSelected(key) {
    if (!key || !wires.has(key)) {
      announce("Hãy chọn một dây trong sơ đồ hoặc danh sách.");
      return;
    }
    wires.delete(key);
    selectedWire = null;
    dirty = true;
    render();
    announce("Đã xóa dây.");
  }
  remove.addEventListener("click", () => deleteSelected(selectedWire));
  form.addEventListener("submit", event => {
    const action = event.submitter?.value;
    if (action === "add") {
      event.preventDefault();
      const a = form.querySelector("input[name=terminalA]:checked");
      const b = form.querySelector("input[name=terminalB]:checked");
      if (a && b) connect(a.value, b.value);
      else announce("Chọn đủ chân A và chân B.");
    } else if (action === "remove") {
      event.preventDefault();
      deleteSelected(form.querySelector("input[name=removePair]:checked")?.value);
    }
  });
})();
