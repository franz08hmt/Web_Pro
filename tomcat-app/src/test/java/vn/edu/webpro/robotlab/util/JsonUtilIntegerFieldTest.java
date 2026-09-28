package vn.edu.webpro.robotlab.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JsonUtilIntegerFieldTest {
    @Test
    void readsBareJsonIntegerAfterWhitespace() {
        assertEquals(25, JsonUtil.intField("{\"quantity\": 25, \"active\": true}", "quantity"));
    }

    @Test
    void rejectsStringsDecimalsAndOverflow() {
        assertThrows(IllegalArgumentException.class,
                () -> JsonUtil.intField("{\"quantity\": \"2\"}", "quantity"));
        assertThrows(IllegalArgumentException.class,
                () -> JsonUtil.intField("{\"quantity\": 2.5}", "quantity"));
        assertThrows(IllegalArgumentException.class,
                () -> JsonUtil.intField("{\"quantity\": 2147483648}", "quantity"));
    }
}
