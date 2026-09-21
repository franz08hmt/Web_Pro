package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
import vn.edu.webpro.robotlab.dao.AssemblySessionDao;
import vn.edu.webpro.robotlab.dao.RobotDao;
import vn.edu.webpro.robotlab.model.AssemblySession;

public final class AssemblySessionService {
    private final AssemblySessionDao sessions;private final RobotDao robots;
    public AssemblySessionService(AssemblySessionDao sessions,RobotDao robots){this.sessions=sessions;this.robots=robots;}
    public AssemblySession create(long userId,String robotId)throws SQLException{if(robotId==null||!robotId.matches("[a-z0-9]+(?:-[a-z0-9]+)*")||!robots.exists(robotId))throw new IllegalArgumentException("robot");return sessions.createOrResume(userId,robotId);}
    public AssemblySession get(long userId,long sessionId)throws SQLException{AssemblySession value=sessions.find(sessionId,userId);if(value==null)throw new IllegalArgumentException("missing");return value;}
}
