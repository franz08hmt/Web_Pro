package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.DesignSheet;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.DesignPlacementDB;
import vn.edu.webpro.robotlab.util.DesignFormUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;

/** Trang hồ sơ: Servlet đọc request/session, JavaBean dựng dữ kiện, JSP chỉ trình bày. */
@WebServlet("/design-sheet")
public class DesignSheetServlet extends HttpServlet {
    /** GET chỉ đọc đúng chủ thiết kế, không kiểm hoặc ghi thay người dùng. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // GET đọc hồ sơ đúng owner, đặt bean vào request rồi forward; không tự kiểm và ghi lại.
        response.setHeader("Cache-Control", "no-store");
        try {
            User user = SessionUtil.getCurrentUser(request);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/pages/tai-khoan.html");
                return;
            }
            long id = DesignFormUtil.id(request.getParameter("id"));
            DesignSheet sheet = id > 0 ? DesignPlacementDB.selectSheet(id, user.getId()) : null;
            if (sheet == null) {
                response.setStatus(404);
                request.setAttribute("designError", "Không tìm thấy thiết kế bạn được phép xem.");
            } else {
                request.setAttribute("designSheet", sheet);
                request.setAttribute("sheetDesign", sheet.getLayout().getDesign());
                request.setAttribute("assemblyDrawing", sheet.getDrawing());
                request.setAttribute("sheetCheck", sheet.getReport());
            }
        } catch (SQLException e) {
            response.setStatus(503);
            request.setAttribute("designError", "Dữ liệu tạm chưa sẵn sàng. Vui lòng thử lại sau.");
        }
        String url = "/WEB-INF/views/design-sheet.jsp";
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
}
