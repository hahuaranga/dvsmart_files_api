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
package com.indra.minsait.dvsmart.files.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain model representing PDF file metadata.
 * <p>
 * Contains all information about a file stored in the system,
 * including its location in the hash-partitioned SFTP destination.
 * </p>
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class FileMetadata
 * @date 14-01-2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    /**
     * Unique identifier (SHA-256 hash of source path).
     */
    private String idUnico;

    /**
     * Original file name (always ends with .pdf).
     */
    private String fileName;

    /**
     * File size in bytes.
     */
    private Long fileSize;

    /**
     * Last modification date of the file.
     */
    private Instant lastModificationDate;

    /**
     * Original source path (historical reference, not used for download).
     */
    private String sourcePath;

    /**
     * Destination path in SFTP with hash partitioning.
     * This is the actual path used for downloading.
     * Example: /organized_data/a1/b2/c3/documento.pdf
     */
    private String destinationPath;

    // Indexing state
    private String indexingStatus;
    private Instant indexedAt;

    // Reorganization state
    private String reorgStatus;
    private Instant reorgCompletedAt;

    // Business metadata
    private String tipoDocumento;
    private String codigoCliente;
    private Integer anio;
    private Integer mes;

    /**
     * Returns a human-readable file size.
     *
     * @return formatted file size (e.g., "1.50 MB")
     */
    public String getFileSizeFormatted() {
        if (fileSize == null) {
            return "0 B";
        }
        if (fileSize < 1024) {
            return fileSize + " B";
        }
        if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        }
        if (fileSize < 1024L * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        }
        return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
    }

    /**
     * Checks if the file is available for download.
     *
     * @return true if reorganization is completed
     */
    public boolean isAvailableForDownload() {
        return "COMPLETED".equals(reorgStatus);
    }
}
