"use strict";

/* Lớp giao diện dùng chung: thu gọn thanh điều hướng và hiệu ứng xuất hiện khi cuộn.
   Nạp trong <head> không kèm defer để trạng thái thu gọn được áp dụng trước khi vẽ,
   tránh hiện tượng nháy layout. Phần còn lại chờ DOMContentLoaded. */

(() => {
  const NAV_KEY = "ral.nav-collapsed";
  const root = document.documentElement;
  const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");

  function setupScrollPosition() {
    const rail = document.createElement("span");
    rail.className = "reading-position";
    rail.setAttribute("aria-hidden", "true");
    document.body.append(rail);
    let frame = 0;
    function update() {
      frame = 0;
      const range = root.scrollHeight - root.clientHeight;
      rail.hidden = reducedMotion.matches || range <= 0;
      rail.style.transform = `scaleX(${range > 0 ? Math.max(0, Math.min(1, window.scrollY / range)) : 0})`;
    }
    function schedule() {
      if (!frame) frame = requestAnimationFrame(update);
    }
    window.addEventListener("scroll", schedule, { passive: true });
    window.addEventListener("resize", schedule);
    reducedMotion.addEventListener("change", schedule);
    // Catalog/filter content may change the page height without a window resize.
    if ("ResizeObserver" in window) new ResizeObserver(schedule).observe(document.body);
    update();
  }

  function setupNavPreview() {
    const nav = document.querySelector("#primary-nav");
    const list = nav?.querySelector(".nav-list");
    if (!list) return;
    function preview(link) {
      nav.classList.toggle("nav-preview", Boolean(link));
      if (!link) return;
      const target = link.getBoundingClientRect();
      const parent = nav.getBoundingClientRect();
      nav.style.setProperty("--nav-preview-y", `${target.top - parent.top + target.height / 2 - 3}px`);
    }
    list.addEventListener("pointerover", (event) => {
      if (event.pointerType === "mouse") preview(event.target.closest("a"));
    });
    list.addEventListener("pointerleave", () => preview(list.contains(document.activeElement) ? document.activeElement.closest("a") : null));
    list.addEventListener("focusin", (event) => preview(event.target.closest("a")));
    list.addEventListener("focusout", (event) => {
      if (!list.contains(event.relatedTarget)) preview(null);
    });
    window.addEventListener("resize", () => preview(null));
    document.querySelector("#nav-toggle")?.addEventListener("click", () => preview(null));
  }

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
        /* Bỏ qua: đây chỉ là tiện ích ghi nhớ, không phải chức năng bắt buộc. */
      }
    }
  };

  // Áp dụng ngay, trước khi trình duyệt vẽ khung đầu tiên.
  if (storage.read(NAV_KEY) === "true") root.classList.add("nav-collapsed");

  function setupNavToggle() {
    const toggle = document.querySelector("#nav-toggle");
    const nav = document.querySelector("#primary-nav");
    if (!toggle || !nav) return;

    const label = toggle.querySelector("span");
    const links = [...nav.querySelectorAll(".nav-list a")];

    function apply(collapsed) {
      root.classList.toggle("nav-collapsed", collapsed);
      toggle.setAttribute("aria-expanded", String(!collapsed));
      toggle.setAttribute("aria-label", collapsed ? "Mở rộng thanh điều hướng" : "Thu gọn thanh điều hướng");
      if (label) label.textContent = "Thu gọn";

      // Khi chỉ còn icon, giữ chú thích nổi để người dùng vẫn biết từng mục là trang nào.
      links.forEach((link) => {
        const text = (link.textContent || "").trim();
        if (collapsed) {
          link.setAttribute("title", text);
        } else {
          link.removeAttribute("title");
        }
      });

      storage.write(NAV_KEY, String(collapsed));
    }

    apply(root.classList.contains("nav-collapsed"));
    toggle.addEventListener("click", () => apply(!root.classList.contains("nav-collapsed")));
  }

  function setupScrollReveal() {
    // Không có IntersectionObserver hoặc người dùng yêu cầu giảm chuyển động
    // thì giữ nguyên nội dung hiển thị, không thêm bất kỳ hiệu ứng nào.
    if (!("IntersectionObserver" in window)) return;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;

    const targets = [...document.querySelectorAll([
      "main > section",
      "main > .assembly-workspace",
      "main > .table-scroll",
      "main > .chart-card",
      ".card-grid > *",
      ".featured-grid > *",
      ".team-grid > *",
      ".component-grid > *"
    ].join(", "))];

    // Homepage showcases animate their cards, not both the section and its children.
    const showcaseCards = [...document.querySelectorAll("#featured-robots > article, .components-section .component-grid > article")];
    const showcaseSections = new Set(showcaseCards.map(card => card.closest("section")));

    if (targets.length === 0) return;

    // Chỉ bật cờ khi chắc chắn observer sẽ chạy, nhờ vậy CSS mặc định luôn là "hiện".
    root.classList.add("reveal-ready");
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.target.classList.contains("showcase-reveal")) {
          if (entry.target.classList.contains("reveal-focused")) return;
          if (entry.isIntersecting && entry.intersectionRatio >= .08) entry.target.classList.add("is-visible");
          else if (!entry.isIntersecting) {
            entry.target.classList.remove("is-visible");
          }
          return;
        }
        if (!entry.isIntersecting) return;
        entry.target.classList.add("is-visible");
        observer.unobserve(entry.target);
      });
    }, { rootMargin: "0px 0px -12% 0px", threshold: [0, 0.08] });

    // Chỉ áp dụng cho nội dung nằm dưới màn hình đầu. Phần đã hiển thị sẵn khi tải
    // trang không được dịch chuyển: nó gây nhấp nháy bố cục và làm người dùng
    // hụt tay khi bấm vào nút vừa mới di chuyển.
    const fold = window.innerHeight * 0.9;
    let revealed = 0;

    targets.forEach((target) => {
      if (target.closest(".photo-explorer") || target.querySelector(".photo-explorer")) return;
      if (showcaseSections.has(target)) return;
      if (target.getBoundingClientRect().top < fold) return;

      target.classList.add("reveal");
      if (showcaseCards.includes(target)) {
        target.classList.add("showcase-reveal");
        const index = [...target.parentElement.children].indexOf(target);
        target.style.transitionDelay = `${Math.min(index, 3) * 90}ms`;
      } else target.style.transitionDelay = `${Math.min(revealed % 6, 5) * 45}ms`;
      observer.observe(target);
      revealed += 1;
    });

    // Focus must never land on content that is still moving into place.
    document.addEventListener("focusin", (event) => {
      let target = event.target.closest(".reveal");
      while (target) {
        target.classList.add("is-visible", "reveal-focused");
        observer.unobserve(target);
        target = target.parentElement?.closest(".reveal");
      }
    });
    reducedMotion.addEventListener("change", () => {
      if (!reducedMotion.matches) return;
      observer.disconnect();
      targets.forEach((target) => target.classList.add("is-visible"));
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    setupNavToggle();
    setupScrollReveal();
    setupScrollPosition();
    setupNavPreview();
  });
})();
