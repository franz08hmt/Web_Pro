package vn.edu.webpro.robotlab.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

/** Mỗi nhánh checkout giữ cùng mã, thông báo và HTTP trước/sau thay switch. */
class CheckoutConflictContractTest {
    @Test
    void checkoutReasonsPreserveTheirJsonContractIncludingNull() throws Exception {
        String[] reasons = {"cart-empty", "product-unavailable", "order-total-out-of-range", "stock", "", null};
        String[] codes = {"CART_EMPTY", "PRODUCT_UNAVAILABLE", "ORDER_TOTAL_LIMIT",
            "STOCK_UNAVAILABLE", "STOCK_UNAVAILABLE", "STOCK_UNAVAILABLE"};
        String[] messages = {"Giỏ hàng đang trống.", "Một sản phẩm trong giỏ đã ngừng bán.",
            "Tổng đơn vượt giới hạn dữ liệu.", "Tồn kho vừa thay đổi; hãy kiểm tra lại giỏ hàng."};
        Method method = OrderServlet.class.getDeclaredMethod("sendCheckoutConflict",
                HttpServletResponse.class, String.class);
        method.setAccessible(true);
        for (int index = 0; index < reasons.length; index++) {
            ResponseCapture captured = new ResponseCapture();
            method.invoke(new OrderServlet(), captured.response(), reasons[index]);
            assertEquals(409, captured.status());
            assertTrue(captured.body().contains("\"code\":\"" + codes[index] + "\""));
            assertTrue(captured.body().contains(messages[index < 3 ? index : 3]));
        }
    }
}
