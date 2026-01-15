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
package com.indra.minsait.dvsmart.files.infrastructure.sftp.adapter;

import com.indra.minsait.dvsmart.files.domain.exception.SftpConnectionException;
import com.indra.minsait.dvsmart.files.domain.port.out.FileContentPort;
import com.indra.minsait.dvsmart.files.infrastructure.config.FilesConfigProperties;
import com.indra.minsait.dvsmart.files.infrastructure.sftp.CustomLazySftpSessionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * SFTP adapter for file content operations.
 * <p>
 * Accesses files from SFTP destination server using hash-partitioned paths.
 * </p>
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class SftpFileContentAdapter
 * @date 14-01-2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SftpFileContentAdapter implements FileContentPort {

    private final CustomLazySftpSessionFactory sessionFactory;
    private final FilesConfigProperties filesConfig;

    @Override
    public InputStream getFileStream(String remotePath) {
        log.debug("Getting file stream for: {}", remotePath);

        // For simplicity, we load content and return as ByteArrayInputStream
        // For very large files, consider a more sophisticated approach
        byte[] content = getFileContent(remotePath);
        return new ByteArrayInputStream(content);
    }

    @Override
    public void streamFileTo(String remotePath, OutputStream outputStream) {
        log.debug("Streaming file to output: {}", remotePath);

        SftpSession session = null;
        try {
            session = sessionFactory.getSession();
            int bufferSize = filesConfig.getDownload().getBufferSize();

            try (InputStream is = session.readRaw(remotePath)) {
                byte[] buffer = new byte[bufferSize];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = is.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                log.debug("Streamed {} bytes for file: {}", totalBytes, remotePath);
            }

            sessionFactory.returnSession(session);
            session = null; // Prevent double-return in finally

        } catch (Exception e) {
            log.error("Failed to stream file: {}", remotePath, e);
            if (session != null) {
                sessionFactory.invalidateSession(session);
                session = null;
            }
            throw new SftpConnectionException("Failed to stream file: " + remotePath, e);
        } finally {
            if (session != null) {
                sessionFactory.returnSession(session);
            }
        }
    }

    @Override
    public byte[] getFileContent(String remotePath) {
        log.debug("Getting file content for: {}", remotePath);

        SftpSession session = null;
        try {
            session = sessionFactory.getSession();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            session.read(remotePath, baos);

            byte[] content = baos.toByteArray();
            log.debug("Retrieved {} bytes for file: {}", content.length, remotePath);

            sessionFactory.returnSession(session);
            session = null;

            return content;

        } catch (Exception e) {
            log.error("Failed to get file content: {}", remotePath, e);
            if (session != null) {
                sessionFactory.invalidateSession(session);
                session = null;
            }
            throw new SftpConnectionException("Failed to get file content: " + remotePath, e);
        } finally {
            if (session != null) {
                sessionFactory.returnSession(session);
            }
        }
    }

    @Override
    public boolean fileExists(String remotePath) {
        log.debug("Checking if file exists: {}", remotePath);

        SftpSession session = null;
        try {
            session = sessionFactory.getSession();
            boolean exists = session.exists(remotePath);
            sessionFactory.returnSession(session);
            session = null;
            return exists;

        } catch (Exception e) {
            log.error("Failed to check file existence: {}", remotePath, e);
            if (session != null) {
                sessionFactory.invalidateSession(session);
                session = null;
            }
            throw new SftpConnectionException("Failed to check file existence: " + remotePath, e);
        } finally {
            if (session != null) {
                sessionFactory.returnSession(session);
            }
        }
    }
}
