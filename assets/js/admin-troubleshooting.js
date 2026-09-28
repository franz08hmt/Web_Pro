"use strict";

(() => {
  const $ = (id) => document.getElementById(id);
  const api = window.RobotAssemblyApi;
  let selectedId = null;
  let guides = [];

  function status(message, state = "") {
    $("status").textContent = message;
    $("status").dataset.state = state;
  }

  function lockOut(message) {
    $("status").hidden = true;
    $("gate-message").textContent = message;
    $("admin-gate").hidden = false;
  }

  function markSelected() {
    for (const button of $("items").querySelectorAll("button[data-guide-id]")) {
      button.setAttribute("aria-current", String(button.dataset.guideId === selectedId));
    }
  }

  function renderList() {
    $("items").replaceChildren();
    if (guides.length === 0) {
      const empty = document.createElement("li");
      empty.textContent = "Chưa có tình huống nào.";
      $("items").append(empty);
      return;
    }
    guides.forEach((guide) => {
      const li = document.createElement("li");
      const button = document.createElement("button");
      button.type = "button";
      button.dataset.guideId = guide.id;
      button.textContent = `${guide.symptom} (${guide.robotId || "dùng chung"})`;
      button.addEventListener("click", () => renderForm(guide));
      li.append(button);
      $("items").append(li);
    });
    markSelected();
  }

  function renderForm(guide) {
    selectedId = guide?.id || null;
    $("editor-title").textContent = selectedId ? `Sửa: ${selectedId}` : "Thêm tình huống";
    $("delete").hidden = !selectedId;
    $("field-id").value = guide?.id || "";
    $("field-id").disabled = Boolean(selectedId);
    $("field-robot").value = guide?.robotId || "";
    $("field-group").value = guide?.componentGroup || "";
    $("field-order").value = guide?.displayOrder || guides.length + 1;
    $("field-symptom").value = guide?.symptom || "";
    $("field-causes").value = guide?.possibleCauses || "";
    $("field-steps").value = guide?.resolutionSteps || "";
    $("field-component").value = guide?.relatedComponentId || "";
    markSelected();
  }

  async function loadGuides() {
    status("Đang tải…", "loading");
    try {
      guides = await api.adminTroubleshooting.list();
      renderList();
      status("");
    } catch (error) {
      status(`Không thể tải danh sách. ${error.message}`, "error");
    }
  }

  $("new-guide").addEventListener("click", () => renderForm(null));

  $("editor").addEventListener("submit", async (event) => {
    event.preventDefault();
    const data = {
      id: $("field-id").value.trim(),
      robotId: $("field-robot").value,
      componentGroup: $("field-group").value.trim(),
      symptom: $("field-symptom").value.trim(),
      possibleCauses: $("field-causes").value.trim(),
      resolutionSteps: $("field-steps").value.trim(),
      relatedComponentId: $("field-component").value.trim(),
      displayOrder: $("field-order").value
    };
    status("Đang lưu…", "loading");
    try {
      if (selectedId) {
        await api.adminTroubleshooting.update(selectedId, data);
      } else {
        await api.adminTroubleshooting.create(data);
      }
      status("Đã lưu tình huống.", "saved");
      await loadGuides();
      renderForm(null);
    } catch (error) {
      status(`Không thể lưu. ${error.message}`, "error");
    }
  });

  $("delete").addEventListener("click", async () => {
    if (!selectedId) return;
    status("Đang xóa…", "loading");
    try {
      await api.adminTroubleshooting.remove(selectedId);
      status("Đã xóa tình huống.", "saved");
      await loadGuides();
      renderForm(null);
    } catch (error) {
      status(`Không thể xóa. ${error.message}`, "error");
    }
  });

  async function start() {
    let session;
    try {
      session = await api.auth.me();
    } catch (error) {
      lockOut(error.code === "AUTH_REQUIRED"
        ? "Cần đăng nhập bằng tài khoản quản trị."
        : `Không thể kiểm tra phiên đăng nhập. ${error.message}`);
      return;
    }
    if (session.role !== "ADMIN") {
      lockOut(`Tài khoản ${session.email} không có quyền quản trị.`);
      return;
    }

    const robots = window.ROBOT_MODELS || [];
    robots.forEach((robot) => {
      const option = document.createElement("option");
      option.value = robot.id;
      option.textContent = robot.name;
      $("field-robot").append(option);
    });

    $("status").hidden = true;
    $("workspace").hidden = false;
    $("workspace").disabled = false;
    renderForm(null);
    void loadGuides();
  }

  void start();
})();
