"use strict";

/* Chú thích nổi dùng chung cho nút icon, huy hiệu trạng thái và từ viết tắt.
   Quy ước: đặt data-tooltip="..." lên phần tử. Tooltip chỉ bổ sung thông tin,
   không bao giờ thay nhãn nút — nút icon vẫn phải có aria-label riêng.

   Dùng một bong bóng duy nhất cho cả trang: tránh tạo hàng trăm node cho các
   danh sách dài và giữ đúng một phần tử role="tooltip" trong cây trợ năng. */

(() => {
  const SHOW_DELAY = 90;
  const TOUCH_HIDE_DELAY = 2600;
  const EDGE = 8;

  const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
  let bubble = null;
  let current = null;
  let showTimer = 0;
  let touchTimer = 0;

  function ensureBubble() {
    if (bubble) return bubble;
    bubble = document.createElement("div");
    bubble.id = "ral-tooltip";
    bubble.className = "tooltip-bubble";
    bubble.setAttribute("role", "tooltip");
    bubble.hidden = true;
    document.body.append(bubble);
    return bubble;
  }

  function place(target) {
    const anchor = target.getBoundingClientRect();
    const box = bubble.getBoundingClientRect();
    const left = Math.min(
      Math.max(EDGE, anchor.left + anchor.width / 2 - box.width / 2),
      window.innerWidth - box.width - EDGE
    );

    // Lật xuống dưới khi phía trên không đủ chỗ, để tooltip không bị cắt mất.
    const above = anchor.top - box.height - 10;
    const below = anchor.bottom + 10;
    const flipped = above < EDGE;

    bubble.dataset.placement = flipped ? "below" : "above";
    bubble.style.left = `${Math.round(left)}px`;
    bubble.style.top = `${Math.round(flipped ? below : above)}px`;
  }

  function hide() {
    window.clearTimeout(showTimer);
    window.clearTimeout(touchTimer);
    showTimer = 0;
    touchTimer = 0;
    if (!current) return;
    if (current.getAttribute("aria-describedby") === "ral-tooltip") {
      current.removeAttribute("aria-describedby");
    }
    current = null;
    if (bubble) {
      bubble.hidden = true;
      bubble.classList.remove("is-visible");
    }
  }

  function show(target) {
    const text = target.dataset.tooltip;
    if (!text) return;
    if (current === target) return;
    hide();
    current = target;
    ensureBubble();
    bubble.textContent = text;
    bubble.hidden = false;
    place(target);
    // Đọc lại vị trí sau khi có nội dung thật: chiều cao bong bóng chỉ biết được
    // sau khi chữ đã được bố trí, nhất là khi chú thích xuống dòng.
    place(target);
    bubble.classList.toggle("is-visible", true);

    // Chỉ gắn aria-describedby khi phần tử chưa có mô tả riêng, tránh ghi đè
    // quan hệ trợ năng mà trang đã khai báo.
    if (!target.hasAttribute("aria-describedby")) {
      target.setAttribute("aria-describedby", "ral-tooltip");
    }
  }

  function scheduleShow(target) {
    window.clearTimeout(showTimer);
    if (reducedMotion.matches) {
      show(target);
      return;
    }
    showTimer = window.setTimeout(() => show(target), SHOW_DELAY);
  }

  function targetFrom(node) {
    return node instanceof Element ? node.closest("[data-tooltip]") : null;
  }

  document.addEventListener("pointerover", (event) => {
    if (event.pointerType === "touch") return;
    const target = targetFrom(event.target);
    if (target) scheduleShow(target);
    else if (current && !current.contains(event.target)) hide();
  });

  document.addEventListener("pointerdown", (event) => {
    const target = targetFrom(event.target);
    if (event.pointerType !== "touch") {
      // Khi đã bấm, chú thích không còn cần thiết và dễ che mất nội dung.
      hide();
      return;
    }
    if (!target) {
      hide();
      return;
    }
    show(target);
    touchTimer = window.setTimeout(hide, TOUCH_HIDE_DELAY);
  });

  document.addEventListener("focusin", (event) => {
    const target = targetFrom(event.target);
    if (target) show(target);
    else hide();
  });

  document.addEventListener("focusout", (event) => {
    if (current && current === targetFrom(event.target)) hide();
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") hide();
  });

  /* Cuộn trang không được hủy chú thích: trình duyệt thường cuộn phần tử vào
     tầm nhìn ngay trước khi con trỏ tới nơi, và nếu cuộn gọi hide() thì hẹn giờ
     hiện đang chờ bị xóa, chú thích không bao giờ xuất hiện. Bám lại vị trí neo
     là đủ để bong bóng không trôi khỏi phần tử. */
  window.addEventListener("scroll", () => {
    if (current && bubble && !bubble.hidden) place(current);
  }, { passive: true });
  window.addEventListener("resize", hide);

  // Nội dung do JS dựng lại (danh sách linh kiện, kết quả lọc) có thể xóa đúng
  // phần tử đang được chú thích; bong bóng phải biến mất cùng nó.
  if ("MutationObserver" in window) {
    new MutationObserver(() => {
      if (current && !current.isConnected) hide();
    }).observe(document.documentElement, { childList: true, subtree: true });
  }
})();
