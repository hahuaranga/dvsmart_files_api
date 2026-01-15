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
package com.indra.minsait.dvsmart.files.infrastructure.sftp;

import com.indra.minsait.dvsmart.files.infrastructure.config.SftpConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;

import java.time.Duration;

/**
 * Custom lazy SFTP session factory with connection pooling.
 * <p>
 * Features:
 * <ul>
 *   <li>Lazy initialization - connections created on demand</li>
 *   <li>Connection validation before use (testOnBorrow)</li>
 *   <li>Automatic eviction of idle connections</li>
 *   <li>Configurable pool size and timeouts</li>
 * </ul>
 * </p>
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class CustomLazySftpSessionFactory
 * @date 14-01-2026
 */
@Slf4j
public class CustomLazySftpSessionFactory {

    private final SftpConfigProperties config;
    private final GenericObjectPool<SftpSession> pool;
    private final DefaultSftpSessionFactory sessionFactory;

    public CustomLazySftpSessionFactory(SftpConfigProperties config) {
        this.config = config;
        this.sessionFactory = createSessionFactory();
        this.pool = createPool();

        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║ SFTP Connection Pool Initialized (LAZY)                      ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║ Host: {}:{}", config.getHost(), config.getPort());
        log.info("║ User: {}", config.getUser());
        log.info("║ Base Directory: {}", config.getBaseDir());
        log.info("║ Pool Max Size: {}", config.getPool().getMaxSize());
        log.info("║ Test On Borrow: {}", config.getPool().isTestOnBorrow());
        log.info("╚══════════════════════════════════════════════════════════════╝");
    }

    private DefaultSftpSessionFactory createSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(config.getHost());
        factory.setPort(config.getPort());
        factory.setUser(config.getUser());
        factory.setPassword(config.getPassword());
        factory.setTimeout(config.getTimeout());
        factory.setAllowUnknownKeys(true);

        if (config.getKnownHostsFile() != null && !config.getKnownHostsFile().isEmpty()) {
            factory.setKnownHostsResource(
                    new org.springframework.core.io.FileSystemResource(config.getKnownHostsFile()));
        }

        return factory;
    }

    private GenericObjectPool<SftpSession> createPool() {
        SftpConfigProperties.PoolConfig poolConfig = config.getPool();

        GenericObjectPoolConfig<SftpSession> poolObjConfig = new GenericObjectPoolConfig<>();
        poolObjConfig.setMaxTotal(poolConfig.getMaxSize());
        poolObjConfig.setMinIdle(poolConfig.getMinIdle());
        poolObjConfig.setMaxWait(Duration.ofMillis(poolConfig.getMaxWaitMillis()));
        poolObjConfig.setTestOnBorrow(poolConfig.isTestOnBorrow());
        poolObjConfig.setTestWhileIdle(poolConfig.isTestWhileIdle());
        poolObjConfig.setTimeBetweenEvictionRuns(
                Duration.ofMillis(poolConfig.getTimeBetweenEvictionRunsMillis()));
        poolObjConfig.setMinEvictableIdleDuration(
                Duration.ofMillis(poolConfig.getMinEvictableIdleTimeMillis()));
        poolObjConfig.setBlockWhenExhausted(true);
        poolObjConfig.setJmxEnabled(false);

        return new GenericObjectPool<>(new SftpSessionFactory(), poolObjConfig);
    }

    /**
     * Gets a session from the pool.
     *
     * @return an active SFTP session
     * @throws Exception if unable to obtain a session
     */
    public SftpSession getSession() throws Exception {
        return pool.borrowObject();
    }

    /**
     * Returns a session to the pool.
     *
     * @param session the session to return
     */
    public void returnSession(SftpSession session) {
        if (session != null) {
            pool.returnObject(session);
        }
    }

    /**
     * Invalidates a session (removes it from the pool).
     *
     * @param session the session to invalidate
     */
    public void invalidateSession(SftpSession session) {
        if (session != null) {
            try {
                pool.invalidateObject(session);
            } catch (Exception e) {
                log.warn("Failed to invalidate SFTP session", e);
            }
        }
    }

    /**
     * Gets current pool statistics.
     *
     * @return pool statistics
     */
    public PoolStats getPoolStats() {
        return PoolStats.builder()
                .active(pool.getNumActive())
                .idle(pool.getNumIdle())
                .maxTotal(pool.getMaxTotal())
                .totalCreated(pool.getCreatedCount())
                .totalDestroyed(pool.getDestroyedCount())
                .totalBorrowed(pool.getBorrowedCount())
                .totalReturned(pool.getReturnedCount())
                .build();
    }

    /**
     * Closes the pool and all connections.
     */
    public void close() {
        log.info("Closing SFTP connection pool...");
        pool.close();
    }

    /**
     * Factory for creating pooled SFTP sessions.
     */
    private class SftpSessionFactory extends BasePooledObjectFactory<SftpSession> {

        @Override
        public SftpSession create() throws Exception {
            log.debug("Creating new SFTP session to {}:{}", config.getHost(), config.getPort());
            SftpSession session = sessionFactory.getSession();
            log.debug("SFTP session created successfully");
            return session;
        }

        @Override
        public PooledObject<SftpSession> wrap(SftpSession session) {
            return new DefaultPooledObject<>(session);
        }

        @Override
        public void destroyObject(PooledObject<SftpSession> pooledObject) throws Exception {
            SftpSession session = pooledObject.getObject();
            if (session != null && session.isOpen()) {
                log.debug("Closing SFTP session");
                session.close();
            }
        }

        @Override
        public boolean validateObject(PooledObject<SftpSession> pooledObject) {
            SftpSession session = pooledObject.getObject();
            if (session == null || !session.isOpen()) {
                log.debug("Session validation failed: session is null or closed");
                return false;
            }

            try {
                // Validate by listing the base directory
                session.list(config.getBaseDir());
                return true;
            } catch (Exception e) {
                log.debug("Session validation failed: {}", e.getMessage());
                return false;
            }
        }
    }

    /**
     * Pool statistics.
     */
    @lombok.Data
    @lombok.Builder
    public static class PoolStats {
        private int active;
        private int idle;
        private int maxTotal;
        private long totalCreated;
        private long totalDestroyed;
        private long totalBorrowed;
        private long totalReturned;

        public double getUtilizationPercent() {
            return maxTotal > 0 ? (active * 100.0) / maxTotal : 0;
        }

        public int getAvailableSlots() {
            return maxTotal - active;
        }
    }
}
