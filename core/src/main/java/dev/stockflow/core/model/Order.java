package dev.stockflow.core.model;

import dev.stockflow.core.exceprion.ProductException;
import dev.stockflow.core.state.StatusOrder;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


public class Order {
    @Getter
    private String orderId;
    @Getter
    private String clientId;
    @Getter
    private StatusOrder statusOrder = StatusOrder.DRAFT;
    private Map<String, Product> products = new LinkedHashMap<>();

    public Order(String clientId) {
        if (Objects.isNull(clientId) || clientId.trim().isEmpty()) {
            throw new ProductException("Не указан или неверный идентификатор клиента");
        }

        this.clientId = clientId.trim();
    }

    public void addProduct(Product product) {
        this.ensureDraft();

        String productId = product.getProductId();
        if (products.containsKey(productId)) {
            Integer productQuantityInOrder = products.get(productId).getQuantity();
            Integer productQuantity = product.getQuantity();
            Product updateProduct = new Product(product.getId(), product.getProductId(), productQuantity + productQuantityInOrder);
            products.put(productId, updateProduct);
        } else {
            products.putIfAbsent(productId, product);
        }
    }

    public void confirm() {
        if (this.getStatusOrder() == StatusOrder.CANCELLED) {
            throw new IllegalStateException("Статус заказа нельзя изменить после Отмены");
        }
        if (this.getProducts().isEmpty()) {
            throw new IllegalStateException("Нельзя подтвердить пустой заказ");
        }
        if (this.getStatusOrder() == StatusOrder.DRAFT) {
            this.statusOrder = StatusOrder.CONFIRMED;
        }
    }

    public void cancel() {
        if (this.getStatusOrder() == StatusOrder.CONFIRMED) {
            throw new IllegalStateException("Статус заказа нельзя изменить после Подтверждения");
        }
        if (this.getStatusOrder() == StatusOrder.DRAFT) {
            this.statusOrder = StatusOrder.CANCELLED;
        }
    }

    public Map<String, Product> getProducts() {
        return Collections.unmodifiableMap(products);
    }

    public void deleteProduct(String productId) {
        this.ensureDraft();
        products.remove(productId);

    }

    public void setProductQuantity(String productId, Integer newQuantity) {
        this.ensureDraft();
        if (Objects.isNull(products.get(productId))) {
            throw new ProductException("Товара с таким идентификатором не найдено");
        }

        Product product = products.get(productId);

        Long oldId = product.getId();
        Product newProduct = new Product(oldId, productId, newQuantity);
        products.put(productId, newProduct);

    }

    public void ensureDraft() {
        if (this.getStatusOrder() == StatusOrder.CANCELLED) {
            throw new IllegalStateException("Заказ нельзя изменить после Отмены");
        }
        if (this.getStatusOrder() == StatusOrder.CONFIRMED) {
            throw new IllegalStateException("Заказ нельзя изменить после Подтверждения");
        }
    }
}
