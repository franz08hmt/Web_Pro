"use strict";

let robotModels = window.ROBOT_MODELS || [];
const componentsData = window.COMPONENTS_DATA || [];

// Hàm mã hóa chống lỗ hổng bảo mật Cross-Site Scripting (XSS)
function escapeHtml(value) {
  return String(value)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
}

// Đọc/ghi localStorage an toàn: chế độ riêng tư có thể ném lỗi.
const storage = {
  read(key) {
    try {
      return window.localStorage.getItem(key);
    } catch (error) {
      return null;
    }
  },
  write(key, value) {
    try {
      window.localStorage.setItem(key, value);
    } catch (error) {
      /* Bỏ qua: trạng thái đã lưu chỉ là tiện ích, không phải chức năng bắt buộc. */
    }
  },
  remove(key) {
    try {
      window.localStorage.removeItem(key);
    } catch (error) {
      /* Bỏ qua. */
    }
  }
};

// Bổ sung bảng thông số kỹ thuật vào các thẻ linh kiện tĩnh.
// Số lượng thẻ trong HTML không thay đổi, chỉ thêm nội dung bên trong.
function setupComponentSpecs() {
  const cards = document.querySelectorAll("[data-component-id]");
  if (cards.length === 0) return;

  cards.forEach((card) => {
    const item = componentsData.find((entry) => entry.id === card.dataset.componentId);
    if (!item || !item.specs) return;

    const rows = Object.entries(item.specs).map(([label, value]) => `
      <dt>${escapeHtml(label)}</dt><dd>${escapeHtml(value)}</dd>
    `).join("");

    card.insertAdjacentHTML("beforeend", `
      <details class="spec-details">
        <summary>Thông số kỹ thuật</summary>
        <dl class="spec-list">${rows}</dl>
      </details>
    `);
  });
}

// Bảng so sánh ba mẫu robot (pages/mau-robot.html)
function setupCompareTable() {
  const body = document.querySelector("#compare-body");
  if (!body || robotModels.length === 0) return;

  body.innerHTML = robotModels.map((model) => `
    <tr>
      <th scope="row">${escapeHtml(model.name)}</th>
      <td>${escapeHtml(model.level)}</td>
      <td>${model.parts.length}</td>
      <td>${model.steps.length}</td>
      <td>${escapeHtml(model.buildTime || "—")}</td>
      <td>${escapeHtml(model.mainSensor || "—")}</td>
      <td>${escapeHtml(model.skills || "—")}</td>
    </tr>
  `).join("");
}

// Render trang Danh Mục Robot (pages/mau-robot.html)
function renderCatalog(models) {
  const catalog = document.querySelector("#robot-catalog");
  const emptyState = document.querySelector("#robot-empty");
  if (!catalog || !emptyState) return;

  catalog.innerHTML = models.map((model) => `
    <article class="card robot-card">
      <figure class="media-placeholder card-image">
        <img src="${escapeHtml(model.image)}" alt="${escapeHtml(model.name)}" width="1024" height="1024" loading="lazy" decoding="async">
      </figure>
      <p class="card-kicker">${escapeHtml(model.level)}</p>
      <h2>${escapeHtml(model.name)}</h2>
      <p>${escapeHtml(model.summary)}</p>
      <p><strong>${model.parts.length}</strong> nhóm linh kiện · <strong>${model.steps.length}</strong> bước · <strong>${escapeHtml(model.buildTime || "—")}</strong></p>
      <a class="card-link" href="lap-rap.html?model=${encodeURIComponent(model.id)}">Chọn mẫu này →</a>
    </article>
  `).join("");

  emptyState.hidden = models.length !== 0;
}

function setupCatalog() {
  const searchInput = document.querySelector("#robot-search");
  if (!searchInput) return;

  renderCatalog(robotModels);
  searchInput.addEventListener("input", () => {
    const keyword = searchInput.value.trim().toLocaleLowerCase("vi");
    const filtered = robotModels.filter((model) =>
        `${model.name} ${model.summary} ${model.level}`.toLocaleLowerCase("vi").includes(keyword)
    );
    renderCatalog(filtered);
  });
}

