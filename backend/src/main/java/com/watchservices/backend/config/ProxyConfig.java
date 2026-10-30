package com.watchservices.backend.config;

import com.watchservices.backend.proxy.LoggingProxy;
import com.watchservices.backend.proxy.MicroserviceProxy;
import com.watchservices.backend.repository.ServiceLogRepository;
import com.watchservices.backend.service.InventoryService;
import com.watchservices.backend.service.OrderService;
import com.watchservices.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyConfig {

    @Bean("inventoryProxy")
    public MicroserviceProxy<Object> inventoryProxy(
            @Qualifier("inventoryServiceReal") InventoryService real,
            ServiceLogRepository logRepository) {
        return new LoggingProxy<>(real, "inventory", logRepository);
    }

    @Bean("orderProxy")
    public MicroserviceProxy<Object> orderProxy(
            @Qualifier("orderServiceReal") OrderService real,
            ServiceLogRepository logRepository) {
        return new LoggingProxy<>(real, "orders", logRepository);
    }

    @Bean("paymentProxy")
    public MicroserviceProxy<Object> paymentProxy(
            @Qualifier("paymentServiceReal") PaymentService real,
            ServiceLogRepository logRepository) {
        return new LoggingProxy<>(real, "payments", logRepository);
    }
}
