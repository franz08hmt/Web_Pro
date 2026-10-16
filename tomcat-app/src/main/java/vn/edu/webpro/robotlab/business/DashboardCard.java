package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Một thẻ hành động; JSP tạo URL bằng c:url và c:param. */
public class DashboardCard implements Serializable {
    private String title = "";
    private String summary = "Chưa có dữ liệu.";
    private String action = "";
    private String path = "";
    private String parameterName = "";
    private String parameterValue = "";
    private String model = "";
    private boolean secondary;

    /** Tạo bean rỗng. */
    public DashboardCard() {
    }

    /** Đọc và ghi dữ kiện trình bày. */
    /** Đọc Title. */
    public String getTitle() { return title; }
    /** Ghi Title. */
    public void setTitle(String title) { this.title = title; }
    /** Đọc Summary. */
    public String getSummary() { return summary; }
    /** Ghi Summary. */
    public void setSummary(String summary) { this.summary = summary; }
    /** Đọc Action. */
    public String getAction() { return action; }
    /** Ghi Action. */
    public void setAction(String action) { this.action = action; }
    /** Đọc Path. */
    public String getPath() { return path; }
    /** Ghi Path. */
    public void setPath(String path) { this.path = path; }
    /** Đọc ParameterName. */
    public String getParameterName() { return parameterName; }
    /** Ghi ParameterName. */
    public void setParameterName(String parameterName) { this.parameterName = parameterName; }
    /** Đọc ParameterValue. */
    public String getParameterValue() { return parameterValue; }
    /** Ghi ParameterValue. */
    public void setParameterValue(String parameterValue) { this.parameterValue = parameterValue; }
    /** Đọc Model. */
    public String getModel() { return model; }
    /** Ghi Model. */
    public void setModel(String model) { this.model = model; }
    /** Cho biết thẻ thuộc nhóm vật tư và đơn hàng. */
    public boolean isSecondary() { return secondary; }
    /** Ghi nhóm trình bày của thẻ. */
    public void setSecondary(boolean secondary) { this.secondary = secondary; }
}
