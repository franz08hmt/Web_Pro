package vn.edu.webpro.robotlab.data;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.OrderItem;
import vn.edu.webpro.robotlab.business.ShopOrder;
import vn.edu.webpro.robotlab.business.ShopProduct;

/** Lịch sử và checkout mô phỏng; mọi giao dịch kho/đơn/giỏ là nguyên tử. */
public class OrderDB {
    private static final BigDecimal MAX_ORDER_TOTAL = new BigDecimal("99999999999999");

    /**
     * Khóa user để tuần tự hóa các thao tác giỏ cùng tài khoản, khóa các dòng giỏ,
     * rồi khóa từng sản phẩm theo product_id tăng dần trước khi kiểm tra/trừ kho.
     */
    public static ShopOrder checkout(long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean originalAutoCommit = true;
        boolean autoCommitRead = false;
        try {
            originalAutoCommit = connection.getAutoCommit();
            autoCommitRead = true;
            connection.setAutoCommit(false);
            lockUser(connection, userId);
            List<CartLine> cart = selectCartForUpdate(connection, userId);
            if (cart.isEmpty()) throw new IllegalStateException("cart-empty");

            ShopOrder order = new ShopOrder();
            order.setUserId(userId);
            BigDecimal total = BigDecimal.ZERO;
            for (CartLine line : cart) {
                ShopProduct product = selectProductForUpdate(connection, line.productId);
                if (product == null || !product.isActive()) {
                    throw new IllegalStateException("product-unavailable");
                }
                if (!product.isAvailableFor(line.quantity)) {
                    throw new IllegalStateException("insufficient-stock");
                }

                OrderItem item = new OrderItem();
                item.setProductId(product.getId());
                item.setProductNameSnapshot(product.getName());
                item.setUnitPriceVnd(product.getPriceVnd());
                item.setQuantity(line.quantity);
                order.getItems().add(item);
                total = total.add(item.getLineTotalVnd());
            }
            if (total.signum() < 0 || total.compareTo(MAX_ORDER_TOTAL) > 0) {
                throw new IllegalStateException("order-total-out-of-range");
            }
            order.setTotalVnd(total);

            insertOrder(connection, order);
            decrementStock(connection, order.getItems());
            insertOrderItems(connection, order);
            deleteCart(connection, userId);
            order.setCreatedAt(selectCreatedAt(connection, order.getId(), userId));

            connection.commit();
            return order;
        } catch (SQLException | RuntimeException e) {
            rollback(connection);
            throw e;
        } finally {
            if (autoCommitRead) restoreAutoCommit(connection, originalAutoCommit);
            pool.freeConnection(connection);
        }
    }

