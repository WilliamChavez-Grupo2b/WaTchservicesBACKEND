package com.watchservices.backend.controller;

import com.watchservices.backend.dto.MetricsSummary;
import com.watchservices.backend.dto.ServiceRequest;
import com.watchservices.backend.model.ServiceLog;
import com.watchservices.backend.proxy.MicroserviceProxy;
import com.watchservices.backend.repository.ServiceLogRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final ServiceLogRepository logRepository;
    private final MicroserviceProxy<Object> inventoryProxy;
    private final MicroserviceProxy<Object> orderProxy;
    private final MicroserviceProxy<Object> paymentProxy;

    private static final List<String> ALL_OPERATIONS = List.of(
            "checkStock", "updateStock", "listProducts", "reserveItem",
            "createOrder", "getOrder", "listOrders", "cancelOrder",
            "processPayment", "refundPayment", "checkBalance", "validateCard"
    );

    private static final Map<String, MicroserviceProxy<Object>> SERVICE_MAP = new HashMap<>();

    public MetricsController(
            ServiceLogRepository logRepository,
            @Qualifier("inventoryProxy") MicroserviceProxy<Object> inventoryProxy,
            @Qualifier("orderProxy") MicroserviceProxy<Object> orderProxy,
            @Qualifier("paymentProxy") MicroserviceProxy<Object> paymentProxy) {
        this.logRepository = logRepository;
        this.inventoryProxy = inventoryProxy;
        this.orderProxy = orderProxy;
        this.paymentProxy = paymentProxy;
    }

    /** GET /api/metrics/summary — total calls, error rate, avg response per service */
    @GetMapping("/summary")
    public ResponseEntity<List<MetricsSummary>> getSummary() {
        List<Object[]> raw = logRepository.findMetricsSummary();
        List<MetricsSummary> summaries = raw.stream().map(row -> {
            String serviceId = (String) row[0];
            long total = (Long) row[1];
            long errors = (Long) row[2];
            double avg = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            double errorRate = total > 0 ? (double) errors / total * 100.0 : 0.0;
            return new MetricsSummary(serviceId, total, errors,
                    Math.round(errorRate * 100.0) / 100.0,
                    Math.round(avg * 100.0) / 100.0);
        }).toList();
        return ResponseEntity.ok(summaries);
    }

    /** GET /api/metrics/logs — filtered & paginated logs */
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getLogs(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ServiceLog.LogStatus logStatus = null;
        if (status != null && !status.isBlank()) {
            try { logStatus = ServiceLog.LogStatus.valueOf(status.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }

        Page<ServiceLog> result = logRepository.findFiltered(
                (service != null && service.isBlank()) ? null : service,
                logStatus, from, to,
                PageRequest.of(page, size));

        return ResponseEntity.ok(Map.of(
                "logs", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "currentPage", result.getNumber()
        ));
    }

    /** POST /api/metrics/simulate-load — fires 50 random calls to populate logs */
    @PostMapping("/simulate-load")
    public ResponseEntity<Map<String, Object>> simulateLoad() {
        Random rnd = new Random();
        List<String> inventoryOps = List.of("checkStock", "listProducts", "reserveItem", "updateStock");
        List<String> orderOps = List.of("createOrder", "listOrders", "cancelOrder");
        List<String> paymentOps = List.of("processPayment", "refundPayment", "checkBalance", "validateCard");

        int success = 0, errors = 0;

        for (int i = 0; i < 50; i++) {
            int svc = rnd.nextInt(3);
            try {
                if (svc == 0) {
                    String op = inventoryOps.get(rnd.nextInt(inventoryOps.size()));
                    inventoryProxy.execute(op, "SKU-00" + (rnd.nextInt(3) + 1));
                } else if (svc == 1) {
                    String op = orderOps.get(rnd.nextInt(orderOps.size()));
                    orderProxy.execute(op, "CUST-" + rnd.nextInt(100));
                } else {
                    String op = paymentOps.get(rnd.nextInt(paymentOps.size()));
                    paymentProxy.execute(op, String.valueOf(10 + rnd.nextInt(500)));
                }
                success++;
            } catch (Exception ignored) {
                errors++;
            }
        }

        return ResponseEntity.ok(Map.of(
                "message", "Simulated 50 calls",
                "success", success,
                "errors", errors
        ));
    }

    /** GET /api/metrics/recent/{serviceId} — last 20 calls for a service (for line chart) */
    @GetMapping("/recent/{serviceId}")
    public ResponseEntity<List<ServiceLog>> getRecent(@PathVariable String serviceId) {
        return ResponseEntity.ok(logRepository.findTop20ByServiceIdOrderByTimestampDesc(serviceId));
    }
}
