package vn.edu.webpro.robotlab.business;

import java.util.ArrayList;
import java.util.Date;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DashboardSummaryTest {
    @Test
    void emptyCardsHaveAnExplicitStateAndZeroQuizRemainsAResult() {
        DashboardSummary summary = new DashboardSummary();
        assertTrue(summary.getCards().isEmpty());
        assertEquals("Chưa có dữ liệu.", summary.addCard("Mẫu", null, "Mở", "/robots").getSummary());
        ArrayList<QuizAttempt> attempts = new ArrayList<>();
        assertEquals("Chưa có lượt kiểm tra kiến thức.", summary.quizSummary(attempts));
        QuizAttempt zero = new QuizAttempt();
        zero.setTotalQuestions(5);
        attempts.add(zero);
        assertEquals("Lượt gần nhất: 0/5 câu đúng.", summary.quizSummary(attempts));
    }

    @Test
    void taskCountsExcludeClosedAndOverdueFromUpcoming() {
        DashboardSummary summary = new DashboardSummary();
        ArrayList<PracticeTask> tasks = new ArrayList<>();
        Date now = new Date(1000000);
        PracticeTask upcoming = new PracticeTask();
        upcoming.setState("OPEN");
        upcoming.setDueAt(new Date(now.getTime() + 3600000));
        tasks.add(upcoming);
        PracticeTask overdue = new PracticeTask();
        overdue.setState("OPEN");
        overdue.setDueAt(new Date(now.getTime() - 1));
        tasks.add(overdue);
        PracticeTask closed = new PracticeTask();
        closed.setState("CLOSED");
        tasks.add(closed);
        assertEquals("2 nhiệm vụ đang mở · 1 nhiệm vụ đến hạn trong 3 ngày tới.",
                summary.taskSummary(tasks, now));
    }
}
