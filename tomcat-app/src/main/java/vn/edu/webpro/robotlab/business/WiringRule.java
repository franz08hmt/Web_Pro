package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.*;
import java.math.*;
import java.text.*;

/** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
public class WiringRule implements Serializable {
    private long terminalA = 0;
    private long terminalB = 0;
    private String kind = "REQUIRED";
    private String explanation = "";

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public WiringRule() {
    }

    /** Đọc terminalA. */
    public long getTerminalA() {
        return terminalA;
    }

    /** Gán terminalA. */
    public void setTerminalA(long terminalA) {
        this.terminalA = terminalA;
    }

    /** Đọc terminalB. */
    public long getTerminalB() {
        return terminalB;
    }

    /** Gán terminalB. */
    public void setTerminalB(long terminalB) {
        this.terminalB = terminalB;
    }

    /** Đọc kind. */
    public String getKind() {
        return kind;
    }

    /** Gán kind. */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /** Đọc explanation. */
    public String getExplanation() {
        return explanation;
    }

    /** Gán explanation. */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /** Chuẩn hóa cặp không hướng, từ chối tự nối và mã không hợp lệ. */
    public void normalize() {
        if (terminalA <= 0 || terminalB <= 0 || terminalA == terminalB) {
            throw new IllegalArgumentException("Hai đầu nối phải tồn tại và khác nhau.");
        }
        if (terminalA > terminalB) {
            long value = terminalA;
            terminalA = terminalB;
            terminalB = value;
        }
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public String getPairKey() {
        return terminalA + ":" + terminalB;
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public boolean isRequired() {
        return "REQUIRED".equals(kind);
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public boolean isForbidden() {
        return "FORBIDDEN".equals(kind);
    }

}
