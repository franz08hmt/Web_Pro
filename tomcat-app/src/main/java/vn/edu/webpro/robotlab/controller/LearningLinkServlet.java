package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.ComponentContext;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.LearningLinkDB;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;

/** API GET chỉ đọc cho các liên kết catalog, kit và bài học liên quan. */
@WebServlet("/api/learning-links")
public class LearningLinkServlet extends HttpServlet {
    /** Danh tính lấy từ session; không nhận user_id trên URL. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            User user = SessionUtil.getCurrentUser(request);
            StringBuilder json = new StringBuilder("{\"data\":[");
            for (ComponentContext item : LearningLinkDB.selectLinks(user == null ? 0 : user.getId())) {
                if (json.length() > 9) {
                    json.append(',');
                }
                json.append(item.toJson(user != null));
            }
            ResponseUtil.sendJson(response, 200, json.append("]}").toString());
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }
}
