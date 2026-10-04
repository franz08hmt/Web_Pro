package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

final class LearningProfileTest {

    @Test
    void classifiesEveryRobotSessionStateFromTheRecordedFacts() {
        LearningProfile profile = profile(
                List.of(robot("complete"), robot("active"), robot("stopped"), robot("new")),
                List.of(
                        session("complete", "COMPLETED", 2, 13, date("2026-09-24T17:30:00Z")),
                        session("active", "PREPARING", 1, 21, date("2026-09-24T10:00:00Z")),
                        session("active", "IN_PROGRESS", 1, 22, date("2026-09-24T11:00:00Z")),
                        session("stopped", "ABANDONED", 1, 33, date("2026-09-23T10:00:00Z"))
                ), List.of(), List.of());

        profile.buildProfile();

        assertEquals("COMPLETED", entry(profile, "complete").getStatusKey());
        assertEquals("Đã hoàn thành", entry(profile, "complete").getStatusLabel());
        assertEquals(2, entry(profile, "complete").getCompletedCount());
        assertEquals(13, entry(profile, "complete").getCompletionSessionId());
        assertEquals("25/09/2026", entry(profile, "complete").getCompletionDate());
        assertEquals("IN_PROGRESS", entry(profile, "active").getStatusKey());
        assertEquals("Đang lắp ráp", entry(profile, "active").getLatestSessionStatusLabel());
        assertEquals("ABANDONED", entry(profile, "stopped").getStatusKey());
        assertEquals("Đã dừng", entry(profile, "stopped").getStatusLabel());
        assertEquals("NO_DATA", entry(profile, "new").getStatusKey());
        assertEquals("Chưa có dữ liệu", entry(profile, "new").getStatusLabel());
    }

    @Test
    void selectsBestByPercentageThenRecencyAndLatestByTimestampThenId() {
        LearningProfile profile = profile(List.of(robot("quiz")), List.of(), List.of(
                attempt("quiz", 1, 4, 5, date("2026-09-20T10:00:00Z")),
                attempt("quiz", 2, 8, 10, date("2026-09-21T10:00:00Z")),
                attempt("quiz", 3, 18, 30, date("2026-09-22T10:00:00Z")),
                attempt("quiz", 4, 0, 7, date("2026-09-23T10:00:00Z")),
                attempt("quiz", 5, 1, 3, date("2026-09-24T10:00:00Z")),
                attempt("quiz", 6, 2, 6, date("2026-09-24T10:00:00Z"))
        ), List.of());

        profile.buildProfile();
        ProfileRobotEntry result = entry(profile, "quiz");

        assertTrue(result.isQuizDataAvailable());
        assertEquals(6, result.getQuizAttemptCount());
        assertEquals(8, result.getBestQuizScore());
        assertEquals(10, result.getBestQuizTotalQuestions(), "Tỷ lệ 4/5 và 8/10 hòa; lượt mới hơn thắng");
        assertEquals(2, result.getLatestQuizScore());
        assertEquals(6, result.getLatestQuizTotalQuestions(), "Khi thời điểm bằng nhau, id lớn hơn là lượt gần nhất");
    }

    @Test
    void preservesRealZeroOutOfSevenAndSeparatesItFromNoAttempt() {
        LearningProfile profile = profile(List.of(robot("zero"), robot("none")), List.of(), List.of(
                attempt("zero", 5, 0, 7, date("2026-09-22T10:00:00Z"))
        ), List.of());

        profile.buildProfile();

        assertTrue(entry(profile, "zero").isQuizDataAvailable());
        assertEquals(0, entry(profile, "zero").getBestQuizScore());
        assertEquals(7, entry(profile, "zero").getBestQuizTotalQuestions());
        assertFalse(entry(profile, "none").isQuizDataAvailable());
        assertEquals("Chưa có dữ liệu", entry(profile, "none").getBestQuizDisplay());
    }

    @Test
    void computesAverageOfEachRobotsBestPercentageAndRoundsHalfUp() {
        LearningProfile profile = profile(List.of(robot("first"), robot("second")), List.of(), List.of(
                attempt("first", 1, 2, 3, date("2026-09-20T00:00:00Z")),
                attempt("second", 2, 1, 2, date("2026-09-20T00:00:00Z"))
        ), List.of());

        profile.buildProfile();

        assertEquals(2, profile.getQuizRobotCount());
        assertEquals(2, profile.getQuizAttemptCount());
        assertEquals(58, profile.getAverageBestScorePercent());
        assertEquals("58%", profile.getAverageBestScoreLabel());

        LearningProfile one = profile(List.of(robot("single")), List.of(), List.of(
                attempt("single", 1, 2, 3, date("2026-09-20T00:00:00Z"))
        ), List.of());
        one.buildProfile();
        assertEquals(67, one.getAverageBestScorePercent());

        LearningProfile halfUp = profile(List.of(robot("half-up")), List.of(), List.of(
                attempt("half-up", 1, 1, 8, date("2026-09-20T00:00:00Z"))
        ), List.of());
        halfUp.buildProfile();
        assertEquals(13, halfUp.getAverageBestScorePercent(), "12.5% phải làm tròn HALF_UP thành 13%");
    }

