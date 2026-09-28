package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.CartItem;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.CartDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/** Giỏ riêng của user đang đăng nhập; PUT đặt số lượng, DELETE bỏ một dòng. */
@WebServlet("/api/cart/*")
public class CartServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int MAX_BODY = 4096;
    private static final int MAX_QUANTITY = 10000;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null) return;
        if (request.getPathInfo() != null && !request.getPathInfo().isEmpty()) {
            sendNotFound(response);
            return;
        }
        try {
            sendCart(response, CartDB.selectCart(user.getId()));
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) return;
        String productId = productId(request);
        if (!ValidationUtil.isSlug(productId) || productId.length() > 64) {
            sendNotFound(response);
            return;
        }
        try {
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            int quantity = JsonUtil.intField(body, "quantity");
            if (quantity < 1 || quantity > MAX_QUANTITY) {
                sendInvalid(response);
                return;
            }
            CartDB.setQuantity(user.getId(), productId, quantity);
            sendCart(response, CartDB.selectCart(user.getId()));
        } catch (IllegalArgumentException e) {
            if ("product-unavailable".equals(e.getMessage())) {
                sendProductUnavailable(response);
            } else {
                sendInvalid(response);
            }
        } catch (IllegalStateException e) {
            sendStockConflict(response);
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) return;
        String productId = productId(request);
        if (!ValidationUtil.isSlug(productId) || productId.length() > 64) {
            sendNotFound(response);
            return;
        }
        try {
            if (CartDB.removeItem(user.getId(), productId) == 0) {
                sendNotFound(response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    private String productId(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (path == null || !path.startsWith("/items/")) return "";
        String id = path.substring("/items/".length());
        return id.contains("/") ? "" : id;
    }

    private void sendCart(HttpServletResponse response, List<CartItem> cart) throws IOException {
        StringBuilder items = new StringBuilder();
        for (CartItem item : cart) {
            if (items.length() > 0) items.append(',');
            items.append(item.toJson());
        }
        ResponseUtil.sendJson(response, HttpServletResponse.SC_OK, "{\"data\":[" + items + "]}");
    }

    private void sendInvalid(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                "VALIDATION_ERROR", "Số lượng phải là số nguyên từ 1 đến 10.000.");
    }

    private void sendProductUnavailable(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "NOT_FOUND", "Sản phẩm không tồn tại hoặc đã ngừng bán.");
    }

    private void sendStockConflict(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                "STOCK_UNAVAILABLE", "Số lượng yêu cầu vượt quá tồn kho hiện tại.");
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "NOT_FOUND", "Không tìm thấy sản phẩm trong giỏ.");
    }
}
