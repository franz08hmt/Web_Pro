package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
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
}
