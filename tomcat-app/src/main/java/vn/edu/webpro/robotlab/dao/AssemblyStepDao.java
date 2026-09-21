package vn.edu.webpro.robotlab.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.model.AssemblyStep;

public final class AssemblyStepDao {
    private final DatabaseConnectionFactory connections;
    public AssemblyStepDao(DatabaseConnectionFactory connections) { this.connections = connections; }

    public List<AssemblyStep> listByRobot(String robotId, int limit, int offset) throws SQLException {
        String sql = "SELECT id, robot_id, step_order, title, instruction, illustration FROM assembly_steps "
                + "WHERE robot_id = ? ORDER BY step_order, id LIMIT ? OFFSET ?";
        List<AssemblyStep> steps = new ArrayList<>();
        try (Connection connection = connections.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, robotId); statement.setInt(2, limit); statement.setInt(3, offset);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) steps.add(new AssemblyStep(rows.getString("id"), rows.getString("robot_id"),
                        rows.getInt("step_order"), rows.getString("title"), rows.getString("instruction"), rows.getString("illustration")));
            }
        }
        return steps;
    }

    public long countByRobot(String robotId) throws SQLException {
        try (Connection connection = connections.openConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) AS total FROM assembly_steps WHERE robot_id = ?")) {
            statement.setString(1, robotId);
            try (ResultSet rows = statement.executeQuery()) { rows.next(); return rows.getLong("total"); }
        }
    }
    public boolean belongsToRobot(String robotId,String stepId)throws SQLException{try(Connection c=connections.openConnection();PreparedStatement s=c.prepareStatement("SELECT id FROM assembly_steps WHERE robot_id=? AND id=?")){s.setString(1,robotId);s.setString(2,stepId);try(ResultSet r=s.executeQuery()){return r.next();}}}
    public List<AssemblyStep> list(int limit,int offset)throws SQLException{List<AssemblyStep> data=new ArrayList<>();try(Connection c=connections.openConnection();PreparedStatement s=c.prepareStatement("SELECT id,robot_id,step_order,title,instruction,illustration FROM assembly_steps ORDER BY robot_id,step_order,id LIMIT ? OFFSET ?")){s.setInt(1,limit);s.setInt(2,offset);try(ResultSet r=s.executeQuery()){while(r.next())data.add(map(r));}}return data;}
    public AssemblyStep create(AssemblyStep step)throws SQLException{try(Connection c=connections.openConnection();PreparedStatement s=c.prepareStatement("INSERT INTO assembly_steps (id,robot_id,step_order,title,instruction,illustration) VALUES (?,?,?,?,?,CAST(? AS JSON))")){bind(s,step,false);s.executeUpdate();return step;}}
    public AssemblyStep update(String id,AssemblyStep step)throws SQLException{try(Connection c=connections.openConnection();PreparedStatement s=c.prepareStatement("UPDATE assembly_steps SET robot_id=?,step_order=?,title=?,instruction=?,illustration=CAST(? AS JSON) WHERE id=?")){bind(s,step,true);s.setString(6,id);if(s.executeUpdate()==0)return null;return new AssemblyStep(id,step.robotId(),step.stepOrder(),step.title(),step.instruction(),step.illustrationJson());}}
    public boolean remove(String id)throws SQLException{try(Connection c=connections.openConnection();PreparedStatement s=c.prepareStatement("DELETE FROM assembly_steps WHERE id=?")){s.setString(1,id);return s.executeUpdate()>0;}}
    private AssemblyStep map(ResultSet r)throws SQLException{return new AssemblyStep(r.getString("id"),r.getString("robot_id"),r.getInt("step_order"),r.getString("title"),r.getString("instruction"),r.getString("illustration"));}
    private void bind(PreparedStatement s,AssemblyStep step,boolean withoutId)throws SQLException{int p=1;if(!withoutId)s.setString(p++,step.id());s.setString(p++,step.robotId());s.setInt(p++,step.stepOrder());s.setString(p++,step.title());s.setString(p++,step.instruction());s.setString(p,step.illustrationJson());}
}
