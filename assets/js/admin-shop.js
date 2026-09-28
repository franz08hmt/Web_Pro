"use strict";

(() => {
  const api = window.RobotAssemblyApi;
  const status = document.getElementById("admin-shop-status");
  const workspace = document.getElementById("admin-shop-workspace");
  const form = document.getElementById("shop-product-form");
  const rows = document.getElementById("admin-products");
  if (!api || !status || !workspace || !form || !rows) return;

  const idInput = form.elements.id;
  const componentSelect = form.elements.componentId;
  const priceInput = form.elements.priceVnd;
  const stockInput = form.elements.stockQuantity;
  const activeInput = form.elements.active;
  const title = document.getElementById("shop-form-title");
  const saveButton = document.getElementById("shop-save-button");
  const cancelButton = document.getElementById("shop-cancel-edit");
  const money = new Intl.NumberFormat("vi-VN", { maximumFractionDigits: 0 });
  const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, (char) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);
  let products = [];
  let editingId = null;

  function setStatus(message, isError = false) {
    status.textContent = message;
    status.classList.toggle("is-error", isError);
  }

  function resetForm() {
    editingId = null;
    form.reset();
    idInput.disabled = false;
    activeInput.checked = true;
    title.textContent = "Thêm sản phẩm";
    saveButton.textContent = "Thêm sản phẩm";
    cancelButton.hidden = true;
  }

  async function loadProducts() {
    const result = await api.adminShop.list({ page: 1, limit: 100 });
    products = result.items || [];
    if (!products.length) {
      rows.innerHTML = '<tr><td colspan="5">Chưa có sản phẩm. Có thể thêm từ linh kiện kỹ thuật.</td></tr>';
      return;
    }
    rows.innerHTML = products.map((product) => `<tr>
      <td><strong>${escapeHtml(product.name)}</strong><br><span>${escapeHtml(product.id)} · ${escapeHtml(product.componentId)}</span></td>
      <td>${money.format(Number(product.priceVnd) || 0)} ₫</td>
      <td>${Number(product.stockQuantity) || 0}</td>
      <td>${product.active ? "Đang bán" : "Đã ngừng bán"}</td>
      <td><div class="shop-admin-actions">
        <button type="button" data-edit="${escapeHtml(product.id)}">Sửa</button>
        ${product.active ? `<button type="button" data-deactivate="${escapeHtml(product.id)}">Ngừng bán</button>` : ""}
      </div></td>
    </tr>`).join("");
  }

  async function loadComponents() {
    const result = await api.components.list({ page: 1, limit: 100 });
    componentSelect.innerHTML = '<option value="">-- Chọn linh kiện kỹ thuật --</option>' +
      (result.items || []).map((component) => `<option value="${escapeHtml(component.id)}">${escapeHtml(component.name)} (${escapeHtml(component.id)})</option>`).join("");
  }

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!form.reportValidity()) return;
    const product = {
      componentId: componentSelect.value,
      priceVnd: Number(priceInput.value),
      stockQuantity: Number(stockInput.value),
      active: activeInput.checked
    };
    if (!editingId) product.id = idInput.value.trim();
    saveButton.disabled = true;
    try {
      if (editingId) await api.adminShop.update(editingId, product);
      else await api.adminShop.create(product);
      resetForm();
      await loadProducts();
      setStatus("Đã lưu sản phẩm. Catalog đọc lại dữ liệu mới từ máy chủ.");
    } catch (error) {
      setStatus(error.message || "Không lưu được sản phẩm.", true);
    } finally {
      saveButton.disabled = false;
    }
  });

  cancelButton.addEventListener("click", resetForm);

  rows.addEventListener("click", async (event) => {
    const edit = event.target.closest("[data-edit]");
    const deactivate = event.target.closest("[data-deactivate]");
    if (edit) {
      const product = products.find((entry) => entry.id === edit.dataset.edit);
      if (!product) return;
      editingId = product.id;
      idInput.value = product.id;
      idInput.disabled = true;
      componentSelect.value = product.componentId;
      priceInput.value = String(Math.round(Number(product.priceVnd)));
      stockInput.value = String(product.stockQuantity);
      activeInput.checked = Boolean(product.active);
      title.textContent = `Sửa ${product.name}`;
      saveButton.textContent = "Lưu thay đổi";
      cancelButton.hidden = false;
      form.scrollIntoView({ behavior: "smooth", block: "start" });
      componentSelect.focus();
      return;
    }
    if (deactivate) {
      const product = products.find((entry) => entry.id === deactivate.dataset.deactivate);
      if (!product || !window.confirm(`Ngừng bán “${product.name}”? Đơn cũ vẫn được giữ nguyên.`)) return;
      deactivate.disabled = true;
      try {
        await api.adminShop.deactivate(product.id);
        await loadProducts();
        setStatus("Đã ngừng bán; dữ liệu sản phẩm và lịch sử đơn vẫn được giữ.");
      } catch (error) {
        setStatus(error.message || "Không thể ngừng bán sản phẩm.", true);
      }
    }
  });

  (async () => {
    try {
      const user = await api.auth.me();
      if (String(user.role || "").toUpperCase() !== "ADMIN") {
        setStatus("Tài khoản hiện tại không có quyền quản trị cửa hàng.", true);
        return;
      }
      await Promise.all([loadComponents(), loadProducts()]);
      workspace.hidden = false;
      setStatus(`Đã xác minh quyền ADMIN. Xin chào ${user.fullName}.`);
    } catch (error) {
      setStatus(error.status === 401 || error.status === 403
        ? "Bạn cần đăng nhập bằng tài khoản ADMIN để mở chức năng này."
        : (error.message || "Không tải được dữ liệu quản trị."), true);
    }
  })();
})();
