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
package com.indra.minsait.dvsmart.files.adapter.in.rest.mapper;

import com.indra.minsait.dvsmart.files.adapter.in.rest.dto.FileSearchRequest;
import com.indra.minsait.dvsmart.files.domain.model.SearchCriteria;
import com.indra.minsait.dvsmart.files.infrastructure.config.FilesConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper from request DTO to domain search criteria.
 */
/**
 * @author Hector Huaranga <hahuaranga@indracompany.com>
 * @class SearchCriteriaMapper
 * @date 14-01-2026
 */
@Component
@RequiredArgsConstructor
public class SearchCriteriaMapper {

    private final FilesConfigProperties config;

    /**
     * Maps a request DTO to domain search criteria.
     *
     * @param request request DTO
     * @return domain search criteria
     */
    public SearchCriteria toDomain(FileSearchRequest request) {
        int pageSize = request.getSize() != null 
                ? Math.min(request.getSize(), config.getSearch().getMaxPageSize())
                : config.getSearch().getDefaultPageSize();

        return SearchCriteria.builder()
                .query(request.getQ())
                .tipoDocumento(request.getTipoDocumento())
                .codigoCliente(request.getCodigoCliente())
                .anio(request.getAnio())
                .mes(request.getMes())
                .page(request.getPage() != null ? request.getPage() : 0)
                .size(pageSize)
                .sortField(request.getSort() != null ? request.getSort() : "fileName")
                .sortDirection(request.getDirection() != null ? request.getDirection() : "asc")
                .build();
    }
}
