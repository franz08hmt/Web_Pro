package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.*;
import java.math.*;
import java.text.*;

/** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
public class WiringConnection implements Serializable {
    private long terminalA = 0;
    private long terminalB = 0;
    private WiringTerminal first = null;
    private WiringTerminal second = null;
    private String resultLabel = "";
    private String explanation = "";
    private boolean forbidden = false;

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public WiringConnection() {
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

    /** Đọc first. */
    public WiringTerminal getFirst() {
        return first;
    }

    /** Gán first. */
    public void setFirst(WiringTerminal first) {
        this.first = first;
    }

    /** Đọc second. */
    public WiringTerminal getSecond() {
        return second;
    }

    /** Gán second. */
    public void setSecond(WiringTerminal second) {
        this.second = second;
    }

    /** Đọc resultLabel. */
    public String getResultLabel() {
        return resultLabel;
    }

    /** Gán resultLabel. */
    public void setResultLabel(String resultLabel) {
        this.resultLabel = resultLabel;
    }

    /** Đọc explanation. */
    public String getExplanation() {
        return explanation;
    }

    /** Gán explanation. */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /** Đọc forbidden. */
    public boolean isForbidden() {
        return forbidden;
    }

    /** Gán forbidden. */
    public void setForbidden(boolean forbidden) {
        this.forbidden = forbidden;
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

}
