"use strict";

/* Menu state comes from the server; stored preferences never grant a role. */
(() => {
  const nav = document.querySelector("[data-site-nav]");
  if (!nav) return;
  document.documentElement.classList.remove("nav-collapsed");
  const groups = [...nav.querySelectorAll("details")];
  groups.forEach(group => {
    group.addEventListener("toggle", () => {
      if (group.open) groups.forEach(other => { if (other !== group) other.open = false; });
    });
  });
  nav.addEventListener("keydown", event => {
    if (event.key !== "Escape") return;
    const group = event.target.closest("details[open]");
    if (group) { group.open = false; group.querySelector("summary").focus(); }
  });
  document.addEventListener("click", event => {
    if (!nav.contains(event.target)) groups.forEach(group => { group.open = false; });
  });
  // Relative URLs also work under a non-root Tomcat context.
  if (nav.hasAttribute("data-server-nav")) return;
  const script = document.currentScript || [...document.scripts].find(s => /\/site-nav\.js(?:\?|$)/.test(s.src));
  const root = new URL("../../", script.src);
  nav.querySelectorAll("a[data-nav-target]").forEach(link => {
    const target = new URL(link.href);
    if (target.pathname === location.pathname) link.setAttribute("aria-current", "page");
  });
  fetch(new URL("api/auth/me", root), {credentials: "same-origin", cache: "no-store"})
    .then(response => response.ok ? response.json() : null)
    .then(result => {
      const user = result?.data?.user;
      groups.forEach(group => {
        const role = group.dataset.navRole;
        group.hidden = role === "AUTH" ? !user : role === "ADMIN" ? user?.role !== "ADMIN" : false;
      });
      nav.querySelectorAll('[data-nav-audience="LEARNER"]').forEach(item => {
        item.hidden = user?.role === "ADMIN";
      });
      const auth = nav.querySelector(".site-nav-auth");
      auth.textContent = user ? "Tài khoản" : "Đăng nhập / Đăng ký";
    }).catch(() => { /* Auth-only links stay hidden; server remains the permission boundary. */ });
})();
