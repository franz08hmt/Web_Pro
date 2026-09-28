package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.ShopProduct;
import vn.edu.webpro.robotlab.data.ShopProductDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/** Quản trị catalog bán hàng; DELETE ngừng bán thay vì xóa lịch sử sản phẩm. */
@WebServlet("/api/admin/shop/products/*")
public class AdminShopServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int LIST_LIMIT = 100;
    private static final int MAX_BODY = 16384;
    private static final long MAX_PRICE_VND = 999_999_999_999L;
    private static final int MAX_STOCK = 1_000_000;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("PATCH".equals(request.getMethod())) {
            save(request, response, false);
            return;
        }
        super.service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;
        try {
            int page = ValidationUtil.parsePositiveInt(
                    request.getParameter("page"), 1, ValidationUtil.MAX_PAGE);
            int limit = ValidationUtil.parsePositiveInt(
                    request.getParameter("limit"), LIST_LIMIT, LIST_LIMIT);
            List<ShopProduct> products = ShopProductDB.selectProducts(limit, (page - 1) * limit, false);
            sendProducts(response, products, page, limit, ShopProductDB.countProducts(false));
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Phân trang không hợp lệ.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        save(request, response, true);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;
        if (!SessionUtil.hasValidCsrfToken(request, response)) return;
        String id = pathId(request);
        if (!isValidId(id)) {
            sendInvalid(response);
            return;
        }
        try {
            if (ShopProductDB.deactivate(id) == 0) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                        "NOT_FOUND", "Không tìm thấy sản phẩm.");
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    private void save(HttpServletRequest request, HttpServletResponse response, boolean creating)
            throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;
        if (!SessionUtil.hasValidCsrfToken(request, response)) return;
        try {
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            ShopProduct product = new ShopProduct();
            product.setId(creating ? JsonUtil.stringField(body, "id").trim() : pathId(request));
            product.setComponentId(JsonUtil.stringField(body, "componentId").trim());
            long priceVnd = JsonUtil.longField(body, "priceVnd");
            int stockQuantity = JsonUtil.intField(body, "stockQuantity");
            product.setPriceVnd(BigDecimal.valueOf(priceVnd));
            product.setStockQuantity(stockQuantity);
            product.setActive(JsonUtil.booleanField(body, "active"));

            if (!isValidId(product.getId()) || !isValidId(product.getComponentId())
                    || priceVnd < 1 || priceVnd > MAX_PRICE_VND
                    || stockQuantity < 0 || stockQuantity > MAX_STOCK) {
                sendInvalid(response);
                return;
            }

            int changed = creating ? ShopProductDB.insert(product) : ShopProductDB.update(product);
            if (changed == 0) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                        "NOT_FOUND", "Không tìm thấy sản phẩm.");
                return;
            }
            ShopProduct saved = ShopProductDB.selectProduct(product.getId());
            ResponseUtil.sendJson(response,
                    creating ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK,
                    "{\"data\":" + saved.toJson() + "}");
        } catch (IllegalArgumentException e) {
            sendInvalid(response);
        } catch (SQLException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "CONFLICT", "Không thể lưu sản phẩm; hãy kiểm tra mã linh kiện đã được bán chưa.");
        }
    }

    private void sendProducts(HttpServletResponse response, List<ShopProduct> products,
                              int page, int limit, long total) throws IOException {
        StringBuilder items = new StringBuilder();
        for (ShopProduct product : products) {
            if (items.length() > 0) items.append(',');
            items.append(product.toJson());
        }
        ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                "{\"data\":[" + items + "],\"meta\":{\"page\":" + page
                        + ",\"limit\":" + limit + ",\"total\":" + total + "}}");
    }

    private String pathId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || !path.startsWith("/") ? "" : path.substring(1);
    }

    private boolean isValidId(String id) {
        return ValidationUtil.isSlug(id) && id.length() <= 64;
    }

    private void sendInvalid(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                "VALIDATION_ERROR", "Mã, giá hoặc số lượng tồn kho không hợp lệ.");
    }
}
