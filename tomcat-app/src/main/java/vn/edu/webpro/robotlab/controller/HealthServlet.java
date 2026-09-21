package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.model.HealthPayload;
import vn.edu.webpro.robotlab.service.DatabaseHealthService;
import vn.edu.webpro.robotlab.service.HealthService;

/** Controller for GET /api/health. It owns HTTP concerns, not SQL. */
@WebServlet("/api/health")
public final class HealthServlet extends HttpServlet {
    private final HealthService healthService = new HealthService(
            new DatabaseHealthService(System.getenv())
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HealthPayload health = healthService.readHealth();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(health.toJson());
    }
}
