package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.ShopProduct;

/** Đọc/ghi sản phẩm bán và JOIN metadata kỹ thuật từ components. */
public class ShopProductDB {
    private static final String SELECT_FIELDS = "p.id, p.component_id, c.name, c.category, "
            + "c.image, c.description, c.specs, p.price_vnd, p.stock_quantity, p.is_active";
    private static final String FROM_COMPONENT = " FROM shop_products p "
            + "JOIN components c ON c.id = p.component_id ";

    public static List<ShopProduct> selectProducts(int limit, int offset, boolean activeOnly)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = "SELECT " + SELECT_FIELDS + FROM_COMPONENT
                + (activeOnly ? "WHERE p.is_active = TRUE " : "")
                + "ORDER BY p.id LIMIT ? OFFSET ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            rs = ps.executeQuery();
            List<ShopProduct> products = new ArrayList<>();
            while (rs.next()) products.add(mapProduct(rs));
            return products;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static long countProducts(boolean activeOnly) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = "SELECT COUNT(*) AS total FROM shop_products"
                + (activeOnly ? " WHERE is_active = TRUE" : "");
        try {
            ps = connection.prepareStatement(query);
            rs = ps.executeQuery();
            rs.next();
            return rs.getLong("total");
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static ShopProduct selectProduct(String id) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT " + SELECT_FIELDS + FROM_COMPONENT + "WHERE p.id = ?");
            ps.setString(1, id);
            rs = ps.executeQuery();
            return rs.next() ? mapProduct(rs) : null;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static int insert(ShopProduct product) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        String query = "INSERT INTO shop_products (id, component_id, price_vnd, stock_quantity, is_active) "
                + "VALUES (?, ?, ?, ?, ?)";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, product.getId());
            ps.setString(2, product.getComponentId());
            ps.setBigDecimal(3, product.getPriceVnd());
            ps.setInt(4, product.getStockQuantity());
            ps.setBoolean(5, product.isActive());
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static int update(ShopProduct product) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        String query = "UPDATE shop_products SET component_id = ?, price_vnd = ?, "
                + "stock_quantity = ?, is_active = ? WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, product.getComponentId());
            ps.setBigDecimal(2, product.getPriceVnd());
            ps.setInt(3, product.getStockQuantity());
            ps.setBoolean(4, product.isActive());
            ps.setString(5, product.getId());
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** DELETE quản trị là ngừng bán, giữ nguyên khóa và lịch sử đặt hàng. */
    public static int deactivate(String id) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("UPDATE shop_products SET is_active = FALSE WHERE id = ?");
            ps.setString(1, id);
            int changed = ps.executeUpdate();
            if (changed > 0) return changed;
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("SELECT id FROM shop_products WHERE id = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            try {
                return rs.next() ? 1 : 0;
            } finally {
                DBUtil.closeResultSet(rs);
            }
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Dùng chung mapper cho truy vấn catalog, cart và checkout. */
    static ShopProduct mapProduct(ResultSet rs) throws SQLException {
        ShopProduct product = new ShopProduct();
        product.setId(rs.getString("id"));
        product.setComponentId(rs.getString("component_id"));
        product.setName(rs.getString("name"));
        product.setCategory(rs.getString("category"));
        product.setImage(rs.getString("image"));
        product.setDescription(rs.getString("description"));
        product.setSpecsJson(rs.getString("specs"));
        product.setPriceVnd(rs.getBigDecimal("price_vnd"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setActive(rs.getBoolean("is_active"));
        return product;
    }
}
