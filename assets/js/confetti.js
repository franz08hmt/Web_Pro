"use strict";

/*
 * Hiệu ứng ăn mừng nhẹ khi hoàn tất lắp ráp (Chức năng 4).
 *
 * Chỉ tạo phần tử DOM + animation CSS, không dùng canvas/requestAnimationFrame,
 * nên không cạnh tranh hiệu năng với vòng render Three.js của phòng 3D. Tự dọn
 * toàn bộ phần tử sau khi hiệu ứng kết thúc. Tôn trọng prefers-reduced-motion:
 * khi người dùng bật giảm chuyển động, hàm này không tạo hiệu ứng gì — phần
 * thông báo văn bản ở nơi gọi vẫn đảm nhiệm việc báo tin thành công. Không tự
 * phát âm thanh. Lớp phủ dùng pointer-events: none nên không chặn chuột/bàn phím.
 */
(() => {
  const COLORS = ["#ff8748", "#38d3c3", "#ffd166", "#6ee7ff", "#c4f0c2"];
  const PIECE_COUNT = 36;
  const DURATION_MS = 2600;

  function celebrate() {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    if (document.querySelector(".ral-confetti-layer")) return;

    const layer = document.createElement("div");
    layer.className = "ral-confetti-layer";
    layer.setAttribute("aria-hidden", "true");

    for (let index = 0; index < PIECE_COUNT; index += 1) {
      const piece = document.createElement("span");
      piece.className = "ral-confetti-piece";
      piece.style.left = `${Math.random() * 100}%`;
      piece.style.background = COLORS[index % COLORS.length];
      piece.style.animationDelay = `${Math.random() * 300}ms`;
      piece.style.animationDuration = `${1800 + Math.random() * 700}ms`;
      piece.style.transform = `rotate(${Math.random() * 360}deg)`;
      layer.append(piece);
    }

    document.body.append(layer);
    window.setTimeout(() => layer.remove(), DURATION_MS);
  }

  window.RobotConfetti = Object.freeze({ celebrate });
})();
