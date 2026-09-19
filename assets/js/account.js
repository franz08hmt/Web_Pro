"use strict";

(() => {
  const api = window.RobotAssemblyApi;
  const tabs = [...document.querySelectorAll(".auth-tab")];
  const panels = { login: document.querySelector("#panel-login"), signup: document.querySelector("#panel-signup") };
  const entry = document.querySelector("#account-entry");
  const sessionPanel = document.querySelector("#account-session");
  const pageStatus = document.querySelector("#account-status");
  const title = document.querySelector("#account-title");
  const historyList = document.querySelector("#account-history");
  const historyStatus = document.querySelector("#account-history-status");
  const historyMore = document.querySelector("#account-history-more");
  let historyRevision = 0;
  let historyPage = 0;
  let authRevision = 0;
  if (!api || tabs.length === 0) {
    if (pageStatus) pageStatus.textContent = "Không thể tải kết nối API. Hãy tải lại trang.";
    return;
  }

  function activate(id, focusTab) {
    tabs.forEach((tab) => {
      const isActive = tab.dataset.tab === id;
      tab.setAttribute("aria-selected", String(isActive));
      tab.tabIndex = isActive ? 0 : -1;
      if (isActive && focusTab) tab.focus();
    });
    Object.entries(panels).forEach(([key, panel]) => { panel.hidden = key !== id; });
  }

  tabs.forEach((tab) => tab.addEventListener("click", () => activate(tab.dataset.tab, false)));

  document.querySelector(".auth-tabs").addEventListener("keydown", (event) => {
    if (!["ArrowLeft", "ArrowRight", "Home", "End"].includes(event.key)) return;
    event.preventDefault();
    const current = tabs.indexOf(document.activeElement);
    let index = current;
    if (event.key === "ArrowRight") index = (current + 1) % tabs.length;
    if (event.key === "ArrowLeft") index = (current - 1 + tabs.length) % tabs.length;
    if (event.key === "Home") index = 0;
    if (event.key === "End") index = tabs.length - 1;
    activate(tabs[index].dataset.tab, true);
  });

  function showNote(form, message, state = "") {
    const note = form.querySelector(".form-note");
    if (!note) return;
    note.textContent = message;
    note.dataset.state = state;
  }

  function setSubmitting(form, submitting, pendingText) {
    const button = form.querySelector("button[type='submit']");
    if (!button) return;
    if (!button.dataset.label) button.dataset.label = button.textContent;
    button.disabled = submitting;
    button.textContent = submitting ? pendingText : button.dataset.label;
    form.setAttribute("aria-busy", String(submitting));
  }

  function modelImage(model) {
    const path = model?.image || "";
    if (path.startsWith("/assets/images/")) return path;
    if (path.startsWith("../assets/images/")) return path;
    return "";
  }

  function renderSession(session, models) {
    const model = models.get(session.robotId);
    const item = document.createElement("li");
    const imagePath = modelImage(model);
    if (imagePath) {
      const image = document.createElement("img");
      image.src = imagePath;
      image.alt = model.name;
      image.loading = "lazy";
      item.append(image);
    }
    const name = document.createElement("strong");
    name.textContent = model?.name || session.robotId;
    const state = document.createElement("span");
    state.textContent = ({
      PREPARING: "Đang chuẩn bị", READY: "Đã đủ linh kiện", IN_PROGRESS: "Đang lắp ráp",
      COMPLETED: "Đã hoàn thành", ABANDONED: "Đã dừng"
    })[session.status] || session.status;
    const date = document.createElement("small");
    const updated = new Date(session.updatedAt);
    date.textContent = Number.isNaN(updated.getTime()) ? "" : `Cập nhật ${updated.toLocaleDateString("vi-VN")}`;
    const preparation = document.createElement("span");
    preparation.textContent = `Chuẩn bị linh kiện: ${session.progressPercent}%`;
    const progress = document.createElement("progress");
    progress.max = 100;
    progress.value = session.progressPercent;
    progress.setAttribute("aria-label", `Tiến độ chuẩn bị ${name.textContent}`);
    const steps = document.createElement("span");
    steps.textContent = `Bước lắp ráp: ${session.completedStepCount}/${session.totalStepCount}`;
    item.append(name, state, date, preparation, progress, steps);
    if (session.status !== "ABANDONED") {
      const link = document.createElement("a");
      const page = session.status === "PREPARING" ? "lap-rap.html" : "lap-rap-3d.html";
      link.href = `${page}?model=${encodeURIComponent(session.robotId)}&session=${encodeURIComponent(session.id)}`;
      link.textContent = session.status === "COMPLETED" ? "Xem phiên đã hoàn thành →" : "Tiếp tục phiên này →";
      item.append(link);
    }
    return item;
  }

  async function loadHistory(page, revision) {
    historyMore.disabled = true;
    historyStatus.textContent = "Đang tải các phiên lắp ráp…";
    try {
      const [result, liveModels] = await Promise.all([
        api.assemblySessions.list({ page, pageSize: 5 }),
        window.RobotContentApi?.loadRobots()?.catch(() => null)
      ]);
      if (revision !== historyRevision) return;
      const models = new Map((liveModels || window.ROBOT_MODELS || []).map((model) => [model.id, model]));
      if (page === 1) historyList.replaceChildren();
      historyList.append(...result.items.map((session) => renderSession(session, models)));
      historyPage = page;
      historyStatus.textContent = result.meta.total === 0
        ? "Bạn chưa có phiên thực hành nào. Hãy chọn một mẫu robot để bắt đầu."
        : `Đang hiển thị ${historyList.children.length}/${result.meta.total} phiên.`;
      historyMore.hidden = historyList.children.length >= result.meta.total;
    } catch (error) {
      if (revision !== historyRevision) return;
      historyStatus.textContent = `Không thể tải lịch sử. ${error.message}`;
    } finally {
      if (revision === historyRevision) historyMore.disabled = false;
    }
  }

  historyMore.addEventListener("click", () => void loadHistory(historyPage + 1, historyRevision));

  function showUser(user) {
    entry.hidden = true;
    sessionPanel.hidden = false;
    pageStatus.textContent = "";
    title.textContent = "Tài khoản của bạn";
    document.querySelector("#account-user-name").textContent = user.fullName;
    document.querySelector("#account-user-email").textContent = user.email;
    document.querySelector("#account-user-role").textContent = user.role === "ADMIN" ? "Quản trị nội dung" : "Thành viên";
    historyList.replaceChildren();
    historyMore.hidden = true;
    void loadHistory(1, ++historyRevision);
  }

  function showAuth() {
    historyRevision += 1;
    historyList.replaceChildren();
    sessionPanel.hidden = true;
    entry.hidden = false;
    pageStatus.textContent = "";
    title.textContent = "Đăng nhập hoặc tạo tài khoản";
  }

  const loginForm = document.querySelector("#login-form");
  if (loginForm) {
    loginForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      if (!loginForm.reportValidity()) return;
      authRevision += 1;
      showNote(loginForm, "");
      setSubmitting(loginForm, true, "Đang đăng nhập…");
      try {
        const fields = new FormData(loginForm);
        showUser(await api.auth.login({ email: fields.get("email"), password: fields.get("password") }));
        loginForm.reset();
      } catch (error) {
        showNote(loginForm, error.message || "Không thể đăng nhập. Vui lòng thử lại.", "error");
      } finally {
        setSubmitting(loginForm, false, "");
      }
    });
  }

  const signupForm = document.querySelector("#signup-form");
  if (signupForm) {
    const password = signupForm.querySelector("#signup-password");
    const confirm = signupForm.querySelector("#signup-confirm");
    const syncConfirm = () => {
      confirm.setCustomValidity(confirm.value && confirm.value !== password.value ? "Mật khẩu nhập lại chưa khớp." : "");
    };
    password.addEventListener("input", syncConfirm);
    confirm.addEventListener("input", syncConfirm);
    signupForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      syncConfirm();
      if (!signupForm.reportValidity()) return;
      authRevision += 1;
      showNote(signupForm, "");
      setSubmitting(signupForm, true, "Đang tạo tài khoản…");
      try {
        const fields = new FormData(signupForm);
        showUser(await api.auth.register({
          fullName: fields.get("fullName"),
          email: fields.get("email"),
          password: fields.get("password")
        }));
        signupForm.reset();
      } catch (error) {
        showNote(signupForm, error.message || "Không thể tạo tài khoản. Vui lòng thử lại.", "error");
      } finally {
        setSubmitting(signupForm, false, "");
      }
    });
  }

  document.querySelector("#logout-button").addEventListener("click", async (event) => {
    authRevision += 1;
    const button = event.currentTarget;
    button.disabled = true;
    pageStatus.textContent = "Đang đăng xuất…";
    try {
      await api.auth.logout();
      showAuth();
      document.querySelector("#login-email").focus();
    } catch (error) {
      pageStatus.textContent = error.message || "Không thể đăng xuất. Vui lòng thử lại.";
      pageStatus.dataset.state = "error";
    } finally {
      button.disabled = false;
    }
  });

  const initialRevision = authRevision;
  api.auth.me()
    .then((user) => { if (initialRevision === authRevision) showUser(user); })
    .catch((error) => {
      if (initialRevision !== authRevision) return;
      if (error.code === "AUTH_REQUIRED") showAuth();
      else {
        showAuth();
        pageStatus.textContent = error.message || "Không thể kiểm tra phiên đăng nhập.";
        pageStatus.dataset.state = "error";
      }
    });
})();
