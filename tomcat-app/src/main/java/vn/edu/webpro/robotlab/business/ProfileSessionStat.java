package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Date;

/** Dữ kiện phiên đã gom theo robot và trạng thái để dựng hồ sơ cá nhân. */
public class ProfileSessionStat implements Serializable {
    private String robotId;
    private String status;
    private long sessionCount;
    private long latestSessionId;
    private Date latestEventAt;

    public ProfileSessionStat() {
        robotId = "";
        status = "";
        latestEventAt = null;
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

    public Date getLatestEventAt() {
        return latestEventAt;
    }

    public void setLatestEventAt(Date latestEventAt) {
        this.latestEventAt = latestEventAt;
    }
}
