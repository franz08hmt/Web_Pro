package vn.edu.webpro.robotlab.business;

import java.util.ArrayList;
import java.util.Date;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DashboardSummaryTest {
    private DashboardQuizCandidate candidate(String id, int questions, int attempts) {
        DashboardQuizCandidate candidate = new DashboardQuizCandidate();
        candidate.setRobotId(id);
        candidate.setRobotName(id);
        candidate.setQuestionCount(questions);
        candidate.setAttemptCount(attempts);
        return candidate;
    }

    @Test
    void emptyProfileKeepsAllSevenCardsInTheOriginalOrder() {
        DashboardSummary summary = new DashboardSummary();
        summary.addAssemblyCards(null, null, null, null);
        summary.addQuizCard(new ArrayList<QuizAttempt>(), new ArrayList<DashboardQuizCandidate>());
        summary.addTaskCard(new User(), new int[] {0, 0});
        summary.addDesignCard(null);
        summary.addKitCard(new ArrayList<KitItem>());
        summary.addOrderCard(new ArrayList<ShopOrder>());
        String[] titles = {"Đang lắp ráp", "Mẫu đã hoàn tất", "Kiểm tra kiến thức", "Nhiệm vụ",
                "Thiết kế gần đây", "Bộ linh kiện", "Đơn hàng gần nhất"};
        assertEquals(titles.length, summary.getCards().size());
        for (int i = 0; i < titles.length; i++) {
            assertEquals(titles[i], summary.getCards().get(i).getTitle());
        }
        assertEquals("Chưa có dữ liệu.", summary.getCards().get(0).getSummary());
        assertEquals("Chưa có dữ liệu.", summary.getCards().get(6).getSummary());
    }

    @Test
    void preparingReadyAndInProgressHaveTheCorrectContinuationPage() {
        for (String status : new String[] {"PREPARING", "READY", "IN_PROGRESS"}) {
            DashboardSummary summary = new DashboardSummary();
            AssemblySession session = new AssemblySession();
            session.setId(9);
            session.setRobotId("robot-a");
            session.setStatus(status);
            Robot robot = new Robot();
            robot.setName("Tên robot");
            summary.addAssemblyCards(session, robot, null, null);
            DashboardCard card = summary.getCards().get(0);
            assertEquals("Tiếp tục lắp ráp", card.getAction());
            assertEquals("IN_PROGRESS".equals(status) ? "/pages/lap-rap-3d.html" : "/pages/lap-rap.html",
                    card.getPath());
            assertEquals("9", card.getParameterValue());
            assertEquals("robot-a", card.getModel());
            assertTrue(card.getSummary().startsWith("Tên robot · Chuẩn bị linh kiện:"));
        }
    }

    @Test
    void detailedProgressAndCompletedReviewKeepTheirExistingMeaning() {
        DashboardSummary summary = new DashboardSummary();
        AssemblySession open = new AssemblySession();
        open.setAssemblyMode("DETAILED");
        open.setPlacedSlotCount(1);
        open.setRequiredSlotCount(10);
        AssemblySession complete = new AssemblySession();
        complete.setStatus("COMPLETED");
        complete.setId(31);
        summary.addAssemblyCards(open, null, complete, null);
        assertEquals("Đã gắn 1/10 điểm gắn.", summary.getCards().get(0).getSummary());
        assertEquals("/assembly-review", summary.getCards().get(1).getPath());
        assertEquals("31", summary.getCards().get(1).getParameterValue());
    }

    @Test
    void onlyAnUnattemptedCompletedRobotWithQuestionsIsSuggested() {
        DashboardSummary summary = new DashboardSummary();
        ArrayList<DashboardQuizCandidate> candidates = new ArrayList<>();
        candidates.add(candidate("no-questions", 0, 0));
        candidates.add(candidate("already-attempted", 5, 1));
        candidates.add(candidate("eligible", 5, 0));
        summary.addQuizCard(new ArrayList<QuizAttempt>(), candidates);
        assertEquals("eligible", summary.getCards().get(0).getParameterValue());
        assertTrue(summary.getCards().get(0).getSummary().contains("Bạn đã lắp eligible"));
    }

    @Test
    void multipleEligibleRobotsKeepTheQueryOrderAndDoNotSuggestAnEmptyQuiz() {
        DashboardSummary summary = new DashboardSummary();
        ArrayList<DashboardQuizCandidate> candidates = new ArrayList<>();
        candidates.add(candidate("alphabetical-first", 3, 0));
        candidates.add(candidate("alphabetical-second", 4, 0));
        summary.addQuizCard(new ArrayList<QuizAttempt>(), candidates);
        assertEquals("alphabetical-first", summary.getCards().get(0).getParameterValue());
        summary = new DashboardSummary();
        candidates.clear();
        candidates.add(candidate("empty", 0, 0));
        summary.addQuizCard(new ArrayList<QuizAttempt>(), candidates);
        assertEquals("", summary.getCards().get(0).getParameterName());
    }

    @Test
    void kitTotalsIgnoreZeroQuantities() {
        DashboardSummary summary = new DashboardSummary();
        ArrayList<KitItem> items = new ArrayList<>();
        for (int quantity : new int[] {2, 0, 5}) {
            KitItem item = new KitItem();
            item.setQuantity(quantity);
            items.add(item);
        }
        summary.addKitCard(items);
        assertEquals("2 loại · 7 vật thể đang có.", summary.getCards().get(0).getSummary());
    }

    @Test
    void userAndAdminKeepTheirRoleSpecificTaskCard() {
        for (String role : new String[] {"USER", "ADMIN"}) {
            DashboardSummary summary = new DashboardSummary();
            User user = new User();
            user.setRole(role);
            summary.addTaskCard(user, new int[] {3, 1});
            assertEquals(user.isAdmin() ? "/admin-tasks" : "/tasks", summary.getCards().get(0).getPath());
            assertEquals(user.isAdmin() ? "Quản lý nhiệm vụ" : "Nhiệm vụ", summary.getCards().get(0).getTitle());
            if (!user.isAdmin()) {
                assertEquals("3 nhiệm vụ đang mở · 1 nhiệm vụ đến hạn trong 3 ngày tới.",
                        summary.getCards().get(0).getSummary());
            }
        }
    }

    @Test
    void latestDesignAndOrdersKeepTheirActionsAndUpcomingWindowIsSeventyTwoHours() {
        DashboardSummary summary = new DashboardSummary();
        RobotDesign design = new RobotDesign();
        design.setId(8);
        design.setName("Thiết kế");
        summary.addDesignCard(design);
        assertEquals("/design-sheet", summary.getCards().get(0).getPath());
        assertEquals("8", summary.getCards().get(0).getParameterValue());
        ArrayList<ShopOrder> orders = new ArrayList<>();
        ShopOrder order = new ShopOrder();
        order.setStatus("CONFIRMED");
        orders.add(order);
        summary.addOrderCard(orders);
        assertEquals(order.getStatusLabel(), summary.getCards().get(1).getSummary());
        Date now = new Date(1000);
        assertEquals(72L * 60 * 60 * 1000, summary.upcomingDeadline(now).getTime() - now.getTime());
    }

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

    @Test
    void visualGroupsKeepEveryCardAndOriginalLearningOrder() {
        DashboardSummary summary = new DashboardSummary();
        assertEquals(0, summary.getLearningCards().size());
        summary.addAssemblyCards(null, null, null, null);
        summary.addKitCard(new ArrayList<KitItem>());
        summary.addOrderCard(new ArrayList<ShopOrder>());
        assertEquals(4, summary.getCards().size());
        assertEquals(2, summary.getLearningCards().size());
        assertEquals("Đang lắp ráp", summary.getLearningCards().get(0).getTitle());
        assertEquals("Mẫu đã hoàn tất", summary.getLearningCards().get(1).getTitle());
        assertEquals(2, summary.getResourceCards().size());
        assertEquals("/my-kit", summary.getResourceCards().get(0).getPath());
        assertEquals("/order-history", summary.getResourceCards().get(1).getPath());
    }
}
