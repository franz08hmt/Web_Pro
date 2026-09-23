package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import vn.edu.webpro.robotlab.dao.UserDao;
import vn.edu.webpro.robotlab.model.User;

/** Rules for role changes; the controller still enforces HttpSession and CSRF. */
public final class UserManagementService {
    private static final Set<String> ROLES = Set.of("USER", "ADMIN");
    private final UserDao users;

    public UserManagementService(UserDao users) { this.users = users; }

    public record UserPage(List<User> items, long total) { }

    public UserPage page(long actorId, int limit, int offset) throws SQLException {
        requireAdmin(actorId);
        return new UserPage(users.list(limit, offset), users.count());
    }

    public User changeRole(long actorId, long targetId, String role) throws SQLException {
        requireAdmin(actorId);
        if (actorId == targetId || !ROLES.contains(role)) throw new IllegalArgumentException("validation");
        User target = users.findById(targetId);
        if (target == null) throw new IllegalArgumentException("not-found");
        if (target.role().equals(role)) return target;
        return users.updateRole(targetId, role);
    }

    private void requireAdmin(long actorId) throws SQLException {
        User actor = users.findById(actorId);
        if (actor == null || !"ADMIN".equals(actor.role())) throw new SecurityException("forbidden");
    }
}
