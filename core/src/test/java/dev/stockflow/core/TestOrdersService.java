package dev.stockflow.core;

import dev.stockflow.core.exceprion.ProductException;
import dev.stockflow.core.model.Order;
import dev.stockflow.core.model.Product;
import dev.stockflow.core.state.StatusOrder;
import dev.stockflow.core.service.OrderService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestOrdersService {
    Product product_A = new Product(1L, "A-100", 2);
    Product product_B = new Product(2L, "B-200", 3);
    List<Map.Entry<String, Product>> checklist = List.of(Map.entry("A-100", product_A), Map.entry("B-200", product_B));


    @Test
    public void create_sameClientTwice_returnsAndStoresDifferentOrders() {
        OrderService orderService = new OrderService();
        Order A1 = orderService.create("C-1");
        Order A2 = orderService.create("C-1");
        List<Order> ordersCheckedList = List.of(A1, A2);

        assertEquals(true, A1.getProducts().isEmpty());
        assertEquals(true, A2.getProducts().isEmpty());
        assertEquals(StatusOrder.DRAFT, A1.getStatusOrder());
        assertEquals(StatusOrder.DRAFT, A2.getStatusOrder());
        assertEquals(false, A1.equals(A2));

        assertIterableEquals(ordersCheckedList, orderService.findByClientId("C-1"));
    }

    @Test
    public void findByClientId_mixedOrders_returnsOnlyClientOrdersInCreationOrder() {
        OrderService orderService_C1 = new OrderService();
        Order A1 = orderService_C1.create("C-1");
        Order B1 = orderService_C1.create("B-1");
        Order A2 = orderService_C1.create("C-1");
        Order A3 = orderService_C1.create("C-1");
        A2.addProduct(product_A);
        A2.confirm();
        A3.cancel();

        List<Order> ordersCheckedListA = List.of(A1, A2, A3);
        List<Order> ordersCheckedListB = List.of(B1);
        assertEquals(true, A1.getProducts().isEmpty());
        assertEquals(true, B1.getProducts().isEmpty());

        assertEquals(StatusOrder.CONFIRMED, A2.getStatusOrder());
        assertEquals(product_A, A2.getProducts().get("A-100"));
        assertEquals(StatusOrder.CANCELLED, A3.getStatusOrder());

        assertIterableEquals(ordersCheckedListA, orderService_C1.findByClientId("C-1"));
        assertIterableEquals(ordersCheckedListB, orderService_C1.findByClientId("B-1"));
        assertIterableEquals(ordersCheckedListA, orderService_C1.findByClientId("C-1"));
    }

    @Test
    public void findByClientId_noClientOrders_returnsEmptyList() {
        OrderService orderService_C1 = new OrderService();
        List<Order> checkListC1 = new ArrayList<>();
        assertIterableEquals(checkListC1, orderService_C1.findByClientId("C-1"));

        Order C2 = orderService_C1.create("C-2");
        assertIterableEquals(checkListC1, orderService_C1.findByClientId("C-1"));

        Order C1 = orderService_C1.create("C-1");
        List<Order> ordersCheckedListC1 = List.of(C1);
        assertIterableEquals(ordersCheckedListC1, orderService_C1.findByClientId("C-1"));
    }

    @Test
    public void findByClientId_clientIdVariations_ignoresOnlyOuterSpaces() {
        OrderService orderService = new OrderService();
        Order A1 = orderService.create(" C 42 ");
        List<Order> ordersCheckedListA1 = List.of(A1);
        List<Order> checkEmptyList = new ArrayList<>();

        assertIterableEquals(ordersCheckedListA1, orderService.findByClientId(new String("C 42")));
        assertIterableEquals(ordersCheckedListA1, orderService.findByClientId(" C 42 "));

        assertIterableEquals(checkEmptyList, orderService.findByClientId("c 42"));
        assertIterableEquals(checkEmptyList, orderService.findByClientId("C42"));

        Order A2 = orderService.create(" C 42 ");
        List<Order> ordersCheckedListA2 = List.of(A1, A2);
        assertIterableEquals(ordersCheckedListA2, orderService.findByClientId(" C 42 "));
    }

    @Test
    public void createAndFind_invalidClientId_throwWithoutChangingStoredOrders() {
        OrderService orderService_C1 = new OrderService();
        Order A1 = orderService_C1.create("C-1");
        Order B1 = orderService_C1.create("C-2");

        List<Order> ordersCheckedListC1 = List.of(A1);
        List<Order> ordersCheckedListC2 = List.of(B1);
        Throwable exception = assertThrows(ProductException.class, () -> {
            orderService_C1.create("  ");
        });
        assertEquals("Не указан или неверный идентификатор клиента", exception.getMessage());

        Throwable exception1 = assertThrows(ProductException.class, () -> {
            orderService_C1.findByClientId(null);
        });
        assertEquals("Не указан или неверный идентификатор клиента", exception1.getMessage());
        assertIterableEquals(ordersCheckedListC1, orderService_C1.findByClientId("C-1"));
        assertIterableEquals(ordersCheckedListC2, orderService_C1.findByClientId("C-2"));

        Throwable exception3 = assertThrows(ProductException.class, () -> {
            orderService_C1.findByClientId("");
        });
        assertEquals("Не указан или неверный идентификатор клиента", exception3.getMessage());
        assertIterableEquals(ordersCheckedListC1, orderService_C1.findByClientId("C-1"));
        assertIterableEquals(ordersCheckedListC2, orderService_C1.findByClientId("C-2"));

        Throwable exception5 = assertThrows(ProductException.class, () -> {
            orderService_C1.findByClientId("  ");
        });
        assertEquals("Не указан или неверный идентификатор клиента", exception5.getMessage());
        assertIterableEquals(ordersCheckedListC1, orderService_C1.findByClientId("C-1"));
        assertIterableEquals(ordersCheckedListC2, orderService_C1.findByClientId("C-2"));

    }

    @Test
    public void findByClientId_resultModification_throwsUnsupportedOperationException() {
        OrderService orderService_C1 = new OrderService();
        Order A1 = orderService_C1.create("C-1");
        Order A2 = orderService_C1.create("C-1");
        Order B1 = orderService_C1.create("C-2");

        List<Order> ordersCheckedListC1 = List.of(A1, A2);
        List<Order> ordersCheckedListC2 = List.of(B1);
        List<Order> checkEmptyList = new ArrayList<>();
        assertIterableEquals(ordersCheckedListC1, orderService_C1.findByClientId("C-1"));
        assertIterableEquals(checkEmptyList, orderService_C1.findByClientId("X"));
        Throwable exception = assertThrows(UnsupportedOperationException.class, () -> {
            orderService_C1.findByClientId("C-1").add(B1);
        });
        assertIterableEquals(ordersCheckedListC1, orderService_C1.findByClientId("C-1"));
        assertIterableEquals(checkEmptyList, orderService_C1.findByClientId("X"));
        Throwable exception2 = assertThrows(UnsupportedOperationException.class, () -> {
            orderService_C1.findByClientId("C-1").remove(0);
        });
        assertIterableEquals(ordersCheckedListC1, orderService_C1.findByClientId("C-1"));
        assertIterableEquals(checkEmptyList, orderService_C1.findByClientId("X"));
        Throwable exception3 = assertThrows(UnsupportedOperationException.class, () -> {
            orderService_C1.findByClientId("C-1").set(0, B1);
        });
        assertIterableEquals(ordersCheckedListC1, orderService_C1.findByClientId("C-1"));
        assertIterableEquals(checkEmptyList, orderService_C1.findByClientId("X"));
        Throwable exception4 = assertThrows(UnsupportedOperationException.class, () -> {
            orderService_C1.findByClientId("X").add(B1);
        });
        assertIterableEquals(ordersCheckedListC1, orderService_C1.findByClientId("C-1"));
        assertIterableEquals(checkEmptyList, orderService_C1.findByClientId("X"));


    }

    @Test
    public void findByClientId_afterChanges_keepsOriginalOrdersWithUpdatedStatuses() {
        OrderService orderService_C1 = new OrderService();
        Order A1 = orderService_C1.create("C-1");
        List<Order> ordersCheckedList = orderService_C1.findByClientId("C-1");
        Order A2 = orderService_C1.create("C-1");
        A1.cancel();
        List<Order> ordersCheckedListC1 = List.of(A1, A2);
        assertIterableEquals(ordersCheckedListC1, orderService_C1.findByClientId("C-1"));
        assertEquals(StatusOrder.CANCELLED, ordersCheckedList.get(0).getStatusOrder());
        assertIterableEquals(ordersCheckedList, List.of(A1));
    }

    @Test
    public void create_separateServices_storesOrdersIndependently() {
        OrderService orderService_C1 = new OrderService();
        OrderService orderService_B1 = new OrderService();
        Order A1 = orderService_C1.create("C-1");

        List<Order> ordersCheckedListA1 = List.of(A1);
        List<Order> checkEmptyList = new ArrayList<>();

        assertIterableEquals(ordersCheckedListA1, orderService_C1.findByClientId("C-1"));
        assertIterableEquals(checkEmptyList, orderService_B1.findByClientId("C-1"));

        Order A2 = orderService_B1.create("C-1");
        List<Order> ordersCheckedListA2 = List.of(A2);
        assertIterableEquals(ordersCheckedListA1, orderService_C1.findByClientId("C-1"));
        assertIterableEquals(ordersCheckedListA2, orderService_B1.findByClientId("C-1"));

    }
}

