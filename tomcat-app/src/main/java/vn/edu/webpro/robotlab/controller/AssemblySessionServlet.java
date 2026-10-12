package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.AssemblySession;
import vn.edu.webpro.robotlab.business.RobotComponent;
import vn.edu.webpro.robotlab.business.SessionStep;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.AssemblySessionDB;
import vn.edu.webpro.robotlab.data.AssemblyStepDB;
import vn.edu.webpro.robotlab.data.RobotDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.business.AssemblyLayout;
import vn.edu.webpro.robotlab.data.AssemblyPlacementDB;
import vn.edu.webpro.robotlab.util.AssemblyPlacementJson;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/**
 * Phiên lắp ráp của người đang đăng nhập.
 *
 * POST   /api/assembly-sessions                     tạo hoặc tiếp tục phiên của một robot
 * GET    /api/assembly-sessions                     danh sách phiên
 * GET    /api/assembly-sessions/{id}                chi tiết một phiên
 * PATCH  /api/assembly-sessions/{id}                đổi trạng thái phiên
 * PUT    /api/assembly-sessions/{id}/components/{c} tick linh kiện
 * PUT    /api/assembly-sessions/{id}/steps/{s}      đánh dấu bước lắp ráp
 * PUT    /api/assembly-sessions/{id}/visual-parts/{c}  lắp/gỡ part 3D
 *
 * Servlet kiểm tra quyền và dữ liệu, hỏi JavaBean AssemblySession xem thao tác có
 * hợp lệ không, rồi gọi AssemblySessionDB để ghi. Mọi thao tác ghi cần CSRF token.
 */
