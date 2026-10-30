package com.watchservices.backend.proxy;

import com.watchservices.backend.model.ServiceLog;
import com.watchservices.backend.repository.ServiceLogRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
public class LoggingProxy<T> implements MicroserviceProxy<T> {

    private final MicroserviceProxy<T> realService;
    private final String serviceId;
    private final ServiceLogRepository logRepository;

    public LoggingProxy(MicroserviceProxy<T> realService,
                        String serviceId,
                        ServiceLogRepository logRepository) {
        this.realService = realService;
        this.serviceId = serviceId;
        this.logRepository = logRepository;
    }

    @Override
    public T execute(String operation, Object... params) {
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        LocalDateTime timestamp = LocalDateTime.now();

        log.debug("[{}] Starting operation '{}' on service '{}' | params: {}",
                requestId, operation, serviceId, Arrays.toString(params));

        try {
            T result = realService.execute(operation, params);
            long duration = System.currentTimeMillis() - startTime;

            ServiceLog entry = ServiceLog.builder()
                    .requestId(requestId)
                    .serviceId(serviceId)
                    .operation(operation)
                    .durationMs(duration)
                    .status(ServiceLog.LogStatus.SUCCESS)
                    .inputParams(Arrays.toString(params))
                    .responseData(result != null ? result.toString() : "null")
                    .timestamp(timestamp)
                    .build();
            logRepository.save(entry);

            log.debug("[{}] Completed '{}' in {}ms — SUCCESS", requestId, operation, duration);
            return result;

        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            String shortTrace = buildShortTrace(ex);

            ServiceLog entry = ServiceLog.builder()
                    .requestId(requestId)
                    .serviceId(serviceId)
                    .operation(operation)
                    .durationMs(duration)
                    .status(ServiceLog.LogStatus.ERROR)
                    .inputParams(Arrays.toString(params))
                    .errorMessage(shortTrace)
                    .timestamp(timestamp)
                    .build();
            logRepository.save(entry);

            log.error("[{}] Failed '{}' in {}ms — ERROR: {}", requestId, operation, duration, shortTrace);
            throw ex;
        }
    }

    private String buildShortTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getSimpleName()).append(": ").append(ex.getMessage());
        StackTraceElement[] stack = ex.getStackTrace();
        int limit = Math.min(3, stack.length);
        for (int i = 0; i < limit; i++) {
            sb.append(" | at ").append(stack[i].toString());
        }
        return sb.toString();
    }
}
