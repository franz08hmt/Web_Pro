package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện phiên đã gom theo robot và trạng thái để dựng hồ sơ cá nhân. */
public class ProfileSessionStat implements Serializable {
    private String robotId;
    private String status;
    private long sessionCount;
    private long latestSessionId;
    private String latestEventAtUtc;

    public ProfileSessionStat() {
        robotId = "";
        status = "";
        latestEventAtUtc = "";
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(long sessionCount) {
        this.sessionCount = sessionCount;
    }

    public long getLatestSessionId() {
        return latestSessionId;
    }

    public void setLatestSessionId(long latestSessionId) {
        this.latestSessionId = latestSessionId;
    }

    public String getLatestEventAtUtc() {
        return latestEventAtUtc;
    }

    public void setLatestEventAtUtc(String latestEventAtUtc) {
        this.latestEventAtUtc = latestEventAtUtc;
    }
}
