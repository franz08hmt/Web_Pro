package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.CartItem;
import vn.edu.webpro.robotlab.business.ShopProduct;
import vn.edu.webpro.robotlab.business.DesignCartResult;
import vn.edu.webpro.robotlab.business.DesignInstance;
import vn.edu.webpro.robotlab.business.KitItem;
import vn.edu.webpro.robotlab.business.RobotDesign;
import java.util.HashMap;

/** JDBC cho giỏ hàng; userId luôn đến từ session ở Servlet, không từ body. */
public class CartDB {
    private static final int MAX_QUANTITY = 10000;

    public static List<CartItem> selectCart(long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = "SELECT ci.quantity, p.id, p.component_id, c.name, c.category, c.image, "
                + "c.description, c.specs, p.price_vnd, p.stock_quantity, p.is_active "
                + "FROM cart_items ci JOIN shop_products p ON p.id = ci.product_id "
                + "JOIN components c ON c.id = p.component_id "
                + "WHERE ci.user_id = ? ORDER BY p.id";
        try {
            ps = connection.prepareStatement(query);
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            List<CartItem> items = new ArrayList<>();
            while (rs.next()) {
                CartItem item = new CartItem();
                item.setQuantity(rs.getInt("quantity"));
                item.setProduct(ShopProductDB.mapProduct(rs));
                items.add(item);
            }
            return items;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /**
     * Trả 1 nếu lưu, ném IllegalArgumentException nếu sản phẩm không bán,
     * IllegalStateException nếu lượng yêu cầu vượt tồn kho hiện tại.
     */
    public static int setQuantity(long userId, String productId, int quantity) throws SQLException {
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("invalid-quantity");
        }
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean originalAutoCommit = true;
        boolean autoCommitRead = false;
        try {
            originalAutoCommit = connection.getAutoCommit();
            autoCommitRead = true;
            connection.setAutoCommit(false);
            lockUser(connection, userId);
            int stock = selectActiveStockForUpdate(connection, productId);
            if (stock < 0) throw new IllegalArgumentException("product-unavailable");
            if (quantity > stock) throw new IllegalStateException("insufficient-stock");

            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)")) {
                ps.setLong(1, userId);
                ps.setString(2, productId);
                ps.setInt(3, quantity);
                ps.executeUpdate();
            }
            connection.commit();
            return 1;
        } catch (SQLException | RuntimeException e) {
            rollback(connection);
            throw e;
        } finally {
            if (autoCommitRead) restoreAutoCommit(connection, originalAutoCommit);
            pool.freeConnection(connection);
        }
    }

    public static int removeItem(long userId, String productId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean originalAutoCommit = true;
        boolean autoCommitRead = false;
        try {
            originalAutoCommit = connection.getAutoCommit();
            autoCommitRead = true;
            connection.setAutoCommit(false);
            lockUser(connection, userId);
            int changed;
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?")) {
                ps.setLong(1, userId);
                ps.setString(2, productId);
                changed = ps.executeUpdate();
            }
            connection.commit();
            return changed;
        } catch (SQLException | RuntimeException e) {
            rollback(connection);
            throw e;
        } finally {
            if (autoCommitRead) restoreAutoCommit(connection, originalAutoCommit);
            pool.freeConnection(connection);
        }
    }

    private static void lockUser(Connection connection, long userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM users WHERE id = ? FOR UPDATE")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("user-not-found");
            }
        }
    }

    /** Bổ sung từ snapshot thiết kế trong một transaction, dùng khóa/luật kho của giỏ hiện có. */
    public static DesignCartResult ensureMissing(long userId, long designId) throws SQLException {
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
            lockUser(connection, userId);
            RobotDesign design = DesignDB.read(connection, designId, userId, true);
            if (design == null) {
                connection.rollback();
                return null;
            }
            HashMap<String, Integer> needed = new HashMap<>();
            for (DesignInstance instance : DesignDB.readInstances(connection, designId, userId)) {
                Integer count = needed.get(instance.getComponentId());
                needed.put(instance.getComponentId(), count == null ? 1 : count + 1);
            }
            DesignCartResult result = new DesignCartResult();
            for (KitItem item : UserKitDB.readItems(connection, userId,
                    DesignRuleDB.read(connection, design.getRuleSetId()))) {
                Integer count = needed.get(item.getComponentId());
                if (count == null || count <= item.getQuantity()) {
                    continue;
                }
                if (item.getProductId() == null || item.getProductId().isEmpty()) {
                    result.getMessages().add(item.getName() + ": chưa có sản phẩm đang bán.");
                    continue;
                }
                int stock = selectActiveStockForUpdate(connection, item.getProductId());
                if (stock <= 0) {
                    result.getMessages().add(item.getName() + (stock < 0 ? ": ngừng bán." : ": hết hàng."));
                    continue;
                }
                ps = connection.prepareStatement("SELECT quantity FROM cart_items "
                        + "WHERE user_id = ? AND product_id = ? FOR UPDATE");
                ps.setLong(1, userId);
                ps.setString(2, item.getProductId());
                rs = ps.executeQuery();
                int current = rs.next() ? rs.getInt(1) : 0;
                DBUtil.closeResultSet(rs);
                DBUtil.closePreparedStatement(ps);
                int target = result.targetQuantity(count, item.getQuantity(), current, stock, MAX_QUANTITY);
                if (target > current) {
                    ps = connection.prepareStatement("INSERT INTO cart_items (user_id,product_id,quantity) "
                            + "VALUES (?,?,?) ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)");
                    ps.setLong(1, userId);
                    ps.setString(2, item.getProductId());
                    ps.setInt(3, target);
                    ps.executeUpdate();
                    DBUtil.closePreparedStatement(ps);
                    result.getMessages().add(item.getName() + ": thêm " + (target - current) + " vào giỏ.");
                } else {
                    result.getMessages().add(item.getName() + ": đã có trong giỏ, không cộng trùng.");
                }
                if (target < count - item.getQuantity() || current > stock) {
                    result.getMessages().add(item.getName() + ": tồn kho chưa đủ lượng cần; hãy kiểm tra lại giỏ.");
                }
            }
            if (result.getMessages().isEmpty()) {
                result.getMessages().add("Bộ linh kiện của bạn đã đủ cho thiết kế này.");
            }
            connection.commit();
            return result;
        } catch (SQLException | RuntimeException e) {
            if (started) {
                rollback(connection);
            }
            throw e;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            if (started) {
                restoreAutoCommit(connection, original);
            }
            pool.freeConnection(connection);
        }
    }

    private static int selectActiveStockForUpdate(Connection connection, String productId)
            throws SQLException {
        String query = "SELECT stock_quantity FROM shop_products "
                + "WHERE id = ? AND is_active = TRUE FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("stock_quantity") : -1;
            }
        }
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve the original failure; the pool connection is still returned in finally.
        }
    }

    private static void restoreAutoCommit(Connection connection, boolean originalAutoCommit) {
        try {
            connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException ignored) {
            // A broken connection will be handled by the pool's normal validation path.
        }
    }
}
