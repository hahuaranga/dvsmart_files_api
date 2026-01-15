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
package com.indra.minsait.dvsmart.files.domain.service;

import com.indra.minsait.dvsmart.files.domain.exception.FileNotAvailableException;
import com.indra.minsait.dvsmart.files.domain.exception.FileNotFoundException;
import com.indra.minsait.dvsmart.files.domain.model.FileMetadata;
import com.indra.minsait.dvsmart.files.domain.port.in.DownloadFileUseCase;
import com.indra.minsait.dvsmart.files.domain.port.out.FileContentPort;
import com.indra.minsait.dvsmart.files.domain.port.out.FileMetadataPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.OutputStream;

/**
 * Domain service for file download operations.
 * <p>
 * Downloads files from SFTP destination server using the hash-partitioned path.
 * </p>
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class FileDownloadService
 * @date 14-01-2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadService implements DownloadFileUseCase {

    private final FileMetadataPort fileMetadataPort;
    private final FileContentPort fileContentPort;

    @Override
    public FileMetadata execute(String idUnico, OutputStream outputStream) {
        log.debug("Starting download for file: {}", idUnico);

        // 1. Get file metadata from MongoDB
        FileMetadata metadata = fileMetadataPort.findByIdUnico(idUnico)
                .orElseThrow(() -> {
                    log.warn("File not found for download: {}", idUnico);
                    return new FileNotFoundException(idUnico);
                });

        // 2. Validate file is available (reorg completed)
        if (!metadata.isAvailableForDownload()) {
            log.warn("File not available for download: {} (status: {})",
                    idUnico, metadata.getReorgStatus());
            throw new FileNotAvailableException(idUnico, metadata.getReorgStatus());
        }

        // 3. Stream file from SFTP destination using hash-partitioned path
        String destinationPath = metadata.getDestinationPath();
        log.debug("Streaming file from SFTP destination: {}", destinationPath);

        fileContentPort.streamFileTo(destinationPath, outputStream);

        log.info("Download completed for file: {} ({} bytes)", 
                metadata.getFileName(), metadata.getFileSize());

        return metadata;
    }
}
