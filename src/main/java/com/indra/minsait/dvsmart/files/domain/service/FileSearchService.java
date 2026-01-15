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

import com.indra.minsait.dvsmart.files.domain.exception.FileNotFoundException;
import com.indra.minsait.dvsmart.files.domain.model.FileMetadata;
import com.indra.minsait.dvsmart.files.domain.model.FileStatistics;
import com.indra.minsait.dvsmart.files.domain.model.PagedResult;
import com.indra.minsait.dvsmart.files.domain.model.SearchCriteria;
import com.indra.minsait.dvsmart.files.domain.port.in.GetFileUseCase;
import com.indra.minsait.dvsmart.files.domain.port.in.GetStatisticsUseCase;
import com.indra.minsait.dvsmart.files.domain.port.in.SearchFilesUseCase;
import com.indra.minsait.dvsmart.files.domain.port.out.FileMetadataPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Domain service for file search operations.
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class FileSearchService
 * @date 14-01-2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileSearchService implements SearchFilesUseCase, GetFileUseCase, GetStatisticsUseCase {

    private final FileMetadataPort fileMetadataPort;

    @Override
    public PagedResult<FileMetadata> execute(SearchCriteria criteria) {
        log.debug("Searching files with criteria: query={}, tipoDocumento={}, codigoCliente={}, page={}, size={}",
                criteria.getQuery(), criteria.getTipoDocumento(), criteria.getCodigoCliente(),
                criteria.getPage(), criteria.getSize());

        PagedResult<FileMetadata> result = fileMetadataPort.search(criteria);

        log.debug("Search completed: found {} files (total: {})",
                result.getContent().size(), result.getTotalElements());

        return result;
    }

    @Override
    public FileMetadata execute(String idUnico) {
        log.debug("Getting file metadata for idUnico: {}", idUnico);

        return fileMetadataPort.findByIdUnico(idUnico)
                .orElseThrow(() -> {
                    log.warn("File not found: {}", idUnico);
                    return new FileNotFoundException(idUnico);
                });
    }

    @Override
    public FileStatistics execute() {
        log.debug("Getting file statistics");
        return fileMetadataPort.getStatistics();
    }
}