    @Test
    void formatsUtcAtTheVietnamMidnightBoundary() {
        assertEquals("25/09/2026 00:30", LearningProfile.formatDateTime(date("2026-09-24T17:30:00Z")));
        assertEquals("25/09/2026", LearningProfile.formatDate(date("2026-09-24T17:30:00Z")));
    }

    @Test
    void skillRowsKeepOriginalTextAndRequireACompletedRobotAsEvidence() {
        String exactSkills = "Kết hợp tín hiệu số và đo khoảng cách; ưu tiên sự kiện, điều khiển PWM...";
        Robot done = robot("done");
        done.setName("Robot đã làm");
        done.setSkills(exactSkills);
        Robot notDone = robot("not-done");
        notDone.setSkills("Không được thêm dòng này");
        LearningProfile profile = profile(List.of(done, notDone), List.of(
                session("done", "COMPLETED", 1, 61, date("2026-09-24T17:30:00Z"))
        ), List.of(attempt("done", 3, 5, 7, date("2026-09-24T18:00:00Z"))), List.of("Bánh xe", "Arduino Uno"));

        profile.buildProfile();

        ProfileSkill skill = profile.getSkillLines().get(0);
        assertEquals(exactSkills, skill.getContent());
        assertTrue(skill.getEvidence().contains("Hoàn thành mẫu Robot đã làm ngày 25/09/2026, phiên #61"));
        assertTrue(skill.getEvidence().contains("Kiểm tra: 5/7"));
        assertEquals("Nhận biết và chuẩn bị linh kiện", profile.getSkillLines().get(1).getTitle());
        assertEquals("Bánh xe, Arduino Uno", profile.getSkillLines().get(1).getContent());
        assertEquals(2, profile.getSkillLines().size());
    }

    @Test
    void quizOnlyProfileReportsNoCompletedRobotInsteadOfNoData() {
        LearningProfile profile = profile(List.of(robot("quiz")), List.of(), List.of(
                attempt("quiz", 1, 0, 7, date("2026-09-24T10:00:00Z"))
        ), List.of());

        profile.buildProfile();

        assertEquals("Chưa hoàn thành mẫu nào", profile.getOverallStatusLabel());
        assertEquals(1, profile.getQuizAttemptCount());
        assertEquals("0%", profile.getAverageBestScoreLabel());
        assertEquals("Chưa có dữ liệu", entry(profile, "quiz").getStatusLabel());
        assertTrue(profile.getSkillLines().isEmpty());
    }

    @Test
    void abandonedProfileReportsStoppedWithOrWithoutQuiz() {
        for (List<ProfileQuizAttempt> attempts : List.of(List.<ProfileQuizAttempt>of(), List.of(
                attempt("stopped", 1, 5, 7, date("2026-09-24T10:00:00Z"))))) {
            LearningProfile profile = profile(List.of(robot("stopped")), List.of(
                    session("stopped", "ABANDONED", 1, 33, date("2026-09-23T10:00:00Z"))
            ), attempts, List.of());

            profile.buildProfile();

            assertEquals("Đã dừng", profile.getOverallStatusLabel());
            assertEquals(0, profile.getCompletedRobotCount());
            assertTrue(profile.getSkillLines().isEmpty());
        }
    }

    @Test
    void overallStatusPrioritizesCompletionThenEveryOpenSessionState() {
        for (String openStatus : List.of("PREPARING", "READY", "IN_PROGRESS")) {
            LearningProfile profile = profile(List.of(robot("done"), robot("active"), robot("stopped")), List.of(
                    session("active", openStatus, 1, 22, date("2026-09-24T11:00:00Z")),
                    session("stopped", "ABANDONED", 1, 33, date("2026-09-23T10:00:00Z"))
            ), List.of(attempt("stopped", 1, 5, 7, date("2026-09-24T10:00:00Z"))), List.of());
            profile.buildProfile();
            assertEquals("Đang thực hiện", profile.getOverallStatusLabel());

            profile.setSessionStats(List.of(
                    session("done", "COMPLETED", 1, 13, date("2026-09-24T17:30:00Z")),
                    session("active", openStatus, 1, 22, date("2026-09-24T11:00:00Z")),
                    session("stopped", "ABANDONED", 1, 33, date("2026-09-23T10:00:00Z"))
            ));
            profile.buildProfile();
            assertEquals("Đã hoàn thành 1 mẫu", profile.getOverallStatusLabel());
        }
    }

    @Test
    void emptyProfileHasNoInventedQuizScoreOrSkillEvidence() {
        LearningProfile profile = profile(List.of(robot("empty")), List.of(), List.of(), List.of());
        profile.buildProfile();

        assertEquals("Chưa có dữ liệu", profile.getAverageBestScoreLabel());
        assertEquals("Chưa có dữ liệu", profile.getOverallStatusLabel());
        assertEquals("Chưa có dữ liệu", entry(profile, "empty").getBestQuizDisplay());
        assertEquals(0, entry(profile, "empty").getQuizAttemptCount());
        assertTrue(profile.getSkillLines().isEmpty());
        assertEquals("Chưa có dữ liệu — hoàn thành ít nhất một mẫu robot để có căn cứ ghi nhận.",
                profile.getSkillEmptyMessage());

        LearningProfile noCatalog = profile(List.of(), List.of(), List.of(), List.of());
        noCatalog.buildProfile();
        assertEquals(0, noCatalog.getRobotCount());
        assertEquals("Đã hoàn thành 0/0 mẫu robot", noCatalog.getCompletionSummary());
        assertEquals("Chưa có dữ liệu", noCatalog.getOverallStatusLabel());
    }

