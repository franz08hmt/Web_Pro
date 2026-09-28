package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Số phiên lắp ráp của một mẫu robot trên toàn hệ thống — dùng ở trang thống kê quản trị. */
public class RobotPopularity implements Serializable {
    private String robotId;
    private String robotName;
    private long sessionCount;

    public RobotPopularity() {
        robotId = "";
        robotName = "";
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public long getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(long sessionCount) {
        this.sessionCount = sessionCount;
    }
}
