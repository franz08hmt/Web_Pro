package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.ShopOrder;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.OrderDB;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/** Checkout mô phỏng và lịch sử chỉ của tài khoản hiện tại. */
@WebServlet("/api/orders")
public class OrderServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int HISTORY_LIMIT = 20;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null) return;
        try {
            int page = ValidationUtil.parsePositiveInt(
                    request.getParameter("page"), 1, ValidationUtil.MAX_PAGE);
            int limit = ValidationUtil.parsePositiveInt(
                    request.getParameter("limit"), HISTORY_LIMIT, HISTORY_LIMIT);
            List<ShopOrder> orders = OrderDB.selectOrders(user.getId(), limit, (page - 1) * limit);
            StringBuilder items = new StringBuilder();
            for (ShopOrder order : orders) {
                if (items.length() > 0) items.append(',');
                items.append(order.toJson());
            }
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + items + "],\"meta\":{\"page\":" + page
                            + ",\"limit\":" + limit + ",\"total\":"
                            + OrderDB.countOrders(user.getId()) + "}}");
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Phân trang không hợp lệ.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) return;
        try {
            // Không đọc giá/tổng/user từ body; checkout tiêu thụ cart và giá do DB cung cấp.
            ShopOrder order = OrderDB.checkout(user.getId());
            ResponseUtil.sendJson(response, HttpServletResponse.SC_CREATED,
                    "{\"data\":" + order.toJson() + "}");
        } catch (IllegalStateException e) {
            sendCheckoutConflict(response, e.getMessage());
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    private void sendCheckoutConflict(HttpServletResponse response, String reason) throws IOException {
        String code = switch (reason == null ? "" : reason) {
            case "cart-empty" -> "CART_EMPTY";
            case "product-unavailable" -> "PRODUCT_UNAVAILABLE";
            case "order-total-out-of-range" -> "ORDER_TOTAL_LIMIT";
            default -> "STOCK_UNAVAILABLE";
        };
        String message = switch (code) {
            case "CART_EMPTY" -> "Giỏ hàng đang trống.";
            case "PRODUCT_UNAVAILABLE" -> "Một sản phẩm trong giỏ đã ngừng bán.";
            case "ORDER_TOTAL_LIMIT" -> "Tổng đơn vượt giới hạn dữ liệu.";
            default -> "Tồn kho vừa thay đổi; hãy kiểm tra lại giỏ hàng.";
        };
        ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT, code, message);
    }
}
