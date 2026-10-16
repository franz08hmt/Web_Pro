package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

/** Giữ nguyên phép cộng số phiên theo trạng thái, kể cả khi tổng hợp lại bean. */
class LearningSummaryTest {
    @Test
    void emptyCountsAreZero() {
        LearningSummary summary = new LearningSummary();
        summary.summarizeSessions(new HashMap<String, Long>());
        assertEquals(0, summary.getTotalSessions());
    }

    @Test
    void sumsAllStatusesAndDoesNotAccumulateOnReuse() {
        HashMap<String, Long> counts = new HashMap<>();
        counts.put("PREPARING", 2L);
        counts.put("READY", 0L);
        counts.put("IN_PROGRESS", 3L);
        counts.put("COMPLETED", 5L);
        counts.put("ABANDONED", 1L);
        LearningSummary summary = new LearningSummary();
        summary.summarizeSessions(counts);
        assertEquals(11, summary.getTotalSessions());
        summary.summarizeSessions(counts);
        assertEquals(11, summary.getTotalSessions());
    }

    @Test
    void actionsUseOnlyCompletedSessionsAndModelsWithQuestions() {
        ArrayList<ProfileSessionStat> stats = new ArrayList<>();
        ProfileSessionStat done = new ProfileSessionStat();
        done.setRobotId("first");
        done.setStatus("COMPLETED");
        done.setLatestSessionId(31);
        stats.add(done);
        ProfileSessionStat open = new ProfileSessionStat();
        open.setRobotId("second");
        open.setStatus("IN_PROGRESS");
        open.setLatestSessionId(99);
        stats.add(open);
        ArrayList<DashboardQuizCandidate> candidates = new ArrayList<>();
        DashboardQuizCandidate quiz = new DashboardQuizCandidate();
        quiz.setRobotId("first");
        quiz.setQuestionCount(7);
        candidates.add(quiz);
        DashboardQuizCandidate empty = new DashboardQuizCandidate();
        empty.setRobotId("second");
        candidates.add(empty);
        LearningSummary summary = new LearningSummary();
        summary.summarizeActions(stats, candidates);
        assertEquals(1, summary.getCompletedSessionIds().size());
        assertEquals(31L, summary.getCompletedSessionIds().get("first"));
        assertEquals(1, summary.getQuizAvailable().size());
        summary.summarizeActions(new ArrayList<ProfileSessionStat>(), new ArrayList<DashboardQuizCandidate>());
        assertEquals(0, summary.getCompletedSessionIds().size());
    }
}
