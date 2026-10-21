package dev.stockflow.core;

import dev.stockflow.core.exceprion.ProductException;
import dev.stockflow.core.model.Order;
import dev.stockflow.core.model.Product;
import dev.stockflow.core.state.StatusOrder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestOrders {
    Product product_A = new Product(1L, "A-100", 2);
    Product product_B = new Product(2L, "B-200", 3);
    List<Map.Entry<String, Product>> checklist = List.of(Map.entry("A-100", product_A), Map.entry("B-200", product_B));

    @Test
    public void create_normal_successful() {
        Product product = new Product(1L, "A-100", 3);
        Product product2 = new Product(2L, "A-100", 4);
        Order order = new Order("C-42");
        order.addProduct(product);
        order.addProduct(product2);
        assertEquals(7, order.getProducts().get("A-100").getQuantity());
    }

    @Test
    public void create_quantityExceedMinimum_throwException() {
        Product product = new Product(1L, "A-100", 9998);
        Product product2 = new Product(2L, "A-100", 3);
        Order order = new Order("C-42");
        order.addProduct(product);
        Throwable exception = assertThrows(ProductException.class, () -> {
            order.addProduct(product2);
        });
        assertEquals("Кол-во товара вышло за допустимый диапазон: минимум 1 и максимум 10000", exception.getMessage());
        assertEquals(9998, (order.getProducts().get("A-100").getQuantity()));
    }


    @Test
    public void create_sequentialAddition_successful() {
        Product product_A = new Product(1L, "A-100", 4);
        Product product_B = new Product(2L, "B-200", 7);
        Product product_C = new Product(3L, "C-300", 15);

        List<String> controlList = List.of("C-300", "A-100", "B-200");
        List<String> verificationlList = new ArrayList<>();


        Order order = new Order("C-42");
        order.addProduct(product_C);
        order.addProduct(product_A);
        order.addProduct(product_B);
        order.addProduct(product_A);

        for (String item : order.getProducts().keySet()) {
            verificationlList.add(item);
        }

        assertEquals(controlList, verificationlList);
    }

    @Test
    public void create_getLinkedHashMapAndModify_notChangeCollection() {
        Product product = new Product(1L, "A-100", 3);

        Order order = new Order("C-42");
        order.addProduct(product);
        Throwable exception = assertThrows(UnsupportedOperationException.class, () -> {
            order.getProducts().clear();
        });
        assertEquals(product, order.getProducts().get("A-100"));
    }

    @Test
    public void delete_normal_successful() {
        Product product_A = new Product(1L, "A-100", 10);
        Product product_B = new Product(2L, "B-100", 23);

        Order order = new Order("C-42");
        order.addProduct(product_A);
        order.addProduct(product_B);
        order.deleteProduct("A-100");

        List<String> controlList = List.of("B-100");
        List<String> verificationlList = new ArrayList<>();

        for (String item : order.getProducts().keySet()) {
            verificationlList.add(item);
        }

        assertEquals(controlList, verificationlList);
    }

    @Test
    public void delete_nonExistentProduct_missingKeyDoesNotChangeMap() {
        Product product_A = new Product(1L, "A-100", 4);
        Product product_B = new Product(2L, "B-200", 7);
        Product product_C = new Product(3L, "C-300", 15);

        List<String> controlList = List.of("A-100", "B-200", "C-300");
        List<String> verificationlList = new ArrayList<>();

        List<Integer> controQuantitylList = List.of(4, 7, 15);
        List<Integer> quantitylList = new ArrayList<>();

        Order order = new Order("C-42");
        order.addProduct(product_A);
        order.addProduct(product_B);
        order.addProduct(product_C);
        order.deleteProduct("X-100");

        for (String item : order.getProducts().keySet()) {
            verificationlList.add(item);
        }

        for (String item : order.getProducts().keySet()) {
            quantitylList.add(order.getProducts().get(item).getQuantity());
        }

        assertEquals(controlList, verificationlList);
        assertEquals(controQuantitylList, quantitylList);
    }

    @Test
    public void create_orderNullClientId_throwException() {
        Throwable exception = assertThrows(ProductException.class, () -> {
            Order order = new Order(null);
        });
        assertEquals("Не указан или неверный идентификатор клиента", exception.getMessage());
    }

    @Test
    public void create_emptyStringClientId_throwException() {
        Throwable exception = assertThrows(ProductException.class, () -> {
            Order order = new Order("");
        });
        assertEquals("Не указан или неверный идентификатор клиента", exception.getMessage());
    }

    @Test
    public void create_spacesOonlyClientId_throwException() {
        Throwable exception = assertThrows(ProductException.class, () -> {
            Order order = new Order("   ");
        });
        assertEquals("Не указан или неверный идентификатор клиента", exception.getMessage());
    }

    @Test
    public void create_independence_successful() {
        Product product = new Product(1L, "A-100", 3);
        Order order_1 = new Order("C-42");
        Order order_2 = new Order("C-42");
        assertEquals(true, order_1.getProducts().isEmpty());
        assertEquals(true, order_2.getProducts().isEmpty());

        order_1.addProduct(product);
        List<String> controlList = List.of("A-100");
        List<String> verificationlList = new ArrayList<>();

        for (String item : order_1.getProducts().keySet()) {
            verificationlList.add(item);
        }
        assertEquals(controlList, verificationlList);
        assertEquals(true, order_2.getProducts().isEmpty());

    }

    @Test
    public void create_withNnormalizationClientId_successful() {
        Product product_A = new Product(1L, "A-100", 3);
        Product product_B = new Product(2L, " A-100 ", 4);
        Order order_1 = new Order("C-42");
        order_1.addProduct(product_A);
        order_1.addProduct(product_B);

        assertEquals(7, order_1.getProducts().get("A-100").getQuantity());
        assertEquals(1, order_1.getProducts().size());
    }

    @Test
    public void create_withEnemySemanticClientId_successful() {
        Product product_A = new Product(1L, "A-100", 3);
        Product product_B = new Product(2L, "a-100", 4);
        Order order_1 = new Order("C-42");
        order_1.addProduct(product_A);
        order_1.addProduct(product_B);

        assertEquals(3, order_1.getProducts().get("A-100").getQuantity());
        assertEquals(4, order_1.getProducts().get("a-100").getQuantity());
        assertEquals(2, order_1.getProducts().size());
    }

    @Test
    public void create_maxQuantity_successful() {
        Product product_A = new Product(1L, "A-100", 9998);
        Product product_B = new Product(2L, "A-100", 2);
        Order order_1 = new Order("C-42");
        order_1.addProduct(product_A);
        order_1.addProduct(product_B);

        assertEquals(10000, order_1.getProducts().get("A-100").getQuantity());
        assertEquals(1, order_1.getProducts().size());
    }

    @Test
    public void edit_setNewQuantity_successful() {
        Product product_A = new Product(1L, "A-100", 9998);
        Order order_1 = new Order("C-42");
        order_1.addProduct(product_A);

        assertEquals(9998, order_1.getProducts().get("A-100").getQuantity());
        assertEquals(1, order_1.getProducts().size());

        order_1.setProductQuantity("A-100", 10);
        assertEquals(10, order_1.getProducts().get("A-100").getQuantity());
        assertEquals(1, order_1.getProducts().size());
    }

    @Test
    public void edit_setNewQuantity_throwException() {
        Product product_A = new Product(1L, "A-100", 9998);
        Order order_1 = new Order("C-42");
        order_1.addProduct(product_A);

        Throwable exception = assertThrows(ProductException.class, () -> {
            order_1.setProductQuantity("A-100", 10001);
        });
        assertEquals("Кол-во товара вышло за допустимый диапазон: минимум 1 и максимум 10000", exception.getMessage());

        Throwable exception2 = assertThrows(ProductException.class, () -> {
            order_1.setProductQuantity("A-100", null);
        });
        assertEquals("Не указано кол-во товара", exception2.getMessage());

        Throwable exception3 = assertThrows(ProductException.class, () -> {
            order_1.setProductQuantity("A-100", -1);
        });
        assertEquals("Кол-во товара вышло за допустимый диапазон: минимум 1 и максимум 10000", exception3.getMessage());

        Throwable exception4 = assertThrows(ProductException.class, () -> {
            order_1.setProductQuantity("A-100", 0);
        });
        assertEquals("Кол-во товара вышло за допустимый диапазон: минимум 1 и максимум 10000", exception4.getMessage());

        Throwable exception5 = assertThrows(ProductException.class, () -> {
            order_1.setProductQuantity("B-200", 10);
        });
        assertEquals("Товара с таким идентификатором не найдено", exception5.getMessage());


        assertEquals(9998, order_1.getProducts().get("A-100").getQuantity());
        assertEquals(1, order_1.getProducts().size());
    }

    @Test
    public void edit_setNewQuantitySomeProduct_successful() {
        Product product_A = new Product(1L, "A-100", 10);
        Product product_B = new Product(2L, "B-200", 20);
        Order order_1 = new Order("C-42");
        order_1.addProduct(product_A);
        order_1.addProduct(product_B);

        assertEquals(10, order_1.getProducts().get("A-100").getQuantity());
        assertEquals(20, order_1.getProducts().get("B-200").getQuantity());
        assertEquals(2, order_1.getProducts().size());

        order_1.setProductQuantity("A-100", 11);
        assertEquals(11, order_1.getProducts().get("A-100").getQuantity());
        assertEquals(20, order_1.getProducts().get("B-200").getQuantity());

        order_1.setProductQuantity("B-200", 100);
        assertEquals(100, order_1.getProducts().get("B-200").getQuantity());
        assertEquals(11, order_1.getProducts().get("A-100").getQuantity());
        assertEquals(2, order_1.getProducts().size());

        List<String> controlList = List.of("A-100", "B-200");
        List<String> verificationlList = new ArrayList<>();

        List<Long> controIdlList = List.of(1L, 2L);
        List<Long> quantitylList = new ArrayList<>();


        for (String item : order_1.getProducts().keySet()) {
            verificationlList.add(item);
        }

        for (String item : order_1.getProducts().keySet()) {
            quantitylList.add(order_1.getProducts().get(item).getId());
        }

        assertEquals(controlList, verificationlList);
        assertEquals(controIdlList, quantitylList);
    }

    @Test
    public void create_readStatusOrder_draft() {
        Product product_A = new Product(1L, "A-100", 2);
        Product product_B = new Product(2L, "B-200", 3);
        Order order = new Order("O-1");
        order.addProduct(product_A);
        order.addProduct(product_B);

        assertEquals(StatusOrder.DRAFT, order.getStatusOrder());
    }

    @Test
    public void create_editStatusConfirmed_successful() {
        Product product_A = new Product(1L, "A-100", 2);
        Product product_B = new Product(2L, "B-200", 3);
        Order order = new Order("O-1");
        order.addProduct(product_A);
        order.addProduct(product_B);
        assertEquals(StatusOrder.DRAFT, order.getStatusOrder());

        order.confirm();
        assertEquals(StatusOrder.CONFIRMED, order.getStatusOrder());

        order.confirm();
        assertEquals(StatusOrder.CONFIRMED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());
    }

    @Test
    public void edit_emptyOrderStatusConfirm_throwException() {
        Order order = new Order("O-1");
        assertEquals(StatusOrder.DRAFT, order.getStatusOrder());

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            order.confirm();
        });
        assertEquals("Нельзя подтвердить пустой заказ", exception.getMessage());
        assertEquals(StatusOrder.DRAFT, order.getStatusOrder());
        assertEquals(true, order.getProducts().isEmpty());
    }

    @Test
    public void edit_emptyOrderStatusCancel_throwException() {
        Order order = new Order("O-1");
        assertEquals(StatusOrder.DRAFT, order.getStatusOrder());
        order.cancel();
        assertEquals(StatusOrder.CANCELLED, order.getStatusOrder());
        assertEquals(true, order.getProducts().isEmpty());
    }

    @Test
    public void edit_orderStatusCancel_successful() {
        Product product_A = new Product(1L, "A-100", 2);
        Product product_B = new Product(2L, "B-200", 3);
        Order order = new Order("O-1");
        order.addProduct(product_A);
        order.addProduct(product_B);
        assertEquals(StatusOrder.DRAFT, order.getStatusOrder());
        order.cancel();
        assertEquals(StatusOrder.CANCELLED, order.getStatusOrder());
        order.cancel();
        assertEquals(StatusOrder.CANCELLED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());
    }

    @Test
    public void edit_orderConfirmToCancel_throwException() {
        Product product_A = new Product(1L, "A-100", 2);
        Product product_B = new Product(2L, "B-200", 3);
        Order order = new Order("O-1");
        order.addProduct(product_A);
        order.addProduct(product_B);
        assertEquals(StatusOrder.DRAFT, order.getStatusOrder());
        order.confirm();
        assertEquals(StatusOrder.CONFIRMED, order.getStatusOrder());

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            order.cancel();
        });
        assertEquals("Статус заказа нельзя изменить после Подтверждения", exception.getMessage());
        assertEquals(StatusOrder.CONFIRMED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());
    }

    @Test
    public void edit_orderCancelToConfirm_throwException() {
        Product product_A = new Product(1L, "A-100", 2);
        Product product_B = new Product(2L, "B-200", 3);
        Order order = new Order("O-1");
        order.addProduct(product_A);
        order.addProduct(product_B);
        assertEquals(StatusOrder.DRAFT, order.getStatusOrder());
        order.cancel();
        assertEquals(StatusOrder.CANCELLED, order.getStatusOrder());
        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            order.confirm();
        });
        assertEquals("Статус заказа нельзя изменить после Отмены", exception.getMessage());
        assertEquals(StatusOrder.CANCELLED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());
    }

    @Test
    public void edit_orderConfirm_throwException() {
        Product product_A = new Product(1L, "A-100", 2);
        Product product_B = new Product(2L, "B-200", 3);
        Product product_C = new Product(3L, "C-200", 4);
        Order order = new Order("O-1");
        order.addProduct(product_A);
        order.addProduct(product_B);
        assertEquals(StatusOrder.DRAFT, order.getStatusOrder());
        order.confirm();
        assertEquals(StatusOrder.CONFIRMED, order.getStatusOrder());

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            order.addProduct(product_C);
        });
        assertEquals("Заказ нельзя изменить после Подтверждения", exception.getMessage());
        assertEquals(StatusOrder.CONFIRMED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());

        Throwable exception2 = assertThrows(IllegalStateException.class, () -> {
            order.addProduct(product_A);
        });
        assertEquals("Заказ нельзя изменить после Подтверждения", exception2.getMessage());
        assertEquals(StatusOrder.CONFIRMED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());

        Throwable exception3 = assertThrows(IllegalStateException.class, () -> {
            order.deleteProduct("A-100");
        });
        assertEquals("Заказ нельзя изменить после Подтверждения", exception3.getMessage());
        assertEquals(StatusOrder.CONFIRMED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());

        Throwable exception4 = assertThrows(IllegalStateException.class, () -> {
            order.deleteProduct("X-100");
        });
        assertEquals("Заказ нельзя изменить после Подтверждения", exception4.getMessage());
        assertEquals(StatusOrder.CONFIRMED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());

        Throwable exception5 = assertThrows(IllegalStateException.class, () -> {
            order.setProductQuantity("A-100", 7);
        });
        assertEquals("Заказ нельзя изменить после Подтверждения", exception5.getMessage());
        assertEquals(StatusOrder.CONFIRMED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());
    }

    @Test
    public void edit_orderCancel_throwException() {
        Product product_A = new Product(1L, "A-100", 2);
        Product product_B = new Product(2L, "B-200", 3);
        Product product_C = new Product(3L, "C-200", 4);
        Order order = new Order("O-1");
        order.addProduct(product_A);
        order.addProduct(product_B);
        assertEquals(StatusOrder.DRAFT, order.getStatusOrder());
        order.cancel();
        assertEquals(StatusOrder.CANCELLED, order.getStatusOrder());

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            order.addProduct(product_C);
        });
        assertEquals("Заказ нельзя изменить после Отмены", exception.getMessage());
        assertEquals(StatusOrder.CANCELLED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());

        Throwable exception2 = assertThrows(IllegalStateException.class, () -> {
            order.addProduct(product_A);
        });
        assertEquals("Заказ нельзя изменить после Отмены", exception2.getMessage());
        assertEquals(StatusOrder.CANCELLED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());

        Throwable exception3 = assertThrows(IllegalStateException.class, () -> {
            order.deleteProduct("A-100");
        });
        assertEquals("Заказ нельзя изменить после Отмены", exception3.getMessage());
        assertEquals(StatusOrder.CANCELLED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());

        Throwable exception4 = assertThrows(IllegalStateException.class, () -> {
            order.deleteProduct("X-100");
        });
        assertEquals("Заказ нельзя изменить после Отмены", exception4.getMessage());
        assertEquals(StatusOrder.CANCELLED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());

        Throwable exception5 = assertThrows(IllegalStateException.class, () -> {
            order.setProductQuantity("A-100", 7);
        });
        assertEquals("Заказ нельзя изменить после Отмены", exception5.getMessage());
        assertEquals(StatusOrder.CANCELLED, order.getStatusOrder());
        assertIterableEquals(checklist, order.getProducts().entrySet());
    }


    @Test
    public void create_independenceOfTwOrdersAndTheirStatuses_successful() {
        Product product_A = new Product(1L, "A-100", 2);
        Product product_B = new Product(2L, "B-200", 3);

        Order order = new Order("O-1");
        order.addProduct(product_A);

        Order order2 = new Order("O-1");
        order2.addProduct(product_A);
        assertEquals(StatusOrder.DRAFT, order.getStatusOrder());
        assertEquals(StatusOrder.DRAFT, order2.getStatusOrder());
        order.confirm();
        assertEquals(StatusOrder.CONFIRMED, order.getStatusOrder());
        assertEquals(StatusOrder.DRAFT, order2.getStatusOrder());
        order2.addProduct(product_B);
        assertEquals(StatusOrder.CONFIRMED, order.getStatusOrder());
        assertEquals(StatusOrder.DRAFT, order2.getStatusOrder());

        assertIterableEquals(checklist, order2.getProducts().entrySet());
        assertIterableEquals(List.of(Map.entry("A-100", product_A)), order.getProducts().entrySet());
    }
}

