package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

/** Khóa kết quả và null behavior trước khi thay cú pháp ngoài phạm vi môn học. */
class CourseSyntaxContractTest {
    @Test
    void emptyAndFractionalOrdersKeepBigDecimalValueAndScale() {
        ShopOrder order = new ShopOrder();
        assertEquals(BigDecimal.ZERO, order.calculateTotal());
        OrderItem first = new OrderItem();
        first.setUnitPriceVnd(new BigDecimal("0.10"));
        first.setQuantity(3);
        OrderItem second = new OrderItem();
        second.setUnitPriceVnd(new BigDecimal("0.20"));
        second.setQuantity(2);
        OrderItem missingPrice = new OrderItem();
        missingPrice.setUnitPriceVnd(null);
        order.getItems().add(first);
        order.getItems().add(second);
        order.getItems().add(missingPrice);
        assertEquals(new BigDecimal("0.70"), order.calculateTotal());
    }

    @Test
    void nullOrderItemStillFailsInsteadOfSilentlyDroppingIt() {
        ShopOrder order = new ShopOrder();
        order.getItems().add(null);
        boolean failed = false;
        try {
            order.calculateTotal();
        } catch (NullPointerException expected) {
            failed = true;
        }
        assertTrue(failed);
    }

    @Test
    void catalogMembershipKeepsCaseSensitivityAndNullRejection() {
        for (String level : new String[] {"Cơ bản", "Trung bình", "Nâng cao"}) {
            assertTrue(Robot.isValidLevel(level));
        }
        assertFalse(Robot.isValidLevel("cơ bản"));
        assertFalse(Robot.isValidLevel(""));
        for (String type : new String[] {"image", "document", "link"}) {
            assertTrue(LibraryResource.isValidType(type));
        }
        assertFalse(LibraryResource.isValidType("IMAGE"));
        assertFalse(LibraryResource.isValidType("video"));
        boolean robotFailed = false;
        boolean resourceFailed = false;
        try {
            Robot.isValidLevel(null);
        } catch (NullPointerException expected) {
            robotFailed = true;
        }
        try {
            LibraryResource.isValidType(null);
        } catch (NullPointerException expected) {
            resourceFailed = true;
        }
        assertTrue(robotFailed);
        assertTrue(resourceFailed);
    }

    @Test
    void sessionAggregationKeepsEmptyMultipleAndLargeCounts() {
        LearningSummary summary = new LearningSummary();
        HashMap<String, Long> counts = new HashMap<>();
        summary.summarizeSessions(counts);
        assertEquals(0, summary.getTotalSessions());
        counts.put("PREPARING", 4L);
        counts.put("IN_PROGRESS", 9L);
        counts.put("COMPLETED", 3000000000L);
        summary.summarizeSessions(counts);
        assertEquals(3000000013L, summary.getTotalSessions());
    }
}
