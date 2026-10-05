package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

/** Tin bất biến và bản chụp do server dựng từ lượt đã lưu. */
public class WiringSupportMessage implements Serializable {
    private long id;
    private long requestId;
    private long authorId;
    private String authorName;
    private boolean admin;
    private String content;
    private Long terminalId;
    private Date createdAt;
    private String snapshotText;
    private String snapshotPairs;
    private int snapshotVersion;
    private String snapshotState;
    private BigDecimal snapshotScore;
    private Date snapshotAt;
    private WiringExercise exercise;

    /** Tạo JavaBean rỗng để Servlet/JDBC dựng dữ liệu. */
    public WiringSupportMessage() {
    }

    /** Đọc id. */
    public long getId() {
        return id;
    }

    /** Gán id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc requestId. */
    public long getRequestId() {
        return requestId;
    }

    /** Gán requestId. */
    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    /** Đọc authorId. */
    public long getAuthorId() {
        return authorId;
    }

    /** Gán authorId. */
    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    /** Đọc authorName. */
    public String getAuthorName() {
        return authorName;
    }

    /** Gán authorName. */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /** Đọc admin. */
    public boolean isAdmin() {
        return admin;
    }

    /** Gán admin. */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    /** Đọc content. */
    public String getContent() {
        return content;
    }

    /** Gán content. */
    public void setContent(String content) {
        this.content = content;
    }

    /** Đọc terminalId. */
    public Long getTerminalId() {
        return terminalId;
    }

    /** Gán terminalId. */
    public void setTerminalId(Long terminalId) {
        this.terminalId = terminalId;
    }

    /** Đọc createdAt. */
    public Date getCreatedAt() {
        return createdAt;
    }

    /** Gán createdAt. */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /** Đọc snapshotText. */
    public String getSnapshotText() {
        return snapshotText;
    }

    /** Gán snapshotText. */
    public void setSnapshotText(String snapshotText) {
        this.snapshotText = snapshotText;
    }

    /** Đọc snapshotPairs. */
    public String getSnapshotPairs() {
        return snapshotPairs;
    }

    /** Gán snapshotPairs. */
    public void setSnapshotPairs(String snapshotPairs) {
        this.snapshotPairs = snapshotPairs;
    }

    /** Đọc snapshotVersion. */
    public int getSnapshotVersion() {
        return snapshotVersion;
    }

    /** Gán snapshotVersion. */
    public void setSnapshotVersion(int snapshotVersion) {
        this.snapshotVersion = snapshotVersion;
    }

    /** Đọc snapshotState. */
    public String getSnapshotState() {
        return snapshotState;
    }

    /** Gán snapshotState. */
    public void setSnapshotState(String snapshotState) {
        this.snapshotState = snapshotState;
    }

    /** Đọc snapshotScore. */
    public BigDecimal getSnapshotScore() {
        return snapshotScore;
    }

    /** Gán snapshotScore. */
    public void setSnapshotScore(BigDecimal snapshotScore) {
        this.snapshotScore = snapshotScore;
    }

    /** Đọc snapshotAt. */
    public Date getSnapshotAt() {
        return snapshotAt;
    }

    /** Gán snapshotAt. */
    public void setSnapshotAt(Date snapshotAt) {
        this.snapshotAt = snapshotAt;
    }

    /** Đọc exercise. */
    public WiringExercise getExercise() {
        return exercise;
    }

    /** Gán exercise. */
    public void setExercise(WiringExercise exercise) {
        this.exercise = exercise;
    }


    /** Trim và kiểm giới hạn riêng của câu hỏi/phản hồi. */
    public void validate(boolean admin) {
        if (content == null) {
            content = "";
        }
        content = content.trim();
        int minimum = 20;
        if (admin) {
            minimum = 10;
        }
        // Đếm ký tự Unicode như CHAR_LENGTH của MySQL, tránh emoji hợp lệ thành lỗi JDBC.
        int length = content.codePointCount(0, content.length());
        if (length < minimum || length > 2000) {
            throw new IllegalArgumentException("Nội dung phải có " + minimum + "–2000 ký tự sau khi bỏ khoảng trắng.");
        }
        this.admin = admin;
    }

    /** Bản chụp có dữ liệu được server ghi nhận, không do client tự khai. */
    public boolean isHasSnapshot() {
        return snapshotText != null;
    }

    /** Chụp riêng nhãn/dây/trạng thái/điểm; không giữ tham chiếu mutable của bản nguồn. */
    public void capture(WiringAttempt attempt, Date now) {
        snapshotVersion = attempt.getVersion();
        snapshotState = attempt.getState();
        snapshotAt = new Date(now.getTime());
        snapshotScore = null;
        StringBuilder text = new StringBuilder();
        text.append("Bài: ").append(attempt.getExercise().getTitle());
        text.append("\nRobot: ").append(attempt.getExercise().getRobotName());
        text.append("\nLượt #").append(attempt.getId()).append(" · phiên bản ").append(snapshotVersion);
        text.append("\nTrạng thái: ");
        if (attempt.isSubmitted()) {
            snapshotScore = attempt.getGrade().getScore();
            text.append("Đã nộp · điểm ").append(attempt.getGrade().getScoreDisplay()).append("/100");
        } else {
            text.append("Nháp · chưa chấm");
        }
        text.append("\nGửi lúc: ").append(format(snapshotAt));
        text.append("\nĐầu nối:");
        ArrayList<WiringTerminal> terminals = attempt.getExercise().getTerminals();
        for (int i = 0; i < terminals.size(); i++) {
            text.append("\n#").append(terminals.get(i).getId()).append(" ").append(terminals.get(i).getLabel());
        }
        StringBuilder pairs = new StringBuilder();
        text.append("\nDây đã lưu:");
        for (int i = 0; i < attempt.getConnections().size(); i++) {
            WiringConnection c = attempt.getConnections().get(i);
            text.append("\n").append(c.getFirst().getLabel()).append(" — ").append(c.getSecond().getLabel());
            if (pairs.length() > 0) {
                pairs.append(";");
            }
            pairs.append(c.getPairKey());
        }
        if (attempt.getConnections().isEmpty()) {
            text.append("\nChưa có dây.");
        }
        snapshotText = text.toString();
        snapshotPairs = pairs.toString();
    }

    /** Dựng SVG từ cặp server đã chụp và bài công bố bất biến, không đọc dây hiện tại. */
    public ArrayList<WiringConnection> getSnapshotConnections() {
        ArrayList<WiringConnection> list = new ArrayList<>();
        if (snapshotPairs != null && !snapshotPairs.isEmpty()) {
            String[] pairs = snapshotPairs.split(";");
            for (int i = 0; i < pairs.length; i++) {
                String[] ids = pairs[i].split(":");
                WiringConnection c = new WiringConnection();
                c.setTerminalA(Long.parseLong(ids[0]));
                c.setTerminalB(Long.parseLong(ids[1]));
                list.add(c);
            }
        }
        if (exercise != null) {
            return exercise.normalize(list);
        }
        return list;
    }

    /** Nhãn đầu nối được nhắc phải thuộc đúng bài của hỗ trợ. */
    public String getTerminalLabel() {
        if (terminalId == null || exercise == null) {
            return "Không chọn đầu nối riêng";
        }
        return exercise.terminal(terminalId).getLabel();
    }

    /** Định dạng thời điểm tin theo giờ Việt Nam. */
    public String getCreatedAtDisplay() {
        return format(createdAt);
    }

    private String format(Date date) {
        if (date == null) {
            return "Chưa có dữ liệu";
        }
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
        f.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return f.format(date);
    }

}
