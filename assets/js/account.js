"use strict";

/* Đăng nhập/đăng ký chỉ xem trước giao diện: chưa có cơ sở dữ liệu (xem README,
   mục "Giai đoạn 2 — Ứng dụng full-stack"). Validate thật bằng ràng buộc HTML5,
   không giả lập đăng nhập thành công bằng localStorage hay bất kỳ cách nào khác. */

(() => {
  const tabs = [...document.querySelectorAll(".auth-tab")];
  const panels = { login: document.querySelector("#panel-login"), signup: document.querySelector("#panel-signup") };
  if (tabs.length === 0) return;

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

  function showPending(form, message) {
    const note = form.querySelector(".form-note");
    if (note) note.textContent = message;
  }

  const loginForm = document.querySelector("#login-form");
  if (loginForm) {
    loginForm.addEventListener("submit", (event) => {
      event.preventDefault();
      if (!loginForm.reportValidity()) return;
      showPending(loginForm, "Thông tin hợp lệ. Đăng nhập sẽ hoạt động khi website kết nối cơ sở dữ liệu ở giai đoạn phát triển tiếp theo.");
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
    signupForm.addEventListener("submit", (event) => {
      event.preventDefault();
      syncConfirm();
      if (!signupForm.reportValidity()) return;
      showPending(signupForm, "Thông tin hợp lệ. Tạo tài khoản sẽ hoạt động khi website kết nối cơ sở dữ liệu ở giai đoạn phát triển tiếp theo.");
    });
  }
})();
