package dev.ariel.tfm.orders.repository;

import dev.ariel.tfm.orders.api.dto.Order;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Almacen en memoria de ordenes. Los datos son efimeros.
 * Conserva solo los ultimos registros para que las pruebas de carga no agoten la memoria.
 */
@ApplicationScoped
public class OrderRepository {

    private static final int MAX_ENTRIES = 10_000;

    private final Map<String, Order> orders = Collections.synchronizedMap(new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Order> eldest) {
            return size() > MAX_ENTRIES;
        }
    });

    public Order save(Order order) {
        orders.put(order.getOrderId(), order);
        return order;
    }

    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

}
