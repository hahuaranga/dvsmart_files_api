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

import java.util.Map;

/**
 * Domain model representing file statistics.
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class FileStatistics
 * @date 14-01-2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileStatistics {

    /**
     * Total number of files.
     */
    private long totalFiles;

    /**
     * Total size in bytes.
     */
    private long totalSize;

    /**
     * Count by document type.
     */
    private Map<String, Long> byTipoDocumento;

    /**
     * Count by reorg status.
     */
    private Map<String, Long> byStatus;

    /**
     * Count by year.
     */
    private Map<Integer, Long> byYear;

    /**
     * Returns a human-readable total size.
     *
     * @return formatted size (e.g., "5.00 TB")
     */
    public String getTotalSizeFormatted() {
        if (totalSize < 1024) {
            return totalSize + " B";
        }
        if (totalSize < 1024 * 1024) {
            return String.format("%.2f KB", totalSize / 1024.0);
        }
        if (totalSize < 1024L * 1024 * 1024) {
            return String.format("%.2f MB", totalSize / (1024.0 * 1024));
        }
        if (totalSize < 1024L * 1024 * 1024 * 1024) {
            return String.format("%.2f GB", totalSize / (1024.0 * 1024 * 1024));
        }
        return String.format("%.2f TB", totalSize / (1024.0 * 1024 * 1024 * 1024));
    }
}
