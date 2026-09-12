"use strict";

(() => {
  const base = new URL("../../", document.currentScript.src);
  const url = path => new URL(path, base).href;
  const references = window.LAB_REFERENCES || [];
  const reduced = matchMedia("(prefers-reduced-motion: reduce)");
  const make = (tag, className, text) => {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text) node.textContent = text;
    return node;
  };
  function icon(name) {
    const paths = {
      next: "M4.5 12h15m-6.75-6.75L19.5 12l-6.75 6.75",
      previous: "M19.5 12h-15m6.75 6.75L4.5 12l6.75-6.75",
      play: "M5.25 5.25v13.5L18.75 12 5.25 5.25Z",
      pause: "M9 5.25v13.5m6-13.5v13.5",
      chip: "M8.25 3v1.5M12 3v1.5M15.75 3v1.5M8.25 19.5V21M12 19.5V21M15.75 19.5V21M3 8.25h1.5M3 12h1.5M3 15.75h1.5M19.5 8.25H21M19.5 12H21M19.5 15.75H21M6.75 4.5h10.5a2.25 2.25 0 0 1 2.25 2.25v10.5a2.25 2.25 0 0 1-2.25 2.25H6.75a2.25 2.25 0 0 1-2.25-2.25V6.75A2.25 2.25 0 0 1 6.75 4.5Z"
    };
    const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    for (const [key, value] of Object.entries({viewBox:"0 0 24 24", class:"icon", fill:"none", stroke:"currentColor", "stroke-width":"1.5", "aria-hidden":"true", focusable:"false"})) svg.setAttribute(key, value);
    const path = document.createElementNS(svg.namespaceURI, "path");
    path.setAttribute("d", paths[name]);
    path.setAttribute("stroke-linecap", "round"); path.setAttribute("stroke-linejoin", "round");
    svg.append(path);
    return svg;
  }
  function picture(item) {
    const img = make("img");
    Object.assign(img, {src:url(item.image), alt:item.alt || item.name, width:1280, height:960, loading:"lazy", decoding:"async"});
    return img;
  }
  function credit(item) {
    const p = make("p", "reference-credit");
    if (!item.author) return p;
    const source = make("a", "", item.author); source.href = item.photoSource;
    const license = make("a", "", item.license); license.href = item.licenseUrl;
    p.append("Ảnh: ", source, " · ", license, " · Thu nhỏ, chuyển WebP.");
    return p;
  }
  // The same editorial records are shown on the catalog pages and in the library.
  document.querySelectorAll("[data-reference-kind]").forEach(grid => {
    const items = references.filter(item => grid.dataset.referenceKind === "all" || item.kind === grid.dataset.referenceKind);
    items.forEach(item => {
      const card = make("article", "reference-card"); card.id = `reference-${item.id}`;
      const figure = make("figure"); figure.append(picture(item));
      const link = make("a", "text-link", "Đọc tài liệu nguồn ↗"); link.href = item.source;
      card.append(figure, make("p", "section-label", item.category), make("h3", "", item.name), make("p", "", item.summary), make("p", "", item.detail), link, credit(item));
      grid.append(card);
    });
  });

  function setupExplorer(root, items, headingId, label) {
    if (!root || items.length < 2) return;
    root.className = "photo-explorer";
    root.removeAttribute("aria-live");
    root.setAttribute("role", "region"); root.setAttribute("aria-roledescription", "trình chiếu ảnh"); root.setAttribute("aria-labelledby", headingId);
    const photo = make("figure", "explorer-photo"), img = picture(items[0]); photo.append(img);
    const top = make("header", "explorer-top"), brand = make("span", "", "Robot Assembly Lab"), kind = make("span", "explorer-kind");
    brand.prepend(icon("chip")); top.append(brand, kind);
    const copy = make("article", "explorer-copy"), category = make("p", "explorer-category"), title = make("h3"), summary = make("p", "explorer-summary"), facts = make("ul", "explorer-facts"), link = make("a", "explorer-link");
    copy.id = `${root.id}-active`; copy.append(category, title, summary, facts, link);
    const thumbs = make("ul", "explorer-thumbs"); thumbs.setAttribute("aria-label", `Chọn ${label}`);
    const controls = make("div", "explorer-controls"), counter = make("p", "explorer-count"), status = make("p", "sr-only");
    status.setAttribute("role", "status");
    const sourceCredit = make("div", "explorer-credit");
    let current = 0, request = 0, timer, playing = false, visible = false, hovered = false, focusPaused = false;
    const buttons = items.map((item, index) => {
      const li = make("li"), button = make("button", "explorer-thumb"), text = make("span");
      button.type = "button"; button.setAttribute("aria-label", `Xem ${item.name}`); button.setAttribute("aria-controls", copy.id);
      text.append(make("small", "", item.category), make("strong", "", item.name));
      const thumbnail = picture(item); thumbnail.alt = "";
      button.append(thumbnail, text); li.append(button); thumbs.append(li);
      button.addEventListener("click", () => { stop(); select(index); });
      return button;
    });
    function control(name, text, action) {
      const b = make("button", "explorer-control"); b.type = "button";
      b.setAttribute("aria-label", `${label}: ${text}`); b.setAttribute("aria-controls", copy.id);
      b.append(icon(name)); b.addEventListener("click", action); return b;
    }
    const previous = control("previous", "trước", () => {stop(); select(current - 1);});
    const next = control("next", "tiếp theo", () => {stop(); select(current + 1);});
    const play = control("play", "bật tự phát", () => {
      playing = !playing;
      // An explicit Play action may start while its button still has focus.
      if (playing) {hovered = false; focusPaused = false;}
      syncPlay(); schedule();
    });
    play.classList.add("explorer-play");
    const playShape = play.querySelector("path"), playText = make("span");
    play.append(playText);
    function syncPlay() {
      // Keep the hovered button subtree stable: replacing it retriggers mouseenter.
      playShape.setAttribute("d", playing ? "M9 5.25v13.5m6-13.5v13.5" : "M5.25 5.25v13.5L18.75 12 5.25 5.25Z");
      playText.textContent = playing ? "Dừng tự phát" : "Tự phát";
      play.setAttribute("aria-label", `${label}: ${playing ? "dừng" : "bật"} tự phát`);
      play.setAttribute("aria-pressed", String(playing));
      play.disabled = reduced.matches;
      play.title = reduced.matches ? "Tự phát tắt theo tùy chọn giảm chuyển động của thiết bị" : "Chuyển ảnh sau mỗi 6 giây";
    }
    function stop() {playing = false; clearTimeout(timer); syncPlay();}
    function schedule() {
      clearTimeout(timer);
      if (playing && visible && !hovered && !focusPaused && !document.hidden && !reduced.matches) timer = setTimeout(() => select(current + 1, false), 6000);
    }
    function render(announce) {
      const item = items[current];
      img.src = url(item.image); img.alt = item.alt || item.name;
      category.textContent = item.category; title.textContent = item.name; summary.textContent = item.summary;
      kind.textContent = item.reference ? "Tư liệu tham khảo" : label;
      facts.replaceChildren(...(item.facts || []).map(text => make("li", "", text)));
      link.replaceChildren(document.createTextNode(item.action), icon("next")); link.href = url(item.href);
      counter.replaceChildren(document.createTextNode(String(current + 1).padStart(2, "0")), make("small", "", ` / ${String(items.length).padStart(2, "0")}`));
      buttons.forEach((b, i) => b.setAttribute("aria-pressed", String(i === current)));
      sourceCredit.replaceChildren(credit(item));
      if (announce) status.textContent = `${current + 1} trên ${items.length}: ${item.name}`;
    }
    async function select(index, announce = true) {
      const target = (index + items.length) % items.length, token = ++request;
      clearTimeout(timer);
      const preload = new Image(); preload.src = url(items[target].image);
      try { await preload.decode(); } catch {
        if (token === request) {status.textContent = "Ảnh chưa tải được. Bạn có thể chọn mục khác."; stop();}
        return;
      }
      if (token !== request) return;
      current = target; render(announce);
      const button = buttons[current], left = button.parentElement.offsetLeft - thumbs.offsetLeft;
      thumbs.scrollTo({left:Math.max(0, left - 4), behavior:reduced.matches ? "instant" : "smooth"});
      if (!reduced.matches) {
        img.getAnimations().forEach(a => a.cancel()); copy.getAnimations().forEach(a => a.cancel());
        img.animate([{opacity:.35, transform:"scale(1.025)"}, {opacity:1, transform:"scale(1)"}], {duration:500, easing:"ease-out"});
        copy.animate([{opacity:0, transform:"translateY(12px)"}, {opacity:1, transform:"translateY(0)"}], {duration:350, easing:"ease-out"});
      }
      schedule();
    }
    thumbs.addEventListener("keydown", event => {
      const index = buttons.indexOf(event.target); if (index < 0) return;
      let target;
      if (event.key === "ArrowRight") target = (index + 1) % items.length;
      if (event.key === "ArrowLeft") target = (index + items.length - 1) % items.length;
      if (event.key === "Home") target = 0;
      if (event.key === "End") target = items.length - 1;
      if (target !== undefined) {event.preventDefault(); stop(); buttons[target].focus({preventScroll:true}); select(target);}
    });
    let touchStart;
    root.addEventListener("touchstart", e => {
      if (e.target.closest("button, a, .explorer-thumbs")) return;
      const t = e.touches[0]; touchStart = {x:t.clientX, y:t.clientY};
    }, {passive:true});
    root.addEventListener("touchend", e => {
      if (!touchStart) return;
      const t = e.changedTouches[0], dx = t.clientX - touchStart.x, dy = t.clientY - touchStart.y;
      touchStart = null;
      if (Math.abs(dx) > 60 && Math.abs(dx) > Math.abs(dy) * 1.5) {stop(); select(current + (dx < 0 ? 1 : -1));}
    }, {passive:true});
    root.addEventListener("touchcancel", () => {touchStart = null;}, {passive:true});
    root.addEventListener("mouseenter", () => {hovered = true; schedule();});
    root.addEventListener("mouseleave", () => {hovered = false; schedule();});
    root.addEventListener("focusin", () => {focusPaused = true; schedule();});
    root.addEventListener("focusout", () => setTimeout(() => {focusPaused = root.contains(document.activeElement); schedule();}, 0));
    document.addEventListener("visibilitychange", schedule);
    reduced.addEventListener("change", () => {
      if (reduced.matches) {stop(); root.getAnimations({subtree:true}).forEach(a => a.cancel());}
      syncPlay();
    });
    if ("IntersectionObserver" in window) new IntersectionObserver(entries => {visible = entries[0].isIntersecting; schedule();}, {threshold:.1}).observe(root);
    else visible = true;
    controls.append(counter, previous, next, play);
    root.replaceChildren(photo, top, copy, thumbs, controls, sourceCredit, status);
    syncPlay(); render(false);
  }

  const referenceSlide = item => ({...item, reference:true, href:`pages/thu-vien.html#reference-${item.id}`, action:"Khám phá tư liệu"});
  const robots = (window.ROBOT_MODELS || []).map(model => ({
    name:model.name, category:model.level, image:model.image.replace(/^\.\.\//, ""), summary:model.summary,
    facts:[`${model.parts.length} nhóm linh kiện`, `${model.steps.length} bước hướng dẫn`],
    href:`pages/lap-rap.html?model=${encodeURIComponent(model.id)}`, action:"Khám phá mẫu robot"
  }));
  setupExplorer(document.querySelector("#featured-robots"), [...robots, ...references.filter(i => i.kind === "robots").map(referenceSlide)], "featured-heading", "Mẫu robot");
  const grid = document.querySelector(".components-section .component-grid");
  if (grid) {
    const components = [...grid.children].map(card => ({name:card.querySelector("h3").textContent, category:card.querySelector(".component-category").textContent,
      summary:card.querySelector("h3 + p").textContent, image:card.querySelector("img").getAttribute("src"),
      facts:[...card.querySelectorAll("dd")].map(dd => dd.textContent), href:"pages/linh-kien.html", action:"Tìm hiểu linh kiện"}));
    grid.id = "component-explorer";
    setupExplorer(grid, [...components, ...references.filter(i => i.kind === "components").map(referenceSlide)], "components-heading", "Linh kiện");
  }
})();
