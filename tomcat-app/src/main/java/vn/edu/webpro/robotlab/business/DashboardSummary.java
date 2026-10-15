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
