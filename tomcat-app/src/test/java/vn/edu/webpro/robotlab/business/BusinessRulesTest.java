package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Luật nghiệp vụ nằm trong business object, giống lớp Cart tự có addItem/removeItem. */
final class BusinessRulesTest {

    @Test
    void onlyAnAdminMayChangeAnotherAccountsRole() {
        User admin = new User(1, "Quản trị", "admin@example.com", User.ROLE_ADMIN, "", 0);
        User member = new User(2, "Thành viên", "member@example.com", User.ROLE_USER, "", 0);

        assertTrue(admin.canChangeRoleOf(member));
        assertFalse(admin.canChangeRoleOf(admin), "Admin không được tự đổi quyền của mình");
        assertFalse(member.canChangeRoleOf(admin), "Tài khoản USER không được đổi quyền ai");
        assertFalse(admin.canChangeRoleOf(null));

        assertTrue(User.isValidRole("USER"));
        assertTrue(User.isValidRole("ADMIN"));
        assertFalse(User.isValidRole("OWNER"));
        assertFalse(User.isValidRole("admin"), "Role phải đúng chữ hoa như tầng Java quy ước");
    }

    @Test
    void userJsonNeverExposesTheSessionVersion() {
        User user = new User(5, "An", "an@example.com", User.ROLE_USER, "2026-09-23T00:00:00Z", 3);
        String json = user.toJson();

        assertTrue(json.contains("\"id\":\"5\""));
        assertTrue(json.contains("\"createdAt\""));
        assertFalse(json.contains("sessionVersion"));
        assertFalse(json.toLowerCase().contains("password"));
    }

    @Test
    void sessionStatusMayOnlyMoveForward() {
        AssemblySession session = new AssemblySession();

        session.setStatus(AssemblySession.PREPARING);
        assertFalse(session.canChangeStatusTo(AssemblySession.IN_PROGRESS), "Chưa đủ linh kiện thì chưa được lắp");
        assertFalse(session.canChangeStatusTo(AssemblySession.COMPLETED), "Không được nhảy thẳng tới hoàn thành");
        assertTrue(session.canChangeStatusTo(AssemblySession.ABANDONED));

        session.setStatus(AssemblySession.READY);
        assertTrue(session.canChangeStatusTo(AssemblySession.IN_PROGRESS));

        session.setStatus(AssemblySession.IN_PROGRESS);
        assertTrue(session.canChangeStatusTo(AssemblySession.COMPLETED));
        assertFalse(session.canChangeStatusTo(AssemblySession.READY));

        session.setStatus(AssemblySession.COMPLETED);
        assertFalse(session.canChangeStatusTo(AssemblySession.ABANDONED), "Đã hoàn thành thì không dừng lại được");
    }

    @Test
    void progressAndReadinessAreComputedFromPreparedComponents() {
        AssemblySession session = new AssemblySession();
        session.setRequiredComponentCount(4);
        session.setComponents(List.of(
                new SessionComponent("arduino-uno", true),
                new SessionComponent("l298n", true),
                new SessionComponent("dc-motor", false)
        ));

        assertEquals(50, session.getProgressPercent());
        assertEquals(AssemblySession.PREPARING, session.getExpectedPreparationStatus());

        session.setComponents(List.of(
                new SessionComponent("arduino-uno", true),
                new SessionComponent("l298n", true),
                new SessionComponent("dc-motor", true),
                new SessionComponent("wheel", true)
        ));
        assertEquals(100, session.getProgressPercent());
        assertEquals(AssemblySession.READY, session.getExpectedPreparationStatus());
    }

    @Test
    void aRobotWithoutRequiredComponentsIsNeverReady() {
        AssemblySession session = new AssemblySession();
        session.setRequiredComponentCount(0);

        assertEquals(0, session.getProgressPercent());
        assertEquals(AssemblySession.PREPARING, session.getExpectedPreparationStatus());
    }

    @Test
    void sessionJsonCarriesTheCountsTheAccountPageDisplays() {
        AssemblySession session = new AssemblySession();
        session.setId(42);
        session.setRobotId("line-follower");
        session.setTotalStepCount(5);
        session.setUpdatedAt("2026-09-23T10:00:00Z");
        session.setSteps(List.of(
                new SessionStep("line-follower-step-1", SessionStep.COMPLETED),
                new SessionStep("line-follower-step-2", SessionStep.PENDING)
        ));

        String json = session.toJson();
        assertTrue(json.contains("\"completedStepCount\":1"));
        assertTrue(json.contains("\"totalStepCount\":5"));
        assertTrue(json.contains("\"updatedAt\":\"2026-09-23T10:00:00Z\""));
    }
}
