/*
 * /////////////////////////////////////////////////////////////////////////////
 *
 * Copyright (c) 2026 Indra Sistemas, S.A. All Rights Reserved.
 * http://www.indracompany.com/
 *
 * The contents of this file are owned by Indra Sistemas, S.A. copyright holder.
 * This file can only be copied, distributed and used all or in part with the
 * written permission of Indra Sistemas, S.A, or in accordance with the terms and
 * conditions laid down in the agreement / contract under which supplied.
 *
 * /////////////////////////////////////////////////////////////////////////////
 */
package com.indra.minsait.dvsmart.files.adapter.in.rest;

import com.indra.minsait.dvsmart.files.infrastructure.sftp.CustomLazySftpSessionFactory;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for monitoring endpoints.
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class MonitoringController
 * @date 14-01-2026
 */
@Slf4j
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final CustomLazySftpSessionFactory sftpSessionFactory;

    @GetMapping("/sftp-pool")
    public ResponseEntity<Map<String, Object>> getSftpPoolStats() {
        CustomLazySftpSessionFactory.PoolStats stats = sftpSessionFactory.getPoolStats();

        Map<String, Object> response = new HashMap<>();
        response.put("active", stats.getActive());
        response.put("idle", stats.getIdle());
        response.put("maxTotal", stats.getMaxTotal());
        response.put("totalCreated", stats.getTotalCreated());
        response.put("totalDestroyed", stats.getTotalDestroyed());
        response.put("totalBorrowed", stats.getTotalBorrowed());
        response.put("totalReturned", stats.getTotalReturned());
        response.put("utilizationPercent", stats.getUtilizationPercent());
        response.put("availableSlots", stats.getAvailableSlots());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sftp-pool/health")
    public ResponseEntity<Map<String, Object>> getSftpPoolHealth() {
        CustomLazySftpSessionFactory.PoolStats stats = sftpSessionFactory.getPoolStats();

        double utilization = stats.getUtilizationPercent();
        String status;
        String message;

        if (utilization < 80) {
            status = "HEALTHY";
            message = "Pool utilization is normal";
        } else if (utilization < 95) {
            status = "WARNING";
            message = "Pool utilization is high, consider increasing pool size";
        } else {
            status = "CRITICAL";
            message = "Pool is nearly exhausted, immediate action required";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("message", message);
        response.put("utilizationPercent", utilization);
        response.put("activeConnections", stats.getActive());
        response.put("maxConnections", stats.getMaxTotal());

        return ResponseEntity.ok(response);
    }
}
