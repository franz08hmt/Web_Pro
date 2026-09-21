package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Demonstrates request attributes and forwarding to a JSP under WEB-INF. */
@WebServlet("/architecture")
public final class ArchitectureServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, javax.servlet.ServletException {
        request.setAttribute("controllerName", getClass().getSimpleName());
        request.setAttribute("apiContract", "/api/health");
        request.setAttribute("databaseLayer", "DAO JDBC + PreparedStatement");

        RequestDispatcher view = request.getRequestDispatcher("/WEB-INF/views/architecture.jsp");
        view.forward(request, response);
    }
}
