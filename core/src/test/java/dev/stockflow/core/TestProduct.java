package dev.stockflow.core;

import dev.stockflow.core.exceprion.ProductException;
import dev.stockflow.core.model.Order;
import dev.stockflow.core.model.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestProduct{
    @Test
    public void create_normal_successful() {
        Product product = new Product(1L, "A-100", 10);
        assertEquals("A-100", product.getProductId());
        assertEquals(10, product.getQuantity());
    }

    @Test
    public void create_minQuantity_successful() {
        Product product = new Product(1L, "A-100", 1);

        assertEquals("A-100", product.getProductId());
        assertEquals(1, product.getQuantity());
    }

    @Test
    public void create_maxQuantity_successful() {
        Product product = new Product(1L, "A-100", 10000);

        assertEquals("A-100", product.getProductId());
        assertEquals(10000, product.getQuantity());
    }

    @Test
    public void create_emptyProductId_throwException() {
        Throwable exception = assertThrows(ProductException.class, () -> {
            Product product = new Product(1L, "", 10);
        });
        assertEquals("Не указан или неверный идентификатор товара", exception.getMessage());
    }

    @Test
    public void create_nullProductId_throwException() {
        Throwable exception = assertThrows(ProductException.class, () -> {
            Product product = new Product(1L, null, 10);
        });
        assertEquals("Не указан или неверный идентификатор товара", exception.getMessage());
    }

    @Test
    public void create_productIdWithSpace_successful() {
        Product product = new Product(1L, " A-100", 10);
        assertEquals("A-100", product.getProductId());
        assertEquals(10, product.getQuantity());
    }

    @Test
    public void create_productIdWithManySpace_successful() {
        Product product = new Product(1L, " A 100 ", 10);

        assertEquals("A 100", product.getProductId());
        assertEquals(10, product.getQuantity());
    }

    @Test
    public void create_quantityIsNull_throwException() {
        Throwable exception = assertThrows(ProductException.class, () -> {
            Product product = new Product(1L, "A-100", null);
        });
        assertEquals("Не указано кол-во товара", exception.getMessage());

    }

    @Test
    public void create_quantityExceedMinimum_throwException() {
        Throwable exception = assertThrows(ProductException.class, () -> {
            Product product = new Product(1L, "A-100", 0);
        });
        assertEquals("Кол-во товара вышло за допустимый диапазон: минимум 1 и максимум 10000", exception.getMessage());

    }

    @Test
    public void create_quantityExceedMaximum_throwException() {
        Throwable exception = assertThrows(ProductException.class, () -> {
            Product product = new Product(1L, "A-100", 10001);
        });
        assertEquals("Кол-во товара вышло за допустимый диапазон: минимум 1 и максимум 10000", exception.getMessage());
    }

    @Test
    public void create_QuantityNegativeValue_throwException(){
        Throwable exception = assertThrows(ProductException.class, () -> {
            Product product = new Product(1L, "A-100", -1);
        });
        assertEquals("Кол-во товара вышло за допустимый диапазон: минимум 1 и максимум 10000", exception.getMessage());
    }

    @Test
    public void create_productIdConsistingOnlyOfSpaces_throwException() {
        Throwable exception = assertThrows(ProductException.class, () -> {
            Product product = new Product(1L, "   ", 10);
        });
        assertEquals("Не указан или неверный идентификатор товара", exception.getMessage());
    }
}
