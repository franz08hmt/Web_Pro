package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
import java.util.List;
import vn.edu.webpro.robotlab.dao.AssemblyStepDao;
import vn.edu.webpro.robotlab.dao.RobotDao;
import vn.edu.webpro.robotlab.model.AssemblyStep;
import vn.edu.webpro.robotlab.model.Robot;
import vn.edu.webpro.robotlab.model.RobotComponent;

public final class RobotService {
    private final RobotDao robotDao;
    private final AssemblyStepDao stepDao;
    public RobotService(RobotDao robotDao, AssemblyStepDao stepDao) { this.robotDao = robotDao; this.stepDao = stepDao; }
    public List<Robot> list(int page, int limit) throws SQLException { validate(page, limit); return robotDao.list(limit, (page - 1) * limit); }
    public long count() throws SQLException { return robotDao.count(); }
    public List<RobotComponent> parts(String id) throws SQLException { requireRobot(id); return robotDao.parts(id); }
    public List<AssemblyStep> steps(String id, int page, int limit) throws SQLException { validate(page, limit); requireRobot(id); return stepDao.listByRobot(id, limit, (page - 1) * limit); }
    public long stepCount(String id) throws SQLException { return stepDao.countByRobot(id); }
    private void requireRobot(String id) throws SQLException { if (!id.matches("[a-z0-9]+(?:-[a-z0-9]+)*") || !robotDao.exists(id)) throw new IllegalArgumentException("not-found"); }
    private void validate(int page, int limit) { if (page < 1 || page > 100000 || limit < 1 || limit > 100) throw new IllegalArgumentException("pagination"); }
}
