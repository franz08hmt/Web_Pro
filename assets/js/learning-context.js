"use strict";

/* Read-only links connect existing lessons; no correctness or score is decided here. */
(() => {
  const script = [...document.scripts].find(s => /\/learning-context\.js(?:\?|$)/.test(s.src));
  const root = new URL("../../", script.src);
  const local = path => new URL(path, root).href;
  let pending;
  const load = () => pending ||= fetch(local("api/learning-links"), {credentials: "same-origin", cache: "no-store"})
    .then(r => { if (!r.ok) throw Error("Liên kết liên quan tạm chưa sẵn sàng."); return r.json(); })
    .then(result => result.data);
  const link = (text, path) => {
    const a = document.createElement("a"); a.textContent = text; a.href = local(path); return a;
  };
  function componentUrl(id) {
      // encodeURIComponent giữ mã linh kiện là dữ liệu trong URL, không biến nó thành cú pháp URL.
    return local(`pages/linh-kien.html?component=${encodeURIComponent(id)}#component-${encodeURIComponent(id)}`);
  }
  function componentLink(id) {
    const info = document.createElement("a");
    info.textContent = "Thông tin linh kiện";
    info.href = componentUrl(id);
    info.className = "learning-context-link";
    return info;
  }
  const learnerLinks = new Set();
  function learnerLink(text, path) {
    const action = link(text, path);
    action.hidden = window.SiteAuthState?.status !== "ready" || window.SiteAuthState.user?.role !== "USER";
    learnerLinks.add(action);
    return action;
  }
  window.addEventListener("robot:auth-ready", () => {
    learnerLinks.forEach(action => {
      if (!action.isConnected) { learnerLinks.delete(action); return; }
      action.hidden = window.SiteAuthState?.status !== "ready" || window.SiteAuthState.user?.role !== "USER";
    });
  });
  async function errorLinks(container, componentId, code) {
    container.replaceChildren();
    const learning = ["WRONG_COMPONENT", "SLOT_OCCUPIED", "ORIENTATION_NOT_ALLOWED", "PARENT_MISSING",
      "ALREADY_PLACED", "HAS_DEPENDENTS", "MOUNT_INCOMPATIBLE", "MOUNT_OCCUPIED", "HOST_MISSING", "NOT_SUPPORTED"];
    if (!componentId || !learning.includes(code)) return;
    const info = componentLink(componentId); info.textContent = "Xem thông tin linh kiện";
    container.append(info);
    try {
      const item = (await load()).find(i => i.componentId === componentId);
      const requested = new URLSearchParams(location.search).get("model");
      const robot = requested ? item?.robots.find(r => r.id === requested && r.hasGuide) : item?.robots.find(r => r.hasGuide);
      if (robot && container.contains(info)) container.append(document.createTextNode(" · "),
        link("Tra cứu lỗi", `pages/tra-cuu-loi.html?robotId=${encodeURIComponent(robot.id)}`));
    } catch { /* The component link remains available if the optional catalog fails. */ }
  }
  window.LearningContext = {componentLink, componentUrl, errorLinks, learnerLink};

  // Catalog identity and navigation also work when optional context data fails.
  function componentTarget() {
    const catalog = document.querySelector("#component-catalog");
    if (!catalog) return;
    const notice = document.createElement("p");
    notice.id = "component-link-status";
    notice.setAttribute("role", "status");
    catalog.before(notice);
    let followed = "";
    function locate() {
      catalog.querySelectorAll("article[data-component-id]").forEach(card => {
        card.id = `component-${card.dataset.componentId}`;
      });
      const query = new URLSearchParams(location.search).get("component");
      let requested = query;
      if (requested === null && location.hash) {
        try { requested = decodeURIComponent(location.hash.slice(1)).replace(/^component-/, ""); }
        catch { notice.textContent = "Liên kết linh kiện không hợp lệ. Hãy chọn linh kiện trong danh mục."; return; }
      }
      if (!requested) { notice.textContent = ""; return; }
      if (catalog.dataset.catalogState === "loading") {
        notice.textContent = "Đang tìm linh kiện trong danh mục…";
        return;
      }
      const target = [...catalog.querySelectorAll("article[data-component-id]")]
        .find(card => card.dataset.componentId === requested);
      if (!target) {
        notice.textContent = "Không tìm thấy linh kiện được yêu cầu trong danh mục đang hiển thị.";
        return;
      }
      const key = location.search + location.hash;
      if (followed === key) return;
      followed = key;
      // A category filter must not hide the requested card.
      document.querySelector('.filter-button[data-filter="all"]')?.click();
      target.hidden = false;
      target.setAttribute("tabindex", "-1");
      target.scrollIntoView({block: "center"});
      target.focus({preventScroll: true});
      notice.textContent = "Đang xem: " + (target.querySelector("h2")?.textContent || "linh kiện được yêu cầu")
        + (catalog.dataset.catalogState === "fallback" ? " (dữ liệu dự phòng)." : ".");
    }
    document.addEventListener("robot:components-updated", locate);
    window.addEventListener("hashchange", () => { followed = ""; locate(); });
    new MutationObserver(locate).observe(catalog, {childList: true});
    locate();
  }
  componentTarget();

  async function catalogContext() {
    const components = document.querySelector("#component-catalog"), robots = document.querySelector("#robot-catalog");
    if (!components && !robots) return;
    let items;
    try { items = await load(); } catch { return; }
    const render = () => {
      components?.querySelectorAll("article[data-component-id]").forEach(card => {
        const item = items.find(i => i.componentId === card.dataset.componentId);
        if (!item) return;
        card.querySelector(".learning-context")?.remove();
        const p = document.createElement("div"); p.className = "learning-context";
        const usage = document.createElement("p"); usage.textContent = "Dùng trong các mẫu robot: ";
        item.robots.forEach((r, n) => { if (n) usage.append(", ");
          usage.append(link(r.name, `pages/lap-rap.html?model=${encodeURIComponent(r.id)}`)); });
        if (!item.robots.length) usage.append("Chưa gán vào mẫu chính thức.");
        p.append(usage);
        if (item.owned !== null) {
          const owned = document.createElement("p"); owned.textContent = `Có trong bộ của tôi: ${item.owned}`; p.append(owned);
        }
        if (item.productId) p.append(link("Mua ở cửa hàng", `pages/cua-hang.html#shop-product-${encodeURIComponent(item.productId)}`));
        card.append(p);
      });
      robots?.querySelectorAll("article").forEach(card => {
        const assembly = card.querySelector('a[href*="lap-rap.html?model="]');
        if (!assembly) return;
        const id = new URL(assembly.href).searchParams.get("model");
        const robot = items.flatMap(i => i.robots).find(r => r.id === id);
        assembly.textContent = "Lắp mẫu này";
        card.querySelector(".learning-context")?.remove();
        const actions = document.createElement("p"); actions.className = "learning-context button-group";
        actions.append(learnerLink("Làm bài kiểm tra", `pages/kiem-tra.html?model=${encodeURIComponent(id)}`));
        if (robot?.hasWiring) actions.append(learnerLink("Bài nối dây", `wiring?robotId=${encodeURIComponent(id)}`));
        if (robot?.hasGuide) actions.append(link("Tra cứu lỗi của mẫu", `pages/tra-cuu-loi.html?robotId=${encodeURIComponent(id)}`));
        card.append(actions);
      });
    };
    render();
    for (const catalog of [components, robots].filter(Boolean)) new MutationObserver(render).observe(catalog, {childList: true});
  }
  void catalogContext();
  if (location.pathname.endsWith("/gio-hang.html") && new URLSearchParams(location.search).has("designNotice")) {
    const notice = document.createElement("section"); notice.setAttribute("aria-label", "Kết quả bổ sung vật tư");
    notice.setAttribute("role", "status"); document.querySelector("#cart-status")?.before(notice);
    fetch(local("api/design-cart-notice"), {credentials: "same-origin", cache: "no-store"})
      .then(r => { if (!r.ok) throw Error(); return r.json(); })
      .then(result => result.data.forEach(text => { const p = document.createElement("p"); p.textContent = text; notice.append(p); }))
      .catch(() => { notice.textContent = "Thông báo bổ sung tạm chưa sẵn sàng. Hãy kiểm tra số lượng trong giỏ."; });
  }
})();
