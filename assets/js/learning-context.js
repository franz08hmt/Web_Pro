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
  function componentLink(id) {
    const info = link("Thông tin linh kiện", `pages/linh-kien.html?component=${encodeURIComponent(id)}#component-${encodeURIComponent(id)}`);
    info.className = "learning-context-link";
    return info;
  }
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
  window.LearningContext = {componentLink, errorLinks};

  async function catalogContext() {
    const components = document.querySelector("#component-catalog"), robots = document.querySelector("#robot-catalog");
    if (!components && !robots) return;
    let items;
    try { items = await load(); } catch { return; }
    const render = () => {
      components?.querySelectorAll("article[data-component-id]").forEach(card => {
        const item = items.find(i => i.componentId === card.dataset.componentId);
        if (!item) return;
        card.id = `component-${item.componentId}`;
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
        actions.append(link("Làm bài kiểm tra", `pages/kiem-tra.html?model=${encodeURIComponent(id)}`));
        if (robot?.hasWiring) actions.append(link("Bài nối dây", `wiring?robotId=${encodeURIComponent(id)}`));
        if (robot?.hasGuide) actions.append(link("Tra cứu lỗi của mẫu", `pages/tra-cuu-loi.html?robotId=${encodeURIComponent(id)}`));
        card.append(actions);
      });
      const requested = new URLSearchParams(location.search).get("component");
      if (requested && components) document.getElementById(`component-${requested}`)?.scrollIntoView({block: "start"});
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
