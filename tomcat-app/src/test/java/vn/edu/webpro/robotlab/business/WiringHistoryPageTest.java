package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/** Giới hạn trang không làm mất đường xem lịch sử cũ. */
class WiringHistoryPageTest {
    @Test
    void emptyAndTwentyOneItemsHaveAccessiblePages() {
        WiringHistoryPage page = new WiringHistoryPage();
        assertEquals(1, page.getTotalPages());
        assertEquals(0, page.getOffset());
        assertFalse(page.isHasNext());
        page.setTotalItems(21);
        assertTrue(page.isHasNext());
        page.setRequestedPage(2);
        assertEquals(20, page.getOffset());
        assertTrue(page.isHasPrevious());
        assertFalse(page.isHasNext());
    }

    @Test
    void tooHighPageClampsToLastAndInvalidInputIsRejected() {
        WiringHistoryPage page = new WiringHistoryPage();
        page.setTotalItems(41);
        page.setRequestedPage(999);
        assertEquals(3, page.getPage());
        assertEquals(40, page.getOffset());
        try {
            page.setRequestedPage(0);
            fail("Phải từ chối trang không hợp lệ");
        } catch (IllegalArgumentException expected) {
            assertEquals("Trang lịch sử không hợp lệ.", expected.getMessage());
        }
        try {
            page.setPageSize(1000);
            fail("Phải giới hạn kích thước trang");
        } catch (IllegalArgumentException expected) {
            assertEquals("Số lượt mỗi trang không hợp lệ.", expected.getMessage());
        }
    }
}
