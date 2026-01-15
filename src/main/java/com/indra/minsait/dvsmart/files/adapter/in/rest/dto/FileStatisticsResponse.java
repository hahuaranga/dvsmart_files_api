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
package com.indra.minsait.dvsmart.files.adapter.in.rest.dto;

import com.indra.minsait.dvsmart.files.domain.model.FileStatistics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for file statistics.
 */
/**
 * @author Hector Huaranga <hhuaranga@indracompany.com>
 * @class FileStatisticsResponse
 * @date 14-01-2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileStatisticsResponse {

    private long totalFiles;
    private long totalSize;
    private String totalSizeFormatted;
    private Map<String, Long> byTipoDocumento;
    private Map<String, Long> byStatus;
    private Map<Integer, Long> byYear;

    /**
     * Creates a response from domain model.
     *
     * @param stats domain statistics
     * @return response DTO
     */
    public static FileStatisticsResponse fromDomain(FileStatistics stats) {
        return FileStatisticsResponse.builder()
                .totalFiles(stats.getTotalFiles())
                .totalSize(stats.getTotalSize())
                .totalSizeFormatted(stats.getTotalSizeFormatted())
                .byTipoDocumento(stats.getByTipoDocumento())
                .byStatus(stats.getByStatus())
                .byYear(stats.getByYear())
                .build();
    }
}
