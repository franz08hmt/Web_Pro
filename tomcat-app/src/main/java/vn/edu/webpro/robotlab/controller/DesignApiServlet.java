package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.business.DesignLayout;
import vn.edu.webpro.robotlab.data.DesignPlacementDB;
import vn.edu.webpro.robotlab.util.DesignException;
import vn.edu.webpro.robotlab.util.DesignFormUtil;
import vn.edu.webpro.robotlab.util.DesignJson;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;

/** API tương tác của Xưởng; danh sách/form/hồ sơ vẫn dùng Servlet/JSP. */
@WebServlet("/api/designs/*")
public class DesignApiServlet extends HttpServlet {
    /** GET chỉ đọc layout của chủ thiết kế. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handle(request, response);
    }

    /** POST gắn/undo cần token header như API hiện có. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handle(request, response);
    }

    /** DELETE tháo vật thể, không xóa thiết kế bằng GET. */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        User user = SessionUtil.requireUser(request, response);
        if (user == null) {
            return;
        }
        String method = request.getMethod();
        if (!"GET".equals(method) && !SessionUtil.hasValidCsrfToken(request, response)) {
            return;
        }
        try {
            String path = request.getPathInfo();
            String[] parts = path == null ? new String[0] : path.substring(1).split("/");
            long id = parts.length > 0 ? DesignFormUtil.id(parts[0]) : -1;
            if (id <= 0 || parts.length < 2) {
                throw new IllegalArgumentException("invalid-path");
            }
            DesignLayout current = DesignPlacementDB.selectLayout(id, user.getId());
            if (current == null) {
                throw new DesignException("NOT_FOUND", "Không tìm thấy thiết kế bạn được phép dùng.");
            }
            DesignLayout result;
            if ("GET".equals(method) && parts.length == 2 && "layout".equals(parts[1])) {
                result = current;
            } else if ("POST".equals(method) && parts.length == 2) {
                String body = JsonUtil.readBody(request.getReader(), 4096);
                int version = version(JsonUtil.longField(body, "expectedLayoutVersion"));
                if ("undo".equals(parts[1])) {
                    result = DesignPlacementDB.change(id, user.getId(), "undo", 0, "", "", version);
                } else if ("placements".equals(parts[1])) {
                    long item = JsonUtil.longField(body, "instanceId");
                    String slot = JsonUtil.stringField(body, "slotId");
                    String orientation = JsonUtil.stringField(body, "orientation");
                    if (item <= 0 || orientation == null || !orientation.matches("Y(0|90|180|270)")) {
                        throw new IllegalArgumentException("invalid-intent");
                    }
                    result = DesignPlacementDB.change(id, user.getId(), "attach", item, slot, orientation, version);
                } else {
                    throw new IllegalArgumentException("invalid-action");
                }
            } else if ("DELETE".equals(method) && parts.length == 3 && "placements".equals(parts[1])) {
                long item = DesignFormUtil.id(parts[2]);
                int version = version(DesignFormUtil.id(request.getParameter("expectedLayoutVersion")));
                if (item <= 0) {
                    throw new IllegalArgumentException("invalid-item");
                }
                result = DesignPlacementDB.change(id, user.getId(), "detach", item, "", "", version);
            } else {
                throw new IllegalArgumentException("invalid-action");
            }
            ResponseUtil.sendJson(response, 200, "{\"data\":" + DesignJson.layout(result) + "}");
        } catch (DesignException e) {
            int status = "NOT_FOUND".equals(e.getCode()) ? 404 : "LAYOUT_CONFLICT".equals(e.getCode()) ? 409 : 422;
            ResponseUtil.sendError(response, status, e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, 422, "VALIDATION_ERROR",
                    "Thao tác không hợp lệ. Hãy chọn vật thể, điểm gắn, hướng và tải lại phiên bản mới nhất.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    private int version(long value) {
        if (value <= 0 || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("invalid-version");
        }
        return (int) value;
    }
}
