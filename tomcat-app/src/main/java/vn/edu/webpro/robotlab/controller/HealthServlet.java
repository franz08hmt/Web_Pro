package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.data.HealthDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;

/** GET /api/health — cho biết server đang chạy và database có kết nối được không. */
@WebServlet("/api/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String database = HealthDB.selectDatabaseStatus();
        ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                "{\"data\":{\"service\":\"robot-assembly-lab-api\",\"status\":\"ok\",\"database\":"
                        + JsonUtil.quote(database) + "}}");
    }
}
