package vn.edu.webpro.robotlab.dao;

import java.sql.*;
import vn.edu.webpro.robotlab.model.AssemblySession;

public final class AssemblySessionDao {
    private final DatabaseConnectionFactory connections;
    public AssemblySessionDao(DatabaseConnectionFactory connections){this.connections=connections;}
    public AssemblySession createOrResume(long userId,String robotId)throws SQLException{try(Connection c=connections.openConnection()){try(PreparedStatement s=c.prepareStatement("SELECT id,user_id,robot_id,status FROM assembly_sessions WHERE user_id=? AND robot_id=? AND status IN ('PREPARING','READY','IN_PROGRESS') ORDER BY updated_at DESC,id DESC LIMIT 1")){s.setLong(1,userId);s.setString(2,robotId);try(ResultSet r=s.executeQuery()){if(r.next())return map(r);}}try(PreparedStatement s=c.prepareStatement("INSERT INTO assembly_sessions (user_id,robot_id,status) VALUES (?,?,'PREPARING')",Statement.RETURN_GENERATED_KEYS)){s.setLong(1,userId);s.setString(2,robotId);s.executeUpdate();try(ResultSet k=s.getGeneratedKeys()){k.next();return find(c,k.getLong(1),userId);}}}}
    public AssemblySession find(long id,long userId)throws SQLException{try(Connection c=connections.openConnection()){return find(c,id,userId);}}
    private AssemblySession find(Connection c,long id,long userId)throws SQLException{try(PreparedStatement s=c.prepareStatement("SELECT id,user_id,robot_id,status FROM assembly_sessions WHERE id=? AND user_id=?")){s.setLong(1,id);s.setLong(2,userId);try(ResultSet r=s.executeQuery()){return r.next()?map(r):null;}}}
    private AssemblySession map(ResultSet r)throws SQLException{return new AssemblySession(r.getLong("id"),r.getLong("user_id"),r.getString("robot_id"),r.getString("status"));}
}
