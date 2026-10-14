package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và quy tắc trình bày của KitItem. */
public class KitItem implements Serializable {
    private String componentId = "";
    private String name = "";
    private String category = "";
    private String image = "";
    private String productId = "";
    private int quantity;
    private boolean supported;
    private String inputValue = "0";

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public KitItem() {
    }

    /** Đọc dữ kiện componentId. */
    public String getComponentId() {
        return componentId;
    }

    /** Ghi dữ kiện componentId. */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /** Đọc dữ kiện name. */
    public String getName() {
        return name;
    }

    /** Ghi dữ kiện name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Đọc dữ kiện category. */
    public String getCategory() {
        return category;
    }

    /** Ghi dữ kiện category. */
    public void setCategory(String category) {
        this.category = category;
    }

    /** Đọc dữ kiện image. */
    public String getImage() {
        return image;
    }

    /** Ghi dữ kiện image. */
    public void setImage(String image) {
        this.image = image;
    }

    /** Đọc dữ kiện productId. */
    public String getProductId() {
        return productId;
    }

    /** Ghi dữ kiện productId. */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /** Đọc dữ kiện quantity. */
    public int getQuantity() {
        return quantity;
    }

    /** Ghi dữ kiện quantity. */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /** Đọc dữ kiện supported. */
    public boolean isSupported() {
        return supported;
    }

    /** Ghi dữ kiện supported. */
    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    /** Đọc dữ kiện inputValue. */
    public String getInputValue() {
        return inputValue;
    }

    /** Ghi dữ kiện inputValue. */
    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }

    /** Quy tắc số lượng dùng ở form và transaction, số không nghĩa là xóa khỏi kit. */
    public String validateQuantity(int value) {
        return value < 0 || value > 10 ? "Số lượng của " + name + " phải từ 0 đến 10." : "";
    }

    /** Bản đầu chỉ giữ tối đa mười vật thể cùng loại, không tính tồn kho vật lý. */
    public int capPurchased(long value) {
        return (int) Math.min(10, Math.max(0, value));
    }

    /** Nhãn trạng thái không lộ role/mã nội bộ. */
    public String getSupportLabel() {
        return supported ? "Hỗ trợ kiểm chứng" : "Chưa hỗ trợ kiểm chứng";
    }

    /** Chỉ ảnh cục bộ, escape riêng ở JSP. */
    public String getImagePath() {
        String value = image;
        if (value == null) {
            return "";
        }
        if (value.startsWith("/assets/")) {
            value = value.substring(8);
        } else if (value.startsWith("assets/")) {
            value = value.substring(7);
        }
        return value.matches("images/[A-Za-z0-9_./-]+") && !value.contains("..") ? value : "";
    }

    /** Anchor sản phẩm có thật, không ghép URL ngoài từ dữ liệu. */
    public String getShopPath() {
        return productId != null && productId.matches("[A-Za-z0-9_-]{1,64}")
                ? "/pages/cua-hang.html#shop-product-" + productId : "";
    }

}
