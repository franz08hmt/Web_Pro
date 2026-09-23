package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Trang tóm tắt kiến trúc: đặt vài thuộc tính vào request rồi forward sang JSP. */
@WebServlet("/architecture")
public class ArchitectureServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("controllerName", getClass().getSimpleName());
        request.setAttribute("apiContract", "/api/health");
        request.setAttribute("databaseLayer", "UserDB, RobotDB... + ConnectionPool + PreparedStatement");

        String url = "/WEB-INF/views/architecture.jsp";
        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }
}
