package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ShopOrderTest {
    @Test
    void productAvailabilityRequiresActiveAndSufficientPositiveStock() {
        ShopProduct product = new ShopProduct();
        product.setActive(true);
        product.setStockQuantity(3);

        assertTrue(product.isAvailableFor(3));
        assertFalse(product.isAvailableFor(4));
        assertFalse(product.isAvailableFor(0));
        product.setActive(false);
        assertFalse(product.isAvailableFor(1));
    }

    @Test
    void orderTotalIsCalculatedExactlyFromPriceSnapshotsAndQuantities() {
        OrderItem first = new OrderItem();
        first.setUnitPriceVnd(new BigDecimal("116444"));
        first.setQuantity(2);
        OrderItem second = new OrderItem();
        second.setUnitPriceVnd(new BigDecimal("33339"));
        second.setQuantity(1);

        ShopOrder order = new ShopOrder();
        order.getItems().add(first);
        order.getItems().add(second);

        assertEquals(new BigDecimal("266227"), order.calculateTotal());
    }
}
