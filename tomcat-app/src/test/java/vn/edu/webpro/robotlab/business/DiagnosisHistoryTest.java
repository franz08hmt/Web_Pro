package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

class DiagnosisHistoryTest {
    @Test
    void historyUsesExactlyTheSameScoreAsTheSavedAttempt() {
        for (int done = 0; done <= 8; done++) {
            DiagnosisAttempt attempt = new DiagnosisAttempt();
            attempt.setId(27);
            attempt.setRequiredDone(done);
            attempt.setRequiredTotal(8);
            attempt.setCauseCorrect(done % 2 == 0);
            attempt.setActionCorrect(done % 3 == 0);
            DiagnosisHistoryItem item = new DiagnosisHistoryItem();
            item.useAttempt(attempt);
            assertEquals(attempt.getScoreExact(), item.getScore());
            assertEquals(attempt.getScoreDisplay(), item.getScoreDisplay());
            assertEquals(27, item.getId());
        }
    }

    @Test
    void zeroIsARealScoreButMissingScoreIsNotZero() {
        DiagnosisHistoryItem item = new DiagnosisHistoryItem();
        assertEquals("Chưa có dữ liệu", item.getScoreDisplay());
        item.setScore(BigDecimal.ZERO);
        assertEquals("0,0", item.getScoreDisplay());
        item.setScore(new BigDecimal("6.25"));
        assertEquals("6,3", item.getScoreDisplay());
    }

    @Test
    void submittedTimeUsesVietnamTimeIncludingTheNextDay() throws Exception {
        SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT);
        utc.setTimeZone(TimeZone.getTimeZone("UTC"));
        DiagnosisHistoryItem item = new DiagnosisHistoryItem();
        assertEquals("Chưa có dữ liệu", item.getSubmittedAtDisplay());
        item.setSubmittedAt(utc.parse("2026-10-11 17:30"));
        assertEquals("12/10/2026 00:30", item.getSubmittedAtDisplay());
    }

    @Test
    void invalidEvidenceIsNotSilentlyReplacedWithZero() {
        DiagnosisAttempt invalid = new DiagnosisAttempt();
        DiagnosisHistoryItem item = new DiagnosisHistoryItem();
        assertThrows(IllegalArgumentException.class, () -> item.useAttempt(invalid));
        assertThrows(IllegalArgumentException.class, () -> item.useAttempt(null));
    }

    @Test
    void pagingKeepsAtLeastOnePageAndClampsToLastPage() {
        DiagnosisHistoryPage page = new DiagnosisHistoryPage();
        assertEquals(1, page.getTotalPages());
        assertEquals(1, page.getPage());
        page.setTotalItems(21);
        page.setRequestedPage(9);
        assertEquals(3, page.getTotalPages());
        assertEquals(3, page.getPage());
        assertEquals(10, page.getPageSize());
        page.setTotalItems(20);
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void pagingRejectsUnsafeValuesAndDoesNotOverflow() {
        DiagnosisHistoryPage page = new DiagnosisHistoryPage();
        assertThrows(IllegalArgumentException.class, () -> page.setRequestedPage(0));
        assertThrows(IllegalArgumentException.class, () -> page.setRequestedPage(100001));
        assertThrows(IllegalArgumentException.class, () -> page.setPageSize(0));
        assertThrows(IllegalArgumentException.class, () -> page.setPageSize(101));
        assertThrows(IllegalArgumentException.class, () -> page.setTotalItems(-1));
        page.setTotalItems(Integer.MAX_VALUE);
        page.setRequestedPage(100000);
        assertEquals(214748365, page.getTotalPages());
        assertEquals(100000, page.getPage());
    }
}
