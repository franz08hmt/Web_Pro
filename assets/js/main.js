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

// Render trang Linh Kiện (pages/linh-kien.html)
function setupComponents() {
  const componentContainer = document.querySelector("#component-list");
  if (!componentContainer || componentsData.length === 0) return;

  componentContainer.innerHTML = componentsData.map((item) => `
    <article class="card component-card">
      <div class="media-placeholder card-image">
        <img src="${escapeHtml(item.image)}" alt="${escapeHtml(item.name)}">
      </div>
      <span class="card-kicker">${escapeHtml(item.category)}</span>
      <h3>${escapeHtml(item.name)}</h3>
      <p>${escapeHtml(item.description)}</p>
    </article>
  `).join("");
}

// Render trang Danh Mục Robot (pages/mau-robot.html)
function renderCatalog(models) {
  const catalog = document.querySelector("#robot-catalog");
  const emptyState = document.querySelector("#robot-empty");
  if (!catalog || !emptyState) return;

  catalog.innerHTML = models.map((model) => `
    <article class="card robot-card">
      <div class="media-placeholder card-image">
        <img src="${escapeHtml(model.image)}" alt="${escapeHtml(model.name)}">
      </div>
      <p class="card-kicker">${escapeHtml(model.level)}</p>
      <h2>${escapeHtml(model.name)}</h2>
      <p>${escapeHtml(model.summary)}</p>
      <p><strong>${model.parts.length}</strong> nhóm linh kiện · <strong>${model.steps.length}</strong> bước</p>
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

  modelSelect.innerHTML = robotModels.map((model) =>
      `<option value="${escapeHtml(model.id)}">${escapeHtml(model.name)} · ${escapeHtml(model.level)}</option>`
  ).join("");

  const requestedModel = new URLSearchParams(window.location.search).get("model");
  if (robotModels.some((model) => model.id === requestedModel)) modelSelect.value = requestedModel;

  function updateProgress() {
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
  }

  function renderSelectedModel() {
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
      </label>
    `).join("");

    steps.innerHTML = model.steps.map((step) => `<li>${escapeHtml(step)}</li>`).join("");
    updateProgress();
  }

  checklist.addEventListener("change", updateProgress);
  modelSelect.addEventListener("change", renderSelectedModel);
  renderSelectedModel();
}

// Khởi chạy hệ thống
setupComponents();
setupCatalog();
setupAssembly();