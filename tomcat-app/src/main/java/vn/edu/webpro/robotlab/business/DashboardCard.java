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

    /** Tạo bean rỗng. */
    public DashboardCard() {
    }

    /** Đọc và ghi dữ kiện trình bày. */
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getParameterName() { return parameterName; }
    public void setParameterName(String parameterName) { this.parameterName = parameterName; }
    public String getParameterValue() { return parameterValue; }
    public void setParameterValue(String parameterValue) { this.parameterValue = parameterValue; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
