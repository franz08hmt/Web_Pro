package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

/** Luật chuyển trạng thái hỗ trợ; không tác động điểm/sơ đồ của lượt. */
public class WiringSupportRequest implements Serializable {
    private long id;
    private long attemptId;
    private long exerciseId;
    private long userId;
    private String ownerName;
    private String exerciseTitle;
    private String state = "OPEN";
    private int version = 1;
    private Date createdAt;
    private Date updatedAt;
    private Date closedAt;
    private WiringExercise exercise;
    private ArrayList<WiringSupportMessage> messages = new ArrayList<>();

    /** Tạo JavaBean rỗng để Servlet/JDBC dựng dữ liệu. */
    public WiringSupportRequest() {
    }

    /** Đọc id. */
    public long getId() {
        return id;
    }

    /** Gán id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc attemptId. */
    public long getAttemptId() {
        return attemptId;
    }

    /** Gán attemptId. */
    public void setAttemptId(long attemptId) {
        this.attemptId = attemptId;
    }

    /** Đọc exerciseId. */
    public long getExerciseId() {
        return exerciseId;
    }

    /** Gán exerciseId. */
    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    /** Đọc userId. */
    public long getUserId() {
        return userId;
    }

    /** Gán userId. */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /** Đọc ownerName. */
    public String getOwnerName() {
        return ownerName;
    }

    /** Gán ownerName. */
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    /** Đọc exerciseTitle. */
    public String getExerciseTitle() {
        return exerciseTitle;
    }

    /** Gán exerciseTitle. */
    public void setExerciseTitle(String exerciseTitle) {
        this.exerciseTitle = exerciseTitle;
    }

    /** Đọc state. */
    public String getState() {
        return state;
    }

    /** Gán state. */
    public void setState(String state) {
        this.state = state;
    }

    /** Đọc version. */
    public int getVersion() {
        return version;
    }

    /** Gán version. */
    public void setVersion(int version) {
        this.version = version;
    }

    /** Đọc createdAt. */
    public Date getCreatedAt() {
        return createdAt;
    }

    /** Gán createdAt. */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /** Đọc updatedAt. */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /** Gán updatedAt. */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** Đọc closedAt. */
    public Date getClosedAt() {
        return closedAt;
    }

    /** Gán closedAt. */
    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    /** Đọc exercise. */
    public WiringExercise getExercise() {
        return exercise;
    }

    /** Gán exercise. */
    public void setExercise(WiringExercise exercise) {
        this.exercise = exercise;
    }

    /** Đọc messages. */
    public ArrayList<WiringSupportMessage> getMessages() {
        return messages;
    }

    /** Gán messages. */
    public void setMessages(ArrayList<WiringSupportMessage> messages) {
        this.messages = messages;
    }


    /** Hỗ trợ đang cần Admin phản hồi. */
    public boolean isOpen() {
        return "OPEN".equals(state);
    }

    /** Admin đã phản hồi, User có thể gửi cập nhật. */
    public boolean isAnswered() {
        return "ANSWERED".equals(state);
    }

    /** Hỗ trợ đã kết thúc, không nhận tin tiếp. */
    public boolean isClosed() {
        return "CLOSED".equals(state);
    }

    /** Nhãn hỗ trợ không khẳng định kết quả bài nối dây. */
    public String getStateLabel() {
        if (isOpen()) {
            return "Cần Admin phản hồi";
        } else if (isAnswered()) {
            return "Admin đã phản hồi";
        }
        return "Đã kết thúc hỗ trợ";
    }

    /** Ngăn form cũ và tin nhắn vào yêu cầu đã đóng. */
    public void requireCurrent(int expectedVersion) {
        if (isClosed()) {
            throw new IllegalArgumentException("Hỗ trợ đã đóng; hãy tạo yêu cầu mới nếu cần.");
        }
        if (version != expectedVersion) {
            throw new IllegalArgumentException("Có cập nhật mới; tải lại trang trước khi phản hồi hoặc đóng.");
        }
    }

    /** Mỗi tin mới đổi trạng thái theo vai trò và tăng phiên bản. */
    public void applyMessage(boolean admin, int expectedVersion) {
        requireCurrent(expectedVersion);
        if (admin) {
            state = "ANSWERED";
        } else {
            state = "OPEN";
        }
        version++;
        updatedAt = new Date();
    }

    /** User giải quyết hoặc Admin đóng chỉ kết thúc hỗ trợ. */
    public void close(int expectedVersion) {
        requireCurrent(expectedVersion);
        state = "CLOSED";
        version++;
        closedAt = new Date();
        updatedAt = closedAt;
    }

}
