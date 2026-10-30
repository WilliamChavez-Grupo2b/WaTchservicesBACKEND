package com.watchservices.backend.service;

import com.watchservices.backend.proxy.MicroserviceProxy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component("inventoryServiceReal")
public class InventoryService implements MicroserviceProxy<Object> {

    private final Random random = new Random();

    // Simulated in-memory inventory
    private final Map<String, Integer> stock = new HashMap<>(Map.of(
            "SKU-001", 100,
            "SKU-002", 50,
            "SKU-003", 200
    ));

    @Override
    public Object execute(String operation, Object... params) {
        return switch (operation) {
            case "checkStock" -> {
                String sku = params.length > 0 ? params[0].toString() : "SKU-001";
                simulateLatency(10, 80);
                int qty = stock.getOrDefault(sku, 0);
                yield Map.of("sku", sku, "quantity", qty, "available", qty > 0);
            }
            case "updateStock" -> {
                String sku = params.length > 0 ? params[0].toString() : "SKU-001";
                int delta = params.length > 1 ? Integer.parseInt(params[1].toString()) : -1;
                simulateLatency(20, 100);
                stock.merge(sku, delta, Integer::sum);
                yield Map.of("sku", sku, "newQuantity", stock.get(sku), "updated", true);
            }
            case "listProducts" -> {
                simulateLatency(30, 150);
                yield Map.of("products", stock.entrySet().stream()
                        .map(e -> Map.of("sku", e.getKey(), "quantity", e.getValue()))
                        .toList());
            }
            case "reserveItem" -> {
                String sku = params.length > 0 ? params[0].toString() : "SKU-001";
                simulateLatency(15, 90);
                int current = stock.getOrDefault(sku, 0);
                if (current <= 0) throw new IllegalStateException("Out of stock: " + sku);
                stock.put(sku, current - 1);
                yield Map.of("sku", sku, "reserved", true, "remaining", current - 1);
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
