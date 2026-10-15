"use strict";

(() => {
  const api = window.RobotAssemblyApi;
  const grid = document.getElementById("shop-products");
  const status = document.getElementById("shop-status");
  const pagination = document.getElementById("shop-pagination");
  const userLabel = document.getElementById("shop-user");
  if (!api || !grid || !status) return;

  const PAGE_SIZE = 20;
  const state = { page: 1, total: 0, user: null, cart: [] };
  let listVersion = 0, focusVersion = 0, visibleItems = [], focusedProduct = null, followedHash = "";
  const focusArea = document.createElement("section");
  focusArea.setAttribute("aria-label", "Sản phẩm từ liên kết");
  focusArea.hidden = true;
  const focusStatus = document.createElement("p");
  focusStatus.setAttribute("role", "status");
  const focusGrid = document.createElement("div");
  focusGrid.className = "shop-product-grid";
  focusArea.append(focusStatus, focusGrid);
  grid.before(focusArea);
  const money = new Intl.NumberFormat("vi-VN", { maximumFractionDigits: 0 });
  const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, (char) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);
  const formatMoney = (amount) => `${money.format(Number(amount) || 0)} ₫`;

  function imageUrl(value) {
    if (typeof value !== "string" || !value.startsWith("/assets/images/")) return "";
    try {
      const url = new URL(value, window.location.origin);
      return url.origin === window.location.origin ? url.href : "";
    } catch {
      return "";
    }
  }

  function setStatus(message, isError = false) {
    status.textContent = message;
    status.classList.toggle("is-error", isError);
  }

  async function readSession() {
    try {
      state.user = await api.auth.me();
      if (userLabel) userLabel.textContent = `Xin chào, ${state.user.fullName}`;
      const role = String(state.user.role || "").toUpperCase();
      const adminLink = document.getElementById("admin-shop-link");
      if (adminLink && role === "ADMIN") adminLink.hidden = false;
      state.cart = await api.cart.get();
    } catch (error) {
      state.user = null;
      state.cart = [];
      if (userLabel) userLabel.textContent = "Khách";
      if (error.status !== 401 && error.status !== 403) throw error;
    }
  }

  function renderProducts(items, container = grid) {
    if (!items.length) {
      container.innerHTML = '<p class="shop-empty">Chưa có sản phẩm đang mở bán.</p>';
      return;
    }
    container.innerHTML = items.map((product) => {
      const src = imageUrl(product.image);
      const cartItem = state.cart.find((item) => item.product?.id === product.id);
      const quantity = cartItem?.quantity || 0;
      const outOfStock = Number(product.stockQuantity) < 1;
      const stockText = outOfStock ? "Tạm hết hàng" : `Còn ${Number(product.stockQuantity)} sản phẩm demo`;
      return `<article class="shop-product-card" id="shop-product-${escapeHtml(product.id)}">
        ${src ? `<img src="${escapeHtml(src)}" alt="" loading="lazy" width="480" height="360">` : ""}
        <p class="shop-category">${escapeHtml(product.category)} · mã ${escapeHtml(product.componentId)}</p>
        <h3>${escapeHtml(product.name)}</h3>
        <p class="shop-description">${escapeHtml(product.description)}</p>
        <p class="shop-product-price">${formatMoney(product.priceVnd)}</p>
        <p class="shop-stock${Number(product.stockQuantity) <= 3 ? " is-low" : ""}">${stockText}</p>
        <button class="button" type="button" data-add-product="${escapeHtml(product.id)}" ${outOfStock ? "disabled" : ""}>
          ${state.user ? (quantity ? `Thêm vào giỏ (${quantity})` : "Thêm vào giỏ") : "Đăng nhập để thêm"}
        </button>
        <a class="shop-inline-link" href="${escapeHtml(window.LearningContext.componentUrl(product.componentId))}">Xem thông số kỹ thuật</a>
      </article>`;
    }).join("");
  }

  function renderPagination() {
    const pages = Math.max(1, Math.ceil(state.total / PAGE_SIZE));
    pagination.hidden = pages <= 1;
    if (pages <= 1) return;
    pagination.innerHTML = `<button type="button" data-page="${state.page - 1}" ${state.page <= 1 ? "disabled" : ""}>← Trang trước</button>
      <span>Trang ${state.page} / ${pages}</span>
      <button type="button" data-page="${state.page + 1}" ${state.page >= pages ? "disabled" : ""}>Trang sau →</button>`;
  }

  function requestedProduct() {
    if (!location.hash.startsWith("#shop-product-")) return null;
    return decodeURIComponent(location.hash.slice("#shop-product-".length));
  }

  function renderFocus() {
    // Clear the focused copy before rendering a page that might contain the same SKU.
    focusGrid.replaceChildren();
    if (!focusedProduct) return;
    const onPage = visibleItems.some(item => item.id === focusedProduct.id);
    focusArea.hidden = onPage;
    if (!onPage) renderProducts([focusedProduct], focusGrid);
    const target = document.getElementById(`shop-product-${focusedProduct.id}`);
    if (target && followedHash !== location.hash) {
      followedHash = location.hash;
      target.setAttribute("tabindex", "-1");
      target.scrollIntoView({block: "center"});
      target.focus({preventScroll: true});
    }
  }

  async function loadFocusedProduct() {
    const version = ++focusVersion;
    focusedProduct = null;
    focusGrid.replaceChildren();
    focusArea.hidden = true;
    let id;
    try { id = requestedProduct(); }
    catch {
      focusArea.hidden = false;
      focusStatus.textContent = "Liên kết sản phẩm không hợp lệ. Bạn vẫn có thể xem catalog bên dưới.";
      return;
    }
    if (!id) return;
    focusArea.hidden = false;
    focusStatus.textContent = "Đang tìm sản phẩm từ liên kết…";
    try {
      const product = await api.shop.get(id);
      if (version !== focusVersion) return;
      focusedProduct = product;
      focusStatus.textContent = Number(product.stockQuantity) < 1
        ? "Sản phẩm được yêu cầu hiện tạm hết hàng." : "Sản phẩm được yêu cầu từ liên kết:";
      renderFocus();
    } catch (error) {
      if (version !== focusVersion) return;
      focusStatus.textContent = error.status === 404
        ? "Sản phẩm không tồn tại hoặc đã ngừng bán. Bạn vẫn có thể xem catalog bên dưới."
        : "Chưa tải được sản phẩm từ liên kết. Hãy tải lại trang; catalog bên dưới vẫn dùng được.";
    }
  }

  async function loadProducts() {
    const version = ++listVersion;
    const page = state.page;
    setStatus("Đang tải catalog…");
    grid.setAttribute("aria-busy", "true");
    try {
      const result = await api.shop.list({ page, limit: PAGE_SIZE });
      if (version !== listVersion) return;
      state.total = Number(result.meta?.total) || 0;
      visibleItems = result.items || [];
      focusGrid.replaceChildren();
      renderProducts(visibleItems);
      renderFocus();
      renderPagination();
      setStatus(state.user
        ? "Giá tham khảo và tồn kho được hiển thị từ máy chủ; đơn đặt chỉ là dữ liệu mô phỏng."
        : "Bạn đang xem với tư cách khách. Đăng nhập để thêm linh kiện vào giỏ.");
    } catch (error) {
      if (version !== listVersion) return;
      setStatus(error.message || "Không tải được catalog. Vui lòng thử lại.", true);
    } finally {
      if (version === listVersion) grid.setAttribute("aria-busy", "false");
    }
  }

  async function addProduct(event) {
    const button = event.target.closest("[data-add-product]");
    if (!button) return;
    if (!state.user) {
      setStatus("Hãy đăng nhập trước khi thêm vào giỏ.", true);
      window.location.href = "tai-khoan.html";
      return;
    }
    button.disabled = true;
    try {
      const id = button.dataset.addProduct;
      const current = state.cart.find((item) => item.product?.id === id)?.quantity || 0;
      state.cart = await api.cart.setQuantity(id, current + 1);
      await loadProducts();
      setStatus("Đã cập nhật giỏ hàng.");
    } catch (error) {
      setStatus(error.message || "Không thêm được sản phẩm vào giỏ.", true);
    } finally {
      button.disabled = false;
    }
  }
  grid.addEventListener("click", addProduct);
  focusGrid.addEventListener("click", addProduct);
  window.addEventListener("hashchange", () => { followedHash = ""; void loadFocusedProduct(); });

  pagination.addEventListener("click", async (event) => {
    const button = event.target.closest("[data-page]");
    if (!button || button.disabled) return;
    state.page = Number(button.dataset.page);
    await loadProducts();
  });

  (async () => {
    try {
      await readSession();
    } catch {
      if (userLabel) userLabel.textContent = "Chưa kết nối";
    }
    await loadProducts();
    await loadFocusedProduct();
  })();
})();
