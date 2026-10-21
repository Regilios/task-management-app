package dev.stockflow.core.service;

import dev.stockflow.core.exceprion.ProductException;
import dev.stockflow.core.model.Order;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrderService {
    private Map<String, List<Order>> orders = new LinkedHashMap<>();

    public Order create(String clientId) {
        Order order = new Order(clientId);
        String cID = clientId.trim();
        if (orders.containsKey(cID)) {
            List<Order> temp = new ArrayList<>();
            orders.get(cID).forEach(temp::add);
            temp.add(order);
            orders.put(cID, temp);
        } else {
            orders.putIfAbsent(cID, List.of(order));
        }

        return order;
    }

    public List<Order> findByClientId(String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new ProductException("Не указан или неверный идентификатор клиента");
        }
        String cID = clientId.trim();
        List<Order> temp = new ArrayList<>();
        if (orders.containsKey(cID)) {
            temp.addAll(orders.get(cID));
        }

        return Collections.unmodifiableList(temp);
    }
}
