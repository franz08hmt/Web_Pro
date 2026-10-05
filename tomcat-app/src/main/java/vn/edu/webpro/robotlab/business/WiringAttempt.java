package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.*;
import java.math.*;
import java.text.*;

/** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
public class WiringAttempt implements Serializable {
    private long id = 0;
    private long exerciseId = 0;
    private long userId = 0;
    private int version = 1;
    private String state = "DRAFT";
    private Date createdAt = null;
    private Date updatedAt = null;
    private Date submittedAt = null;
    private WiringExercise exercise = null;
    private ArrayList<WiringConnection> connections = new ArrayList<>();
    private WiringGrade grade = null;

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public WiringAttempt() {
    }

    /** Đọc id. */
    public long getId() {
        return id;
    }

    /** Gán id. */
    public void setId(long id) {
        this.id = id;
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

    /** Đọc version. */
    public int getVersion() {
        return version;
    }

    /** Gán version. */
    public void setVersion(int version) {
        this.version = version;
    }

    /** Đọc state. */
    public String getState() {
        return state;
    }

    /** Gán state. */
    public void setState(String state) {
        this.state = state;
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

    /** Đọc submittedAt. */
    public Date getSubmittedAt() {
        return submittedAt;
    }

    /** Gán submittedAt. */
    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }

    /** Đọc exercise. */
    public WiringExercise getExercise() {
        return exercise;
    }

    /** Gán exercise. */
    public void setExercise(WiringExercise exercise) {
        this.exercise = exercise;
    }

    /** Đọc connections. */
    public ArrayList<WiringConnection> getConnections() {
        return connections;
    }

    /** Gán connections. */
    public void setConnections(ArrayList<WiringConnection> connections) {
        this.connections = connections;
    }

    /** Đọc grade. */
    public WiringGrade getGrade() {
        return grade;
    }

    /** Gán grade. */
    public void setGrade(WiringGrade grade) {
        this.grade = grade;
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public boolean isDraft() {
        return "DRAFT".equals(state);
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public boolean isSubmitted() {
        return "SUBMITTED".equals(state);
    }

    /** Chặn sửa lượt đã nộp và từ chối form cũ bằng expectedVersion. */
    public void requireEditable(int expectedVersion) {
        if (!isDraft()) {
            throw new IllegalArgumentException("Lượt đã nộp, không thể sửa dây.");
        }
        if (version != expectedVersion) {
            throw new IllegalArgumentException("Bản nháp đã thay đổi ở tab khác. Hãy tải lại trước khi sửa.");
        }
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public String getStateLabel() {
        if (isDraft()) {
            return "Nháp — chưa chấm";
        }
        return "Đã nộp";
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public String getSubmittedAtDisplay() {
        return formatDate(submittedAt);
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public String getUpdatedAtDisplay() {
        return formatDate(updatedAt);
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "Chưa có dữ liệu";
        }
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return format.format(date);
    }

}
