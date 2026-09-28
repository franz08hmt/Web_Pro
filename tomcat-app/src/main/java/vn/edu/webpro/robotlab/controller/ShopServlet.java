package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.ShopProduct;
import vn.edu.webpro.robotlab.data.ShopProductDB;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/** Catalog bán hàng công khai, chỉ đọc các sản phẩm đang hoạt động. */
@WebServlet("/api/shop/products")
public class ShopServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int page = ValidationUtil.parsePositiveInt(
                    request.getParameter("page"), 1, ValidationUtil.MAX_PAGE);
            int limit = ValidationUtil.parsePositiveInt(
                    request.getParameter("limit"), 20, ValidationUtil.MAX_LIMIT);
            List<ShopProduct> products = ShopProductDB.selectProducts(limit, (page - 1) * limit, true);
            StringBuilder items = new StringBuilder();
            for (ShopProduct product : products) {
                if (items.length() > 0) items.append(',');
                items.append(product.toJson());
            }
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + items + "],\"meta\":{\"page\":" + page
                            + ",\"limit\":" + limit + ",\"total\":"
                            + ShopProductDB.countProducts(true) + "}}");
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Phân trang không hợp lệ.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }
}
