package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Phân trang lịch sử nối dây đã lọc theo chủ sở hữu ở lớp dữ liệu. */
public class WiringHistoryPage implements Serializable {
    private int requestedPage = 1;
    private int totalItems;
    private int pageSize = 20;

    /** Tạo trang đầu rỗng. */
    public WiringHistoryPage() {
    }

    /** Đọc trang được yêu cầu. */
    public int getRequestedPage() { return requestedPage; }
    /** Ghi trang hợp lệ. */
    public void setRequestedPage(int value) {
        if (value < 1 || value > 100000) {
            throw new IllegalArgumentException("Trang lịch sử không hợp lệ.");
        }
        requestedPage = value;
    }
    /** Đọc tổng số lượt của chủ sở hữu. */
    public int getTotalItems() { return totalItems; }
    /** Ghi tổng số lượt. */
    public void setTotalItems(int value) { totalItems = value; }
    /** Đọc số lượt mỗi trang. */
    public int getPageSize() { return pageSize; }
    /** Ghi kích thước trang có giới hạn. */
    public void setPageSize(int value) {
        if (value < 1 || value > 100) {
            throw new IllegalArgumentException("Số lượt mỗi trang không hợp lệ.");
        }
        pageSize = value;
    }
    /** Đọc số trang, trang rỗng vẫn là trang đầu. */
    public int getTotalPages() {
        if (totalItems == 0) {
            return 1;
        }
        return (totalItems - 1) / pageSize + 1;
    }
    /** Không để URL quá trang cuối làm mất đường về lịch sử. */
    public int getPage() { return Math.min(requestedPage, getTotalPages()); }
    /** Vị trí bắt đầu dùng cho LIMIT/OFFSET của truy vấn theo owner. */
    public int getOffset() { return (getPage() - 1) * pageSize; }
    /** Còn trang trước. */
    public boolean isHasPrevious() { return getPage() > 1; }
    /** Còn trang sau. */
    public boolean isHasNext() { return getPage() < getTotalPages(); }
}
