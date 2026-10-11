"use strict";

(() => {
  const api = window.RobotContentApi;
  const catalog = document.querySelector("#library-catalog");
  const status = document.querySelector("#library-data-status");
  const robotFilter = document.querySelector("#library-robot-filter");
  const pageSize = document.querySelector("#library-page-size");
  const pagination = document.querySelector("#library-pagination");
  const previous = document.querySelector("#library-prev");
  const next = document.querySelector("#library-next");
  const pageSummary = document.querySelector("#library-page-summary");
  const state = { page: 1, limit: 6, robotId: "", total: 0 };

  if (!api || !catalog || !status || !robotFilter || !pageSize || !pagination || !previous || !next || !pageSummary) return;

  const make = (tag, className, text) => {
    const element = document.createElement(tag);
    if (className) element.className = className;
    if (text !== undefined) element.textContent = text;
    return element;
  };

  function renderResource(resource) {
    const card = make("article", "card");
    if (resource.type === "image") {
      const figure = make("figure", "card-image");
      const image = make("img");
      Object.assign(image, {
        src: resource.url,
        alt: resource.title,
        width: 1024,
        height: 1024,
        loading: "lazy",
        decoding: "async"
      });
      figure.append(image);
      card.append(figure);
    }
    card.append(
      make("p", "card-kicker", resource.type === "image" ? "Hình ảnh mô hình" : "Tài liệu"),
      make("h2", "", resource.title),
      make("p", "", resource.description)
    );
    if (resource.type !== "image") {
      const link = make("a", "card-link", "Mở tài nguyên →");
      link.href = resource.url;
      if (/^https?:/i.test(resource.url)) { link.target = "_blank"; link.rel = "noopener noreferrer"; }
      card.append(link);
    }
    return card;
  }

  function setControlsEnabled(enabled) {
    robotFilter.disabled = !enabled;
    pageSize.disabled = !enabled;
  }

  function renderPagination() {
    const totalPages = Math.max(1, Math.ceil(state.total / state.limit));
    if (totalPages <= 1) {
      pagination.hidden = true;
      return;
    }
    pagination.hidden = false;
    previous.disabled = state.page <= 1;
    next.disabled = state.page >= totalPages;
    pageSummary.textContent = state.total ? `Trang ${state.page}/${totalPages} · ${state.total} tài nguyên` : "Không có tài nguyên";
  }

  function renderEmpty() {
    const message = make("p", "muted-text", "Không có tài liệu phù hợp với mẫu robot đã chọn.");
    message.setAttribute("role", "status");
    catalog.replaceChildren(message);
  }

  async function loadPage() {
    status.textContent = "Đang tải tài nguyên thư viện…";
    setControlsEnabled(false);
    try {
      const payload = await api.loadLibraryResources(state);
      // Tương thích response Content API cũ chỉ trả mảng data trong khi WAR đang được cập nhật.
      const resources = Array.isArray(payload) ? payload : payload.data;
      const total = payload.meta?.total ?? resources.length;
      if (!Array.isArray(resources)) throw new Error("Dữ liệu thư viện không hợp lệ.");
      state.total = total;
      const totalPages = Math.max(1, Math.ceil(state.total / state.limit));
      if (state.page > totalPages) { state.page = totalPages; return loadPage(); }
      if (resources.length) catalog.replaceChildren(...resources.map(renderResource)); else renderEmpty();
      status.textContent = state.total ? `Đã tải ${resources.length} trên ${state.total} tài nguyên từ Content API.` : "Không có tài liệu trong thư viện.";
      renderPagination();
    } catch (error) {
      status.textContent = `API lỗi: ${error.message} Đang hiển thị dữ liệu dự phòng.`;
      pagination.hidden = true;
    } finally { setControlsEnabled(true); }
  }

  async function loadRobotOptions() {
    try {
      const robots = await api.loadRobotOptions();
      robots.forEach(robot => {
        const option = make("option", "", robot.name);
        option.value = robot.id;
        robotFilter.append(option);
      });
    } catch (_) { /* Lỗi được hiển thị bởi lần tải tài nguyên. */ }
  }

  async function start() {
    if (!api.enabled) {
      status.textContent = "Chế độ dữ liệu dự phòng: đang hiển thị nội dung tĩnh vì không kết nối Content API.";
      return;
    }
    robotFilter.addEventListener("change", () => { state.robotId = robotFilter.value; state.page = 1; loadPage(); });
    pageSize.addEventListener("change", () => { state.limit = Number(pageSize.value); state.page = 1; loadPage(); });
    previous.addEventListener("click", () => { state.page -= 1; loadPage(); });
    next.addEventListener("click", () => { state.page += 1; loadPage(); });
    await Promise.all([loadPage(), loadRobotOptions()]);
  }

  start();
})();
