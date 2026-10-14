package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và quy tắc trình bày của DrawingItem. */
public class DrawingItem implements Serializable {
    private int number;
    private String instanceLabel = "";
    private String slotLabel = "";
    private String orientationCode = "Y0";
    private String shape = "rect";
    private double svgX;
    private double svgY;
    private double svgWidth;
    private double svgHeight;

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DrawingItem() {
    }

    /** Đọc dữ kiện number. */
    public int getNumber() {
        return number;
    }

    /** Ghi dữ kiện number. */
    public void setNumber(int number) {
        this.number = number;
    }

    /** Đọc dữ kiện instanceLabel. */
    public String getInstanceLabel() {
        return instanceLabel;
    }

    /** Ghi dữ kiện instanceLabel. */
    public void setInstanceLabel(String instanceLabel) {
        this.instanceLabel = instanceLabel;
    }

    /** Đọc dữ kiện slotLabel. */
    public String getSlotLabel() {
        return slotLabel;
    }

    /** Ghi dữ kiện slotLabel. */
    public void setSlotLabel(String slotLabel) {
        this.slotLabel = slotLabel;
    }

    /** Đọc dữ kiện orientationCode. */
    public String getOrientationCode() {
        return orientationCode;
    }

    /** Ghi dữ kiện orientationCode. */
    public void setOrientationCode(String orientationCode) {
        this.orientationCode = orientationCode;
    }

    /** Đọc dữ kiện shape. */
    public String getShape() {
        return shape;
    }

    /** Ghi dữ kiện shape. */
    public void setShape(String shape) {
        this.shape = shape;
    }

    /** Đọc dữ kiện svgX. */
    public double getSvgX() {
        return svgX;
    }

    /** Ghi dữ kiện svgX. */
    public void setSvgX(double svgX) {
        this.svgX = svgX;
    }

    /** Đọc dữ kiện svgY. */
    public double getSvgY() {
        return svgY;
    }

    /** Ghi dữ kiện svgY. */
    public void setSvgY(double svgY) {
        this.svgY = svgY;
    }

    /** Đọc dữ kiện svgWidth. */
    public double getSvgWidth() {
        return svgWidth;
    }

    /** Ghi dữ kiện svgWidth. */
    public void setSvgWidth(double svgWidth) {
        this.svgWidth = svgWidth;
    }

    /** Đọc dữ kiện svgHeight. */
    public double getSvgHeight() {
        return svgHeight;
    }

    /** Ghi dữ kiện svgHeight. */
    public void setSvgHeight(double svgHeight) {
        this.svgHeight = svgHeight;
    }

    /** Chọn thẻ SVG bằng getter, không ghép markup từ dữ liệu. */
    public boolean isCircle() {
        return "circle".equals(shape);
    }

    /** Tâm hình theo dữ kiện số của bean. */
    public double getCenterX() {
        return svgX + svgWidth / 2;
    }

    /** Tâm hình theo dữ kiện số của bean. */
    public double getCenterY() {
        return svgY + svgHeight / 2;
    }

    /** Bán kính nằm trong hộp bao SVG. */
    public double getRadius() {
        return Math.min(svgWidth, svgHeight) / 2;
    }

    /** Hướng có đơn vị, không hiển thị enum. */
    public String getOrientationLabel() {
        return orientationCode == null ? "—" : orientationCode.substring(1) + "°";
    }
}
