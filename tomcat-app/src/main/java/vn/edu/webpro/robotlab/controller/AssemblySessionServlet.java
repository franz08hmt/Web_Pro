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
import vn.edu.webpro.robotlab.business.SessionStep;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.AssemblySessionDB;
import vn.edu.webpro.robotlab.data.AssemblyStepDB;
import vn.edu.webpro.robotlab.data.RobotDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
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
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) return;

        try {
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
        if (user == null) return;

        try {
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
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) return;

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

    private void changeStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) return;

        try {
            String path = request.getPathInfo();
            AssemblySession session = path == null ? null : findSession(path.substring(1), user);
            if (session == null) {
                sendSessionNotFound(response);
                return;
            }
            refresh(session);

            String target = JsonUtil.stringField(JsonUtil.readBody(request.getReader(), MAX_BODY), "status");
            if (!target.equals(session.getStatus())) {
                if (!session.canChangeStatusTo(target)) {
                    ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                            "INVALID_STATE_TRANSITION", "Không thể chuyển sang trạng thái yêu cầu.");
                    return;
                }
                AssemblySessionDB.updateStatus(session.getId(), user.getId(), target);
                session = AssemblySessionDB.selectSession(session.getId(), user.getId());
            }
            sendSession(response, HttpServletResponse.SC_OK, refresh(session));
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Không thể chuyển sang trạng thái yêu cầu.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    /** Tick linh kiện: chỉ trong giai đoạn chuẩn bị và linh kiện phải thuộc robot này. */
    private boolean updateComponent(AssemblySession session, String componentId, String body)
            throws SQLException {
        boolean prepared = Boolean.parseBoolean(JsonUtil.stringField(body, "isPrepared"));
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
        boolean assembled = Boolean.parseBoolean(JsonUtil.stringField(body, "isAssembled"));
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
        if (!rawId.matches("[1-9][0-9]{0,18}")) return null;
        return AssemblySessionDB.selectSession(Long.parseLong(rawId), user.getId());
    }

    private void sendSessionList(HttpServletResponse response, User user) throws SQLException, IOException {
        List<AssemblySession> sessions = AssemblySessionDB.selectSessions(user.getId());
        StringBuilder items = new StringBuilder();
        for (AssemblySession session : sessions) {
            if (items.length() > 0) items.append(',');
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
}
