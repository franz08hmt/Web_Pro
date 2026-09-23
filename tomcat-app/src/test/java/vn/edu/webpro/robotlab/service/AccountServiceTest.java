package vn.edu.webpro.robotlab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import vn.edu.webpro.robotlab.dao.UserDao;
import vn.edu.webpro.robotlab.model.User;
import vn.edu.webpro.robotlab.web.Json;

final class AccountServiceTest {
    @Test
    void aNewAccountIsAlwaysAUserAndCannotChooseItsRole() throws SQLException {
        MemoryUsers users = new MemoryUsers();
        AuthService service = new AuthService(users, new PasswordService());

        User created = service.register("  Nguyễn An  ", "AN@example.com", "MatKhau123!");

        assertEquals("Nguyễn An", created.fullName());
        assertEquals("an@example.com", created.email());
        assertEquals("USER", created.role());
        assertThrows(IllegalStateException.class,
                () -> service.register("Nguyễn An", "an@example.com", "MatKhau123!"));
    }

    @Test
    void anAccountMayEditOnlyItsNameAndMustKnowItsPasswordToChangeIt() throws SQLException {
        MemoryUsers users = new MemoryUsers();
        AuthService service = new AuthService(users, new PasswordService());
        User created = service.register("Nguyễn An", "an@example.com", "MatKhau123!");

        User updated = service.updateProfile(created.id(), "  Nguyễn An Bình  ");
        assertEquals("Nguyễn An Bình", updated.fullName());
        assertEquals("USER", updated.role());
        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword(created.id(), "sai-mat-khau", "MatKhauMoi123!"));

        service.changePassword(created.id(), "MatKhau123!", "MatKhauMoi123!");
        assertEquals(1, users.findById(created.id()).sessionVersion());
        assertThrows(IllegalArgumentException.class,
                () -> service.login("an@example.com", "MatKhau123!"));
        assertEquals(created.id(), service.login("an@example.com", "MatKhauMoi123!").id());
    }

    @Test
    void onlyAnAdminCanChangeAnotherAccountRoleAndSelfDemotionIsRejected() throws SQLException {
        MemoryUsers users = new MemoryUsers();
        UserManagementService service = new UserManagementService(users);
        users.add(new User(1, "Quản trị", "admin@example.com", "ADMIN", "2026-09-23T00:00:00Z", 0));
        users.add(new User(2, "Thành viên", "member@example.com", "USER", "2026-09-23T00:00:00Z", 0));

        assertThrows(SecurityException.class, () -> service.changeRole(2, 1, "USER"));
        assertThrows(SecurityException.class, () -> service.page(2, 20, 0));
        assertEquals(2, service.page(1, 20, 0).total());
        assertThrows(IllegalArgumentException.class, () -> service.changeRole(1, 1, "USER"));
        assertThrows(IllegalArgumentException.class, () -> service.changeRole(1, 2, "OWNER"));
        User promoted = service.changeRole(1, 2, "ADMIN");
        assertEquals("ADMIN", promoted.role());
        assertEquals(1, promoted.sessionVersion());
        assertEquals(1, service.changeRole(1, 2, "ADMIN").sessionVersion());
    }

    @Test
    void sessionsBecomeStaleWhenPasswordOrRoleVersionChanges() {
        User original = new User(5, "An", "an@example.com", "USER", "2026-09-23T00:00:00Z", 0);
        User changed = new User(5, "An", "an@example.com", "ADMIN", "2026-09-23T00:00:00Z", 1);
        assertTrue(AccountSession.isCurrent(original, original));
        assertFalse(AccountSession.isCurrent(original, changed));
        assertFalse(AccountSession.isCurrent(original, null));
        assertFalse(original.toJson().contains("sessionVersion"));
        assertTrue(original.toJson().contains("createdAt"));
    }

    @Test
    void oversizedAccountPayloadIsRejected() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> Json.readBody(new StringReader("x".repeat(8193)), 8192));
    }

    private static final class MemoryUsers extends UserDao {
        private final Map<Long, User> byId = new HashMap<>();
        private final Map<Long, String> hashes = new HashMap<>();
        private long nextId = 1;

        MemoryUsers() { super(null); }
        void add(User user) { byId.put(user.id(), user); nextId = Math.max(nextId, user.id() + 1); }
        @Override public User findById(long id) { return byId.get(id); }
        @Override public User findByEmail(String email) {
            return byId.values().stream().filter(user -> user.email().equals(email)).findFirst().orElse(null);
        }
        @Override public String passwordHash(String email) {
            User user = findByEmail(email);
            return user == null ? null : hashes.get(user.id());
        }
        @Override public String passwordHashById(long id) { return hashes.get(id); }
        @Override public User create(String name, String email, String hash) {
            User user = new User(nextId++, name, email, "USER", "2026-09-23T00:00:00Z", 0);
            add(user); hashes.put(user.id(), hash); return user;
        }
        @Override public User updateName(long id, String name) {
            User old = byId.get(id);
            User updated = new User(id, name, old.email(), old.role(), old.createdAt(), old.sessionVersion());
            add(updated); return updated;
        }
        @Override public void updatePasswordHash(long id, String hash) {
            hashes.put(id, hash);
            User old = byId.get(id);
            add(new User(id, old.fullName(), old.email(), old.role(), old.createdAt(), old.sessionVersion() + 1));
        }
        @Override public User updateRole(long id, String role) {
            User old = byId.get(id);
            User updated = new User(id, old.fullName(), old.email(), role, old.createdAt(), old.sessionVersion() + 1);
            add(updated); return updated;
        }
        @Override public java.util.List<User> list(int limit, int offset) {
            return byId.values().stream().sorted(java.util.Comparator.comparingLong(User::id))
                    .skip(offset).limit(limit).toList();
        }
        @Override public long count() { return byId.size(); }
    }
}