// Render trang Lắp Ráp & Xử lý Tiến Độ (pages/lap-rap.html)
async function setupAssembly() {
  const modelSelect = document.querySelector("#model-select");
  const start3DButton = document.querySelector("#start-3d-assembly");
  if (!modelSelect) return;

  const checklist = document.querySelector("#parts-checklist");
  const steps = document.querySelector("#assembly-steps");
  const summary = document.querySelector("#model-summary");
  const progress = document.querySelector("#parts-progress");
  const progressValue = document.querySelector("#progress-value");
  const status = document.querySelector("#assembly-status");
  const modelImage = document.querySelector("#model-preview-image");

  // Phần bổ sung của Phase 2, có thể vắng mặt nên luôn kiểm tra trước khi dùng.
  const wiringBody = document.querySelector("#wiring-body");
  const wiringCaption = document.querySelector("#wiring-caption");
  const dialValue = document.querySelector("#dial-value");
  const dialLabel = document.querySelector("#dial-label");
  const resetButton = document.querySelector("#reset-progress");
  const sessionApi = window.RobotAssemblyApi?.assemblySessions;
  let activeSession = null;
  let selectionRevision = 0;
  const params = new URLSearchParams(window.location.search);
  let requestedSessionId = params.get("session");

  const DIAL_LENGTH = 339.29; // chu vi đường tròn bán kính 54

  if (window.RobotContentApi?.enabled) {
    status.textContent = "Đang tải dữ liệu lắp ráp từ Content API…";
    try {
      robotModels = await window.RobotContentApi.loadRobots() || robotModels;
    } catch (error) {
      status.textContent = `Không thể đồng bộ Content API; đang dùng dữ liệu dự phòng. ${error.message}`;
    }
  }
  if (robotModels.length === 0) return;

  modelSelect.innerHTML = robotModels.map((model) =>
      `<option value="${escapeHtml(model.id)}">${escapeHtml(model.name)} · ${escapeHtml(model.level)}</option>`
  ).join("");

  const requestedModel = params.get("model");
  const storedModel = storage.read("ral.model");

  // Query string do người dùng vừa bấm luôn được ưu tiên hơn trạng thái đã lưu.
  if (robotModels.some((model) => model.id === requestedModel)) {
    modelSelect.value = requestedModel;
  } else if (robotModels.some((model) => model.id === storedModel)) {
    modelSelect.value = storedModel;
  }

  function partsKey() {
    return `ral.parts.${modelSelect.value}`;
  }

  function savePartsState(items) {
    storage.write(partsKey(), JSON.stringify(items.map((item) => item.checked)));
  }

  function restorePartsState(items) {
    const raw = storage.read(partsKey());
    if (!raw) return;

    try {
      const saved = JSON.parse(raw);
      if (!Array.isArray(saved) || saved.length !== items.length) return;
      items.forEach((item, index) => { item.checked = saved[index] === true; });
    } catch (error) {
      storage.remove(partsKey());
    }
  }

  function updateProgress(persist, notice = "") {
    const items = [...checklist.querySelectorAll("input[type='checkbox']")];
    const selected = items.filter((item) => item.checked).length;
    const percent = items.length === 0 ? 0 : Math.round((selected / items.length) * 100);

    progress.value = percent;
    progressValue.textContent = `${percent}%`;
    status.textContent = notice || (percent === 100
        ? "Đã đủ linh kiện. Bạn có thể thực hiện các bước lắp ráp."
        : `Còn thiếu ${items.length - selected} nhóm linh kiện.`);

    if (start3DButton) {
      start3DButton.hidden = percent !== 100;
    }

    if (percent === 100 && start3DButton) {
      start3DButton.href =
          `lap-rap-3d.html?model=${encodeURIComponent(modelSelect.value)}${activeSession ? `&session=${encodeURIComponent(activeSession.id)}` : ""}`;
    }

    status.classList.toggle("is-complete", percent === 100);
    steps.classList.toggle("is-ready", percent === 100);

    if (dialValue) {
      dialValue.style.strokeDasharray = String(DIAL_LENGTH);
      dialValue.style.strokeDashoffset = String(DIAL_LENGTH * (1 - percent / 100));
    }
    if (dialLabel) dialLabel.textContent = `${percent}%`;

    if (persist) savePartsState(items);
  }

  /* Bảng nối dây đầy ký hiệu chỉ người đã học điện tử mới đọc được. Chú thích
     giải nghĩa từng ký hiệu ngay tại chỗ, nhưng chữ gốc vẫn giữ nguyên để
     người dùng đối chiếu được với chân in trên mạch thật. */
  const WIRING_GLOSSARY = new Map([
    ["HC-SR04", "HC-SR04 — cảm biến siêu âm, đo khoảng cách bằng thời gian dội của xung."],
    ["L298N", "L298N — module cầu H, nhận tín hiệu điều khiển và cấp dòng lớn cho động cơ."],
    ["VCC", "VCC — chân cấp nguồn dương cho module; ở bài này lấy 5V từ Arduino."],
    ["GND", "GND — chân mass, mức 0V. Mọi module phải nối chung GND thì tín hiệu mới đúng."],
    ["PWM", "PWM — điều chế độ rộng xung, dùng chỉnh tốc độ động cơ thay vì chỉ bật/tắt."],
    ["TRIG", "TRIG — chân kích, nhận xung 10 µs để cảm biến phát sóng siêu âm."],
    ["ECHO", "ECHO — chân phản hồi, độ rộng xung tỉ lệ với khoảng cách đo được."],
    ["ENA", "ENA — chân cho phép kênh A của L298N, nhận PWM để chỉnh tốc độ."],
    ["ENB", "ENB — chân cho phép kênh B của L298N, nhận PWM để chỉnh tốc độ."],
    ["IN1", "IN1 — cùng IN2 quyết định chiều quay của kênh động cơ A."],
    ["IN2", "IN2 — cùng IN1 quyết định chiều quay của kênh động cơ A."],
    ["IN3", "IN3 — cùng IN4 quyết định chiều quay của kênh động cơ B."],
    ["IN4", "IN4 — cùng IN3 quyết định chiều quay của kênh động cơ B."]
  ]);
  // Ký hiệu dài đứng trước để HC-SR04 không bị cắt rời thành các mảnh ngắn hơn.
  const WIRING_PATTERN = new RegExp(`(${[...WIRING_GLOSSARY.keys()].join("|")})`, "g");

  function annotateWiring(text) {
    const fragment = document.createDocumentFragment();
    let lastIndex = 0;
    for (const match of String(text).matchAll(WIRING_PATTERN)) {
      if (match.index > lastIndex) {
        fragment.append(text.slice(lastIndex, match.index));
      }
      const term = document.createElement("abbr");
      term.textContent = match[0];
      term.dataset.tooltip = WIRING_GLOSSARY.get(match[0]);
      fragment.append(term);
      lastIndex = match.index + match[0].length;
    }
    fragment.append(text.slice(lastIndex));
    return fragment;
  }

  function renderWiring(model) {
    if (!wiringBody) return;

    const rows = model.wiring || [];
    wiringBody.replaceChildren(...rows.map((row) => {
      const tr = document.createElement("tr");
      const pin = document.createElement("th");
      pin.scope = "row";
      const code = document.createElement("span");
      code.className = "pin-code";
      code.append(annotateWiring(row.pin));
      pin.append(code);
      const target = document.createElement("td");
      target.append(annotateWiring(row.target));
      const note = document.createElement("td");
      note.append(annotateWiring(row.note));
      tr.append(pin, target, note);
      return tr;
    }));

    if (wiringCaption) {
      wiringCaption.textContent = `Sơ đồ nối dây của ${model.name}: ${rows.length} kết nối cần thực hiện.`;
    }
  }

  function renderSelectedModel(restore) {
    const model = robotModels.find((item) => item.id === modelSelect.value) || robotModels[0];
    summary.textContent = model.summary;

    if (modelImage) {
      modelImage.src = model.image;
      modelImage.alt = model.name;
    }

    checklist.innerHTML = model.parts.map((part, index) => `
      <label class="check-item" for="part-${index}">
        <input id="part-${index}" type="checkbox" data-component-id="${escapeHtml(part.id)}">
        <span>${escapeHtml(part.name)} <strong>× ${part.quantity}</strong></span>
        <svg class="icon tick-icon" width="24" height="24" focusable="false" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" aria-hidden="true">
          <path class="tick" stroke-linecap="round" stroke-linejoin="round" d="m4.5 12.75 6 6 9-13.5"/>
        </svg>
      </label>
    `).join("");

    steps.innerHTML = model.steps.map((step) => `<li>${escapeHtml(step)}</li>`).join("");
    renderWiring(model);

    if (restore) restorePartsState([...checklist.querySelectorAll("input[type='checkbox']")]);
    updateProgress(false);
  }

  function applySession(session, notice) {
    activeSession = session;
    const prepared = new Set(
      (session.components || []).filter((item) => item.isPrepared).map((item) => item.componentId)
    );
    const inputs = [...checklist.querySelectorAll("input[data-component-id]")];
    inputs.forEach((input) => { input.checked = prepared.has(input.dataset.componentId); });
    savePartsState(inputs);
    updateProgress(false, notice);
  }

  async function restoreSession() {
    const revision = ++selectionRevision;
    activeSession = null;
    let restoreError = false;
    const inputs = [...checklist.querySelectorAll("input[data-component-id]")];
    if (!sessionApi) {
      if (requestedSessionId) {
        inputs.forEach((input) => { input.disabled = true; });
        if (resetButton) resetButton.disabled = true;
        if (start3DButton) start3DButton.hidden = true;
        updateProgress(false, "Không thể mở phiên đã chọn vì API phiên chưa sẵn sàng.");
        return;
      }
      updateProgress(false, "Tiến độ đang được lưu trên trình duyệt này.");
      return;
    }

    inputs.forEach((input) => { input.disabled = true; });
    updateProgress(false, "Đang khôi phục tiến độ đã lưu…");
    try {
      const session = requestedSessionId
        ? await sessionApi.get(requestedSessionId)
        : await sessionApi.createOrResume(modelSelect.value);
      if (revision !== selectionRevision) return;
      if (session.robotId !== modelSelect.value) {
        throw new Error("Phiên này không thuộc mẫu robot đã chọn.");
      }
      applySession(session, "Đã đồng bộ tiến độ với tài khoản của bạn.");
      if (["IN_PROGRESS", "COMPLETED", "ABANDONED"].includes(session.status)) {
        status.textContent = session.status === "COMPLETED"
          ? "Phiên đã hoàn thành; chỉ xem tiến độ chuẩn bị."
          : session.status === "ABANDONED"
            ? "Phiên đã dừng; không thể chỉnh sửa."
            : "Phiên đang lắp ráp 3D; phần chuẩn bị chỉ để xem.";
        if (session.status === "ABANDONED" && start3DButton) start3DButton.hidden = true;
      }
    } catch (error) {
      if (revision !== selectionRevision) return;
      restoreError = Boolean(requestedSessionId);
      const message = error.code === "AUTH_REQUIRED"
        ? (restoreError ? "Đăng nhập để mở đúng phiên đã chọn." : "Đăng nhập để lưu tiến độ trên tài khoản; hiện đang lưu trên trình duyệt này.")
        : (restoreError ? `Không thể mở phiên đã chọn. ${error.message}` : `Không thể đồng bộ tiến độ; đang dùng dữ liệu trên trình duyệt. ${error.message}`);
      updateProgress(false, message);
      if (restoreError && start3DButton) start3DButton.hidden = true;
    } finally {
      if (revision === selectionRevision) {
        const readOnly = restoreError || (activeSession && !["PREPARING", "READY"].includes(activeSession.status));
        inputs.forEach((input) => { input.disabled = Boolean(readOnly); });
        if (resetButton) resetButton.disabled = Boolean(readOnly);
      }
    }
  }

  checklist.addEventListener("change", async (event) => {
    const input = event.target.closest("input[data-component-id]");
    if (!input) return;
    const previous = !input.checked;
    updateProgress(true, activeSession ? "Đang lưu tiến độ…" : "Tiến độ đã lưu trên trình duyệt này.");
    if (!activeSession || !sessionApi) return;

    input.disabled = true;
    try {
      activeSession = await sessionApi.setComponentPrepared(
        activeSession.id,
        input.dataset.componentId,
        input.checked
      );
      updateProgress(true, "Đã lưu tiến độ vào tài khoản.");
    } catch (error) {
      input.checked = previous;
      updateProgress(true, `Không thể lưu thay đổi. ${error.message}`);
    } finally {
      input.disabled = false;
    }
  });

  modelSelect.addEventListener("change", async () => {
    requestedSessionId = null;
    const url = new URL(window.location.href);
    url.searchParams.set("model", modelSelect.value);
    url.searchParams.delete("session");
    window.history.replaceState(null, "", url);
    storage.write("ral.model", modelSelect.value);
    renderSelectedModel(true);
    await restoreSession();
  });

  if (resetButton) {
    resetButton.addEventListener("click", async () => {
      storage.remove(partsKey());
      const inputs = [...checklist.querySelectorAll("input[data-component-id]")];
      const preparedIds = inputs.filter((item) => item.checked).map((item) => item.dataset.componentId);
      inputs.forEach((item) => { item.checked = false; });
      updateProgress(false, activeSession ? "Đang đặt lại tiến độ…" : "Đã đặt lại tiến độ trên trình duyệt này.");
      if (!activeSession || !sessionApi || preparedIds.length === 0) return;

      resetButton.disabled = true;
      try {
        for (const componentId of preparedIds) {
          activeSession = await sessionApi.setComponentPrepared(activeSession.id, componentId, false);
        }
        updateProgress(false, "Đã đặt lại tiến độ trong tài khoản.");
      } catch (error) {
        try {
          applySession(await sessionApi.get(activeSession.id), `Không thể đặt lại toàn bộ. ${error.message}`);
        } catch {
          updateProgress(false, `Không thể khôi phục tiến độ. ${error.message}`);
        }
      } finally {
        resetButton.disabled = false;
      }
    });
  }

  renderSelectedModel(true);
  await restoreSession();
}

// Khởi chạy hệ thống
setupComponentSpecs();
setupCompareTable();
setupCatalog();
void setupAssembly();
