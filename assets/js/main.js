"use strict";

const robotModels = window.ROBOT_MODELS || [];
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

// Render trang Linh Kiện (pages/linh-kien.html)
function setupComponents() {
  const componentContainer = document.querySelector("#component-list");
  if (!componentContainer || componentsData.length === 0) return;

  componentContainer.innerHTML = componentsData.map((item) => `
    <article class="card component-card">
      <figure class="media-placeholder card-image">
        <img src="${escapeHtml(item.image)}" alt="${escapeHtml(item.name)}" width="1024" height="1024" loading="lazy" decoding="async">
      </figure>
      <span class="card-kicker">${escapeHtml(item.category)}</span>
      <h3>${escapeHtml(item.name)}</h3>
      <p>${escapeHtml(item.description)}</p>
    </article>
  `).join("");
}

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
function setupAssembly() {
  const modelSelect = document.querySelector("#model-select");
  if (!modelSelect || robotModels.length === 0) return;

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

  const DIAL_LENGTH = 339.29; // chu vi đường tròn bán kính 54

  modelSelect.innerHTML = robotModels.map((model) =>
      `<option value="${escapeHtml(model.id)}">${escapeHtml(model.name)} · ${escapeHtml(model.level)}</option>`
  ).join("");

  const requestedModel = new URLSearchParams(window.location.search).get("model");
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

  function updateProgress(persist) {
    const items = [...checklist.querySelectorAll("input[type='checkbox']")];
    const selected = items.filter((item) => item.checked).length;
    const percent = Math.round((selected / items.length) * 100);

    progress.value = percent;
    progressValue.textContent = `${percent}%`;
    status.textContent = percent === 100
        ? "Đã đủ linh kiện. Bạn có thể thực hiện các bước lắp ráp."
        : `Còn thiếu ${items.length - selected} nhóm linh kiện.`;

    status.classList.toggle("is-complete", percent === 100);
    steps.classList.toggle("is-ready", percent === 100);

    if (dialValue) {
      dialValue.style.strokeDasharray = String(DIAL_LENGTH);
      dialValue.style.strokeDashoffset = String(DIAL_LENGTH * (1 - percent / 100));
    }
    if (dialLabel) dialLabel.textContent = `${percent}%`;

    if (persist) savePartsState(items);
  }

  function renderWiring(model) {
    if (!wiringBody) return;

    const rows = model.wiring || [];
    wiringBody.innerHTML = rows.map((row) => `
      <tr>
        <th scope="row"><span class="pin-code">${escapeHtml(row.pin)}</span></th>
        <td>${escapeHtml(row.target)}</td>
        <td>${escapeHtml(row.note)}</td>
      </tr>
    `).join("");

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
        <input id="part-${index}" type="checkbox">
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

  checklist.addEventListener("change", () => updateProgress(true));

  modelSelect.addEventListener("change", () => {
    storage.write("ral.model", modelSelect.value);
    renderSelectedModel(true);
  });

  if (resetButton) {
    resetButton.addEventListener("click", () => {
      storage.remove(partsKey());
      checklist.querySelectorAll("input[type='checkbox']").forEach((item) => { item.checked = false; });
      updateProgress(false);
    });
  }

  renderSelectedModel(true);
}

// Khởi chạy hệ thống
setupComponents();
setupComponentSpecs();
setupCompareTable();
setupCatalog();
setupAssembly();
