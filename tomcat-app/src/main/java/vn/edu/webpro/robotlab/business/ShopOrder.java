package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Đơn mô phỏng do server lập; tổng tiền bằng tổng các dòng snapshot. */
public class ShopOrder implements Serializable {
    public static final String CONFIRMED = "CONFIRMED";
    public static final String CANCELLED = "CANCELLED";

    private long id;
    private long userId;
    private String status;
    private BigDecimal totalVnd;
    private String createdAt;
    private List<OrderItem> items;

    public ShopOrder() {
        status = CONFIRMED;
        totalVnd = BigDecimal.ZERO;
        createdAt = "";
        items = new ArrayList<>();
    }

    public BigDecimal calculateTotal() {
        return items.stream().map(OrderItem::getLineTotalVnd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getStatusLabel() {
        return CONFIRMED.equals(status) ? "Đã ghi nhận (mô phỏng)" : "Đã hủy";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalVnd() { return totalVnd; }
    public void setTotalVnd(BigDecimal totalVnd) { this.totalVnd = totalVnd; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String toJson() {
        StringBuilder itemJson = new StringBuilder();
        for (OrderItem item : items) {
            if (itemJson.length() > 0) itemJson.append(',');
            itemJson.append(item.toJson());
        }
        return "{\"id\":" + JsonUtil.quote(Long.toString(id))
                + ",\"status\":" + JsonUtil.quote(status)
                + ",\"statusLabel\":" + JsonUtil.quote(getStatusLabel())
                + ",\"totalVnd\":" + totalVnd.toPlainString()
                + ",\"createdAt\":" + JsonUtil.quote(createdAt)
                + ",\"items\":[" + itemJson + "]}";
    }
}
