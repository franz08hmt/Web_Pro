package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class KitItemTest {
    @Test
    void zeroRemovesButImportIsCappedAndUnsupportedIsNotHidden() {
        KitItem item = new KitItem();
        assertEquals("", item.validateQuantity(0));
        assertEquals("", item.validateQuantity(10));
        assertFalse(item.validateQuantity(11).isEmpty());
        assertFalse(item.validateQuantity(-1).isEmpty());
        assertEquals(10, item.capPurchased(100000));
        assertEquals(0, item.capPurchased(-1));
        assertEquals("Chưa hỗ trợ kiểm chứng", item.getSupportLabel());
        assertTrue(new DesignCatalog().isRenderable("arm-frame"));
        assertFalse(new DesignCatalog().isRenderable("other-part"));
    }
}
