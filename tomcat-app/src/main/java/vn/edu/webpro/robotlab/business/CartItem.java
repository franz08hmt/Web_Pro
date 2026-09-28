package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Một dòng giỏ hàng của đúng một tài khoản. */
public class CartItem implements Serializable {
    private ShopProduct product;
    private int quantity;

    public CartItem() {
        product = new ShopProduct();
    }

    public ShopProduct getProduct() { return product; }
    public void setProduct(ShopProduct product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getLineTotalVnd() {
        if (product == null || product.getPriceVnd() == null) return BigDecimal.ZERO;
        return product.getPriceVnd().multiply(BigDecimal.valueOf(quantity));
    }

    public String toJson() {
        return "{\"product\":" + (product == null ? "null" : product.toJson())
                + ",\"quantity\":" + quantity
                + ",\"lineTotalVnd\":" + getLineTotalVnd().toPlainString() + "}";
    }
}
