package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Gom dữ liệu cá nhân thành hành động tiếp theo; không thay đổi kết quả học tập. */
public class DashboardSummary implements Serializable {
    private List<DashboardCard> cards = new ArrayList<>();

    /** Tạo JavaBean rỗng. */
    public DashboardSummary() {
    }

    /** Đọc các thẻ đã tổng hợp. */
    public List<DashboardCard> getCards() { return cards; }

    /** Ghi các thẻ khi cần khôi phục bean. */
    public void setCards(List<DashboardCard> cards) { this.cards = cards; }

    /** Thêm thẻ, luôn có trạng thái rỗng và đường đi thực. */
    public DashboardCard addCard(String title, String summary, String action, String path) {
        DashboardCard card = new DashboardCard();
        card.setTitle(title);
        if (summary != null && !summary.isEmpty()) {
            card.setSummary(summary);
        }
        card.setAction(action);
        card.setPath(path);
        cards.add(card);
        return card;
    }

    /** Chọn hành động của phiên đang làm và phiên hoàn tất; không thay đổi trạng thái phiên. */
    public void addAssemblyCards(AssemblySession open, Robot activeRobot,
            AssemblySession complete, Robot finishedRobot) {
        DashboardCard active = addCard("Đang lắp ráp", null, "Chọn mẫu để lắp", "/pages/lap-rap.html");
        if (open != null) {
            active.setSummary(open.isDetailed()
                    ? "Đã gắn " + open.getPlacedSlotCount() + "/" + open.getRequiredSlotCount() + " điểm gắn."
                    : "Chuẩn bị linh kiện: " + open.getProgressPercent() + "%.");
            if (activeRobot != null) {
                active.setSummary(activeRobot.getName() + " · " + active.getSummary());
            }
            active.setAction("Tiếp tục lắp ráp");
            active.setPath(open.isInProgress() ? "/pages/lap-rap-3d.html" : "/pages/lap-rap.html");
            active.setParameterName("session");
            active.setParameterValue(String.valueOf(open.getId()));
            active.setModel(open.getRobotId());
        }
        DashboardCard finished = addCard("Mẫu đã hoàn tất", null, "Khám phá mẫu robot", "/pages/mau-robot.html");
        if (complete != null) {
            finished.setSummary("Xem lại sản phẩm, quy trình, phiếu và bài kiểm tra của mẫu vừa hoàn tất.");
            if (finishedRobot != null) {
                finished.setSummary(finishedRobot.getName() + ". " + finished.getSummary());
            }
            finished.setAction("Xem trang hoàn thiện");
            finished.setPath("/assembly-review");
            finished.setParameterName("session");
            finished.setParameterValue(String.valueOf(complete.getId()));
        }
    }

    /** Giữ thứ tự tên mẫu từ truy vấn; chỉ gợi ý mẫu có câu hỏi và chưa có lượt làm. */
    public void addQuizCard(List<QuizAttempt> attempts, List<DashboardQuizCandidate> candidates) {
        DashboardCard quiz = addCard("Kiểm tra kiến thức", quizSummary(attempts),
                "Làm bài kiểm tra", "/pages/kiem-tra.html");
        for (DashboardQuizCandidate candidate : candidates) {
            if (candidate.getAttemptCount() == 0 && candidate.getQuestionCount() > 0) {
                quiz.setSummary(quiz.getSummary() + " Bạn đã lắp " + candidate.getRobotName()
                        + " nhưng chưa làm bài kiểm tra của mẫu này.");
                quiz.setParameterName("model");
                quiz.setParameterValue(candidate.getRobotId());
                break;
            }
        }
    }

    /** Khoảng hạn sắp tới do bean quyết định, XxxDB chỉ bind hai mốc thời gian. */
    public Date upcomingDeadline(Date now) {
        return new Date(now.getTime() + 72L * 60 * 60 * 1000);
    }

    /** Thẻ nhiệm vụ phụ thuộc vai trò thật, giữ nguyên nhãn và đích điều hướng. */
    public void addTaskCard(User user, int[] counts) {
        if (user.isAdmin()) {
            addCard("Quản lý nhiệm vụ", "Phân công và chấm nhiệm vụ học tập.",
                    "Mở quản trị nhiệm vụ", "/admin-tasks");
        } else {
            addCard("Nhiệm vụ", counts[0] + " nhiệm vụ đang mở · " + counts[1]
                    + " nhiệm vụ đến hạn trong 3 ngày tới.", "Xem nhiệm vụ", "/tasks");
        }
    }

    /** Một thiết kế gần nhất đã được giới hạn tại DB. */
    public void addDesignCard(RobotDesign recent) {
        DashboardCard design = addCard("Thiết kế gần đây", null, "Mở Xưởng thiết kế", "/designs");
        if (recent != null) {
            design.setSummary(recent.getName() + " · " + recent.getStatusLabel());
            design.setPath("/design-sheet");
            design.setAction("Xem hồ sơ thiết kế");
            design.setParameterName("id");
            design.setParameterValue(String.valueOf(recent.getId()));
        }
    }

    /** Chỉ cộng các loại đang có; số lượng 0 không trở thành một loại sở hữu. */
    public void addKitCard(List<KitItem> items) {
        int kinds = 0;
        int total = 0;
        for (KitItem item : items) {
            if (item.getQuantity() > 0) {
                kinds++;
                total += item.getQuantity();
            }
        }
        addCard("Bộ linh kiện", kinds + " loại · " + total + " vật thể đang có.", "Xem bộ linh kiện", "/my-kit");
    }

    /** Giữ trạng thái chưa có đơn khác với một đơn có nhãn kết quả. */
    public void addOrderCard(List<ShopOrder> orders) {
        addCard("Đơn hàng gần nhất", orders.isEmpty() ? null : orders.get(0).getStatusLabel(),
                "Xem đơn hàng", "/order-history");
    }

    /** Chỉ đếm nhiệm vụ đang mở và sắp đến hạn trong 72 giờ tiếp theo. */
    public String taskSummary(List<PracticeTask> tasks, Date now) {
        int open = 0;
        int soon = 0;
        for (PracticeTask task : tasks) {
            if (task.isOpen()) {
                open++;
                Date due = task.getDueAt();
                if (due != null && due.getTime() >= now.getTime()
                        && due.getTime() - now.getTime() <= 72L * 60 * 60 * 1000) {
                    soon++;
                }
            }
        }
        return open + " nhiệm vụ đang mở · " + soon + " nhiệm vụ đến hạn trong 3 ngày tới.";
    }

    /** Điểm 0 là kết quả thật, không phải trạng thái chưa làm bài. */
    public String quizSummary(List<QuizAttempt> attempts) {
        if (attempts.isEmpty()) {
            return "Chưa có lượt kiểm tra kiến thức.";
        }
        QuizAttempt latest = attempts.get(0);
        return "Lượt gần nhất: " + latest.getScore() + "/" + latest.getTotalQuestions() + " câu đúng.";
    }
}
