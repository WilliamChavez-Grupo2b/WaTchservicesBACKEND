package com.watchservices.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MetricsSummary {
    private String serviceId;
    private long totalCalls;
    private long errorCount;
    private double errorRate;
    private double avgResponseMs;
}
