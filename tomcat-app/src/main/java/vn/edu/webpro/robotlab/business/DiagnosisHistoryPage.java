package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Quy tắc phân trang chỉ đọc; Servlet lấy tham số, bean giới hạn trang theo số bản ghi. */
public class DiagnosisHistoryPage implements Serializable {
    private int requestedPage;
    private int pageSize;
    private int totalItems;

    public DiagnosisHistoryPage() {
        requestedPage = 1;
        pageSize = 10;
        totalItems = 0;
    }

    public int getRequestedPage() {
        return requestedPage;
    }

    public void setRequestedPage(int value) {
        if (value < 1 || value > 100000) {
            throw new IllegalArgumentException("Trang lịch sử không hợp lệ.");
        }
        requestedPage = value;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int value) {
        if (value < 1 || value > 100) {
            throw new IllegalArgumentException("Số lượt mỗi trang không hợp lệ.");
        }
        pageSize = value;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Số lượt lịch sử không hợp lệ.");
        }
        totalItems = value;
    }

    public int getTotalPages() {
        if (totalItems == 0) {
            return 1;
        }
        return (totalItems - 1) / pageSize + 1;
    }

    public int getPage() {
        return Math.min(requestedPage, getTotalPages());
    }
}
