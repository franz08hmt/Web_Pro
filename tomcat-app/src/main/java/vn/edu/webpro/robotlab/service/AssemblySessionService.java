package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import vn.edu.webpro.robotlab.dao.AssemblySessionDao;
import vn.edu.webpro.robotlab.dao.AssemblyStepDao;
import vn.edu.webpro.robotlab.dao.RobotDao;
import vn.edu.webpro.robotlab.model.AssemblySession;

/**
 * Luật nghiệp vụ của phiên lắp ráp.
 *
 * Trình duyệt chỉ gửi lên "linh kiện nào đã tick" hoặc "bước nào đã xong";
 * phần trăm tiến độ và trạng thái phiên đều do server tự tính lại từ database,
 * nên client không thể tự khai báo là đã hoàn thành.
 *
 * Vòng đời: PREPARING ⇄ READY → IN_PROGRESS → COMPLETED, và có thể ABANDONED
 * ở bất kỳ trạng thái nào trừ COMPLETED.
 */
public final class AssemblySessionService {
    private static final String ID_PATTERN = "[a-z0-9]+(?:-[a-z0-9]+)*";
    private static final Set<String> PREPARATION_STATES = Set.of("PREPARING", "READY");
    private static final Set<String> STEP_STATES = Set.of("PENDING", "COMPLETED");

    private final AssemblySessionDao sessions;
    private final RobotDao robots;
    private final AssemblyStepDao stepDao;

    public AssemblySessionService(AssemblySessionDao sessions, RobotDao robots, AssemblyStepDao stepDao) {
        this.sessions = sessions;
        this.robots = robots;
        this.stepDao = stepDao;
    }

    public AssemblySession create(long userId, String robotId) throws SQLException {
        if (robotId == null || !robotId.matches(ID_PATTERN) || !robots.exists(robotId)) {
            throw new IllegalArgumentException("robot");
        }
        return present(sessions.createOrResume(userId, robotId));
    }

    public AssemblySession get(long userId, long sessionId) throws SQLException {
        AssemblySession session = sessions.find(sessionId, userId);
        if (session == null) throw new IllegalArgumentException("missing");
        return present(session);
    }

    public List<AssemblySession> list(long userId) throws SQLException {
        return sessions.list(userId).stream()
                .map(session -> {
                    try {
                        return present(session);
                    } catch (SQLException exception) {
                        throw new IllegalStateException(exception);
                    }
                })
                .toList();
    }

    /** Chỉ cho phép các bước chuyển trạng thái hợp lệ, không cho nhảy cóc. */
    public AssemblySession status(long userId, long sessionId, String target) throws SQLException {
        AssemblySession current = get(userId, sessionId);
        if (current.status().equals(target)) return current;

        boolean allowed =
                ("READY".equals(current.status()) && "IN_PROGRESS".equals(target))
                        || ("IN_PROGRESS".equals(current.status()) && "COMPLETED".equals(target))
                        || (!"COMPLETED".equals(current.status()) && "ABANDONED".equals(target));
        if (!allowed) throw new IllegalArgumentException("transition");

        AssemblySession updated = sessions.updateStatus(sessionId, userId, target);
        if (updated == null) throw new IllegalArgumentException("missing");
        return present(updated);
    }

    /** Tick linh kiện: chỉ ở giai đoạn chuẩn bị, và linh kiện phải thuộc robot này. */
    public AssemblySession component(long userId, long sessionId, String componentId, boolean prepared)
            throws SQLException {
        AssemblySession current = get(userId, sessionId);
        if (!PREPARATION_STATES.contains(current.status())
                || !robots.hasComponent(current.robotId(), componentId)) {
            throw new IllegalArgumentException("component");
        }
        return present(sessions.setComponent(sessionId, userId, componentId, prepared));
    }

    /** Đánh dấu bước lắp ráp: chỉ khi phiên đang IN_PROGRESS. */
    public AssemblySession step(long userId, long sessionId, String stepId, String status)
            throws SQLException {
        AssemblySession current = get(userId, sessionId);
        if (!"IN_PROGRESS".equals(current.status())
                || !STEP_STATES.contains(status)
                || !stepDao.belongsToRobot(current.robotId(), stepId)) {
            throw new IllegalArgumentException("step");
        }
        return present(sessions.setStep(sessionId, userId, stepId, status));
    }

    /** Lưu part đã lắp trong phòng 3D: cũng chỉ khi phiên đang IN_PROGRESS. */
    public AssemblySession visual(long userId, long sessionId, String componentId, boolean assembled)
            throws SQLException {
        AssemblySession current = get(userId, sessionId);
        if (!"IN_PROGRESS".equals(current.status())
                || !robots.hasComponent(current.robotId(), componentId)) {
            throw new IllegalArgumentException("component");
        }
        return present(sessions.setVisual(sessionId, userId, componentId, assembled));
    }

    /* Đối chiếu số linh kiện đã tick với số linh kiện bắt buộc của robot rồi
       tự đồng bộ PREPARING ⇄ READY. Nhờ vậy trạng thái luôn khớp dữ liệu thật,
       kể cả khi admin thêm hoặc bớt linh kiện bắt buộc sau lúc phiên được tạo. */
    private AssemblySession present(AssemblySession session) throws SQLException {
        int required = robots.parts(session.robotId()).size();
        long prepared = session.components().stream().filter(item -> item.isPrepared()).count();
        String expected = required > 0 && prepared == required ? "READY" : "PREPARING";

        if (PREPARATION_STATES.contains(session.status()) && !expected.equals(session.status())) {
            session = sessions.updateStatus(session.id(), session.userId(), expected);
        }

        return new AssemblySession(session.id(), session.userId(), session.robotId(),
                session.status(), session.components(), session.steps(),
                session.assembledPartIds(), required);
    }
}
