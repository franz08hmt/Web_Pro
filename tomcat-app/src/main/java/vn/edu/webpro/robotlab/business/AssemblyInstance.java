package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Dữ kiện và luật lắp ráp mô phỏng do server xác thực. */
public class AssemblyInstance implements Serializable {
    private long id;
    private long sessionId;
    private String componentId = "";
    private int instanceNo = 1;
    private String label = "";

    /** Tạo bean rỗng theo quy ước môn học. */
    public AssemblyInstance() {
    }

    /** Đọc dữ kiện id. */
    public long getId() {
        return id;
    }

    /** Ghi dữ kiện id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc dữ kiện sessionId. */
    public long getSessionId() {
        return sessionId;
    }

    /** Ghi dữ kiện sessionId. */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    /** Đọc dữ kiện componentId. */
    public String getComponentId() {
        return componentId;
    }

    /** Ghi dữ kiện componentId. */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /** Đọc dữ kiện instanceNo. */
    public int getInstanceNo() {
        return instanceNo;
    }

    /** Ghi dữ kiện instanceNo. */
    public void setInstanceNo(int instanceNo) {
        this.instanceNo = instanceNo;
    }

    /** Đọc dữ kiện label. */
    public String getLabel() {
        return label;
    }

    /** Ghi dữ kiện label. */
    public void setLabel(String label) {
        this.label = label;
    }

}
