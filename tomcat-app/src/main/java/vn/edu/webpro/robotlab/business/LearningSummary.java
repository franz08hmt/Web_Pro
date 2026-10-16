package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/** Tổng hợp kết quả đã đọc của một người học; không truy vấn hoặc thay đổi dữ liệu. */
public class LearningSummary implements Serializable {
    private long totalSessions;
    private Map<String, Long> completedSessionIds = new HashMap<>();
    private Map<String, Boolean> quizAvailable = new HashMap<>();

    /** Tạo JavaBean rỗng. */
    public LearningSummary() {
    }

    /** Đọc tổng số phiên. */
    public long getTotalSessions() {
        return totalSessions;
    }

    /** Ghi tổng số phiên khi khôi phục bean. */
    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }

    /** Cộng các nhóm trạng thái đúng một lần, giữ nguyên định nghĩa số phiên của truy vấn. */
    public void summarizeSessions(Map<String, Long> counts) {
        totalSessions = 0;
        for (Long count : counts.values()) {
            totalSessions += count;
        }
    }

    /** Đọc phiên hoàn tất đã được truy vấn theo chủ sở hữu. */
    public Map<String, Long> getCompletedSessionIds() { return completedSessionIds; }
    /** Ghi dữ kiện phiên hoàn tất. */
    public void setCompletedSessionIds(Map<String, Long> values) { completedSessionIds = values; }
    /** Đọc các mẫu có câu hỏi hiện có. */
    public Map<String, Boolean> getQuizAvailable() { return quizAvailable; }
    /** Ghi dữ kiện có bài kiểm tra. */
    public void setQuizAvailable(Map<String, Boolean> values) { quizAvailable = values; }

    /** Chỉ tạo đường kết quả từ phiên COMPLETED đã lọc owner, không ghép mã từ trình duyệt. */
    public void summarizeActions(List<ProfileSessionStat> stats, List<DashboardQuizCandidate> candidates) {
        completedSessionIds.clear();
        quizAvailable.clear();
        for (ProfileSessionStat stat : stats) {
            if ("COMPLETED".equals(stat.getStatus()) && stat.getLatestSessionId() > 0) {
                completedSessionIds.put(stat.getRobotId(), stat.getLatestSessionId());
            }
        }
        for (DashboardQuizCandidate candidate : candidates) {
            if (candidate.getQuestionCount() > 0) {
                quizAvailable.put(candidate.getRobotId(), true);
            }
        }
    }
}
