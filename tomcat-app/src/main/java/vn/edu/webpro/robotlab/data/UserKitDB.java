package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import vn.edu.webpro.robotlab.business.DesignCatalog;
import vn.edu.webpro.robotlab.business.DesignRuleSet;
import vn.edu.webpro.robotlab.business.KitItem;

/** Kit của owner; nhập đơn hàng không đổi đơn, sản phẩm hoặc các phiên lắp ráp. */
public class UserKitDB {
    /** Khóa owner dùng chung với tạo thiết kế để giới hạn/snapshot nhất quán. */
    public static void lockOwner(Connection connection, long userId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT id FROM users WHERE id = ? FOR UPDATE");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new IllegalArgumentException("Không tìm thấy tài khoản bạn được phép dùng.");
            }
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Một JOIN lấy catalog/kit/link shop, không N+1. */
    public static List<KitItem> selectItems(long userId, DesignRuleSet rules) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        try {
            return readItems(connection, userId, rules);
        } finally {
            pool.freeConnection(connection);
        }
    }

    /** Đọc snapshot kit bằng kết nối transaction đang khóa owner. */
    public static List<KitItem> readItems(Connection connection, long userId, DesignRuleSet rules)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<KitItem> items = new ArrayList<>();
        try {
            ps = connection.prepareStatement("SELECT c.id,c.name,c.category,c.image,k.quantity,p.id AS product_id "
                    + "FROM components c LEFT JOIN user_kit_items k ON k.component_id = c.id AND k.user_id = ? "
                    + "LEFT JOIN shop_products p ON p.component_id = c.id AND p.is_active = TRUE ORDER BY c.id");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            DesignCatalog catalog = new DesignCatalog();
            while (rs.next()) {
                if (!catalog.isRenderable(rs.getString("id"))) {
                    continue;
                }
                KitItem item = new KitItem();
                item.setComponentId(rs.getString("id"));
                item.setName(rs.getString("name"));
                item.setCategory(rs.getString("category"));
                item.setImage(rs.getString("image"));
                item.setQuantity(rs.getInt("quantity"));
                item.setInputValue(Integer.toString(item.getQuantity()));
                item.setProductId(rs.getString("product_id"));
                item.setSupported(rules.roleFor(item.getComponentId()) != null);
                items.add(item);
            }
            return items;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Update kit hoặc import; validate toàn bộ trước khi ghi, cùng transaction owner. */
    public static void change(long userId, HashMap<String, Integer> quantities, boolean importOrders)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean original = true;
        boolean started = false;
        try {
            original = connection.getAutoCommit();
            connection.setAutoCommit(false);
            started = true;
            lockOwner(connection, userId);
            if (importOrders) {
                quantities = new HashMap<>();
                ps = connection.prepareStatement("SELECT p.component_id,SUM(i.quantity) AS quantity "
                        + "FROM order_items i JOIN orders o ON o.id = i.order_id "
                        + "JOIN shop_products p ON p.id = i.product_id "
                        + "WHERE o.user_id = ? AND o.status = 'CONFIRMED' GROUP BY p.component_id");
                ps.setLong(1, userId);
                rs = ps.executeQuery();
                DesignCatalog catalog = new DesignCatalog();
                KitItem bounds = new KitItem();
                while (rs.next()) {
                    if (catalog.isRenderable(rs.getString(1))) {
                        quantities.put(rs.getString(1), bounds.capPurchased(rs.getLong(2)));
                    }
                }
                DBUtil.closeResultSet(rs);
                DBUtil.closePreparedStatement(ps);
                rs = null;
                ps = null;
            }
            DesignCatalog catalog = new DesignCatalog();
            KitItem bounds = new KitItem();
            for (String code : quantities.keySet()) {
                Integer value = quantities.get(code);
                if (!catalog.isRenderable(code) || value == null || !bounds.validateQuantity(value).isEmpty()) {
                    throw new IllegalArgumentException("Số lượng linh kiện phải là số nguyên từ 0 đến 10.");
                }
            }
            for (String code : quantities.keySet()) {
                int value = quantities.get(code);
                if (value == 0) {
                    ps = connection.prepareStatement("DELETE FROM user_kit_items "
                            + "WHERE user_id = ? AND component_id = ?");
                    ps.setLong(1, userId);
                    ps.setString(2, code);
                } else {
                    String query = "INSERT INTO user_kit_items (user_id,component_id,quantity) VALUES (?,?,?) "
                            + "ON DUPLICATE KEY UPDATE quantity = "
                            + (importOrders ? "GREATEST(quantity, ?)" : "?") + ", updated_at = CURRENT_TIMESTAMP";
                    ps = connection.prepareStatement(query);
                    ps.setLong(1, userId);
                    ps.setString(2, code);
                    ps.setInt(3, value);
                    ps.setInt(4, value);
                }
                ps.executeUpdate();
                DBUtil.closePreparedStatement(ps);
                ps = null;
            }
            connection.commit();
        } catch (SQLException | IllegalArgumentException e) {
            if (started) {
                try {
                    connection.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }
            }
            throw e;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            try {
                if (started) {
                    connection.setAutoCommit(original);
                }
            } finally {
                pool.freeConnection(connection);
            }
        }
    }
}
