package vn.edu.webpro.robotlab.business;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DesignCartResultTest {
    @Test
    void fillMissingIsIdempotentAndNeverAddsBeyondStockOrCartLimit() {
        DesignCartResult result = new DesignCartResult();
        int first = result.targetQuantity(5, 1, 1, 10, 10000);
        assertEquals(4, first);
        assertEquals(first, result.targetQuantity(5, 1, first, 10, 10000));
        assertEquals(2, result.targetQuantity(5, 1, 0, 2, 10000));
        assertEquals(3, result.targetQuantity(5, 0, 0, 10, 3));
        assertEquals(0, result.targetQuantity(3, 3, 0, 10, 10000));
        assertEquals(6, result.targetQuantity(5, 1, 6, 10, 10000));
        assertEquals(0, result.targetQuantity(5, 0, 0, 0, 10000));
    }
}
