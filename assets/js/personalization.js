"use strict";

(() => {
  const accountLink = document.querySelector('.nav-list a[href$="tai-khoan.html"]');
  const panel = document.querySelector("#home-progress");
  if (!accountLink && !panel) return;

  const title = panel?.querySelector("#home-progress-title");
  const kicker = panel?.querySelector("#home-progress-kicker");
  const summary = panel?.querySelector("#home-progress-summary");
  const steps = panel?.querySelector("#home-progress-steps");
  const meter = panel?.querySelector("#home-progress-meter");
  const action = panel?.querySelector("#home-progress-link");
  const api = window.RobotAssemblyApi;

  function finishPanel() {
    if (panel) panel.setAttribute("aria-busy", "false");
  }

  function showDiscovery(message = "Chọn một mẫu robot để bắt đầu; đăng nhập để lưu tiến độ của bạn.") {
    if (!panel) return;
    kicker.textContent = "Góc thực hành";
    title.textContent = "Khám phá 3 mô hình robot";
    summary.textContent = message;
    steps.hidden = true;
    meter.hidden = true;
    action.href = "pages/mau-robot.html";
    action.textContent = "Xem mẫu robot";
    finishPanel();
  }

  function showRecent(session) {
    const model = (window.ROBOT_MODELS || []).find((item) => item.id === session.robotId);
    const modelName = model?.name || session.robotId;
    const params = new URLSearchParams({ model: session.robotId, session: session.id });
    const page = session.status === "PREPARING" ? "lap-rap.html" : "lap-rap-3d.html";

    kicker.textContent = "Phiên thực hành gần nhất";
    title.textContent = modelName;
    summary.textContent = `Chuẩn bị linh kiện: ${session.progressPercent}%`;
    steps.textContent = `Bước lắp ráp: ${session.completedStepCount}/${session.totalStepCount} bước`;
    steps.hidden = false;
    meter.value = session.progressPercent;
    meter.hidden = false;
    meter.setAttribute("aria-label", `Tiến độ chuẩn bị linh kiện của ${modelName}`);
    action.href = session.status === "ABANDONED"
      ? "pages/tai-khoan.html"
      : `pages/${page}?${params}`;
    action.textContent = session.status === "COMPLETED" ? "Xem mô hình đã hoàn thành"
      : session.status === "ABANDONED" ? "Xem lịch sử thực hành" : "Tiếp tục phiên này";
    finishPanel();
  }

  if (!api || ["4173", "63342"].includes(window.location.port)) {
    showDiscovery();
    return;
  }

  api.auth.me().then(async (user) => {
    if (accountLink) {
      const shortName = user.fullName.trim().split(/\s+/).at(-1);
      accountLink.querySelector("span").textContent = `Chào, ${shortName}`;
      accountLink.setAttribute("aria-label", `Tài khoản của ${user.fullName}`);
      accountLink.dataset.authenticated = "true";
    }
    if (!panel) return;
    try {
      const { items } = await api.assemblySessions.list({ page: 1, pageSize: 1 });
      if (items.length) showRecent(items[0]);
      else showDiscovery("Bạn chưa có phiên thực hành. Chọn một mẫu robot để bắt đầu.");
    } catch {
      showDiscovery("Không thể tải tiến độ lúc này. Bạn có thể mở trang tài khoản để thử lại.");
      action.href = "pages/tai-khoan.html";
      action.textContent = "Mở trang tài khoản";
    }
  }).catch((error) => {
    showDiscovery(error.code === "AUTH_REQUIRED"
      ? undefined
      : "Không thể kiểm tra phiên đăng nhập lúc này. Hãy mở trang tài khoản để thử lại.");
    if (panel && error.code !== "AUTH_REQUIRED") {
      action.href = "pages/tai-khoan.html";
      action.textContent = "Mở trang tài khoản";
    }
  });
})();
