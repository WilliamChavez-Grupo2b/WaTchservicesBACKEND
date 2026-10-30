package com.watchservices.backend.repository;

import com.watchservices.backend.model.ServiceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceLogRepository extends JpaRepository<ServiceLog, Long> {

    @Query("SELECT l FROM ServiceLog l WHERE " +
           "(:service IS NULL OR l.serviceId = :service) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:from IS NULL OR l.timestamp >= :from) AND " +
           "(:to IS NULL OR l.timestamp <= :to) " +
           "ORDER BY l.timestamp DESC")
    Page<ServiceLog> findFiltered(
            @Param("service") String service,
            @Param("status") ServiceLog.LogStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    List<ServiceLog> findTop20ByServiceIdOrderByTimestampDesc(String serviceId);

    @Query("SELECT l.serviceId, COUNT(l), " +
           "SUM(CASE WHEN l.status = 'ERROR' THEN 1 ELSE 0 END), " +
           "AVG(l.durationMs) " +
           "FROM ServiceLog l GROUP BY l.serviceId")
    List<Object[]> findMetricsSummary();
}
