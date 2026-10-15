package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Date;
import vn.edu.webpro.robotlab.business.*;
import vn.edu.webpro.robotlab.data.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.util.SessionUtil;

/**
 * Trang tài khoản dựng ở server: đọc User từ HttpSession, đặt vào request bằng
 * setAttribute rồi forward sang JSP — cùng cách EmailListServlet chuyển sang
 * thanks.jsp ở Chapter 12 slide 44.
 */
@WebServlet("/account")
public class AccountPageServlet extends HttpServlet {

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

        request.setAttribute("user", user);
        request.setAttribute("isAdmin", user.isAdmin());

        response.setHeader("Cache-Control", "no-store");
        try {
            DashboardSummary dashboard = new DashboardSummary();
            AssemblySession open = AssemblySessionDB.selectLatestSession(user.getId(), false);
            DashboardCard active = dashboard.addCard("Đang lắp ráp", null, "Chọn mẫu để lắp", "/pages/lap-rap.html");
            if (open != null) {
                active.setSummary(open.isDetailed()
                        ? "Đã gắn " + open.getPlacedSlotCount() + "/" + open.getRequiredSlotCount() + " điểm gắn."
                        : "Chuẩn bị linh kiện: " + open.getProgressPercent() + "%.");
                active.setAction("Tiếp tục lắp ráp");
                active.setPath(open.isInProgress() ? "/pages/lap-rap-3d.html" : "/pages/lap-rap.html");
                active.setParameterName("session");
                active.setParameterValue(String.valueOf(open.getId()));
                active.setModel(open.getRobotId());
            }
            AssemblySession complete = AssemblySessionDB.selectLatestSession(user.getId(), true);
            DashboardCard finished = dashboard.addCard("Mẫu đã hoàn tất", null, "Khám phá mẫu robot",
                    "/pages/mau-robot.html");
            if (complete != null) {
                finished.setSummary("Xem lại sản phẩm, quy trình, phiếu và bài kiểm tra của mẫu vừa hoàn tất.");
                finished.setAction("Xem trang hoàn thiện");
                finished.setPath("/assembly-review");
                finished.setParameterName("session");
                finished.setParameterValue(String.valueOf(complete.getId()));
            }
            DashboardCard quiz = dashboard.addCard("Kiểm tra kiến thức",
                    dashboard.quizSummary(QuizAttemptDB.selectAttempts(user.getId(), null, 1, 0)),
                    "Làm bài kiểm tra", "/pages/kiem-tra.html");
            for (Robot completedRobot : StatsDB.selectCompletedRobots(user.getId())) {
                if (QuizAttemptDB.countAttempts(user.getId(), completedRobot.getId()) == 0) {
                    quiz.setSummary(quiz.getSummary() + " Bạn đã lắp " + completedRobot.getName()
                            + " nhưng chưa làm bài kiểm tra của mẫu này.");
                    quiz.setParameterName("model");
                    quiz.setParameterValue(completedRobot.getId());
                    break;
                }
            }
            if (user.isAdmin()) {
                dashboard.addCard("Quản lý nhiệm vụ", "Phân công và chấm nhiệm vụ học tập.",
                        "Mở quản trị nhiệm vụ", "/admin-tasks");
            } else {
                dashboard.addCard("Nhiệm vụ", dashboard.taskSummary(
                        PracticeTaskDB.selectTasks(user.getId(), false, "OPEN"), new Date()), "Xem nhiệm vụ", "/tasks");
            }
            List<RobotDesign> designs = DesignDB.selectDesigns(user.getId());
            DashboardCard design = dashboard.addCard("Thiết kế gần đây", null, "Mở Xưởng thiết kế", "/designs");
            if (!designs.isEmpty()) {
                RobotDesign recent = designs.get(0);
                design.setSummary(recent.getName() + " · " + recent.getStatusLabel());
                design.setPath("/design-sheet");
                design.setAction("Xem hồ sơ thiết kế");
                design.setParameterName("id");
                design.setParameterValue(String.valueOf(recent.getId()));
            }
            int kinds = 0;
            int total = 0;
            for (KitItem item : UserKitDB.selectItems(user.getId(), DesignRuleDB.selectPublished())) {
                if (item.getQuantity() > 0) {
                    kinds++;
                    total += item.getQuantity();
                }
            }
            dashboard.addCard("Bộ linh kiện", kinds + " loại · " + total + " vật thể đang có.",
                    "Xem bộ linh kiện", "/my-kit");
            List<ShopOrder> orders = OrderDB.selectOrders(user.getId(), 1, 0);
            dashboard.addCard("Đơn hàng gần nhất", orders.isEmpty() ? null : orders.get(0).getStatusLabel(),
                    "Xem đơn hàng", "/order-history");
            request.setAttribute("dashboard", dashboard);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        String url = "/WEB-INF/views/dashboard.jsp";
        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }
}
