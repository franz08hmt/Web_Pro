"use strict";

/* Menu state comes from the server; stored preferences never grant a role. */
(() => {
  const nav = document.querySelector("[data-site-nav]");
  if (!nav) return;
  if (!document.querySelector("#nav-toggle")) document.documentElement.classList.remove("nav-collapsed");
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
  const publishAuth = (status, user = null) => {
    window.SiteAuthState = {status, user};
    window.dispatchEvent(new CustomEvent("robot:auth-ready", {detail: window.SiteAuthState}));
  };
  publishAuth("pending");
  fetch(new URL("api/auth/me", root), {credentials: "same-origin", cache: "no-store"})
    .then(response => {
      if (response.status === 401) return {data: {user: null}};
      if (!response.ok) throw Error("Xác thực tạm gián đoạn.");
      return response.json();
    })
    .then(result => {
      const user = result?.data?.user;
      if (!result?.data || (user && !["USER", "ADMIN"].includes(user.role))) throw Error("Invalid identity");
      groups.forEach(group => {
        const role = group.dataset.navRole;
        group.hidden = role === "AUTH" ? !user : role === "ADMIN" ? user?.role !== "ADMIN" : false;
      });
      nav.querySelectorAll('[data-nav-audience="LEARNER"]').forEach(item => {
        item.hidden = user?.role === "ADMIN";
      });
      const auth = nav.querySelector(".site-nav-auth");
      auth.textContent = user ? "Tài khoản" : "Đăng nhập / Đăng ký";
      publishAuth("ready", user || null);
    }).catch(() => {
      publishAuth("unavailable");
      nav.querySelector(".site-nav-auth").textContent = "Xác thực tạm gián đoạn";
    });
})();
