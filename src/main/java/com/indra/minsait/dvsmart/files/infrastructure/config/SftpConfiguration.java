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

import com.indra.minsait.dvsmart.files.infrastructure.sftp.CustomLazySftpSessionFactory;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for SFTP connection pool.
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class SftpConfiguration
 * @date 14-01-2026
 */
@Configuration
@RequiredArgsConstructor
public class SftpConfiguration {

    private final SftpConfigProperties sftpConfig;
    private CustomLazySftpSessionFactory sessionFactory;

    @Bean
    CustomLazySftpSessionFactory sftpSessionFactory() {
        this.sessionFactory = new CustomLazySftpSessionFactory(sftpConfig);
        return sessionFactory;
    }

    @PreDestroy
    public void cleanup() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
