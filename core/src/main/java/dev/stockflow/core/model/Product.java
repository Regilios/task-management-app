package dev.stockflow.core.model;


import dev.stockflow.core.exceprion.ProductException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;


@Getter
@EqualsAndHashCode
//@Table
//@Entity
public class Product implements Serializable {
    private static final long serialVersionUID = -82658307871733049L;
    static final Integer minQuantity = 1;
    static final Integer maxQuantity = 10000;
    //@Id
    //@GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    // @NotBlank
    private String productId;
    // @Size(min = 1, max = 10000)
    private Integer quantity;

    public Product(Long id, String productId, Integer quantity) {
        this.id = id;

        if (Objects.isNull(productId) || productId.trim().isEmpty()) {
            throw new ProductException("Не указан или неверный идентификатор товара");
        }
        if (Objects.isNull(quantity)) {
            throw new ProductException("Не указано кол-во товара");
        }
        if (quantity > maxQuantity || quantity < minQuantity) {
            throw new ProductException(String.format("Кол-во товара вышло за допустимый диапазон: минимум %s и максимум %s", minQuantity, maxQuantity));
        }

        this.productId = productId.trim();
        this.quantity = quantity;
    }
}
