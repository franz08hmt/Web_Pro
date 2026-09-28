package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Dòng đơn hàng: tên và giá là snapshot, không phụ thuộc sản phẩm hiện tại. */
public class OrderItem implements Serializable {
    private String productId;
    private String productNameSnapshot;
    private BigDecimal unitPriceVnd;
    private int quantity;

    public OrderItem() {
        productId = "";
        productNameSnapshot = "";
        unitPriceVnd = BigDecimal.ZERO;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }
    public BigDecimal getUnitPriceVnd() { return unitPriceVnd; }
    public void setUnitPriceVnd(BigDecimal unitPriceVnd) { this.unitPriceVnd = unitPriceVnd; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getLineTotalVnd() {
        if (unitPriceVnd == null) return BigDecimal.ZERO;
        return unitPriceVnd.multiply(BigDecimal.valueOf(quantity));
    }

    public String toJson() {
        String price = unitPriceVnd == null ? "0" : unitPriceVnd.toPlainString();
        return "{\"productId\":" + JsonUtil.quote(productId)
                + ",\"productName\":" + JsonUtil.quote(productNameSnapshot)
                + ",\"unitPriceVnd\":" + price
                + ",\"quantity\":" + quantity
                + ",\"lineTotalVnd\":" + getLineTotalVnd().toPlainString() + "}";
    }
}
