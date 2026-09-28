package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.CartItem;
import vn.edu.webpro.robotlab.business.ShopProduct;

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
