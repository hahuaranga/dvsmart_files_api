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
package com.indra.minsait.dvsmart.files.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for SFTP destination server connection.
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class SftpConfigProperties
 * @date 14-01-2026
 */
@Data
@ConfigurationProperties(prefix = "sftp.dest")
public class SftpConfigProperties {

    /**
     * SFTP server hostname.
     */
    private String host = "localhost";

    /**
     * SFTP server port.
     */
    private int port = 22;

    /**
     * SFTP username.
     */
    private String user;

    /**
     * SFTP password.
     */
    private String password;

    /**
     * Base directory where organized files are stored.
     */
    private String baseDir = "/organized_data";

    /**
     * Connection timeout in milliseconds.
     */
    private int timeout = 30000;

    /**
     * Path to known_hosts file (optional).
     */
    private String knownHostsFile;

    /**
     * Pool configuration.
     */
    private PoolConfig pool = new PoolConfig();

    @Data
    public static class PoolConfig {
        /**
         * Whether to initialize pool lazily.
         */
        private boolean lazyInit = true;

        /**
         * Initial pool size.
         */
        private int initialSize = 0;

        /**
         * Maximum pool size.
         */
        private int maxSize = 20;

        /**
         * Minimum idle connections.
         */
        private int minIdle = 0;

        /**
         * Maximum wait time for borrowing a session (ms).
         */
        private long maxWaitMillis = 30000;

        /**
         * Whether to validate sessions on borrow.
         */
        private boolean testOnBorrow = true;

        /**
         * Whether to validate sessions while idle.
         */
        private boolean testWhileIdle = true;

        /**
         * Time between eviction runs (ms).
         */
        private long timeBetweenEvictionRunsMillis = 60000;

        /**
         * Minimum time a session can be idle before eviction (ms).
         */
        private long minEvictableIdleTimeMillis = 300000;
    }
}
