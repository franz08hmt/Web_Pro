package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Map;

/** Tổng hợp kết quả đã đọc của một người học; không truy vấn hoặc thay đổi dữ liệu. */
public class LearningSummary implements Serializable {
    private long totalSessions;

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
}
