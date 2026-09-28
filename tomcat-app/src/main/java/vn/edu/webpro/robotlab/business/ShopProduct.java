package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Sản phẩm bán mô phỏng, bổ sung giá/tồn kho mà không sửa Component kỹ thuật. */
public class ShopProduct implements Serializable {
    private String id;
    private String componentId;
    private String name;
    private String category;
    private String image;
    private String description;
    private String specsJson;
    private BigDecimal priceVnd;
    private int stockQuantity;
    private boolean active;

    public ShopProduct() {
        id = "";
        componentId = "";
        name = "";
        category = "";
        image = "";
        description = "";
        specsJson = "{}";
        priceVnd = BigDecimal.ZERO;
    }

    public boolean isAvailableFor(int quantity) {
        return active && quantity > 0 && quantity <= stockQuantity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getComponentId() { return componentId; }
    public void setComponentId(String componentId) { this.componentId = componentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSpecsJson() { return specsJson; }
    public void setSpecsJson(String specsJson) { this.specsJson = specsJson; }
    public BigDecimal getPriceVnd() { return priceVnd; }
    public void setPriceVnd(BigDecimal priceVnd) { this.priceVnd = priceVnd; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String toJson() {
        String price = priceVnd == null ? "0" : priceVnd.toPlainString();
        return "{\"id\":" + JsonUtil.quote(id)
                + ",\"componentId\":" + JsonUtil.quote(componentId)
                + ",\"name\":" + JsonUtil.quote(name)
                + ",\"category\":" + JsonUtil.quote(category)
                + ",\"image\":" + JsonUtil.quote(image)
                + ",\"description\":" + JsonUtil.quote(description)
                + ",\"specs\":" + JsonUtil.objectOrEmpty(specsJson)
                + ",\"priceVnd\":" + price
                + ",\"stockQuantity\":" + stockQuantity
                + ",\"active\":" + active + "}";
    }
}