@WebServlet("/api/assembly-sessions/*")
public class AssemblySessionServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int MAX_BODY = 8192;
    private static final int PAGE_SIZE = 20;

    /* HttpServlet không có doPatch(), nên PATCH được tách ra trong service(). */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("PATCH".equals(request.getMethod())) {
            changeStatus(request, response);
            return;
        }
        super.service(request, response);
    }

    /** Tạo phiên mới, hoặc trả lại phiên đang mở nếu robot này đã có phiên. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) {
            return;
        }

        try {
            if (handlePlacement(request, response, user)) {
                return;
            }
            String robotId = JsonUtil.stringField(JsonUtil.readBody(request.getReader(), MAX_BODY), "robotId");
            if (!ValidationUtil.isSlug(robotId) || !RobotDB.robotExists(robotId)) {
                ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                        "VALIDATION_ERROR", "Robot không tồn tại hoặc dữ liệu không hợp lệ.");
                return;
            }

            AssemblySession session = AssemblySessionDB.selectOpenSession(user.getId(), robotId);
            if (session == null) {
                long id = AssemblySessionDB.insert(user.getId(), robotId);
                session = AssemblySessionDB.selectSession(id, user.getId());
            }
            sendSession(response, HttpServletResponse.SC_CREATED, refresh(session));
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Robot không tồn tại hoặc dữ liệu không hợp lệ.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null) {
            return;
        }

        try {
            if (handlePlacement(request, response, user)) {
                return;
            }
            String path = request.getPathInfo();
            if (path == null || "/".equals(path)) {
                sendSessionList(response, user);
                return;
            }

            AssemblySession session = findSession(path.substring(1), user);
            if (session == null) {
                sendSessionNotFound(response);
                return;
            }
            sendSession(response, HttpServletResponse.SC_OK, refresh(session));
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    /** Cập nhật một mục tiến độ; đường dẫn dạng {id}/{nhóm}/{mã}. */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) {
            return;
        }

        String[] segments = request.getPathInfo() == null ? new String[0]
                : request.getPathInfo().substring(1).split("/");
        if (segments.length != 3) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                    "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
            return;
        }
        String group = segments[1];
        String itemId = segments[2];

        try {
            if ("visual-parts".equals(group)) {
                AssemblySession candidate = findSession(segments[0], user);
                if (candidate != null && candidate.isDetailed()) {
                    ResponseUtil.sendError(response, 422, "DETAILED_MODE_REQUIRED",
                            "Phiên chi tiết cần gắn từng vật thể qua điểm gắn, không tick nhóm linh kiện.");
                    return;
                }
            }
            AssemblySession session = findSession(segments[0], user);
            if (session == null) {
                sendSessionNotFound(response);
                return;
            }
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);

            boolean accepted;
            if ("components".equals(group)) {
                accepted = updateComponent(session, itemId, body);
            } else if ("steps".equals(group)) {
                accepted = updateStep(session, itemId, body);
            } else if ("visual-parts".equals(group)) {
                accepted = updateVisualPart(session, itemId, body);
            } else {
                ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                        "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
                return;
            }

            if (!accepted) {
                ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                        "VALIDATION_ERROR", "Dữ liệu phiên lắp ráp không hợp lệ.");
                return;
            }
            AssemblySession updated = AssemblySessionDB.selectSession(session.getId(), user.getId());
            sendSession(response, HttpServletResponse.SC_OK, refresh(updated));
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Dữ liệu phiên lắp ráp không hợp lệ.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    /** DELETE /{id}/progress — xóa tiến độ của phiên hiện tại để lắp lại từ đầu. */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) {
            return;
        }

        try {
            if (handlePlacement(request, response, user)) {
                return;
            }
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
            return;
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, 422, "VALIDATION_ERROR", "ID hoặc phiên bản không hợp lệ.");
            return;
        }
        String path = request.getPathInfo();
        String[] segments = path == null ? new String[0] : path.substring(1).split("/");
        if (segments.length != 2 || !"progress".equals(segments[1])) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                    "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
            return;
        }

        try {
            AssemblySession session = findSession(segments[0], user);
            if (session == null) {
                sendSessionNotFound(response);
                return;
            }
            if (!session.canResetProgress()) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                        "INVALID_STATE_TRANSITION", "Phiên này không thể đặt lại tiến độ.");
                return;
            }
            if (!AssemblySessionDB.resetProgress(session.getId(), user.getId())) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                        "INVALID_STATE_TRANSITION", "Phiên này không thể đặt lại tiến độ.");
                return;
            }
            AssemblySession reset = AssemblySessionDB.selectSession(session.getId(), user.getId());
            sendSession(response, HttpServletResponse.SC_OK, refresh(reset));
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    private void changeStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) {
            return;
        }

        try {
            String path = request.getPathInfo();
            AssemblySession session = path == null ? null : findSession(path.substring(1), user);
            if (session == null) {
                sendSessionNotFound(response);
                return;
            }
            refresh(session);

            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            String target = JsonUtil.stringField(body, "status");
            if (session.isDetailed() && AssemblySession.COMPLETED.equals(target)) {
                completePlacement(request, response, session.getId(), user.getId(), body);
                return;
            }
            if (!target.equals(session.getStatus())) {
                if (AssemblySession.COMPLETED.equals(target)) {
                    if (!completeAssembly(session, response)) {
                        return;
                    }
                    session = AssemblySessionDB.selectSession(session.getId(), user.getId());
                } else if (!session.canChangeStatusTo(target)) {
                    ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                            "INVALID_STATE_TRANSITION", "Không thể chuyển sang trạng thái yêu cầu.");
                    return;
                } else {
                    AssemblySessionDB.updateStatus(session.getId(), user.getId(), target);
                    session = AssemblySessionDB.selectSession(session.getId(), user.getId());
                }
            }
            sendSession(response, HttpServletResponse.SC_OK, refresh(session));
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Không thể chuyển sang trạng thái yêu cầu.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    /**
     * Xác nhận hoàn tất thực hành. Hỏi JavaBean trước để trả lỗi rõ ràng ("chưa
     * lắp đủ linh kiện") mà không cần đụng tới database khi rõ ràng chưa đủ
     * điều kiện; nhưng quyết định ghi dữ liệu thật — kể cả khi có request gần
     * đồng thời — luôn thuộc về AssemblySessionDB.completeSession(), vì chỉ nó
     * mới đọc session_visual_parts ngay trong câu UPDATE atomic. Không tin số
     * phần trăm hay cờ "đã hoàn tất" mà client tự gửi lên.
     */
    private boolean completeAssembly(AssemblySession session, HttpServletResponse response)
            throws IOException, SQLException {
        if (session.isInProgress()) {
            List<RobotComponent> required = RobotDB.selectRobotComponents(session.getRobotId());
            if (!session.canCompleteAssembly(required)) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                        "ASSEMBLY_INCOMPLETE", "Chưa lắp đủ linh kiện bắt buộc trong mô hình 3D.");
                return false;
            }
        }

        int result = AssemblySessionDB.completeSession(session.getId(), session.getUserId());
        if (result == AssemblySessionDB.COMPLETE_OK || result == AssemblySessionDB.COMPLETE_ALREADY_DONE) {
            return true;
        }
        if (result == AssemblySessionDB.COMPLETE_INCOMPLETE_PARTS) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "ASSEMBLY_INCOMPLETE", "Chưa lắp đủ linh kiện bắt buộc trong mô hình 3D.");
        } else if (result == AssemblySessionDB.COMPLETE_NOT_FOUND) {
            sendSessionNotFound(response);
        } else {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "INVALID_STATE_TRANSITION", "Không thể chuyển sang trạng thái yêu cầu.");
        }
        return false;
    }

    /** Tick linh kiện: chỉ trong giai đoạn chuẩn bị và linh kiện phải thuộc robot này. */
    private boolean updateComponent(AssemblySession session, String componentId, String body)
            throws SQLException {
        boolean prepared = JsonUtil.booleanField(body, "isPrepared");
        if (!session.isInPreparation()
                || !RobotDB.robotHasComponent(session.getRobotId(), componentId)) {
            return false;
        }
        AssemblySessionDB.updateComponent(session, componentId, prepared);
        return true;
    }

    /** Đánh dấu bước: chỉ khi phiên đang IN_PROGRESS và bước thuộc robot này. */
    private boolean updateStep(AssemblySession session, String stepId, String body) throws SQLException {
        String status = JsonUtil.stringField(body, "status");
        if (!session.isInProgress()
                || !SessionStep.isValidStatus(status)
                || !AssemblyStepDB.stepBelongsToRobot(session.getRobotId(), stepId)) {
            return false;
        }
        AssemblySessionDB.updateStep(session, stepId, SessionStep.COMPLETED.equals(status));
        return true;
    }

    /** Lắp/gỡ part 3D: cũng chỉ khi phiên đang IN_PROGRESS. */
    private boolean updateVisualPart(AssemblySession session, String componentId, String body)
            throws SQLException {
        boolean assembled = JsonUtil.booleanField(body, "isAssembled");
        if (!session.isInProgress()
                || !RobotDB.robotHasComponent(session.getRobotId(), componentId)) {
            return false;
        }
        if (assembled) {
            AssemblySessionDB.insertVisualPart(session, componentId);
        } else {
            AssemblySessionDB.deleteVisualPart(session, componentId);
        }
        return true;
    }

    /**
     * Nạp số linh kiện bắt buộc và số bước của robot vào JavaBean, rồi đồng bộ
     * PREPARING ⇄ READY theo dữ liệu thật trong database. Nhờ vậy trạng thái luôn
     * đúng, kể cả khi admin thêm/bớt linh kiện bắt buộc sau khi phiên được tạo.
     */
    private AssemblySession refresh(AssemblySession session) throws SQLException {
        session.setRequiredComponentCount(RobotDB.selectRobotComponents(session.getRobotId()).size());
        session.setTotalStepCount((int) AssemblyStepDB.countSteps(session.getRobotId()));

        String expected = session.getExpectedPreparationStatus();
        if (session.isInPreparation() && !expected.equals(session.getStatus())) {
            AssemblySessionDB.updateStatus(session.getId(), session.getUserId(), expected);
            session.setStatus(expected);
        }
        return session;
    }

    /** Đọc phiên theo ID trên đường dẫn; ID sai định dạng hoặc phiên của người khác trả về null. */
    private AssemblySession findSession(String rawId, User user) throws SQLException {
        if (!rawId.matches("[1-9][0-9]{0,18}")) {
            return null;
        }
        try {
            return AssemblySessionDB.selectSession(Long.parseLong(rawId), user.getId());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void sendSessionList(HttpServletResponse response, User user) throws SQLException, IOException {
        List<AssemblySession> sessions = AssemblySessionDB.selectSessions(user.getId());
        StringBuilder items = new StringBuilder();
        for (AssemblySession session : sessions) {
            if (items.length() > 0) {
                items.append(',');
            }
            items.append(refresh(session).toJson());
        }
        ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                "{\"data\":[" + items + "],\"meta\":{\"page\":1,\"pageSize\":" + PAGE_SIZE
                        + ",\"total\":" + sessions.size() + "}}");
    }

    private void sendSession(HttpServletResponse response, int status, AssemblySession session)
            throws IOException {
        ResponseUtil.sendJson(response, status, "{\"data\":" + session.toJson() + "}");
    }

    private void sendSessionNotFound(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "NOT_FOUND", "Không tìm thấy phiên lắp ráp.");
    }
    /** Xử lý route layout/placements/check; trả false cho route GROUP hiện có. */
    private boolean handlePlacement(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException, SQLException {
        String path = request.getPathInfo();
        String[] segments = path == null ? new String[0] : path.substring(1).split("/");
        if (segments.length < 2 || (!"layout".equals(segments[1]) && !"placements".equals(segments[1])
                && !"check".equals(segments[1]) && !"progress".equals(segments[1]))) {
            return false;
        }
        long intentInstanceId = 0;
        long intentSlotId = 0;
        try {
            if ("progress".equals(segments[1])) {
                AssemblyLayout current = AssemblyPlacementDB.selectLayout(id(segments[0]), user.getId());
                if (current == null || !"DETAILED".equals(current.getMode())) {
                    return false;
                }
            }
            String method = request.getMethod();
            if (!"GET".equals(method) && !SessionUtil.hasValidCsrfToken(request, response)) {
                return true;
            }
            long sessionId = id(segments[0]);
            if ("GET".equals(method) && segments.length == 2 && "layout".equals(segments[1])) {
                sendLayout(response, AssemblyPlacementDB.selectLayout(sessionId, user.getId()));
            } else if ("POST".equals(method) && segments.length == 2 && "check".equals(segments[1])) {
                AssemblyLayout layout = AssemblyPlacementDB.selectLayout(sessionId, user.getId());
                if (layout == null) {
                    throw new IllegalArgumentException("NOT_FOUND");
                }
                ResponseUtil.sendJson(response, 200, "{\"data\":" + AssemblyPlacementJson.report(layout.check()) + "}");
            } else if ("POST".equals(method) && segments.length == 2 && "placements".equals(segments[1])) {
                String body = JsonUtil.readBody(request.getReader(), 8192);
                intentInstanceId = positive(body, "instanceId");
                intentSlotId = positive(body, "slotId");
                sendLayout(response, AssemblyPlacementDB.change(sessionId, user.getId(), "attach",
                        intentInstanceId, intentSlotId,
                        orientation(body), version(body)));
            } else if ("DELETE".equals(method) && segments.length == 3 && "placements".equals(segments[1])) {
                sendLayout(response, AssemblyPlacementDB.change(sessionId, user.getId(), "detach",
                        id(segments[2]), 0, "", queryVersion(request)));
            } else if ("DELETE".equals(method) && segments.length == 2 && "progress".equals(segments[1])) {
                sendLayout(response, AssemblyPlacementDB.change(sessionId, user.getId(), "reset",
                        0, 0, "", queryVersion(request)));
            } else {
                throw new IllegalArgumentException("VALIDATION_ERROR");
            }
        } catch (IllegalArgumentException e) {
            error(response, e.getMessage(), intentInstanceId, intentSlotId);
        }
        return true;
    }

    /** Hoàn tất chi tiết qua PATCH, không dùng công thức hoàn tất GROUP. */
    private void completePlacement(HttpServletRequest request, HttpServletResponse response,
            long sessionId, long userId, String body) throws IOException, SQLException {
        try {
            sendLayout(response, AssemblyPlacementDB.change(sessionId, userId, "complete", 0, 0, "", version(body)));
        } catch (IllegalArgumentException e) {
            error(response, e.getMessage());
        }
    }

    private static long id(String text) {
        try {
            long value = Long.parseLong(text);
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("VALIDATION_ERROR");
        }
        throw new IllegalArgumentException("VALIDATION_ERROR");
    }

    private static long positive(String body, String key) {
        long value = JsonUtil.longField(body, key);
        if (value <= 0) {
            throw new IllegalArgumentException("VALIDATION_ERROR");
        }
        return value;
    }

    private static int version(String body) {
        return idVersion(JsonUtil.longField(body, "expectedLayoutVersion"));
    }

    private static String orientation(String body) {
        int key = body.indexOf("\"orientation\"");
        int colon = key < 0 ? -1 : body.indexOf(':', key + 13);
        int start = colon + 1;
        while (start > 0 && start < body.length() && Character.isWhitespace(body.charAt(start))) {
            start++;
        }
        if (colon < 0 || start >= body.length() || body.charAt(start) != '"') {
            throw new IllegalArgumentException("VALIDATION_ERROR");
        }
        return JsonUtil.stringField(body, "orientation");
    }

    private static int idVersion(long value) {
        if (value < 1 || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("VALIDATION_ERROR");
        }
        return (int) value;
    }

    private static int queryVersion(HttpServletRequest request) {
        return idVersion(id(request.getParameter("expectedLayoutVersion")));
    }

    private static void sendLayout(HttpServletResponse response, AssemblyLayout layout) throws IOException {
        if (layout == null) {
            throw new IllegalArgumentException("NOT_FOUND");
        }
        ResponseUtil.sendJson(response, 200,
                "{\"accepted\":true,\"data\":" + AssemblyPlacementJson.layout(layout) + "}");
    }

    private static void error(HttpServletResponse response, String code) throws IOException {
        error(response, code, 0, 0);
    }

    private static void error(HttpServletResponse response, String code, long instanceId, long slotId)
            throws IOException {
        if (code == null || !code.matches("NOT_FOUND|LAYOUT_CONFLICT|WRONG_COMPONENT|SLOT_OCCUPIED|"
                + "ORIENTATION_NOT_ALLOWED|PARENT_MISSING|ALREADY_PLACED|HAS_DEPENDENTS|"
                + "SESSION_LOCKED|ASSEMBLY_INCOMPLETE|GROUP_MODE|VALIDATION_ERROR")) {
            code = "VALIDATION_ERROR";
        }
        int status = "NOT_FOUND".equals(code) ? 404 : "LAYOUT_CONFLICT".equals(code) ? 409 : 422;
        String message = new AssemblyLayout().messageFor(code);
        if ("ASSEMBLY_INCOMPLETE".equals(code)) {
            message = "Chưa gắn đủ vật thể bắt buộc. Nhấn Kiểm tra mô hình để xem các điểm còn thiếu.";
        } else if ("VALIDATION_ERROR".equals(code)) {
            message = "ID, hướng hoặc phiên bản không hợp lệ. Hãy tải lại mô hình.";
        }
        ResponseUtil.sendJson(response, status, "{\"accepted\":false,\"code\":" + JsonUtil.quote(code)
                + ",\"message\":" + JsonUtil.quote(message) + ",\"instanceId\":" + instanceId
                + ",\"slotId\":" + slotId + ",\"error\":{\"code\":"
                + JsonUtil.quote(code) + ",\"message\":" + JsonUtil.quote(message) + "}}");
    }
}
