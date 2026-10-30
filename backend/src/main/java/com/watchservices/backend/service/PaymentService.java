package com.watchservices.backend.service;

import com.watchservices.backend.proxy.MicroserviceProxy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Component("paymentServiceReal")
public class PaymentService implements MicroserviceProxy<Object> {

    private final Random random = new Random();

    @Override
    public Object execute(String operation, Object... params) {
        // 10% random failure for PaymentService
        if (random.nextDouble() < 0.10) {
            simulateLatency(5, 30);
            throw new RuntimeException("Payment gateway timeout — transaction rejected");
        }

        return switch (operation) {
            case "processPayment" -> {
                simulateLatency(50, 200);
                String txId = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                double amount = params.length > 0 ? Double.parseDouble(params[0].toString()) : 100.0;
                yield Map.of(
                        "transactionId", txId,
                        "amount", amount,
                        "currency", "USD",
                        "status", "APPROVED",
                        "timestamp", System.currentTimeMillis()
                );
            }
            case "refundPayment" -> {
                simulateLatency(40, 180);
                String txId = params.length > 0 ? params[0].toString() : "TX-UNKNOWN";
                yield Map.of("transactionId", txId, "refunded", true, "status", "REFUNDED");
            }
            case "checkBalance" -> {
                simulateLatency(10, 60);
                String accountId = params.length > 0 ? params[0].toString() : "ACC-001";
                double balance = 500.0 + random.nextDouble() * 2000;
                yield Map.of("accountId", accountId, "balance", Math.round(balance * 100.0) / 100.0, "currency", "USD");
            }
            case "validateCard" -> {
                simulateLatency(20, 90);
                String cardLast4 = params.length > 0 ? params[0].toString() : "4242";
                yield Map.of("cardLast4", cardLast4, "valid", true, "type", "VISA");
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
