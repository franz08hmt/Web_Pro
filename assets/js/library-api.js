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
  let loading = false;
  let hasApiResults = false;
  let robotOptionsReady = false;
  let robotOptionsError = "";
  let pageMessage = "";

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

  function updateStatus() {
    status.textContent = [pageMessage, robotOptionsError].filter(Boolean).join(" ");
  }

  function updateControls() {
    catalog.setAttribute("aria-busy", String(loading));
    robotFilter.disabled = !api.enabled || loading || !robotOptionsReady;
    pageSize.disabled = !api.enabled || loading;
    const totalPages = Math.max(1, Math.ceil(state.total / state.limit));
    previous.disabled = !api.enabled || loading || !hasApiResults || state.page <= 1;
    next.disabled = !api.enabled || loading || !hasApiResults || state.page >= totalPages;
  }

  function renderPagination() {
    const totalPages = Math.max(1, Math.ceil(state.total / state.limit));
    pagination.hidden = !hasApiResults || totalPages <= 1;
    pageSummary.textContent = state.total ? `Trang ${state.page}/${totalPages} · ${state.total} tài nguyên` : "Không có tài nguyên";
  }

  function renderEmpty() {
    const message = make("p", "muted-text", "Không có tài liệu phù hợp với mẫu robot đã chọn.");
    message.setAttribute("role", "status");
    catalog.replaceChildren(message);
  }

  async function loadPage(options = state) {
    // Serialize even queued events; disabled buttons alone cannot guard their handlers.
    if (loading) return;
    const requested = { page: options.page, limit: options.limit, robotId: options.robotId };
    loading = true;
    pageMessage = "Đang tải tài nguyên thư viện…";
    updateStatus();
    updateControls();
    try {
      let payload;
      // The catalog can shrink between requests. Re-fetch the last remaining page
      // while retaining the same loading lock and the previously displayed state.
      while (true) {
        payload = await api.loadLibraryResourcesPage(requested);
        if (!Array.isArray(payload?.data) || !Number.isInteger(payload.meta?.total) || payload.meta.total < 0) {
          throw new Error("Dữ liệu thư viện không hợp lệ.");
        }
        const totalPages = Math.max(1, Math.ceil(payload.meta.total / requested.limit));
        if (requested.page <= totalPages) break;
        requested.page = totalPages;
      }
      const resources = payload.data;
      if (resources.length) catalog.replaceChildren(...resources.map(renderResource)); else renderEmpty();
      Object.assign(state, requested, { total: payload.meta.total });
      hasApiResults = true;
      pageMessage = state.total ? `Đã tải ${resources.length} trên ${state.total} tài nguyên từ Content API.` : "Không có tài liệu trong thư viện.";
    } catch (error) {
      pageMessage = `API lỗi: ${error.message} ${hasApiResults ? "Đang giữ nguyên kết quả trước; bộ lọc và trang vẫn theo dữ liệu đang hiển thị." : "Đang hiển thị nội dung tĩnh dự phòng."}`;
    } finally {
      // Commit controls only with successfully rendered data; roll back failed changes.
      robotFilter.value = state.robotId;
      pageSize.value = String(state.limit);
      loading = false;
      renderPagination();
      updateStatus();
      updateControls();
    }
  }

  async function loadRobotOptions() {
    try {
      const robots = await api.loadRobotOptions();
      if (!Array.isArray(robots)) throw new Error("Dữ liệu mẫu robot không hợp lệ.");
      robots.forEach(robot => {
        const option = make("option", "", robot.name);
        option.value = robot.id;
        robotFilter.append(option);
      });
      robotOptionsReady = true;
    } catch (error) {
      robotOptionsError = `Không thể tải bộ lọc mẫu robot: ${error.message}`;
    } finally {
      updateStatus();
      updateControls();
    }
  }

  async function start() {
    updateControls();
    if (!api.enabled) {
      status.textContent = "Chế độ dữ liệu dự phòng: đang hiển thị nội dung tĩnh vì không kết nối Content API.";
      return;
    }
    robotFilter.addEventListener("change", () => {
      if (robotFilter.disabled || loading) return;
      loadPage({ ...state, robotId: robotFilter.value, page: 1 });
    });
    pageSize.addEventListener("change", () => {
      if (pageSize.disabled || loading) return;
      loadPage({ ...state, limit: Number(pageSize.value), page: 1 });
    });
    previous.addEventListener("click", () => {
      if (previous.disabled || loading) return;
      loadPage({ ...state, page: state.page - 1 });
    });
    next.addEventListener("click", () => {
      if (next.disabled || loading) return;
      loadPage({ ...state, page: state.page + 1 });
    });
    await Promise.all([loadPage(), loadRobotOptions()]);
  }

  start();
})();