    /** Danh sách đơn chỉ của user đăng nhập; mỗi trang tải tối đa 20 đơn. */
    public static List<ShopOrder> selectOrders(long userId, int limit, int offset) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ShopOrder> orders = new ArrayList<>();
        String query = "SELECT id, user_id, status, total_vnd, created_at FROM orders "
                + "WHERE user_id = ? ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
        try {
            try {
                ps = connection.prepareStatement(query);
                ps.setLong(1, userId);
                ps.setInt(2, limit);
                ps.setInt(3, offset);
                rs = ps.executeQuery();
                while (rs.next()) orders.add(mapOrder(rs));
            } finally {
                DBUtil.closeResultSet(rs);
                DBUtil.closePreparedStatement(ps);
            }
            for (ShopOrder order : orders) {
                order.setItems(selectOrderItems(connection, order.getId()));
            }
            return orders;
        } finally {
            pool.freeConnection(connection);
        }
    }

    public static long countOrders(long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT COUNT(*) AS total FROM orders WHERE user_id = ?");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            rs.next();
            return rs.getLong("total");
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
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

    private static List<CartLine> selectCartForUpdate(Connection connection, long userId) throws SQLException {
        String query = "SELECT product_id, quantity FROM cart_items "
                + "WHERE user_id = ? ORDER BY product_id FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<CartLine> cart = new ArrayList<>();
                while (rs.next()) cart.add(new CartLine(rs.getString("product_id"), rs.getInt("quantity")));
                return cart;
            }
        }
    }

    private static ShopProduct selectProductForUpdate(Connection connection, String productId)
            throws SQLException {
        String query = "SELECT p.id, p.component_id, c.name, c.category, c.image, c.description, "
                + "c.specs, p.price_vnd, p.stock_quantity, p.is_active "
                + "FROM shop_products p JOIN components c ON c.id = p.component_id "
                + "WHERE p.id = ? FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? ShopProductDB.mapProduct(rs) : null;
            }
        }
    }

    private static void insertOrder(Connection connection, ShopOrder order) throws SQLException {
        String query = "INSERT INTO orders (user_id, status, total_vnd) VALUES (?, 'CONFIRMED', ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, order.getUserId());
            ps.setBigDecimal(2, order.getTotalVnd());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Không nhận được mã đơn hàng.");
                order.setId(keys.getLong(1));
            }
        }
    }

    private static void decrementStock(Connection connection, List<OrderItem> items) throws SQLException {
        String query = "UPDATE shop_products SET stock_quantity = stock_quantity - ? "
                + "WHERE id = ? AND stock_quantity >= ? AND is_active = TRUE";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            for (OrderItem item : items) {
                ps.setInt(1, item.getQuantity());
                ps.setString(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                if (ps.executeUpdate() != 1) throw new IllegalStateException("insufficient-stock");
            }
        }
    }

    private static void insertOrderItems(Connection connection, ShopOrder order) throws SQLException {
        String query = "INSERT INTO order_items "
                + "(order_id, product_id, product_name_snapshot, unit_price_vnd, quantity) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            for (OrderItem item : order.getItems()) {
                ps.setLong(1, order.getId());
                ps.setString(2, item.getProductId());
                ps.setString(3, item.getProductNameSnapshot());
                ps.setBigDecimal(4, item.getUnitPriceVnd());
                ps.setInt(5, item.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void deleteCart(Connection connection, long userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM cart_items WHERE user_id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    private static String selectCreatedAt(Connection connection, long orderId, long userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT created_at FROM orders WHERE id = ? AND user_id = ?")) {
            ps.setLong(1, orderId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không đọc lại được đơn vừa tạo.");
                Timestamp createdAt = rs.getTimestamp("created_at");
                return createdAt == null ? "" : createdAt.toString();
            }
        }
    }

    private static List<OrderItem> selectOrderItems(Connection connection, long orderId) throws SQLException {
        String query = "SELECT product_id, product_name_snapshot, unit_price_vnd, quantity "
                + "FROM order_items WHERE order_id = ? ORDER BY product_id";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setProductId(rs.getString("product_id"));
                    item.setProductNameSnapshot(rs.getString("product_name_snapshot"));
                    item.setUnitPriceVnd(rs.getBigDecimal("unit_price_vnd"));
                    item.setQuantity(rs.getInt("quantity"));
                    items.add(item);
                }
                return items;
            }
        }
    }

    private static ShopOrder mapOrder(ResultSet rs) throws SQLException {
        ShopOrder order = new ShopOrder();
        order.setId(rs.getLong("id"));
        order.setUserId(rs.getLong("user_id"));
        order.setStatus(rs.getString("status"));
        order.setTotalVnd(rs.getBigDecimal("total_vnd"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        order.setCreatedAt(createdAt == null ? "" : createdAt.toString());
        return order;
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Keep the original exception; do not hide it with rollback failure.
        }
    }

    private static void restoreAutoCommit(Connection connection, boolean originalAutoCommit) {
        try {
            connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException ignored) {
            // Connection-pool validation handles connections that cannot be restored.
        }
    }

    private static final class CartLine {
        private final String productId;
        private final int quantity;

        private CartLine(String productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}
