package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.dao.UserDao;
import vn.edu.webpro.robotlab.model.User;

/** Resolves the current database role instead of trusting stale HttpSession data. */
public final class AccountSession {
    private static final UserDao USERS = new UserDao(new DatabaseConnectionFactory(System.getenv()));

    private AccountSession() { }

    public static boolean isCurrent(User cached, User fresh) {
        return cached != null && fresh != null && cached.id() == fresh.id()
                && cached.sessionVersion() == fresh.sessionVersion();
    }

    public static User load(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (!(value instanceof User cached)) return null;
        User fresh = USERS.findById(cached.id());
        if (!isCurrent(cached, fresh)) {
            session.invalidate();
            return null;
        }
        session.setAttribute("user", fresh);
        return fresh;
    }
}
