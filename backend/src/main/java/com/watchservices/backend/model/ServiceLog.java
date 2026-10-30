package com.watchservices.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String requestId;

    @Column(nullable = false)
    private String serviceId;

    @Column(nullable = false)
    private String operation;

    @Column(nullable = false)
    private Long durationMs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String inputParams;

    @Column(columnDefinition = "TEXT")
    private String responseData;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public enum LogStatus {
        SUCCESS, ERROR
    }
}
