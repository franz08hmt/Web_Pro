package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.AssemblySession;
import vn.edu.webpro.robotlab.business.Robot;
import vn.edu.webpro.robotlab.business.RobotComponent;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.AssemblySessionDB;
import vn.edu.webpro.robotlab.data.ComponentDB;
import vn.edu.webpro.robotlab.data.RobotDB;
import vn.edu.webpro.robotlab.util.SessionUtil;

/**
 * Phiếu kết quả lắp ráp: đọc phiên của người đang đăng nhập, đối chiếu với robot
 * và danh mục linh kiện qua JDBC, rồi setAttribute + forward sang JSP — cùng
 * khuôn với AccountPageServlet/ComponentCatalogPageServlet.
 *
 * GET /assembly-receipt?session={id}
 *
 * AssemblySessionDB.selectSession() lọc theo (id, user_id) nên một tài khoản
 * không xem được phiếu của tài khoản khác kể cả khi đoán đúng ID. Trang tự
 * hiển thị thông báo phù hợp khi phiên chưa hoàn tất, không cần dựng lỗi HTTP.
 */
@WebServlet("/assembly-receipt")
public class AssemblyReceiptPageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user;
        try {
            user = SessionUtil.getCurrentUser(request);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/pages/tai-khoan.html");
            return;
        }

        long sessionId = parseSessionId(request.getParameter("session"));
        if (sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            AssemblySession session = AssemblySessionDB.selectSession(sessionId, user.getId());
            if (session == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Robot robot = RobotDB.selectRobot(session.getRobotId());
            List<RobotComponent> requiredComponents = RobotDB.selectRobotComponents(session.getRobotId());
            Map<String, String> componentNames = ComponentDB.selectComponentNames();

            request.setAttribute("session", session);
            request.setAttribute("robot", robot);
            request.setAttribute("requiredComponents", requiredComponents);
            request.setAttribute("componentNames", componentNames);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/assembly-receipt.jsp")
                .forward(request, response);
    }

    private long parseSessionId(String raw) {
        if (raw == null || !raw.matches("[1-9][0-9]{0,18}")) return -1;
        return Long.parseLong(raw);
    }
}
