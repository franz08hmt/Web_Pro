package vn.edu.webpro.robotlab.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import vn.edu.webpro.robotlab.business.KitItem;

/** Đọc form URL encoded; luật số lượng thuộc JavaBean. */
public class DesignFormUtil {
    /** ID phải là số dương, không nhận owner từ client. */
    public static long id(String value) {
        try {
            if (value != null && value.matches("[1-9][0-9]{0,18}")) {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }

    /** getParameterValues cho danh sách linh kiện; lỗi không ghi một phần. */
    public static HashMap<String, Integer> quantities(HttpServletRequest request, List<KitItem> items) {
        HashMap<String, Integer> values = new HashMap<>();
        HashSet<String> seen = new HashSet<>();
        for (KitItem item : items) {
            String input = request.getParameter("quantity_" + item.getComponentId());
            if (input != null) {
                item.setInputValue(input);
            }
        }
        String[] codes = request.getParameterValues("componentId");
        if (codes == null || codes.length == 0 || codes.length > 13) {
            throw new IllegalArgumentException("Hãy chọn số lượng cho bộ linh kiện.");
        }
        for (String code : codes) {
            KitItem found = null;
            for (KitItem item : items) {
                if (item.getComponentId().equals(code)) {
                    found = item;
                }
            }
            if (found == null || !seen.add(code)) {
                throw new IllegalArgumentException("Danh sách linh kiện không hợp lệ. Hãy tải lại form.");
            }
            String input = request.getParameter("quantity_" + code);
            if (input == null || !input.matches("[0-9]{1,2}")) {
                throw new IllegalArgumentException("Nhập số lượng nguyên từ 0 đến 10 cho " + found.getName() + ".");
            }
            int value = Integer.parseInt(input);
            String error = found.validateQuantity(value);
            if (!error.isEmpty()) {
                throw new IllegalArgumentException(error);
            }
            values.put(code, value);
        }
        return values;
    }
}
