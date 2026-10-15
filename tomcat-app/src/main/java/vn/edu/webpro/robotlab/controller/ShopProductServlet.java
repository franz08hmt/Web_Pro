package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.ShopProduct;
import vn.edu.webpro.robotlab.data.ShopProductDB;
import vn.edu.webpro.robotlab.util.ResponseUtil;

/** Đọc một sản phẩm công khai theo SKU, không phụ thuộc trang của catalog. */
@WebServlet("/api/shop/products/*")
public class ShopProductServlet extends HttpServlet {
    /** Sản phẩm ngừng bán và không tồn tại cùng trả 404, không lộ dữ liệu quản trị. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        String id = path == null ? "" : path.substring(1);
        if (id.isEmpty() || id.length() > 80 || id.contains("/")) {
            notFound(response);
            return;
        }
        try {
            ShopProduct product = ShopProductDB.selectProduct(id);
            if (product == null || !product.isActive()) {
                notFound(response);
                return;
            }
            ResponseUtil.sendJson(response, 200, "{\"data\":" + product.toJson() + "}");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    private void notFound(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, 404, "NOT_FOUND", "Sản phẩm không tồn tại hoặc đã ngừng bán.");
    }
}
