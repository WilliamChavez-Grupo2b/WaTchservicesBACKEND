package com.watchservices.backend.controller;

import com.watchservices.backend.dto.ServiceRequest;
import com.watchservices.backend.proxy.MicroserviceProxy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final MicroserviceProxy<Object> inventoryProxy;
    private final MicroserviceProxy<Object> orderProxy;
    private final MicroserviceProxy<Object> paymentProxy;

    public ServiceController(
            @Qualifier("inventoryProxy") MicroserviceProxy<Object> inventoryProxy,
            @Qualifier("orderProxy") MicroserviceProxy<Object> orderProxy,
            @Qualifier("paymentProxy") MicroserviceProxy<Object> paymentProxy) {
        this.inventoryProxy = inventoryProxy;
        this.orderProxy = orderProxy;
        this.paymentProxy = paymentProxy;
    }

    @PostMapping("/inventory/{operation}")
    public ResponseEntity<?> callInventory(@PathVariable String operation,
                                           @RequestBody(required = false) ServiceRequest body) {
        return callProxy(inventoryProxy, operation, body);
    }

    @PostMapping("/orders/{operation}")
    public ResponseEntity<?> callOrders(@PathVariable String operation,
                                        @RequestBody(required = false) ServiceRequest body) {
        return callProxy(orderProxy, operation, body);
    }

    @PostMapping("/payments/{operation}")
    public ResponseEntity<?> callPayments(@PathVariable String operation,
                                          @RequestBody(required = false) ServiceRequest body) {
        return callProxy(paymentProxy, operation, body);
    }

    private ResponseEntity<?> callProxy(MicroserviceProxy<Object> proxy,
                                        String operation,
                                        ServiceRequest body) {
        try {
            Object[] params = (body != null && body.getParams() != null)
                    ? body.getParams().toArray()
                    : new Object[0];
            Object result = proxy.execute(operation, params);
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "error", ex.getMessage()));
        }
    }
}
