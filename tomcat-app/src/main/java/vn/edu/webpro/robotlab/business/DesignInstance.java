package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và quy tắc trình bày của DesignInstance. */
public class DesignInstance implements Serializable {
    private long id;
    private long designId;
    private String componentId = "";
    private int instanceNo;
    private int quantity = 1;
    private String name = "";

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignInstance() {
    }

    /** Đọc dữ kiện id. */
    public long getId() {
        return id;
    }

    /** Ghi dữ kiện id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc dữ kiện designId. */
    public long getDesignId() {
        return designId;
    }

    /** Ghi dữ kiện designId. */
    public void setDesignId(long designId) {
        this.designId = designId;
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

    /** Đọc dữ kiện quantity. */
    public int getQuantity() {
        return quantity;
    }

    /** Ghi dữ kiện quantity. */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /** Đọc dữ kiện name. */
    public String getName() {
        return name;
    }

    /** Ghi dữ kiện name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Phân biệt từng vật thể cùng loại, không dùng ID số làm tên. */
    public String getLabel() {
        return name + " " + instanceNo + "/" + quantity;
    }

}
