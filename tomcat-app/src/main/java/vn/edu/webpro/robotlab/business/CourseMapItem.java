package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Một chủ đề môn học gắn với chức năng, request và vị trí code thực tế. */
public class CourseMapItem implements Serializable {
    private String topic = "";
    private String feature = "";
    private String path = "";
    private String sourcePath = "";
    private int sourceLine;
    private String explanation = "";
    private boolean requiresLogin;

    /** Tạo JavaBean rỗng. */
    public CourseMapItem() {
    }

    /** Đọc chủ đề. */
    public String getTopic() { return topic; }
    /** Ghi chủ đề. */
    public void setTopic(String topic) { this.topic = topic; }
    /** Đọc chức năng. */
    public String getFeature() { return feature; }
    /** Ghi chức năng. */
    public void setFeature(String feature) { this.feature = feature; }
    /** Đọc đường mở thử. */
    public String getPath() { return path; }
    /** Ghi đường mở thử. */
    public void setPath(String path) { this.path = path; }
    /** Đọc file nguồn. */
    public String getSourcePath() { return sourcePath; }
    /** Ghi file nguồn. */
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
    /** Đọc dòng nguồn đã đối chiếu. */
    public int getSourceLine() { return sourceLine; }
    /** Ghi dòng nguồn. */
    public void setSourceLine(int sourceLine) { this.sourceLine = sourceLine; }
    /** Đọc giải thích. */
    public String getExplanation() { return explanation; }
    /** Ghi giải thích. */
    public void setExplanation(String explanation) { this.explanation = explanation; }
    /** Đọc điều kiện đăng nhập. */
    public boolean isRequiresLogin() { return requiresLogin; }
    /** Ghi điều kiện đăng nhập. */
    public void setRequiresLogin(boolean requiresLogin) { this.requiresLogin = requiresLogin; }
}
