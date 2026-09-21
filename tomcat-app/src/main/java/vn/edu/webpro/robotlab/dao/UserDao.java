package vn.edu.webpro.robotlab.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import vn.edu.webpro.robotlab.model.User;

public final class UserDao {
    private final DatabaseConnectionFactory connections;
    public UserDao(DatabaseConnectionFactory connections) { this.connections = connections; }
    public User findByEmail(String email) throws SQLException { return find("SELECT id, display_name, email, role FROM users WHERE email = ?", email); }
    public String passwordHash(String email) throws SQLException { try (Connection c = connections.openConnection(); PreparedStatement s = c.prepareStatement("SELECT password_hash FROM users WHERE email = ?")) { s.setString(1,email); try(ResultSet r=s.executeQuery()){ return r.next()?r.getString(1):null; } } }
    public User create(String fullName, String email, String passwordHash) throws SQLException { try(Connection c=connections.openConnection(); PreparedStatement s=c.prepareStatement("INSERT INTO users (display_name,email,password_hash,role) VALUES (?,?,?,'user')")){s.setString(1,fullName);s.setString(2,email);s.setString(3,passwordHash);s.executeUpdate();} return findByEmail(email); }
    private User find(String sql, String email) throws SQLException { try(Connection c=connections.openConnection(); PreparedStatement s=c.prepareStatement(sql)){s.setString(1,email);try(ResultSet r=s.executeQuery()){return r.next()?new User(r.getLong("id"),r.getString("display_name"),r.getString("email"),r.getString("role").toUpperCase()):null;}} }
}
