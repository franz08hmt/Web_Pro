package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import vn.edu.webpro.robotlab.business.ComponentContext;
import vn.edu.webpro.robotlab.business.RobotLearningLink;

/** Đọc catalog liên kết bằng một truy vấn; số kit luôn lọc theo user_id. */
public class LearningLinkDB {
    /** Khách truyền userId=0 từ Servlet, không nhận userId do trình duyệt tự khai. */
    public static List<ComponentContext> selectLinks(long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT c.id,COALESCE(k.quantity,0) AS owned,p.id AS product_id,"
                    + "r.id AS robot_id,r.name AS robot_name,"
                    + "EXISTS(SELECT 1 FROM wiring_exercises w WHERE w.robot_id=r.id AND w.state='PUBLISHED') "
                    + "AS has_wiring,EXISTS(SELECT 1 FROM troubleshooting_guides t WHERE t.robot_id=r.id) AS has_guide "
                    + "FROM components c LEFT JOIN user_kit_items k ON k.component_id=c.id AND k.user_id = ? "
                    + "LEFT JOIN shop_products p ON p.component_id=c.id AND p.is_active=TRUE "
                    + "LEFT JOIN robot_components rc ON rc.component_id=c.id LEFT JOIN robots r ON r.id=rc.robot_id "
                    + "ORDER BY c.id,r.id");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            List<ComponentContext> items = new ArrayList<>();
            HashMap<String, ComponentContext> byId = new HashMap<>();
            while (rs.next()) {
                String componentId = rs.getString("id");
                ComponentContext item = byId.get(componentId);
                if (item == null) {
                    item = new ComponentContext();
                    item.setComponentId(componentId);
                    item.setOwned(rs.getInt("owned"));
                    item.setProductId(rs.getString("product_id"));
                    byId.put(componentId, item);
                    items.add(item);
                }
                if (rs.getString("robot_id") != null) {
                    RobotLearningLink robot = new RobotLearningLink();
                    robot.setId(rs.getString("robot_id"));
                    robot.setName(rs.getString("robot_name"));
                    robot.setHasWiring(rs.getBoolean("has_wiring"));
                    robot.setHasGuide(rs.getBoolean("has_guide"));
                    item.getRobots().add(robot);
                }
            }
            return items;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }
}
