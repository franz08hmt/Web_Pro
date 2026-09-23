"use strict";

(() => {
  const status = document.querySelector("#users-status");
  const list = document.querySelector("#users-list");
  const workspace = document.querySelector("#users-workspace");
  const pager = document.querySelector("#users-pager");
  let page = 1;
  let token;
  let selfId;

  async function api(path, method = "GET", body) {
    const response = await fetch(`/api${path}`, {
      method, credentials: "same-origin",
      headers: { Accept: "application/json", ...(body ? { "Content-Type": "application/json" } : {}),
        ...(token ? { "X-CSRF-Token": token } : {}) },
      ...(body ? { body: JSON.stringify(body) } : {})
    });
    const result = await response.json().catch(() => null);
    if (!response.ok) throw new Error(result?.error?.message || `Yêu cầu thất bại (${response.status}).`);
    return result;
  }

  async function load() {
    status.textContent = "Đang tải tài khoản…";
    const result = await api(`/admin/users?page=${page}`);
    list.replaceChildren();
    result.data.forEach((user) => {
      const row = document.createElement("tr");
      const date = new Date(user.createdAt);
      [user.fullName, user.email, Number.isNaN(date.getTime()) ? "" : date.toLocaleDateString("vi-VN")]
        .forEach((value) => { const cell = document.createElement("td"); cell.textContent = value; row.append(cell); });
      const roleCell = document.createElement("td");
      const role = document.createElement("select");
      role.setAttribute("aria-label", `Vai trò của ${user.fullName}`);
      for (const [value, label] of [["USER", "Thành viên"], ["ADMIN", "Quản trị"]]) {
        const option = document.createElement("option");
        option.value = value; option.textContent = label; role.append(option);
      }
      role.value = user.role;
      role.disabled = user.id === selfId;
      roleCell.append(role);
      const actionCell = document.createElement("td");
      const save = document.createElement("button");
      save.type = "button"; save.textContent = "Lưu quyền";
      save.disabled = user.id === selfId;
      save.addEventListener("click", async () => {
        if (role.value === user.role) return;
        if (!window.confirm(`Đổi quyền của ${user.email} thành ${role.value}?`)) return;
        save.disabled = true;
        try {
          await api(`/admin/users/${user.id}/role`, "PATCH", { role: role.value });
          await load();
          status.textContent = `Đã cập nhật quyền của ${user.email}. Các phiên đăng nhập cũ của tài khoản này đã hết hiệu lực.`;
        } catch (error) {
          status.textContent = error.message;
          save.disabled = false;
        }
      });
      actionCell.append(save); row.append(roleCell, actionCell); list.append(row);
    });
    workspace.hidden = false;
    pager.hidden = false;
    document.querySelector("#users-page-number").textContent = `Trang ${page} · ${result.meta.total} tài khoản`;
    document.querySelector("#users-prev").disabled = page === 1;
    document.querySelector("#users-next").disabled = page * result.meta.pageSize >= result.meta.total;
    status.textContent = `Đã tải ${result.data.length} tài khoản.`;
  }

  async function move(delta) {
    page += delta;
    try { await load(); } catch (error) { page -= delta; status.textContent = error.message; }
  }
  document.querySelector("#users-prev").addEventListener("click", () => void move(-1));
  document.querySelector("#users-next").addEventListener("click", () => void move(1));
  api("/auth/me").then((session) => {
    if (session.data.user.role !== "ADMIN") throw new Error("Bạn cần quyền quản trị để xem tài khoản.");
    token = session.csrfToken;
    selfId = session.data.user.id;
    return load();
  }).catch((error) => { status.textContent = error.message; });
})();
