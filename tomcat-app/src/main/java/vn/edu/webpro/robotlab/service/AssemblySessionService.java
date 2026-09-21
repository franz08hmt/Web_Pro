package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
import java.util.List;
import vn.edu.webpro.robotlab.dao.AssemblySessionDao;
import vn.edu.webpro.robotlab.dao.RobotDao;
import vn.edu.webpro.robotlab.model.AssemblySession;

public final class AssemblySessionService {
    private final AssemblySessionDao sessions;private final RobotDao robots;
    public AssemblySessionService(AssemblySessionDao sessions,RobotDao robots){this.sessions=sessions;this.robots=robots;}
    public AssemblySession create(long userId,String robotId)throws SQLException{if(robotId==null||!robotId.matches("[a-z0-9]+(?:-[a-z0-9]+)*")||!robots.exists(robotId))throw new IllegalArgumentException("robot");return sessions.createOrResume(userId,robotId);}
    public AssemblySession get(long userId,long sessionId)throws SQLException{AssemblySession value=sessions.find(sessionId,userId);if(value==null)throw new IllegalArgumentException("missing");return value;}
    public List<AssemblySession> list(long userId)throws SQLException{return sessions.list(userId);}
    public AssemblySession status(long userId,long sessionId,String target)throws SQLException{AssemblySession current=get(userId,sessionId);if(current.status().equals(target))return current;boolean allowed=("READY".equals(current.status())&&"IN_PROGRESS".equals(target))||("IN_PROGRESS".equals(current.status())&&"COMPLETED".equals(target))||(!"COMPLETED".equals(current.status())&&"ABANDONED".equals(target));if(!allowed)throw new IllegalArgumentException("transition");AssemblySession updated=sessions.updateStatus(sessionId,userId,target);if(updated==null)throw new IllegalArgumentException("missing");return updated;}
    public AssemblySession component(long userId,long sessionId,String componentId,boolean prepared)throws SQLException{return sessions.setComponent(sessionId,userId,componentId,prepared);}
    public AssemblySession step(long userId,long sessionId,String stepId,String status)throws SQLException{return sessions.setStep(sessionId,userId,stepId,status);}
    public AssemblySession visual(long userId,long sessionId,String componentId,boolean assembled)throws SQLException{return sessions.setVisual(sessionId,userId,componentId,assembled);}
}
