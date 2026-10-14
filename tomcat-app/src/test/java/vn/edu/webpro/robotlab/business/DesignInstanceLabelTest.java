package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DesignInstanceLabelTest {
    @Test
    void motorLabelUsesTheLoadedNameIncludingFutureAdminEdits() {
        DesignInstance item = new DesignInstance();
        item.setComponentId("dc-motor");
        item.setInstanceNo(1);
        item.setQuantity(2);
        item.setName("Động cơ DC TT (hộp số)");
        assertEquals("Động cơ DC TT (hộp số) 1/2", item.getLabel());
        item.setName("Tên được biên soạn lại");
        assertEquals("Tên được biên soạn lại 1/2", item.getLabel());
    }
}
