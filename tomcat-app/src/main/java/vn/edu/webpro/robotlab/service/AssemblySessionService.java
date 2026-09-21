package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
import java.util.List;
import vn.edu.webpro.robotlab.dao.AssemblySessionDao;
import vn.edu.webpro.robotlab.dao.RobotDao;
import vn.edu.webpro.robotlab.dao.AssemblyStepDao;
import vn.edu.webpro.robotlab.model.AssemblySession;

public final class AssemblySessionService {
    private final AssemblySessionDao sessions;private final RobotDao robots;private final AssemblyStepDao stepDao;
    public AssemblySessionService(AssemblySessionDao sessions,RobotDao robots,AssemblyStepDao stepDao){this.sessions=sessions;this.robots=robots;this.stepDao=stepDao;}
    public AssemblySession create(long userId,String robotId)throws SQLException{if(robotId==null||!robotId.matches("[a-z0-9]+(?:-[a-z0-9]+)*")||!robots.exists(robotId))throw new IllegalArgumentException("robot");return present(sessions.createOrResume(userId,robotId));}
    public AssemblySession get(long userId,long sessionId)throws SQLException{AssemblySession value=sessions.find(sessionId,userId);if(value==null)throw new IllegalArgumentException("missing");return present(value);}
    public List<AssemblySession> list(long userId)throws SQLException{return sessions.list(userId).stream().map(item->{try{return present(item);}catch(SQLException e){throw new IllegalStateException(e);}}).toList();}
    public AssemblySession status(long userId,long sessionId,String target)throws SQLException{AssemblySession current=get(userId,sessionId);if(current.status().equals(target))return current;boolean allowed=("READY".equals(current.status())&&"IN_PROGRESS".equals(target))||("IN_PROGRESS".equals(current.status())&&"COMPLETED".equals(target))||(!"COMPLETED".equals(current.status())&&"ABANDONED".equals(target));if(!allowed)throw new IllegalArgumentException("transition");AssemblySession updated=sessions.updateStatus(sessionId,userId,target);if(updated==null)throw new IllegalArgumentException("missing");return present(updated);}
    public AssemblySession component(long userId,long sessionId,String componentId,boolean prepared)throws SQLException{AssemblySession current=get(userId,sessionId);if(!java.util.Set.of("PREPARING","READY").contains(current.status())||!robots.hasComponent(current.robotId(),componentId))throw new IllegalArgumentException("component");AssemblySession updated=sessions.setComponent(sessionId,userId,componentId,prepared);return present(updated);}
    public AssemblySession step(long userId,long sessionId,String stepId,String status)throws SQLException{AssemblySession current=get(userId,sessionId);if(!"IN_PROGRESS".equals(current.status())||!java.util.Set.of("PENDING","COMPLETED").contains(status)||!stepDao.belongsToRobot(current.robotId(),stepId))throw new IllegalArgumentException("step");return present(sessions.setStep(sessionId,userId,stepId,status));}
    public AssemblySession visual(long userId,long sessionId,String componentId,boolean assembled)throws SQLException{AssemblySession current=get(userId,sessionId);if(!"IN_PROGRESS".equals(current.status())||!robots.hasComponent(current.robotId(),componentId))throw new IllegalArgumentException("component");return present(sessions.setVisual(sessionId,userId,componentId,assembled));}
    private AssemblySession present(AssemblySession value)throws SQLException{int required=robots.parts(value.robotId()).size();long ready=value.components().stream().filter(v->v.isPrepared()).count();String expected=required>0&&ready==required?"READY":"PREPARING";if(java.util.Set.of("PREPARING","READY").contains(value.status())&&!expected.equals(value.status()))value=sessions.updateStatus(value.id(),value.userId(),expected);return new AssemblySession(value.id(),value.userId(),value.robotId(),value.status(),value.components(),value.steps(),value.assembledPartIds(),required);}
}
