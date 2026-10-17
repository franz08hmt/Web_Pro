"use strict";

// Phiên GROUP cũ vẫn giữ nguyên dữ kiện; người dùng chủ động mở phiên chi tiết riêng.
(() => {
  let panel;
  window.addEventListener("robotlab:assembly-session", event => {
    const session = event.detail;
    panel?.remove();
    if (!session || session.assemblyMode !== "GROUP") return;
    const anchor = document.querySelector("#assembly-3d-sync-status, #assembly-status");
    if (!anchor) return;
    panel = document.createElement("section");
    panel.className = "status-message";
    panel.setAttribute("aria-label", "Phiên lắp chi tiết mới");
    const description = document.createElement("p");
    description.textContent = "Đây là phiên cũ lắp theo nhóm. Mở phiên kéo-thả mới để gắn từng vật thể và chọn hướng; phiên cũ được giữ nguyên.";
    const button = document.createElement("button");
    button.type = "button";
    button.className = "reset-button";
    button.textContent = "Mở phiên kéo-thả riêng";
    const status = document.createElement("p");
    status.setAttribute("role", "status");
    panel.append(description, button, status);
    anchor.after(panel);
    button.addEventListener("click", async () => {
      button.disabled = true;
      status.textContent = "Đang mở phiên chi tiết; không thay đổi phiên cũ…";
      try {
        const next = await window.RobotAssemblyApi.assemblySessions.createDetailed(session.robotId);
        const url = new URL("lap-rap.html", window.location.href);
        url.searchParams.set("model", next.robotId);
        url.searchParams.set("session", next.id);
        window.location.assign(url.href);
      } catch (error) {
        status.textContent = `Chưa mở được phiên chi tiết. ${error.message}`;
        button.disabled = false;
      }
    });
  });
})();
