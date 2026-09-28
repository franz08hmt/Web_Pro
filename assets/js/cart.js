"use strict";

(() => {
  const api = window.RobotAssemblyApi;
  const status = document.getElementById("cart-status");
  const content = document.getElementById("cart-content");
  const itemsNode = document.getElementById("cart-items");
  const totalNode = document.getElementById("cart-total");
  const checkoutButton = document.getElementById("checkout-button");
  const resultNode = document.getElementById("checkout-result");
  if (!api || !status || !content || !itemsNode || !checkoutButton) return;

  const money = new Intl.NumberFormat("vi-VN", { maximumFractionDigits: 0 });
  const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, (char) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);
  const formatMoney = (amount) => `${money.format(Number(amount) || 0)} ₫`;
  let cart = [];
  let busy = false;

  function setStatus(message, isError = false) {
    status.textContent = message;
    status.classList.toggle("is-error", isError);
  }

  function safeImage(value) {
    if (typeof value !== "string" || !value.startsWith("/assets/images/")) return "";
    try {
      const url = new URL(value, window.location.origin);
      return url.origin === window.location.origin ? url.href : "";
    } catch {
      return "";
    }
  }

  function render() {
    if (!cart.length) {
      itemsNode.innerHTML = '<p class="shop-empty">Giỏ hàng đang trống. Hãy chọn linh kiện cần tìm hiểu.</p>';
      totalNode.textContent = formatMoney(0);
      checkoutButton.disabled = true;
      return;
    }
    itemsNode.innerHTML = cart.map((item) => {
      const product = item.product || {};
      const image = safeImage(product.image);
      return `<article class="cart-row" data-cart-product="${escapeHtml(product.id)}">
        ${image ? `<img src="${escapeHtml(image)}" alt="" loading="lazy" width="92" height="82">` : ""}
        <div>
          <h3>${escapeHtml(product.name)}</h3>
          <p>${formatMoney(product.priceVnd)} / sản phẩm · còn ${Number(product.stockQuantity) || 0} trong kho demo</p>
          <div class="cart-row-actions">
            <label for="quantity-${escapeHtml(product.id)}">Số lượng</label>
            <input id="quantity-${escapeHtml(product.id)}" data-quantity type="number" min="1" max="10000" step="1" value="${Number(item.quantity) || 1}" ${busy ? "disabled" : ""}>
            <button type="button" data-update ${busy ? "disabled" : ""}>Cập nhật</button>
            <button type="button" data-remove ${busy ? "disabled" : ""}>Bỏ khỏi giỏ</button>
          </div>
        </div>
        <p class="cart-line-total">${formatMoney(item.lineTotalVnd)}</p>
      </article>`;
    }).join("");
    const total = cart.reduce((sum, item) => sum + (Number(item.lineTotalVnd) || 0), 0);
    totalNode.textContent = formatMoney(total);
    checkoutButton.disabled = busy;
  }

  async function loadCart() {
    cart = await api.cart.get();
    render();
    content.hidden = false;
    setStatus(cart.length ? "Giá và số lượng hiện tại đã được tải từ máy chủ." : "Giỏ trống; đơn mô phỏng không phát sinh thanh toán.");
  }

  itemsNode.addEventListener("click", async (event) => {
    const row = event.target.closest("[data-cart-product]");
    if (!row || busy) return;
    const productId = row.dataset.cartProduct;
    busy = true;
    render();
    try {
      if (event.target.closest("[data-remove]")) {
        await api.cart.remove(productId);
      } else if (event.target.closest("[data-update]")) {
        const input = row.querySelector("[data-quantity]");
        const quantity = Number(input.value);
        if (!Number.isInteger(quantity) || quantity < 1 || quantity > 10000) {
          setStatus("Số lượng phải là số nguyên từ 1 đến 10.000.", true);
          return;
        }
        cart = await api.cart.setQuantity(productId, quantity);
      } else {
        return;
      }
      await loadCart();
    } catch (error) {
      setStatus(error.message || "Không thể cập nhật giỏ hàng.", true);
    } finally {
      busy = false;
      render();
    }
  });

  checkoutButton.addEventListener("click", async () => {
    if (busy || !cart.length) return;
    busy = true;
    checkoutButton.disabled = true;
    setStatus("Máy chủ đang xác nhận tồn kho và tạo đơn mô phỏng…");
    try {
      // Payload không chứa userId, giá hay tổng tiền; server tự đọc giỏ và tính lại.
      const order = await api.orders.checkout();
      cart = [];
      render();
      resultNode.innerHTML = `<p class="section-label">Successfully!</p>
        <h2>Đơn mô phỏng đã được ghi nhận</h2>
        <p>Mã đơn <strong>#${escapeHtml(order.id)}</strong> · tổng máy chủ xác nhận <strong>${formatMoney(order.totalVnd)}</strong></p>
        <p>Không có khoản thanh toán hay giao hàng thật nào được thực hiện.</p>
        <a href="../order-history">Xem lịch sử đơn</a><a href="cua-hang.html">Tiếp tục xem cửa hàng</a>`;
      resultNode.hidden = false;
      setStatus("Hoàn tất. Tồn kho và giỏ hàng đã được cập nhật trong transaction.");
    } catch (error) {
      setStatus(error.message || "Không thể xác nhận đơn. Hãy kiểm tra lại tồn kho.", true);
    } finally {
      busy = false;
      checkoutButton.disabled = !cart.length;
    }
  });

  (async () => {
    try {
      await api.auth.me();
      await loadCart();
    } catch (error) {
      if (error.status === 401 || error.status === 403) {
        setStatus("Bạn cần đăng nhập để xem giỏ hàng.", true);
        content.hidden = true;
        const link = document.createElement("a");
        link.className = "button";
        link.href = "tai-khoan.html";
        link.textContent = "Đăng nhập";
        status.insertAdjacentElement("afterend", link);
      } else {
        setStatus(error.message || "Không tải được giỏ hàng.", true);
      }
    }
  })();
})();
