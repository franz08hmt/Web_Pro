package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
import java.util.List;
import vn.edu.webpro.robotlab.dao.AssemblyStepDao;
import vn.edu.webpro.robotlab.dao.RobotDao;
import vn.edu.webpro.robotlab.model.AssemblyStep;
import vn.edu.webpro.robotlab.model.Robot;
import vn.edu.webpro.robotlab.model.RobotComponent;

/** Kiểm tra phân trang và sự tồn tại của robot trước khi giao việc cho DAO. */
public final class RobotService {
    private static final String ID_PATTERN = "[a-z0-9]+(?:-[a-z0-9]+)*";
    private static final int MAX_PAGE = 100000;
    private static final int MAX_LIMIT = 100;

    private final RobotDao robotDao;
    private final AssemblyStepDao stepDao;

    public RobotService(RobotDao robotDao, AssemblyStepDao stepDao) {
        this.robotDao = robotDao;
        this.stepDao = stepDao;
    }

    public List<Robot> list(int page, int limit) throws SQLException {
        validatePaging(page, limit);
        return robotDao.list(limit, (page - 1) * limit);
    }

    public long count() throws SQLException {
        return robotDao.count();
    }

    public List<RobotComponent> parts(String robotId) throws SQLException {
        requireRobot(robotId);
        return robotDao.parts(robotId);
    }

    public List<AssemblyStep> steps(String robotId, int page, int limit) throws SQLException {
        validatePaging(page, limit);
        requireRobot(robotId);
        return stepDao.listByRobot(robotId, limit, (page - 1) * limit);
    }

    public long stepCount(String robotId) throws SQLException {
        return stepDao.countByRobot(robotId);
    }

    /* Kiểm tra dạng ID trước khi truy vấn để một chuỗi rác không tạo thêm
       một lượt đi xuống database. Controller phân biệt "not-found" với lỗi
       phân trang dựa trên thông điệp của exception. */
    private void requireRobot(String robotId) throws SQLException {
        if (!robotId.matches(ID_PATTERN) || !robotDao.exists(robotId)) {
            throw new IllegalArgumentException("not-found");
        }
    }

    private void validatePaging(int page, int limit) {
        if (page < 1 || page > MAX_PAGE || limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("pagination");
        }
    }
}