    @Test
    void statusGettersKeepPresentationRulesInTheBean() {
        ProfileRobotEntry result = new ProfileRobotEntry();
        assertFalse(result.isCompleted());
        assertFalse(result.isInProgress());
        assertFalse(result.isStopped());

        result.setStatusKey("COMPLETED");
        assertTrue(result.isCompleted());
        assertFalse(result.isInProgress());
        assertFalse(result.isStopped());

        result.setStatusKey("IN_PROGRESS");
        assertFalse(result.isCompleted());
        assertTrue(result.isInProgress());
        assertFalse(result.isStopped());

        result.setStatusKey("ABANDONED");
        assertFalse(result.isCompleted());
        assertFalse(result.isInProgress());
        assertTrue(result.isStopped());

        result.setStatusKey(null);
        assertFalse(result.isCompleted());
        assertFalse(result.isInProgress());
        assertFalse(result.isStopped());
    }

    @Test
    void missingDatesAreSafeAndSortBeforeRecordedDatesThenUseAttemptIdForTies() {
        assertEquals("Chưa có dữ liệu", LearningProfile.formatDate(null));
        assertEquals("Chưa có dữ liệu", LearningProfile.formatDateTime(null));
        LearningProfile result = new LearningProfile();
        result.buildProfile();
        assertEquals("Chưa có dữ liệu", result.getGeneratedAtDisplay());

        List<Robot> robots = new ArrayList<>();
        robots.add(robot("dated"));
        List<ProfileQuizAttempt> attempts = new ArrayList<>();
        attempts.add(attempt("dated", 20, 4, 7, null));
        attempts.add(attempt("dated", 10, 4, 7, date("2026-09-24T17:30:00Z")));
        result.setCatalogRobots(robots);
        result.setQuizAttempts(attempts);
        result.buildProfile();
        assertEquals("25/09/2026", entry(result, "dated").getBestQuizDate());
        assertEquals("25/09/2026", entry(result, "dated").getLatestQuizDate());

        attempts.clear();
        attempts.add(attempt("dated", 10, 4, 7, null));
        attempts.add(attempt("dated", 20, 8, 14, null));
        result.buildProfile();
        assertEquals(8, entry(result, "dated").getBestQuizScore());
        assertEquals(14, entry(result, "dated").getBestQuizTotalQuestions());
        assertEquals(8, entry(result, "dated").getLatestQuizScore());
        assertEquals("Chưa có dữ liệu", entry(result, "dated").getLatestQuizDate());
    }

    private static LearningProfile profile(List<Robot> robots, List<ProfileSessionStat> sessions,
            List<ProfileQuizAttempt> attempts, List<String> components) {
        LearningProfile profile = new LearningProfile();
        profile.setFullName("Người học");
        profile.setGeneratedAt(date("2026-09-24T17:30:00Z"));
        profile.setCatalogRobots(robots);
        profile.setSessionStats(sessions);
        profile.setQuizAttempts(attempts);
        profile.setCompletedComponentNames(components);
        return profile;
    }

    private static ProfileRobotEntry entry(LearningProfile profile, String robotId) {
        for (int i = 0; i < profile.getRobotEntries().size(); i++) {
            ProfileRobotEntry value = profile.getRobotEntries().get(i);
            if (robotId.equals(value.getRobot().getId())) {
                return value;
            }
        }
        fail("Không tìm thấy robot " + robotId);
        return null;
    }

    private static Robot robot(String id) {
        Robot robot = new Robot();
        robot.setId(id);
        robot.setName(id);
        robot.setSummary("Mô tả " + id);
        robot.setSkills("Kỹ năng " + id);
        return robot;
    }

    private static ProfileSessionStat session(String robotId, String status, int count, long latestId, Date date) {
        ProfileSessionStat result = new ProfileSessionStat();
        result.setRobotId(robotId);
        result.setStatus(status);
        result.setSessionCount(count);
        result.setLatestSessionId(latestId);
        result.setLatestEventAt(date);
        return result;
    }

    private static ProfileQuizAttempt attempt(String robotId, long id, int score, int total, Date submittedAt) {
        ProfileQuizAttempt result = new ProfileQuizAttempt();
        result.setRobotId(robotId);
        result.setAttemptId(id);
        result.setScore(score);
        result.setTotalQuestions(total);
        result.setSubmittedAt(submittedAt);
        return result;
    }

    private static Date date(String timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        format.setLenient(false);
        try {
            return format.parse(timestamp);
        } catch (ParseException exception) {
            fail("Timestamp test không hợp lệ: " + timestamp);
            return null;
        }
    }
}
