package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và quy tắc trình bày của DesignBomItem. */
public class DesignBomItem implements Serializable {
    private String componentId = "";
    private String name = "";
    private int needed;
    private int owned;
    private String productId;

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignBomItem() {
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

    /** Đọc dữ kiện needed. */
    public int getNeeded() {
        return needed;
    }

    /** Ghi dữ kiện needed. */
    public void setNeeded(int needed) {
        this.needed = needed;
    }

    /** Đọc dữ kiện owned. */
    public int getOwned() {
        return owned;
    }

    /** Ghi dữ kiện owned. */
    public void setOwned(int owned) {
        this.owned = owned;
    }

    /** Đọc dữ kiện productId. */
    public String getProductId() {
        return productId;
    }

    /** Ghi dữ kiện productId. */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /** Thiếu bao nhiêu để chuẩn bị; không trừ kho hoặc đặt hàng tự động. */
    public int getMissing() {
        return Math.max(needed - owned, 0);
    }

    /** Dùng đúng link sản phẩm đã có của cửa hàng. */
    public String getShopPath() {
        KitItem item = new KitItem();
        item.setProductId(productId);
        return item.getShopPath();
    }

}
