package com.watchservices.backend.service;

import com.watchservices.backend.proxy.MicroserviceProxy;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("orderServiceReal")
public class OrderService implements MicroserviceProxy<Object> {

    private final Random random = new Random();
    private final List<Map<String, Object>> orders = new ArrayList<>();

    @Override
    public Object execute(String operation, Object... params) {
        return switch (operation) {
            case "createOrder" -> {
                simulateLatency(30, 120);
                String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Map<String, Object> order = new HashMap<>(Map.of(
                        "orderId", orderId,
                        "customerId", params.length > 0 ? params[0].toString() : "CUST-001",
                        "status", "CREATED",
                        "createdAt", new Date().toString()
                ));
                orders.add(order);
                yield order;
            }
            case "getOrder" -> {
                simulateLatency(10, 60);
                String orderId = params.length > 0 ? params[0].toString() : "";
                yield orders.stream()
                        .filter(o -> orderId.equals(o.get("orderId")))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
            }
            case "listOrders" -> {
                simulateLatency(20, 80);
                yield Map.of("orders", orders, "total", orders.size());
            }
            case "cancelOrder" -> {
                simulateLatency(15, 70);
                String orderId = params.length > 0 ? params[0].toString() : "";
                orders.stream()
                        .filter(o -> orderId.equals(o.get("orderId")))
                        .findFirst()
                        .ifPresent(o -> o.put("status", "CANCELLED"));
                yield Map.of("orderId", orderId, "cancelled", true);
            }
            default -> throw new UnsupportedOperationException("Unknown operation: " + operation);
        };
    }

    private void simulateLatency(int minMs, int maxMs) {
        try {
            Thread.sleep(minMs + random.nextInt(maxMs - minMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
