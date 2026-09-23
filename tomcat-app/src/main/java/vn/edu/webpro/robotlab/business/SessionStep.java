package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Trạng thái một bước lắp ráp trong phiên: PENDING hoặc COMPLETED. */
public class SessionStep implements Serializable {
    public static final String PENDING = "PENDING";
    public static final String COMPLETED = "COMPLETED";

    private String stepId;
    private String status;

    public SessionStep() {
        stepId = "";
        status = PENDING;
    }

    public SessionStep(String stepId, String status) {
        this.stepId = stepId;
        this.status = status;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static boolean isValidStatus(String status) {
        return PENDING.equals(status) || COMPLETED.equals(status);
    }

    public String toJson() {
        return "{\"stepId\":" + JsonUtil.quote(stepId) + ",\"status\":" + JsonUtil.quote(status) + "}";
    }
}
