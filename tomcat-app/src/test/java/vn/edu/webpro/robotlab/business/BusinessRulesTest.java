package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void completionRequiresEveryRequiredComponentIdRegardlessOfQuantity() {
        // wheel yêu cầu 2 cái, nhưng session_visual_parts chỉ lưu một dòng cho mỗi
        // nhóm linh kiện — nên chỉ cần mã "wheel" có mặt là đủ, không cần đếm 2 lần.
        List<RobotComponent> required = List.of(
                component("arduino-uno", 1),
                component("wheel", 2),
                component("l298n", 1)
        );

        AssemblySession session = new AssemblySession();
        session.setStatus(AssemblySession.IN_PROGRESS);

        session.setAssembledPartIds(List.of("arduino-uno", "l298n"));
        assertFalse(session.canCompleteAssembly(required), "Còn thiếu wheel thì chưa đủ điều kiện hoàn tất");

        session.setAssembledPartIds(List.of("arduino-uno", "wheel", "l298n"));
        assertTrue(session.canCompleteAssembly(required),
                "Đủ mọi mã linh kiện bắt buộc (không cần đếm số lượng vật thể) là đủ điều kiện");

        session.setAssembledPartIds(List.of("arduino-uno", "wheel", "l298n", "battery-holder"));
        assertTrue(session.canCompleteAssembly(required), "Lắp thêm linh kiện không bắt buộc không cản trở hoàn tất");

        session.setStatus(AssemblySession.PREPARING);
        assertFalse(session.canCompleteAssembly(required), "Chưa IN_PROGRESS thì không thể hoàn tất");

        session.setStatus(AssemblySession.IN_PROGRESS);
        assertFalse(session.canCompleteAssembly(List.of()), "Robot không có linh kiện bắt buộc thì không có gì để hoàn tất");
    }

    private static RobotComponent component(String id, int quantity) {
        RobotComponent component = new RobotComponent();
        component.setComponentId(id);
        component.setQuantity(quantity);
        return component;
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

    @Test
    void quizQuestionGradesAnAnswerAgainstItsOwnOptionsOnly() {
        QuizQuestion question = new QuizQuestion();
        question.setId("line-follower-q1");
        question.setOptions(List.of(
                option("line-follower-q1-a", "Cảm biến dò line hồng ngoại", true),
                option("line-follower-q1-b", "Cảm biến siêu âm", false),
                option("line-follower-q1-c", "Cảm biến gia tốc", false)
        ));

        assertTrue(question.isCorrectOption("line-follower-q1-a"));
        assertFalse(question.isCorrectOption("line-follower-q1-b"));
        assertFalse(question.isCorrectOption("line-follower-q1-c"));
        // optionId hợp lệ của một câu hỏi KHÁC không được tính là đúng cho câu này.
        assertFalse(question.isCorrectOption("line-follower-q2-a"),
                "optionId thuộc câu hỏi khác không được tự động coi là đúng");

        assertEquals("line-follower-q1-a", question.getCorrectOption().getId());
        assertNull(question.findOption("does-not-exist"), "optionId không thuộc câu hỏi phải trả về null, không ném lỗi");
    }

    private static QuizOption option(String id, String label, boolean correct) {
        QuizOption option = new QuizOption();
        option.setId(id);
        option.setLabel(label);
        option.setCorrect(correct);
        return option;
    }

    @Test
    void sessionJsonExposesCompletedAtOnlyAfterCompletion() {
        AssemblySession session = new AssemblySession();
        session.setId(7);
        session.setCreatedAt("2026-09-23T09:00:00Z");

        assertTrue(session.toJson().contains("\"completedAt\":null"),
                "Phiên chưa hoàn tất thì completedAt phải là null, không phải chuỗi rỗng hay ngày giả");

        session.setCompletedAt("2026-09-23T09:45:00Z");
        String json = session.toJson();
        assertTrue(json.contains("\"createdAt\":\"2026-09-23T09:00:00Z\""));
        assertTrue(json.contains("\"completedAt\":\"2026-09-23T09:45:00Z\""));
    }
}
